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
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * A wizard step panel for configuring homing on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelHoming extends AbstractWizardPanel implements UGSEventListener {
    private JCheckBox checkboxEnableHoming;
    private JLabel labelHomingNotSupported;
    private JLabel labelHardLimitsNotEnabled;
    private JLabel labelDescription;
    private JLabel labelHomingDirection;
    private JLabel labelHomingInstructions;
    private JComboBox<String> comboBoxInvertDirectionX;
    private JComboBox<String> comboBoxInvertDirectionY;
    private JComboBox<String> comboBoxInvertDirectionZ;
    private JButton buttonHomeMachine;
    private JButton buttonAbort;
    private JSeparator separatorBottom;
    private JSeparator separatorTop;

    public WizardPanelHoming(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.homing.title"));

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 3, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "gapbottom 10, spanx");
        panel.add(checkboxEnableHoming, "spanx");
        panel.add(labelHardLimitsNotEnabled, "spanx");
        panel.add(labelHomingNotSupported, "spanx");

        panel.add(separatorTop, "spanx, hmin 10, gaptop 10, grow");

        panel.add(labelHomingDirection, "spanx, gaptop 10, gapbottom 10");
        panel.add(comboBoxInvertDirectionX, "wmin 130");
        panel.add(comboBoxInvertDirectionY, "wmin 130");
        panel.add(comboBoxInvertDirectionZ, "wmin 130");

        panel.add(separatorBottom, "spanx, hmin 10, gaptop 10, grow");

        panel.add(labelHomingInstructions, "spanx, gaptop 10, gapbottom 10");
        panel.add(buttonHomeMachine, "wmin 130, hmin 36");
        panel.add(buttonAbort, "wmin 130, hmin 36");
        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.homing.intro") +
                "</p></body></html>");

        checkboxEnableHoming = new JCheckBox(Localization.getString("platform.plugin.setupwizard.homing.enable"));
        checkboxEnableHoming.addActionListener(event -> {
            try {
                getBackend().getController().getFirmwareSettings().setHomingEnabled(checkboxEnableHoming.isSelected());
            } catch (FirmwareSettingsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't enable/disable the hard limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        });

        labelHardLimitsNotEnabled = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.require-limit-switches") + "</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHardLimitsNotEnabled.setVisible(false);

        labelHomingNotSupported = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.not-available") + "</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHomingNotSupported.setVisible(false);

        separatorTop = new JSeparator(SwingConstants.HORIZONTAL);
        separatorTop.setVisible(false);

        labelHomingDirection = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.instruction1") + "</body></html>");
        labelHomingDirection.setVisible(false);

        initInvertComboBoxes();

        separatorBottom = new JSeparator(SwingConstants.HORIZONTAL);
        separatorBottom.setVisible(false);

        labelHomingInstructions = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.homing.instruction2") +
                "</p></body></html>");
        labelHomingInstructions.setVisible(false);
        initButtons();
    }

    private void initButtons() {
        buttonHomeMachine = new JButton(Localization.getString("platform.plugin.setupwizard.homing.try-homing"));
        buttonHomeMachine.setVisible(false);
        buttonHomeMachine.addActionListener(event -> {
            try {
                getBackend().performHomingCycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonAbort = new JButton(Localization.getString("platform.plugin.setupwizard.homing.abort"));
        buttonAbort.setVisible(false);
        buttonAbort.addActionListener(event -> {
            try {
                getBackend().cancel();
                getBackend().issueSoftReset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initInvertComboBoxes() {
        comboBoxInvertDirectionX = createInvertComboBox(Axis.X);
        comboBoxInvertDirectionY = createInvertComboBox(Axis.Y);
        comboBoxInvertDirectionZ = createInvertComboBox(Axis.Z);
    }

    private JComboBox<String> createInvertComboBox(Axis axis) {
        JComboBox<String> result = new JComboBox<>();
        result.setVisible(false);
        result.addItem("+" + axis.name());
        result.addItem("-" + axis.name());
        result.addActionListener(event -> {
            IController controller = getBackend().getController();
            if (controller != null) {
                try {
                    controller.getFirmwareSettings().setHomingDirectionInverted(axis, result.getSelectedIndex() == 1);
                } catch (FirmwareSettingsException e) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message("Unexpected error while updating setting: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        return result;
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refreshControls();
    }

    private void refreshControls() {
        ThreadHelper.invokeLater(() -> {
                    try {
                        if (getBackend().getController() != null &&
                                getBackend().getController().getCapabilities().hasHoming() &&
                                getBackend().getController().getCapabilities().hasHardLimits() &&
                                getBackend().getController().getFirmwareSettings().isHardLimitsEnabled()) {

                            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                            checkboxEnableHoming.setSelected(firmwareSettings.isHomingEnabled());
                            checkboxEnableHoming.setVisible(true);

                            labelHomingDirection.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionX.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionX.setSelectedIndex(firmwareSettings.isHomingDirectionInverted(Axis.X) ? 1 : 0);

                            comboBoxInvertDirectionY.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionY.setSelectedIndex(firmwareSettings.isHomingDirectionInverted(Axis.Y) ? 1 : 0);

                            comboBoxInvertDirectionZ.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionZ.setSelectedIndex(firmwareSettings.isHomingDirectionInverted(Axis.Z) ? 1 : 0);

                            labelHomingNotSupported.setVisible(false);
                            labelHardLimitsNotEnabled.setVisible(false);

                            labelHomingInstructions.setVisible(firmwareSettings.isHomingEnabled());
                            buttonHomeMachine.setVisible(firmwareSettings.isHomingEnabled());
                            buttonAbort.setVisible(firmwareSettings.isHomingEnabled());
                            separatorTop.setVisible(firmwareSettings.isHomingEnabled());
                            separatorBottom.setVisible(firmwareSettings.isHomingEnabled());
                        } else if (getBackend().getController() != null &&
                                getBackend().getController().getCapabilities().hasHoming() &&
                                !getBackend().getController().getFirmwareSettings().isHardLimitsEnabled()) {
                            checkboxEnableHoming.setVisible(false);
                            comboBoxInvertDirectionX.setVisible(false);
                            comboBoxInvertDirectionY.setVisible(false);
                            comboBoxInvertDirectionZ.setVisible(false);
                            labelHomingNotSupported.setVisible(false);
                            labelHardLimitsNotEnabled.setVisible(true);
                            labelHomingDirection.setVisible(false);
                            labelHomingInstructions.setVisible(false);
                            buttonHomeMachine.setVisible(false);
                            buttonAbort.setVisible(false);
                            separatorTop.setVisible(false);
                            separatorBottom.setVisible(false);
                        } else {
                            checkboxEnableHoming.setVisible(false);
                            comboBoxInvertDirectionX.setVisible(false);
                            comboBoxInvertDirectionY.setVisible(false);
                            comboBoxInvertDirectionZ.setVisible(false);
                            labelHomingNotSupported.setVisible(true);
                            labelHardLimitsNotEnabled.setVisible(false);
                            labelHomingDirection.setVisible(false);
                            labelHomingInstructions.setVisible(false);
                            buttonHomeMachine.setVisible(false);
                            buttonAbort.setVisible(false);
                            separatorTop.setVisible(false);
                            separatorBottom.setVisible(false);
                        }
                    } catch (FirmwareSettingsException e) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't fetch firmware settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                },
                200);
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
            refreshControls();
        }
    }
}
