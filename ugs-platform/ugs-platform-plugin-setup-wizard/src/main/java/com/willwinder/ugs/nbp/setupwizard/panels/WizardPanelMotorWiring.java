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
import com.willwinder.ugs.nbp.setupwizard.NavigationButtons;
import com.willwinder.ugs.nbp.setupwizard.WizardUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.AlarmEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.*;

/**
 * A wizard step panel for configuring motor wiring on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelMotorWiring extends AbstractWizardPanel implements UGSEventListener {

    private static final long TIME_BEFORE_RESET_ON_ALARM = 500;

    private NavigationButtons navigationButtons;

    private JCheckBox checkboxReverseX;
    private JCheckBox checkboxReverseY;
    private JCheckBox checkboxReverseZ;
    private JLabel labelDescription;

    private RoundedPanel softLimitsInfo;

    public WizardPanelMotorWiring(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.motor-wiring.title"));

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 3, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "span 3, gapbottom 10");
        panel.add(softLimitsInfo, "spanx, grow, gapbottom 10");

        String buttonConstraints = "hmin 36, wmin 36";
        panel.add(navigationButtons.getButtonXneg(), buttonConstraints);
        panel.add(navigationButtons.getButtonXpos(), buttonConstraints);
        panel.add(checkboxReverseX);
        panel.add(navigationButtons.getButtonYneg(), buttonConstraints);
        panel.add(navigationButtons.getButtonYpos(), buttonConstraints);
        panel.add(checkboxReverseY);
        panel.add(navigationButtons.getButtonZneg(), buttonConstraints);
        panel.add(navigationButtons.getButtonZpos(), buttonConstraints);
        panel.add(checkboxReverseZ);
        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.motor-wiring.intro") +
                "</p></body></html>");

        softLimitsInfo = new RoundedPanel(8);
        softLimitsInfo.setLayout(new MigLayout("fill, inset 10, gap 0"));
        softLimitsInfo.setBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
        softLimitsInfo.setForeground(ThemeColors.LIGHT_GREY);
        softLimitsInfo.add(new JLabel(ImageUtilities.loadImageIcon("icons/information24.png", false)), "gapright 10");
        softLimitsInfo.add(new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.motor-wiring.soft-limits-enabled") + "</body></html>"));

        navigationButtons = new NavigationButtons(getBackend(), 0.1, 100);

        checkboxReverseX = new JCheckBox(Localization.getString("platform.plugin.setupwizard.motor-wiring.reverse-direction"));
        checkboxReverseX.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setInvertDirection(Axis.X, checkboxReverseX.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });

        checkboxReverseY = new JCheckBox(Localization.getString("platform.plugin.setupwizard.motor-wiring.reverse-direction"));
        checkboxReverseY.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setInvertDirection(Axis.Y, checkboxReverseY.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });

        checkboxReverseZ = new JCheckBox(Localization.getString("platform.plugin.setupwizard.motor-wiring.reverse-direction"));
        checkboxReverseZ.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setInvertDirection(Axis.Z, checkboxReverseZ.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refreshReverseDirectionCheckboxes();
        refreshSoftLimitMessage();

        WizardUtils.killAlarm(getBackend());
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport();
    }

    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof FirmwareSettingEvent) {
            ThreadHelper.invokeLater(this::refreshReverseDirectionCheckboxes);
        } else if (event instanceof ControllerStateEvent) {
            WizardUtils.killAlarm(getBackend());
        } else if (event instanceof AlarmEvent && ((AlarmEvent) event).getAlarm() == Alarm.HARD_LIMIT) {
            ThreadHelper.invokeLater(() -> {
                try {
                    getBackend().issueSoftReset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, TIME_BEFORE_RESET_ON_ALARM);
        }

        if ((event instanceof ControllerStatusEvent)) {
           navigationButtons.refresh(((ControllerStatusEvent) event).getStatus().getMachineCoord());
        }
    }

    private void refreshSoftLimitMessage() {
        try {
            if (getBackend().getController() != null &&
                    getBackend().getController().getFirmwareSettings() != null &&
                    getBackend().getController().getFirmwareSettings().isSoftLimitsEnabled()) {
                softLimitsInfo.setVisible(true);
            } else {
                softLimitsInfo.setVisible(false);
            }
        }  catch (FirmwareSettingsException ignored) {
            softLimitsInfo.setVisible(false);
        }
    }

    private void refreshReverseDirectionCheckboxes() {
        IController controller = getBackend().getController();
        if (controller != null) {
            try {
                checkboxReverseX.setSelected(controller.getFirmwareSettings().isInvertDirection(Axis.X));
                checkboxReverseY.setSelected(controller.getFirmwareSettings().isInvertDirection(Axis.Y));
                checkboxReverseZ.setSelected(controller.getFirmwareSettings().isInvertDirection(Axis.Z));
            } catch (FirmwareSettingsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Unexpected error while getting setting: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }
}
