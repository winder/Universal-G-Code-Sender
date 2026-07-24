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
package com.willwinder.ugs.nbp.interceptor;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorState;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.UserResponse;
import org.openide.windows.WindowManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * A generic dialog that guides the operator through a command interceptor. It presents a different screen depending
 * on the interceptor state: a busy screen while the machine is finishing or moving, and a confirmation
 * screen with instructions and a continue/abort choice when the interceptor is waiting for the operator.
 * <p>
 * To give a pleasant user experience, you should implement a custom dialog tailored to the specific
 * interceptor routine and register it with the {@link InterceptorDialogService}.
 * <p>
 * All methods must be called on the event dispatch thread.
 *
 * @author Joacim Breiler
 */
class GenericDialog implements InterceptorDialog {

    private static final String DEFAULT_TITLE = "Command interceptor";

    private final BackendAPI backend;

    private JDialog dialog;
    private JLabel headerLabel;
    private JLabel messageLabel;
    private JProgressBar progressBar;
    private JButton continueButton;
    private JButton abortButton;

    private InterceptorState interceptorState = InterceptorState.INACTIVE;

    GenericDialog(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void handleEvent(InterceptorStateEvent event) {
        interceptorState = event.getState();
        if (interceptorState == InterceptorState.INACTIVE) {
            close();
            return;
        }

        if (dialog == null) {
            create();
        }

        refresh();
    }

    @Override
    public void handleControllerState(ControllerStateEvent controllerStateEvent) {
        refresh();
    }

    private void create() {
        headerLabel = new JLabel();
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, headerLabel.getFont().getSize() + 4f));

        messageLabel = new JLabel();

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> onContinue());

        abortButton = new JButton("Abort");
        abortButton.addActionListener(e -> onAbort());

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.add(messageLabel, BorderLayout.CENTER);
        body.add(progressBar, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(abortButton);
        buttonPanel.add(continueButton);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.add(headerLabel, BorderLayout.NORTH);
        content.add(body, BorderLayout.CENTER);
        content.add(buttonPanel, BorderLayout.SOUTH);

        dialog = new JDialog(WindowManager.getDefault().getMainWindow(), DEFAULT_TITLE, true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(440, dialog.getHeight()));
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
    }

    private void onContinue() {
        continueButton.setEnabled(false);
        abortButton.setEnabled(false);
        backend.getInterceptorService().provideUserResponse(UserResponse.CONTINUE);
    }

    private void onAbort() {
        continueButton.setEnabled(false);
        abortButton.setEnabled(false);
        backend.getInterceptorService().abort();
    }

    private void refresh() {
        if (dialog == null) {
            return;
        }

        boolean machineIdle = backend.getControllerState() == ControllerState.IDLE;
        boolean waitingForUser = interceptorState == InterceptorState.WAITING_FOR_USER;
        boolean busy = interceptorState == InterceptorState.PENDING
                || interceptorState == InterceptorState.RUNNING
                || interceptorState == InterceptorState.RESUMING;

        headerLabel.setText(headerFor(interceptorState));
        messageLabel.setText(bodyHtml(bodyFor(interceptorState)));
        progressBar.setVisible(busy);

        continueButton.setVisible(waitingForUser);
        continueButton.setEnabled(waitingForUser && machineIdle);
        abortButton.setEnabled((waitingForUser || interceptorState == InterceptorState.FAILED) && machineIdle);

        if (!dialog.isVisible()) {
            dialog.setVisible(true);
        }
    }

    private void close() {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
    }

    private String bodyFor(InterceptorState state) {
        return switch (state) {
            case PENDING -> "The machine is finishing the current commands.";
            case RUNNING -> "Please wait...";
            case WAITING_FOR_USER -> "Waiting for user...";
            case RESUMING -> "Restoring the machine state...";
            case FAILED -> "The interception could not be completed.";
            case INACTIVE -> "";
        };
    }

    private static String headerFor(InterceptorState state) {
        return switch (state) {
            case PENDING, RUNNING -> "Waiting for the machine to settle";
            case WAITING_FOR_USER -> "Action required";
            case RESUMING -> "Resuming the job";
            case FAILED -> "Interception failed";
            case INACTIVE -> "";
        };
    }

    private static String bodyHtml(String text) {
        String escaped = text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
        return "<html><body style='width: 340px'>" + escaped + "</body></html>";
    }
}
