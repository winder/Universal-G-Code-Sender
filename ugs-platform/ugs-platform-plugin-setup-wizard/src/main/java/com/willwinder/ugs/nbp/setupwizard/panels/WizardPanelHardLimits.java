/*
    Copyright 2018-2020 Will Winder

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
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.AlarmEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.uielements.components.RoundedBorder;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * A wizard step panel for configuring hard limits on a controller.
 * This panel will listen for any hard limit alarms and reset the controller.
 *
 * @author Joacim Breiler
 */
public class WizardPanelHardLimits extends AbstractWizardPanel implements UGSEventListener {
    private static final int TIME_BEFORE_RESET_ON_ALARM = 600;
    private JCheckBox checkboxEnableHardLimits;
    private JLabel labelHardLimitsNotSupported;
    private JLabel labelDescription;
    private JLabel labelInstructions;
    private JLabel labelLimitX;
    private JLabel labelLimitY;
    private JLabel labelLimitZ;
    private JCheckBox checkboxInvertLimitPins;

    public WizardPanelHardLimits(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.limit-switches.title"));

        initComponents();
        initLayout();
        setValid(true);
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "growx, wrap, gapbottom 10, spanx");
        panel.add(checkboxEnableHardLimits, "wrap, gapbottom 10, spanx");
        panel.add(labelHardLimitsNotSupported, "wrap, spanx");

        panel.add(labelInstructions, "spanx, gapbottom 10, wrap");
        panel.add(labelLimitX, "wmin 56, hmin 36, gapleft 5");
        panel.add(labelLimitY, "wmin 56, hmin 36, gapleft 10");
        panel.add(labelLimitZ, "wmin 56, hmin 36, gapleft 10, wrap");

        panel.add(checkboxInvertLimitPins, "gaptop 10, spanx, wrap");
        getPanel().add(panel, "grow, wrap");
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.limit-switches.intro") +
                "</p></body></html>");

        labelInstructions = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.limit-switches.instruction") +
                "</p></body></html>");
        labelInstructions.setVisible(false);

        checkboxEnableHardLimits = new JCheckBox("Enable limit switches");
        checkboxEnableHardLimits.addActionListener(event -> onHardLimitsClicked());

        labelHardLimitsNotSupported = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.limit-switches.not-available") + "</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHardLimitsNotSupported.setVisible(false);

        labelLimitX = createLimitLabel("X");
        labelLimitX.setVisible(false);
        labelLimitY = createLimitLabel("Y");
        labelLimitY.setVisible(false);
        labelLimitZ = createLimitLabel("Z");
        labelLimitZ.setVisible(false);

        checkboxInvertLimitPins = new JCheckBox(Localization.getString("platform.plugin.setupwizard.limit-switches.invert"));
        checkboxInvertLimitPins.setVisible(false);
        checkboxInvertLimitPins.addActionListener(event -> {
            if (getBackend().getController() != null) {
                try {
                    getBackend().getController().getFirmwareSettings().setHardLimitsInverted(checkboxInvertLimitPins.isSelected());
                } catch (FirmwareSettingsException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private JLabel createLimitLabel(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(new RoundedBorder(8));
        label.setForeground(Color.WHITE);
        label.setBackground(ThemeColors.LIGHT_GREEN);
        label.setOpaque(true);
        return label;
    }

    private void onHardLimitsClicked() {
        try {
            getBackend().getController().getFirmwareSettings().setHardLimitsEnabled(checkboxEnableHardLimits.isSelected());
        } catch (FirmwareSettingsException e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't enable/disable the hard limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refreshComponents();
    }

    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof FirmwareSettingEvent) {
            refreshComponents();
        } else if (evt instanceof ControllerStatusEvent) {
            ThreadHelper.invokeLater(() -> {
                ControllerStatus controllerStatus = ((ControllerStatusEvent) evt).getStatus();
                labelLimitX.setBackground(controllerStatus.getEnabledPins().X ? ThemeColors.RED : ThemeColors.LIGHT_GREEN);
                labelLimitY.setBackground(controllerStatus.getEnabledPins().Y ? ThemeColors.RED : ThemeColors.LIGHT_GREEN);
                labelLimitZ.setBackground(controllerStatus.getEnabledPins().Z ? ThemeColors.RED : ThemeColors.LIGHT_GREEN);
            });
        } else if (evt instanceof AlarmEvent && ((AlarmEvent) evt).getAlarm() == Alarm.HARD_LIMIT) {
            ThreadHelper.invokeLater(() -> {
                try {
                    getBackend().issueSoftReset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, TIME_BEFORE_RESET_ON_ALARM);
        }
    }

    private void refreshComponents() {
        ThreadHelper.invokeLater(() -> {
            try {
                if (getBackend().getController() != null && getBackend().getController().getCapabilities().hasHardLimits()) {
                    IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                    checkboxEnableHardLimits.setSelected(firmwareSettings.isHardLimitsEnabled());
                    checkboxInvertLimitPins.setSelected(firmwareSettings.isHardLimitsInverted());
                    checkboxEnableHardLimits.setVisible(true);
                    checkboxInvertLimitPins.setVisible(firmwareSettings.isHardLimitsEnabled());
                    labelInstructions.setVisible(firmwareSettings.isHardLimitsEnabled());
                    labelLimitX.setVisible(firmwareSettings.isHardLimitsEnabled());
                    labelLimitY.setVisible(firmwareSettings.isHardLimitsEnabled());
                    labelLimitZ.setVisible(firmwareSettings.isHardLimitsEnabled());
                    labelHardLimitsNotSupported.setVisible(false);
                } else {
                    checkboxEnableHardLimits.setVisible(false);
                    checkboxInvertLimitPins.setVisible(false);
                    labelInstructions.setVisible(false);
                    labelLimitX.setVisible(false);
                    labelLimitY.setVisible(false);
                    labelLimitZ.setVisible(false);
                    labelHardLimitsNotSupported.setVisible(true);
                }
            } catch (FirmwareSettingsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't fetch the hard limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }, 200);
    }
}
