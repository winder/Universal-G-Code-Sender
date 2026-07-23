/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.services.interceptor;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UGSEventDispatcher;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Orchestrates command interception during a running stream. When a streamed command matches a registered
 * {@link CommandInterceptor} the service takes control: it waits for the machine to finish the commands
 * already sent, runs the interceptor routine on a dedicated thread and finally resumes the stream. The
 * virtual {@link InterceptorState} is published on the UGS event bus as {@link InterceptorStateEvent} and is
 * kept separate from the firmware reported controller state.
 *
 * @author Joacim Breiler
 */
public class CommandInterceptorService implements UGSEventListener {
    private static final Logger LOGGER = Logger.getLogger(CommandInterceptorService.class.getName());

    private final BackendAPI backend;
    private final UGSEventDispatcher eventDispatcher;
    private final List<CommandInterceptor> interceptors = new CopyOnWriteArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "interceptor-thread");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean enabled = true;
    private volatile InterceptorState state = InterceptorState.INACTIVE;

    private volatile InterceptingGcodeStreamReader activeReader;
    private volatile CommandInterceptor activeInterceptor;
    private volatile GcodeCommand triggerCommand;
    private volatile Future<?> routineTask;
    private volatile CompletableFuture<UserResponse> userResponseFuture;

    public CommandInterceptorService(BackendAPI backend, UGSEventDispatcher eventDispatcher) {
        this.backend = backend;
        this.eventDispatcher = eventDispatcher;
    }

    public void register(CommandInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void unregister(CommandInterceptor interceptor) {
        interceptors.remove(interceptor);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public InterceptorState getState() {
        return state;
    }

    public boolean isActive() {
        return state != InterceptorState.INACTIVE;
    }

    /**
     * Wraps the given stream so that interception can take place while it is being streamed. Call this before
     * queueing the stream to the controller. If the service is disabled the stream is returned unwrapped.
     *
     * @param delegate the original gcode stream
     * @return the stream to queue to the controller
     */
    public IGcodeStreamReader beginJob(IGcodeStreamReader delegate) {
        if (!enabled || interceptors.isEmpty()) {
            return delegate;
        }
        setState(InterceptorState.INACTIVE, null);
        InterceptingGcodeStreamReader reader = new InterceptingGcodeStreamReader(delegate, this);
        this.activeReader = reader;
        return reader;
    }

    Optional<CommandInterceptor> findInterceptor(GcodeCommand command) {
        if (!enabled || state != InterceptorState.INACTIVE) {
            return Optional.empty();
        }
        return interceptors.stream()
                .filter(interceptor -> interceptor.matches(command))
                .findFirst();
    }

    /**
     * Called by the {@link InterceptingGcodeStreamReader} on the streaming thread when a trigger command has
     * been reached. Must return quickly without blocking; the routine is executed on a background thread.
     */
    void onTriggerReached(CommandInterceptor interceptor, GcodeCommand command, InterceptingGcodeStreamReader reader) {
        this.activeReader = reader;
        this.activeInterceptor = interceptor;
        this.triggerCommand = command;
        setState(InterceptorState.PENDING, "Reached " + command.getCommandString());
        routineTask = executor.submit(this::runRoutine);
    }

    private void runRoutine() {
        IController controller = backend.getController();
        try {
            ControllerUtils.waitOnActiveCommands(controller);

            setState(InterceptorState.RUNNING, null);
            InterceptContext context = new InterceptContext(backend, triggerCommand, this);
            activeInterceptor.execute(context);

            setState(InterceptorState.RESUMING, null);
            controller.restoreParserModalState();
            activeReader.ungate();
            controller.getCommunicator().streamCommands();

            setState(InterceptorState.INACTIVE, null);
        } catch (InterceptAbortedException e) {
            LOGGER.log(Level.INFO, "Interceptor routine aborted by operator");
            cancelStream(controller);
            setState(InterceptorState.INACTIVE, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelStream(controller);
            setState(InterceptorState.INACTIVE, null);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Interceptor routine failed, leaving stream paused", e);
            setState(InterceptorState.FAILED, e.getMessage());
        }
    }

    void awaitUserConfirmation(String message) throws InterceptAbortedException {
        CompletableFuture<UserResponse> future = new CompletableFuture<>();
        this.userResponseFuture = future;
        setState(InterceptorState.WAITING_FOR_USER, message);
        try {
            UserResponse response = future.get();
            if (response == UserResponse.ABORT) {
                throw new InterceptAbortedException("Operator aborted the interceptor routine");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterceptAbortedException("Interceptor routine was interrupted");
        } catch (ExecutionException e) {
            throw new InterceptAbortedException("Interceptor routine failed while waiting for the operator");
        } finally {
            this.userResponseFuture = null;
        }
        setState(InterceptorState.RUNNING, null);
    }

    /**
     * Provides the operator response when the service is waiting for user input.
     *
     * @param response the operator response
     */
    public void provideUserResponse(UserResponse response) {
        CompletableFuture<UserResponse> future = this.userResponseFuture;
        if (future != null) {
            future.complete(response);
        }
    }

    /**
     * Aborts the currently running interceptor routine and cancels the stream.
     */
    public void abort() {
        CompletableFuture<UserResponse> future = this.userResponseFuture;
        if (future != null) {
            future.complete(UserResponse.ABORT);
            return;
        }

        Future<?> task = this.routineTask;
        if (task != null) {
            task.cancel(true);
        }
    }

    void publishMessage(String message) {
        eventDispatcher.sendUGSEvent(new InterceptorStateEvent(state, state, activeInterceptor, triggerCommand, message));
    }

    private void cancelStream(IController controller) {
        try {
            controller.cancelSend();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not cancel the stream after aborting the interceptor", e);
        }
    }

    private synchronized void setState(InterceptorState newState, String message) {
        InterceptorState previous = this.state;
        this.state = newState;
        eventDispatcher.sendUGSEvent(new InterceptorStateEvent(newState, previous, activeInterceptor, triggerCommand, message));

        if (newState == InterceptorState.INACTIVE || newState == InterceptorState.FAILED) {
            this.activeInterceptor = null;
            this.triggerCommand = null;
        }
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent controllerStateEvent
                && controllerStateEvent.getState() == ControllerState.DISCONNECTED
                && isActive()) {
            forceReset();
        }
    }

    private void forceReset() {
        CompletableFuture<UserResponse> future = this.userResponseFuture;
        if (future != null) {
            future.complete(UserResponse.ABORT);
        }
        Future<?> task = this.routineTask;
        if (task != null) {
            task.cancel(true);
        }
        setState(InterceptorState.INACTIVE, null);
    }
}
