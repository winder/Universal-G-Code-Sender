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
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UGSEventDispatcher;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import java.time.Duration;
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

    private static final Duration RESTORE_TIMEOUT = Duration.ofMinutes(2);

    /**
     * Time to wait after the machine becomes idle so the asynchronously updated gcode parser state has been
     * applied for all commands executed before the interception.
     */
    private static final Duration GCODE_STATE_SETTLE_TIME = Duration.ofMillis(250);

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
    private volatile InterceptorPrompt currentPrompt;

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
        setState(InterceptorState.INACTIVE);
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
     * @return true if the communicator still has commands that have been sent but not yet acknowledged by
     * the controller.
     */
    boolean hasActiveCommands() {
        IController controller = backend.getController();
        return controller != null && controller.getCommunicator().hasCommandsAwaitingResponse();
    }

    /**
     * Called by the {@link InterceptingGcodeStreamReader} on the streaming thread when a trigger command has
     * been reached. Must return quickly without blocking; the routine is executed on a background thread.
     */
    void onTriggerReached(CommandInterceptor interceptor, GcodeCommand command, InterceptingGcodeStreamReader reader) {
        this.activeReader = reader;
        this.activeInterceptor = interceptor;
        this.triggerCommand = command;
        setState(InterceptorState.PENDING);
        routineTask = executor.submit(this::runRoutine);
    }

    private void runRoutine() {
        IController controller = backend.getController();

        try {
            // Wait for the machine to settle to idle before capturing the state to restore afterwards.
            ControllerUtils.waitForState(controller, ControllerState.IDLE);

            // The gcode parser state is updated asynchronously as command responses are processed. Give the
            // event dispatcher a moment to apply the updates for the commands executed before the interception,
            // otherwise the captured state (coordinates and modal state) may still hold an earlier value.
            Thread.sleep(GCODE_STATE_SETTLE_TIME.toMillis());
            GcodeState stateBeforeInterception = controller.getCurrentGcodeState().copy();

            setState(InterceptorState.RUNNING);
            InterceptContext context = new InterceptContext(backend, triggerCommand, this);
            activeInterceptor.execute(context);

            setState(InterceptorState.RESUMING);
            restoreState(controller, stateBeforeInterception);
            activeReader.ungate();
            controller.getCommunicator().queueStreamForComm(activeReader);
            controller.getCommunicator().streamCommands();

            setState(InterceptorState.INACTIVE);
        } catch (InterceptAbortedException e) {
            LOGGER.log(Level.INFO, "Interceptor routine aborted by operator");
            cancelStream(controller);
            setState(InterceptorState.INACTIVE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelStream(controller);
            setState(InterceptorState.INACTIVE);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Interceptor routine failed, leaving stream paused", e);
            setState(InterceptorState.FAILED);
        }
    }

    UserResponse awaitUserPrompt(InterceptorPrompt prompt) throws InterceptAbortedException {
        CompletableFuture<UserResponse> future = new CompletableFuture<>();
        this.userResponseFuture = future;
        this.currentPrompt = prompt;
        setState(InterceptorState.WAITING_FOR_USER);

        UserResponse response;
        try {
            response = future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterceptAbortedException("Interceptor routine was interrupted");
        } catch (ExecutionException e) {
            throw new InterceptAbortedException("Interceptor routine failed while waiting for the operator");
        } finally {
            this.userResponseFuture = null;
            this.currentPrompt = null;
        }

        if (response == UserResponse.ABORT) {
            throw new InterceptAbortedException("Operator aborted the interceptor routine");
        }

        setState(InterceptorState.RUNNING);
        return response;
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
        if (task != null && !task.isDone()) {
            task.cancel(true);
            return;
        }

        if (isActive()) {
            cancelStream(backend.getController());
            setState(InterceptorState.INACTIVE);
        }
    }

    /**
     * Restores the gcode state that was captured before the interceptor took control. This re-applies the
     * modal state, spindle, coolant and feed rate and moves the machine back to the position the program was
     * at when it was intercepted, so the stream can continue from where it left off.
     */
    private void restoreState(IController controller, GcodeState state) throws Exception {
        String unitsCode = state.getUnits() == UnitUtils.Units.INCH ? "G20" : "G21";
        sendRestoreCommand(controller, unitsCode + " G90");
        sendRestoreCommand(controller, state.toAccessoriesCode());

        Position point = state.currentPoint;
        if (point != null) {
            String horizontalMove = PartialPosition.builder(point.getUnits()).setX(point.getX()).setY(point.getY()).build().getFormattedGCode();
            if (!horizontalMove.isEmpty()) {
                sendRestoreCommand(controller, "G90 G0" + horizontalMove);
            }

            String verticalMove = PartialPosition.builder(point.getUnits()).setZ(point.getZ()).build().getFormattedGCode();
            if (!verticalMove.isEmpty()) {
                sendRestoreCommand(controller, "G90 G0" + verticalMove);
            }
        }

        sendRestoreCommand(controller, state.machineStateCode());
    }

    private void sendRestoreCommand(IController controller, String gcode) throws Exception {
        if (gcode == null || gcode.trim().isEmpty()) {
            return;
        }
        GcodeCommand command = controller.createCommand(gcode);
        command.setTemporaryParserModalChange(true);
        ControllerUtils.sendAndWaitForCompletion(controller, command, RESTORE_TIMEOUT);
    }

    private void cancelStream(IController controller) {
        try {
            controller.cancelSend();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not cancel the stream after aborting the interceptor", e);
        }
    }

    private synchronized void setState(InterceptorState newState) {
        InterceptorState previous = this.state;
        this.state = newState;
        eventDispatcher.sendUGSEvent(new InterceptorStateEvent(newState, previous, activeInterceptor, triggerCommand, currentPrompt));

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
        setState(InterceptorState.INACTIVE);
    }
}
