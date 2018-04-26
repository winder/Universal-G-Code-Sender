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
package com.willwinder.ugs.nbp.setupwizard.panels;

import com.willwinder.ugs.nbp.setupwizard.AbstractWizardPanel;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * A wizard step panel for configuring motor wiring on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelMotorWiring extends AbstractWizardPanel implements UGSEventListener, ControllerStateListener {

    private static final long TIME_BEFORE_RESET_ON_ALARM = 500;

    private JButton buttonXpos;
    private JButton buttonXneg;
    private JButton buttonYpos;
    private JButton buttonYneg;
    private JButton buttonZpos;
    private JButton buttonZneg;

    private JCheckBox checkboxReverseX;
    private JCheckBox checkboxReverseY;
    private JCheckBox checkboxReverseZ;
    private JLabel labelDescription;

    public WizardPanelMotorWiring(BackendAPI backend) {
        super(backend, "Motor wiring");

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 3, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "span 3, gapbottom 10");
        panel.add(buttonXneg, "hmin 36, wmin 36");
        panel.add(buttonXpos, "hmin 36, wmin 36");
        panel.add(checkboxReverseX);
        panel.add(buttonYneg, "hmin 36, wmin 36");
        panel.add(buttonYpos, "hmin 36, wmin 36");
        panel.add(checkboxReverseY);
        panel.add(buttonZneg, "hmin 36, wmin 36");
        panel.add(buttonZpos, "hmin 36, wmin 36");
        panel.add(checkboxReverseZ);

        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body>" +
                "<p>We will now test that your motors are wired correctly. Test each axis using the step buttons.</p>" +
                "</body></html>");

        buttonXneg = createJogButton("X-");
        buttonXneg.addActionListener(event -> moveMachine(-1, 0, 0));

        buttonXpos = createJogButton("X+");
        buttonXpos.addActionListener(event -> moveMachine(1, 0, 0));

        checkboxReverseX = new JCheckBox("Reverse direction");
        checkboxReverseX.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setInvertDirectionX(checkboxReverseX.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonYneg = createJogButton("Y-");
        buttonYneg.addActionListener(event -> moveMachine(0, -1, 0));

        buttonYpos = createJogButton("Y+");
        buttonYpos.addActionListener(event -> moveMachine(0, 1, 0));

        checkboxReverseY = new JCheckBox("Reverse direction");
        checkboxReverseY.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setInvertDirectionY(checkboxReverseY.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonZneg = createJogButton("Z-");
        buttonZneg.addActionListener(event -> moveMachine(0, 0, -1));

        buttonZpos = createJogButton("Z+");
        buttonZpos.addActionListener(event -> moveMachine(0, 0, 1));

        checkboxReverseZ = new JCheckBox("Reverse direction");
        checkboxReverseZ.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setInvertDirectionZ(checkboxReverseZ.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void moveMachine(int x, int y, int z) {
        try {
            IController controller = getBackend().getController();
            if (controller.getState() == ControllerState.ALARM) {
                killAlarm();
            } else {
                controller.jogMachine(x, y, z, 0.1, 100, UnitUtils.Units.MM);
            }
        } catch (Exception e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unexpected error while moving the machine: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private JButton createJogButton(String text) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(0, 0, 0, 0));
        return button;
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        getBackend().addControllerStateListener(this);
        refreshReverseDirectionCheckboxes();
        killAlarm();
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport();
    }

    @Override
    public void destroy() {
        getBackend().removeControllerStateListener(this);
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event.getEventType() == UGSEvent.EventType.FIRMWARE_SETTING_EVENT) {
            ThreadHelper.invokeLater(this::refreshReverseDirectionCheckboxes);
        } else if (event.isControllerStatusEvent() || event.isStateChangeEvent()) {
            killAlarm();
        } else if (event.getEventType() == UGSEvent.EventType.ALARM_EVENT && event.getAlarm() == Alarm.HARD_LIMIT) {
            ThreadHelper.invokeLater(() -> {
                try {
                    getBackend().issueSoftReset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, TIME_BEFORE_RESET_ON_ALARM);
        }
    }

    private void refreshReverseDirectionCheckboxes() {
        IController controller = getBackend().getController();
        if (controller != null) {
            checkboxReverseX.setSelected(controller.getFirmwareSettings().isInvertDirectionX());
            checkboxReverseY.setSelected(controller.getFirmwareSettings().isInvertDirectionY());
            checkboxReverseZ.setSelected(controller.getFirmwareSettings().isInvertDirectionZ());
        }
    }

    private void killAlarm() {
        IController controller = getBackend().getController();
        if (controller != null) {
            ControllerState state = controller.getState();
            if (state == ControllerState.ALARM) {
                try {
                    controller.killAlarmLock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
