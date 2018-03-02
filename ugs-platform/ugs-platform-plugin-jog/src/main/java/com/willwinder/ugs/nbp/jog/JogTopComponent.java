/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import java.awt.*;

/**
 * The jog control panel in NetBeans
 *
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "JogTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(
        mode = "top_left",
        openAtStartup = true)
@ActionID(
        category = JogTopComponent.CATEGORY,
        id = JogTopComponent.ACTION_ID)
@ActionReference(
        path = JogTopComponent.WINOW_PATH)
@TopComponent.OpenActionRegistration(
        displayName = "Jog",
        preferredID = "JogTopComponent"
)
public final class JogTopComponent extends TopComponent implements JogPanelListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.jog.JogTopComponent";
    private static final double FEED_STEP_SIZE = 10;

    private final BackendAPI backend;
    private final JogPanel jogPanel;
    private final JogService jogService;

    public JogTopComponent() {
        jogService = CentralLookup.getDefault().lookup(JogService.class);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);

        setMinimumSize(new Dimension(200, 250));
        setPreferredSize(new Dimension(200, 250));
        setLayout(new BorderLayout());
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);

        jogPanel = new JogPanel();
        jogPanel.addJogPanelListener(this);
        jogPanel.setEnabled(jogService.canJog());
        jogPanel.setJogFeedRate(Double.valueOf(backend.getSettings().getJogFeedRate()).intValue());
        jogPanel.setXyStepLength(backend.getSettings().getManualModeStepSize());
        jogPanel.setUnit(backend.getSettings().getPreferredUnits());
        add(jogPanel, BorderLayout.CENTER);
    }

    private void onEvent(UGSEvent event) {
        // Only update the panel if required
        boolean canJog = jogService.canJog();
        if (canJog != jogPanel.isEnabled()) {
            jogPanel.setEnabled(canJog);
        }

        if (event.isSettingChangeEvent()) {
            jogPanel.setJogFeedRate(Double.valueOf(backend.getSettings().getJogFeedRate()).intValue());
            jogPanel.setXyStepLength(backend.getSettings().getManualModeStepSize());
            jogPanel.setUnit(backend.getSettings().getPreferredUnits());
        }
    }

    @Override
    public void onClick(JogPanelButtonEnum button) {
        if (!jogService.canJog()) {
            return;
        }

        try {
            switch (button) {
                case BUTTON_XPOS:
                    jogService.adjustManualLocationXY(1, 0);
                    break;
                case BUTTON_XNEG:
                    jogService.adjustManualLocationXY(-1, 0);
                    break;
                case BUTTON_YPOS:
                    jogService.adjustManualLocationXY(0, 1);
                    break;
                case BUTTON_YNEG:
                    jogService.adjustManualLocationXY(0, -1);
                    break;
                case BUTTON_DIAG_XNEG_YNEG:
                    jogService.adjustManualLocationXY(-1, -1);
                    break;
                case BUTTON_DIAG_XPOS_YNEG:
                    jogService.adjustManualLocationXY(1, -1);
                    break;
                case BUTTON_DIAG_XNEG_YPOS:
                    jogService.adjustManualLocationXY(-1, 1);
                    break;
                case BUTTON_DIAG_XPOS_YPOS:
                    jogService.adjustManualLocationXY(1, 1);
                    break;
                case BUTTON_ZPOS:
                    jogService.adjustManualLocationZ(1);
                    break;
                case BUTTON_ZNEG:
                    jogService.adjustManualLocationZ(-1);
                    break;
                case BUTTON_TOGGLE_UNIT:
                    if (jogService.getUnits() == UnitUtils.Units.MM) {
                        jogService.setUnits(UnitUtils.Units.INCH);
                    } else {
                        jogService.setUnits(UnitUtils.Units.MM);
                    }
                    break;
                case BUTTON_FEED_INC:
                    jogService.setFeedRate(jogService.getFeedRate() + FEED_STEP_SIZE);
                    break;
                case BUTTON_FEED_DEC:
                    jogService.setFeedRate(jogService.getFeedRate() - FEED_STEP_SIZE);
                    break;
                case BUTTON_STEP_INC:
                    jogService.increaseStepSize();
                    break;
                case BUTTON_STEP_DEC:
                    jogService.decreaseStepSize();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPressed(JogPanelButtonEnum button) {
    }

    @Override
    public void onReleased(JogPanelButtonEnum button) {
    }
}
