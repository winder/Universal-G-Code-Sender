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
package com.willwinder.ugs.nbp.interceptor.toolchange;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.services.interceptor.CommandInterceptorService;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorPrompt;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorState;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.ToolChangeInterceptor;
import com.willwinder.universalgcodesender.services.interceptor.UserResponse;
import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ToolChangeDialogTestApp {

    private static ToolChangeDialog dialog;
    private static String currentStepId;
    private static InterceptorState lastState = InterceptorState.INACTIVE;

    private ToolChangeDialogTestApp() {
    }

    public static void main(String[] args) throws Exception {
        setSystemLookAndFeel();

        // Register mocks so the dialog, jog card and probe/zero actions resolve them instead of a real backend.
        markLookupServiceInitialized();
        BackendAPI backend = createMockBackend();
        LookupService.register(backend);
        LookupService.register(createMockJogService());

        SwingUtilities.invokeLater(() -> {
            dialog = new ToolChangeDialog(backend);
            showLauncher();
        });
    }

    private static void showLauncher() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        buttons.add(launcherButton("Change tool (jog + abort/continue)",
                () -> showChangeTool(List.of(UserResponse.ABORT, UserResponse.CONTINUE))));
        buttons.add(launcherButton("Change tool (abort + skip + continue)",
                () -> showChangeTool(List.of(UserResponse.ABORT, UserResponse.SKIP, UserResponse.CONTINUE))));
        buttons.add(launcherButton("Continue step", ToolChangeDialogTestApp::showContinue));
        buttons.add(launcherButton("Busy, then change tool", () -> {
            delay(1500, () -> showChangeTool(List.of(UserResponse.ABORT, UserResponse.CONTINUE)));
            showBusy();
        }));
        buttons.add(launcherButton("Failed", ToolChangeDialogTestApp::showFailed));

        JFrame launcher = new JFrame("ToolChangeDialog tester");
        launcher.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        launcher.setContentPane(buttons);
        launcher.pack();
        launcher.setMinimumSize(new Dimension(320, launcher.getHeight()));
        launcher.setLocationByPlatform(true);
        launcher.setVisible(true);
    }

    private static JButton launcherButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setAlignmentX(0f);
        button.addActionListener(e -> action.run());
        return button;
    }

    private static void showChangeTool(List<UserResponse> options) {
        currentStepId = ToolChangeInterceptor.STEP_CHANGE_TOOL;
        fire(InterceptorState.WAITING_FOR_USER, new InterceptorPrompt(currentStepId, options));
    }

    private static void showContinue() {
        currentStepId = ToolChangeInterceptor.STEP_CONTINUE;
        fire(InterceptorState.WAITING_FOR_USER, new InterceptorPrompt(currentStepId, List.of(UserResponse.ABORT, UserResponse.CONTINUE)));
    }

    private static void showBusy() {
        currentStepId = null;
        fire(InterceptorState.RUNNING, null);
    }

    private static void showFailed() {
        currentStepId = null;
        fire(InterceptorState.FAILED, null);
    }

    private static void goInactive() {
        currentStepId = null;
        fire(InterceptorState.INACTIVE, null);
    }

    /**
     * Simulates how the real service reacts to the operator, advancing the scripted flow to the next step.
     */
    private static void onUserResponse(UserResponse response) {
        if (ToolChangeInterceptor.STEP_CHANGE_TOOL.equals(currentStepId)) {
            showBusy();
            delay(1200, ToolChangeDialogTestApp::showContinue);
        } else {
            currentStepId = null;
            fire(InterceptorState.RESUMING, null);
            delay(1000, ToolChangeDialogTestApp::goInactive);
        }
    }

    private static void fire(InterceptorState state, InterceptorPrompt prompt) {
        InterceptorStateEvent event = new InterceptorStateEvent(state, lastState, null, null, prompt);
        lastState = state;
        dialog.handleEvent(event);
    }

    private static void delay(int milliseconds, Runnable action) {
        Timer timer = new Timer(milliseconds, e -> action.run());
        timer.setRepeats(false);
        timer.start();
    }

    private static BackendAPI createMockBackend() {
        CommandInterceptorService service = mock(CommandInterceptorService.class);
        doAnswer(invocation -> {
            goInactive();
            return null;
        }).when(service).abort();
        doAnswer(invocation -> {
            onUserResponse(invocation.getArgument(0));
            return null;
        }).when(service).provideUserResponse(any());

        IController controller = mock(IController.class);
        when(controller.getCapabilities()).thenReturn(new Capabilities());

        // LocalizingService reads the language from the settings during its static initialization.
        Settings settings = mock(Settings.class);
        when(settings.getLanguage()).thenReturn("en_US");

        BackendAPI backend = mock(BackendAPI.class);
        when(backend.getControllerState()).thenReturn(ControllerState.IDLE);
        when(backend.isConnected()).thenReturn(true);
        when(backend.isIdle()).thenReturn(true);
        when(backend.getController()).thenReturn(controller);
        when(backend.getSettings()).thenReturn(settings);
        when(backend.getInterceptorService()).thenReturn(service);
        return backend;
    }

    private static JogService createMockJogService() {
        JogService jogService = mock(JogService.class);
        when(jogService.canJog()).thenReturn(true);
        return jogService;
    }

    private static void markLookupServiceInitialized() throws Exception {
        // Skip the real initialization so only our mocks are registered in the lookup service.
        Field field = LookupService.class.getDeclaredField("isInitialized");
        field.setAccessible(true);
        field.setBoolean(null, true);
    }

    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to the default look and feel
        }
    }
}
