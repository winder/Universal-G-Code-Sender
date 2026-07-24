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

import com.willwinder.ugs.nbp.core.actions.ResetZCoordinateToZeroAction;
import com.willwinder.ugs.nbp.jog.JogButton;
import com.willwinder.ugs.nbp.jog.JogButtonHandler;
import com.willwinder.ugs.nbp.jog.JogButtonMouseListener;
import com.willwinder.ugs.nbp.jog.JogPanelButtonEnum;
import com.willwinder.ugs.platform.probe.actions.ProbeZAction;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.services.LookupService;
import net.miginfocom.swing.MigLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * The card shown when the operator needs to change the tool. It provides jog controls for positioning the
 * machine together with actions for zeroing and probing the Z axis.
 *
 * <p>The jog buttons are handled by the shared {@link JogButtonHandler} in the same way as the jog plugin, so
 * a short click steps the machine and a long press jogs continuously until released.
 *
 * @author Joacim Breiler
 */
class JogCard extends JPanel {

    /**
     * How long the jog button must be pressed before continuous jog is activated, given in milliseconds.
     */
    private static final int LONG_PRESS_DELAY = 500;

    /**
     * The size of the large, easy to press jog buttons.
     */
    private static final int JOG_BUTTON_SIZE = 64;

    /**
     * The minimum height of the zeroing and probing action buttons.
     */
    private static final int ACTION_BUTTON_HEIGHT = 46;

    private static final String JOG_BUTTON_CONSTRAINTS = "grow, sg jogButton";

    private final BackendAPI backend;
    private final JogService jogService;
    private final JogButtonHandler jogButtonHandler;
    private final List<JButton> jogButtons = new ArrayList<>();
    private final ResetZCoordinateToZeroAction zeroZAction;
    private final ProbeZAction probeZAction;

    JogCard(BackendAPI backend) {
        super(new MigLayout("insets 16, gap 14, fillx", "[center]"));
        this.backend = backend;
        this.jogService = LookupService.lookup(JogService.class);
        this.jogButtonHandler = new JogButtonHandler(backend, jogService);
        this.zeroZAction = new ResetZCoordinateToZeroAction();
        this.probeZAction = new ProbeZAction();

        JPanel jogPad = new JPanel(new MigLayout("wrap 4, insets 0, gap 6"));
        jogPad.add(createSpacer(), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(jogButton(JogPanelButtonEnum.BUTTON_YPOS), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(createSpacer(), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(jogButton(JogPanelButtonEnum.BUTTON_ZPOS), JOG_BUTTON_CONSTRAINTS);

        jogPad.add(jogButton(JogPanelButtonEnum.BUTTON_XNEG), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(createSpacer(), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(jogButton(JogPanelButtonEnum.BUTTON_XPOS), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(createSpacer(), JOG_BUTTON_CONSTRAINTS);

        jogPad.add(createSpacer(), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(jogButton(JogPanelButtonEnum.BUTTON_YNEG), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(createSpacer(), JOG_BUTTON_CONSTRAINTS);
        jogPad.add(jogButton(JogPanelButtonEnum.BUTTON_ZNEG), JOG_BUTTON_CONSTRAINTS);

        JPanel actions = new JPanel(new MigLayout("insets 0, gap 10, fillx", "[grow, sg actionButton][grow, sg actionButton]"));
        actions.add(actionButton(zeroZAction), "growx, hmin " + ACTION_BUTTON_HEIGHT);
        actions.add(actionButton(probeZAction), "growx, hmin " + ACTION_BUTTON_HEIGHT);

        add(jogPad, "wrap");
        add(actions, "growx");
    }

    private JPanel createSpacer() {
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        return spacer;
    }

    void updateEnabledState() {
        boolean canJog = jogService.canJog();
        jogButtons.forEach(button -> button.setEnabled(canJog));
    }

    void destroy() {
        jogButtonHandler.destroy();
        backend.removeUGSEventListener(zeroZAction);
        backend.removeUGSEventListener(probeZAction);
    }

    private JButton jogButton(JogPanelButtonEnum buttonEnum) {
        JButton button = new JogButton(buttonEnum);
        button.setMinimumSize(new Dimension(JOG_BUTTON_SIZE, JOG_BUTTON_SIZE));
        button.setPreferredSize(new Dimension(JOG_BUTTON_SIZE, JOG_BUTTON_SIZE));
        button.addMouseListener(new JogButtonMouseListener(LONG_PRESS_DELAY,
                e -> jogButtonHandler.onJogButtonClicked(buttonEnum),
                e -> jogButtonHandler.onJogButtonLongPressed(buttonEnum),
                e -> jogButtonHandler.onJogButtonLongReleased()));

        jogButtons.add(button);
        return button;
    }

    private JButton actionButton(Action action) {
        JButton button = new JButton(action);
        button.setFocusable(false);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setIconTextGap(8);
        button.setMargin(new Insets(8, 14, 8, 14));
        return button;
    }
}
