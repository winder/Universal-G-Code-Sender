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

import com.willwinder.ugs.nbp.jog.actions.UseSeparateStepSizeAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import java.awt.*;
import org.openide.util.Lookup;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
        mode = "top_left",
        openAtStartup = false)
@ActionID(
        category = JogTopComponent.CATEGORY,
        id = JogTopComponent.ACTION_ID)
@ActionReference(
        path = JogTopComponent.WINOW_PATH)
@TopComponent.OpenActionRegistration(
        displayName = "Jog Controller",
        preferredID = "JogTopComponent"
)
public final class JogTopComponent extends TopComponent implements UGSEventListener, ControllerListener, JogPanelListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW_PLUGIN;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.jog.JogTopComponent";

    /**
     * The inteval in milliseconds to send jog commands to the controller when
     * continuous jog is activated. This should be long enough so that the queue
     * isn't filled up.
     */
    private static final int LONG_PRESS_JOG_INTERVAL = 500;

    /**
     * The step size for continuous jog commands. These should be long enough
     * to keep the controller jogging before a new jog command is queued.
     */
    private static final double LONG_PRESS_MM_STEP_SIZE = 5;
    private static final double LONG_PRESS_INCH_STEP_SIZE = 0.2;

    private final BackendAPI backend;
    private final JogPanel jogPanel;
    private final JogService jogService;
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> continuousJogSchedule;

    public JogTopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        jogService = CentralLookup.getDefault().lookup(JogService.class);
        UseSeparateStepSizeAction action = Lookup.getDefault().lookup(UseSeparateStepSizeAction.class);

        jogPanel = new JogPanel();
        jogPanel.setEnabled(jogService.canJog());
        jogPanel.setFeedRate(Double.valueOf(jogService.getFeedRate()).intValue());
        jogPanel.setStepSizeXY(jogService.getStepSizeXY());
        jogPanel.setStepSizeZ(jogService.getStepSizeZ());
        jogPanel.setUnit(jogService.getUnits());
        jogPanel.setUseStepSizeZ(jogService.useStepSizeZ());
        jogPanel.addListener(this);
        
        backend.addUGSEventListener(this);
        backend.addControllerListener(this);

        setLayout(new BorderLayout());
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);

        setPreferredSize(new Dimension(250, 270));

        add(jogPanel, BorderLayout.CENTER);

        if (action != null) {
            JPopupMenu popupMenu = new JPopupMenu();
            popupMenu.add(action);
            SwingHelpers.traverse(this, (comp) -> comp.setComponentPopupMenu(popupMenu));
        }
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(this);
        backend.removeControllerListener(this);
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
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void receivedAlarm(Alarm alarm) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComplete(GcodeCommand command) {
        // If there is a command with an error, assume we are jogging and cancel any event
        if (command.isError() && continuousJogSchedule != null) {
            continuousJogSchedule.cancel(true);
            jogService.cancelJog();
        }
    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void probeCoordinates(Position p) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {

    }

    @Override
    public void postProcessData(int numRows) {

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
            default:
        }
    }

    @Override
    public void onButtonLongPressed(JogPanelButtonEnum button) {
        if (backend.getController().getCapabilities().hasContinuousJogging()) {

            // Cancel any previous jogging
            if (continuousJogSchedule != null) {
                continuousJogSchedule.cancel(true);
            }

            continuousJogSchedule = EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
                // TODO add a check so that no more than one or two jog commands are queued on the controller. Otherwise a soft limit may trigger if too many commands are queued.
                double stepSize = LONG_PRESS_MM_STEP_SIZE;
                if (jogService.getUnits() == UnitUtils.Units.INCH) {
                    stepSize = LONG_PRESS_INCH_STEP_SIZE;
                }

                switch (button) {
                    case BUTTON_XNEG:
                        jogService.adjustManualLocation(-1, 0, 0, stepSize);
                        break;
                    case BUTTON_XPOS:
                        jogService.adjustManualLocation(1, 0, 0, stepSize);
                        break;
                    case BUTTON_YNEG:
                        jogService.adjustManualLocation(0, -10, 0, stepSize);
                        break;
                    case BUTTON_YPOS:
                        jogService.adjustManualLocation(0, 10, 0, stepSize);
                        break;
                    case BUTTON_DIAG_XNEG_YNEG:
                        jogService.adjustManualLocation(-1, -1, 0, stepSize);
                        break;
                    case BUTTON_DIAG_XNEG_YPOS:
                        jogService.adjustManualLocation(-1, 1, 0, stepSize);
                        break;
                    case BUTTON_DIAG_XPOS_YNEG:
                        jogService.adjustManualLocation(1, -1, 0, stepSize);
                        break;
                    case BUTTON_DIAG_XPOS_YPOS:
                        jogService.adjustManualLocation(1, 1, 0, stepSize);
                        break;
                    case BUTTON_ZNEG:
                        jogService.adjustManualLocation(0, 0, -1, stepSize);
                        break;
                    case BUTTON_ZPOS:
                        jogService.adjustManualLocation(0, 0, 1, stepSize);
                        break;
                    default:
                }
            }, 0, LONG_PRESS_JOG_INTERVAL, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onButtonLongReleased(JogPanelButtonEnum button) {
        if( continuousJogSchedule != null ) {
            continuousJogSchedule.cancel(true);
        }
        jogService.cancelJog();
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
