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

import com.willwinder.ugs.nbp.interceptor.InterceptorDialog;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorPrompt;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorState;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.ToolChangeInterceptor;
import com.willwinder.universalgcodesender.services.interceptor.UserResponse;
import net.miginfocom.swing.MigLayout;
import org.openide.windows.WindowManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * A modal dialog that guides the operator through a tool change. It shows a dedicated card for each step: a
 * busy card while the machine is moving, a jog card with jog controls and a "zero Z" button for setting up
 * the new tool, and an empty card for the plain confirmation steps. The header, message and buttons are
 * shared across the cards and updated from the current interceptor state.
 *
 * <p>All methods must be called on the event dispatch thread.
 *
 * @author Joacim Breiler
 */
public class ToolChangeDialog implements InterceptorDialog {
    private static final String CARD_NONE = "none";
    private static final String CARD_BUSY = "busy";
    private static final String CARD_JOG = "jog";

    private static final int FOOTER_MIN_HEIGHT = 40;
    private static final int FOOTER_BUTTON_MIN_WIDTH = 108;

    private final BackendAPI backend;

    private JDialog dialog;
    private JLabel headerLabel;
    private JTextArea messageArea;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JogCard jogCard;
    private JPanel buttonPanel;

    private InterceptorState interceptorState = InterceptorState.INACTIVE;
    private InterceptorPrompt prompt;

    public ToolChangeDialog(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void handleEvent(InterceptorStateEvent event) {
        interceptorState = event.getState();
        if (interceptorState == InterceptorState.INACTIVE) {
            close();
            return;
        }

        prompt = event.getPrompt().orElse(null);
        if (dialog == null) {
            create();
        }
        refresh();
    }

    @Override
    public void handleControllerState(ControllerStateEvent controllerStateEvent) {
        if (dialog != null) {
            refresh();
        }
    }

    private void create() {
        headerLabel = new JLabel();
        headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, headerLabel.getFont().getSize() + 4f));

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        messageArea.setBorder(null);
        messageArea.setFont(UIManager.getFont("Label.font"));
        messageArea.setForeground(UIManager.getColor("Label.foreground"));

        jogCard = new JogCard(backend);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(new JPanel(), CARD_NONE);
        cardPanel.add(new BusyCard(), CARD_BUSY);
        cardPanel.add(jogCard, CARD_JOG);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.add(messageArea, BorderLayout.NORTH);
        body.add(cardPanel, BorderLayout.CENTER);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel footer = new JPanel(new MigLayout("insets 0, fill", "[grow]", "[]0[grow, fill]"));
        footer.add(new JSeparator(), "growx, wrap");
        footer.add(buttonPanel, "grow");
        footer.setMinimumSize(new Dimension(0, FOOTER_MIN_HEIGHT));

        JPanel main = new JPanel(new BorderLayout(0, 16));
        main.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        main.add(headerLabel, BorderLayout.NORTH);
        main.add(body, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.add(main, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        dialog = new JDialog(WindowManager.getDefault().getMainWindow(), Localization.getString("toolchange.title"), true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(460, 480));
        dialog.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
    }

    private void refresh() {
        if (dialog == null) {
            return;
        }

        boolean machineIdle = backend.getControllerState() == ControllerState.IDLE;
        String stepId = prompt == null ? null : prompt.stepId();

        headerLabel.setText(headerFor(interceptorState, stepId));
        messageArea.setText(bodyFor(interceptorState, stepId));

        boolean busy = interceptorState == InterceptorState.PENDING
                || interceptorState == InterceptorState.RUNNING
                || interceptorState == InterceptorState.RESUMING;
        boolean jogStep = interceptorState == InterceptorState.WAITING_FOR_USER
                && ToolChangeInterceptor.STEP_CHANGE_TOOL.equals(stepId);

        cardLayout.show(cardPanel, busy ? CARD_BUSY : jogStep ? CARD_JOG : CARD_NONE);
        if (jogStep) {
            jogCard.updateEnabledState();
        }

        rebuildButtons(machineIdle);

        // Only show the dialog once the machine has settled to idle, so it does not pop up while the machine
        // is still finishing the commands that were sent before the interception.
        if (!dialog.isVisible() && machineIdle) {
            dialog.setVisible(true);
        }
    }

    private void rebuildButtons(boolean machineIdle) {
        buttonPanel.removeAll();

        if (interceptorState == InterceptorState.WAITING_FOR_USER && prompt != null) {
            if (prompt.hasOption(UserResponse.ABORT)) {
                buttonPanel.add(createActionButton(Localization.getString("toolchange.abort"), () -> backend.getInterceptorService().abort(), machineIdle));
            }
            if (prompt.hasOption(UserResponse.SKIP)) {
                buttonPanel.add(createActionButton(Localization.getString("toolchange.skip"), () -> backend.getInterceptorService().provideUserResponse(UserResponse.SKIP), machineIdle));
            }
            if (prompt.hasOption(UserResponse.CONTINUE)) {
                buttonPanel.add(createActionButton(Localization.getString("toolchange.continue"), () -> backend.getInterceptorService().provideUserResponse(UserResponse.CONTINUE), machineIdle));
            }
        } else if (interceptorState == InterceptorState.FAILED) {
            buttonPanel.add(createActionButton(Localization.getString("toolchange.abort"), () -> backend.getInterceptorService().abort(), true));
        }

        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private JButton createActionButton(String text, Runnable action, boolean enabled) {
        JButton button = new JButton(text);
        button.setFocusable(false);
        button.setEnabled(enabled);

        Dimension size = button.getPreferredSize();
        size.width = Math.max(size.width, FOOTER_BUTTON_MIN_WIDTH);
        size.height = Math.max(size.height, FOOTER_MIN_HEIGHT);
        button.setPreferredSize(size);

        button.addActionListener(e -> {
            disableButtons();
            action.run();
        });
        return button;
    }

    private void disableButtons() {
        for (Component component : buttonPanel.getComponents()) {
            component.setEnabled(false);
        }
    }

    private void close() {
        if (dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
            dialog = null;
        }
        if (jogCard != null) {
            jogCard.destroy();
            jogCard = null;
        }
    }

    private String bodyFor(InterceptorState state, String stepId) {
        return switch (state) {
            case PENDING -> "The machine is finishing the current commands.";
            case RUNNING -> "Please wait…";
            case WAITING_FOR_USER -> messageFor(stepId);
            case RESUMING -> "Restoring the machine state…";
            case FAILED -> "The tool change could not be completed.";
            case INACTIVE -> "";
        };
    }

    private String messageFor(String stepId) {
        if (stepId == null) {
            return "";
        }

        return switch (stepId) {
            case ToolChangeInterceptor.STEP_CHANGE_TOOL ->
                    String.format(Localization.getString("toolchange.change.message"), currentToolNumber());
            case ToolChangeInterceptor.STEP_CONTINUE -> Localization.getString("toolchange.resume.message");
            default -> "";
        };
    }

    private int currentToolNumber() {
        GcodeState state = backend.getGcodeState();
        return state == null ? 0 : state.toolNumber;
    }

    private static String headerFor(InterceptorState state, String stepId) {
        if (state == InterceptorState.WAITING_FOR_USER && stepId != null) {
            return switch (stepId) {
                case ToolChangeInterceptor.STEP_CHANGE_TOOL -> Localization.getString("toolchange.change.title");
                case ToolChangeInterceptor.STEP_CONTINUE -> Localization.getString("toolchange.resume.title");
                default -> "Action required";
            };
        }

        return switch (state) {
            case PENDING, RUNNING -> "Waiting for the machine to become idle";
            case WAITING_FOR_USER -> "Action required";
            case RESUMING -> "Resuming the job";
            case FAILED -> "Tool change failed";
            case INACTIVE -> "";
        };
    }
}
