/*
    Copyright 2018-2019 Will Winder

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

import com.willwinder.ugs.nbp.jog.actions.UseSeparateStepSizeAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import java.awt.*;
import org.openide.util.Lookup;

import javax.swing.JPopupMenu;

/**
 * The jog control panel in NetBeans
 *
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "JogTopComponent"
)
@TopComponent.Registration(
        mode = "middle_left",
        openAtStartup = true)
@ActionID(
        category = JogTopComponent.CATEGORY,
        id = JogTopComponent.ACTION_ID)
@ActionReference(
        path = JogTopComponent.WINOW_PATH)
@TopComponent.OpenActionRegistration(
        displayName = "Jog Controller",
        preferredID = "JogTopComponent"
)
public final class JogTopComponent extends TopComponent implements UGSEventListener, JogPanelListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW_PLUGIN;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.jog.JogTopComponent";

    private final BackendAPI backend;
    private final JogPanel jogPanel;
    private final JogService jogService;
    private final ContinuousJogHandler continuousJogHandler;

    public JogTopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        jogService = CentralLookup.getDefault().lookup(JogService.class);
        continuousJogHandler = new ContinuousJogHandler(backend, jogService);
        UseSeparateStepSizeAction separateStepSizeAction = Lookup.getDefault().lookup(UseSeparateStepSizeAction.class);

        jogPanel = new JogPanel();
        jogPanel.setEnabled(jogService.canJog());
        jogPanel.setFeedRate(Double.valueOf(jogService.getFeedRate()).intValue());
        jogPanel.setStepSizeXY(jogService.getStepSizeXY());
        jogPanel.setStepSizeZ(jogService.getStepSizeZ());
        jogPanel.setUnit(jogService.getUnits());
        jogPanel.setUseStepSizeZ(jogService.useStepSizeZ());
        jogPanel.addListener(this);

        backend.addUGSEventListener(this);
        backend.addControllerListener(continuousJogHandler);

        setLayout(new BorderLayout());
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);

        setPreferredSize(new Dimension(250, 270));

        add(jogPanel, BorderLayout.CENTER);

        if (separateStepSizeAction != null) {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(separateStepSizeAction);
            SwingHelpers.traverse(this, (comp) -> comp.setComponentPopupMenu(popupMenu));
        }
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(this);

        continuousJogHandler.stop();
        backend.removeControllerListener(continuousJogHandler);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        boolean canJog = jogService.canJog();
        if (canJog != jogPanel.isEnabled()) {
            jogPanel.setEnabled(canJog);
        }

        if (event.isSettingChangeEvent()) {
            jogPanel.setFeedRate(Double.valueOf(backend.getSettings().getJogFeedRate()).intValue());
            jogPanel.setStepSizeXY(backend.getSettings().getManualModeStepSize());
            jogPanel.setStepSizeZ(backend.getSettings().getzJogStepSize());
            jogPanel.setUnit(backend.getSettings().getPreferredUnits());
            jogPanel.setUseStepSizeZ(backend.getSettings().useZStepSize());
        }
    }

    @Override
    public void onButtonClicked(JogPanelButtonEnum button) {
        switch (button) {
            case BUTTON_XNEG:
                jogService.adjustManualLocationXY(-1, 0);
                break;
            case BUTTON_XPOS:
                jogService.adjustManualLocationXY(1, 0);
                break;
            case BUTTON_YNEG:
                jogService.adjustManualLocationXY(0, -1);
                break;
            case BUTTON_YPOS:
                jogService.adjustManualLocationXY(0, 1);
                break;
            case BUTTON_DIAG_XNEG_YNEG:
                jogService.adjustManualLocationXY(-1, -1);
                break;
            case BUTTON_DIAG_XNEG_YPOS:
                jogService.adjustManualLocationXY(-1, 1);
                break;
            case BUTTON_DIAG_XPOS_YNEG:
                jogService.adjustManualLocationXY(1, -1);
                break;
            case BUTTON_DIAG_XPOS_YPOS:
                jogService.adjustManualLocationXY(1, 1);
                break;
            case BUTTON_ZNEG:
                jogService.adjustManualLocationZ(-1);
                break;
            case BUTTON_ZPOS:
                jogService.adjustManualLocationZ(1);
                break;
            case BUTTON_TOGGLE_UNIT:
                if (jogService.getUnits() == UnitUtils.Units.MM) {
                    jogService.setUnits(UnitUtils.Units.INCH);
                } else {
                    jogService.setUnits(UnitUtils.Units.MM);
                }
                break;
            case BUTTON_LARGER_STEP:
                jogService.multiplyXYStepSize();
                jogService.multiplyZStepSize();
                break;
            case BUTTON_SMALLER_STEP:
                jogService.divideXYStepSize();
                jogService.divideZStepSize();
                break;
            default:
        }
    }

    @Override
    public void onButtonLongPressed(JogPanelButtonEnum button) {
        if (backend.getController().getCapabilities().hasContinuousJogging()) {
            continuousJogHandler.start(button);
        }
    }

    @Override
    public void onButtonLongReleased(JogPanelButtonEnum button) {
        continuousJogHandler.stop();
    }

    @Override
    public void onStepSizeZChanged(double value) {
        jogService.setStepSizeZ(value);
    }

    @Override
    public void onStepSizeXYChanged(double value) {
        jogService.setStepSizeXY(value);
    }

    @Override
    public void onFeedRateChanged(int value) {
        jogService.setFeedRate(value);
    }
}
