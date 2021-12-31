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
import com.willwinder.ugs.nbp.setupwizard.NavigationButtons;
import com.willwinder.ugs.nbp.setupwizard.WizardUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.model.events.AlarmEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.utils.MathUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * A wizard step panel for configuring step length on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelStepCalibration extends AbstractWizardPanel implements UGSEventListener {

    private static final long TIME_BEFORE_RESET_ON_ALARM = 500;
    public static final String DECIMAL_FORMAT_PATTERN = "0.###";
    private NavigationButtons navigationButtons;
    private JLabel labelEstimatedStepsX;
    private JLabel labelPositionX;
    private JButton buttonUpdateSettingsX;
    private JTextField textFieldMeasuredX;
    private JTextField textFieldSettingStepsX;
    private JButton buttonUpdateSettingsY;
    private JTextField textFieldMeasuredY;
    private JLabel labelEstimatedStepsY;
    private JTextField textFieldSettingStepsY;
    private JLabel labelPositionY;
    private JButton buttonUpdateSettingsZ;
    private JTextField textFieldMeasuredZ;
    private JLabel labelEstimatedStepsZ;
    private JTextField textFieldSettingStepsZ;
    private JLabel labelPositionZ;

    public WizardPanelStepCalibration(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.calibration.title"));

        initComponents();
        initLayout();

        setValid(true);
    }

    private void initLayout() {

        JLabel description = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.calibration.intro") +
                "</p></body></html>");
        getPanel().add(description, "grow, wrap");

        JPanel panel = new JPanel(new MigLayout("fill, inset 0"));
        addHeaderRow(panel);
        addSubHeaderRow(panel);
        addAxisRow(panel, navigationButtons.getButtonXneg(), navigationButtons.getButtonXpos(), buttonUpdateSettingsX, labelPositionX, labelEstimatedStepsX, textFieldMeasuredX, textFieldSettingStepsX);
        addAxisRow(panel, navigationButtons.getButtonYneg(), navigationButtons.getButtonYpos(), buttonUpdateSettingsY, labelPositionY, labelEstimatedStepsY, textFieldMeasuredY, textFieldSettingStepsY);
        addAxisRow(panel, navigationButtons.getButtonZneg(), navigationButtons.getButtonZpos(), buttonUpdateSettingsZ, labelPositionZ, labelEstimatedStepsZ, textFieldMeasuredZ, textFieldSettingStepsZ);
        getPanel().add(panel, "grow");
    }

    private void addAxisRow(JPanel panel,
                            JButton buttonMoveInNegativeDirection,
                            JButton buttonMoveInPositiveDirection,
                            JButton buttonUpdateSettings,
                            JLabel labelCurrentPosition,
                            JLabel labelEstimatedSteps,
                            JTextField textFieldMeasurement,
                            JTextField textFieldSettingSteps) {

        panel.add(buttonMoveInNegativeDirection, "shrink, w 46, gapbottom 5");
        panel.add(labelCurrentPosition, "grow, gapbottom 5");
        panel.add(buttonMoveInPositiveDirection, "shrink, w 46, gapbottom 5");

        JPanel panelMeasureX = new JPanel(new MigLayout("fill, inset 0"));
        panelMeasureX.add(textFieldMeasurement, "growx, wmin 50");
        panelMeasureX.add(new JLabel("mm"), "wrap");
        panel.add(panelMeasureX, "grow, span 2, gapbottom 5");

        JPanel estimationPanelX = new JPanel(new MigLayout("fill, inset 0"));
        estimationPanelX.add(labelEstimatedSteps, "gapleft 5, span 2, wrap");
        estimationPanelX.add(textFieldSettingSteps, "growx, wmin 50");
        estimationPanelX.add(buttonUpdateSettings, "growx");
        panel.add(estimationPanelX, "grow, spanx 3, wrap, gapbottom 5");
    }

    private void addSubHeaderRow(JPanel panel) {
        JButton resetButton = new JButton(Localization.getString("platform.plugin.setupwizard.reset-to-zero"));
        resetButton.setMinimumSize(new Dimension(36, 36));
        resetButton.addActionListener(event -> {
            try {
                getBackend().resetCoordinatesToZero();
            } catch (Exception ignored) {
                // Never mind
            }
        });
        panel.add(resetButton, "grow, spanx 3, gapbottom 0, gaptop 0");
        panel.add(new JLabel(Localization.getString("platform.plugin.setupwizard.calibration.actual-movement")), "span 2, grow");
        panel.add(new JLabel(Localization.getString("platform.plugin.setupwizard.calibration.adjust")), "spanx 5, grow, wrap");
    }

    private void addHeaderRow(JPanel panel) {
        Font labelHeaderFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        JLabel headerLabel = new JLabel(Localization.getString("platform.plugin.setupwizard.move"), JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 3, gapbottom 5, gaptop 7");
        panel.add(new JSeparator(SwingConstants.VERTICAL), "spany 5, gapleft 5, gapright 5, wmin 10, grow");

        headerLabel = new JLabel(Localization.getString("platform.plugin.setupwizard.measure"), JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 2, gapbottom 5, gaptop 7");
        panel.add(new JSeparator(SwingConstants.VERTICAL), "spany 5, gapleft 5, gapright 5, wmin 10, grow");

        headerLabel = new JLabel(Localization.getString("platform.plugin.setupwizard.calibrate"), JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 3, wrap, gapbottom 5, gaptop 7");
    }

    private void initComponents() {
        Font labelEstimatedFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        navigationButtons = new NavigationButtons(getBackend(), 1.0, (int) getBackend().getSettings().getJogFeedRate());

        buttonUpdateSettingsX = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        labelEstimatedStepsX = new JLabel("0 steps/mm");
        labelEstimatedStepsX.setFont(labelEstimatedFont);
        labelPositionX = new JLabel("  0.0 mm", JLabel.RIGHT);
        textFieldMeasuredX = new JTextField("0.0");
        textFieldMeasuredX.addKeyListener(createKeyListener(Axis.X, labelEstimatedStepsX));
        textFieldSettingStepsX = new JTextField("0.0");
        textFieldSettingStepsX.addKeyListener(createKeyListenerChangeSetting());
        buttonUpdateSettingsX.setEnabled(false);
        buttonUpdateSettingsX.addActionListener(createListenerUpdateSetting(Axis.X, textFieldSettingStepsX));

        buttonUpdateSettingsY = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        labelEstimatedStepsY = new JLabel(Localization.getString("platform.plugin.setupwizard.calibration.setting"));
        labelEstimatedStepsY.setFont(labelEstimatedFont);
        labelPositionY = new JLabel("  0.0 mm", JLabel.RIGHT);
        textFieldMeasuredY = new JTextField("0.0");
        textFieldMeasuredY.addKeyListener(createKeyListener(Axis.Y, labelEstimatedStepsY));
        textFieldSettingStepsY = new JTextField("0.0");
        textFieldSettingStepsY.addKeyListener(createKeyListenerChangeSetting());
        buttonUpdateSettingsY.setEnabled(false);
        buttonUpdateSettingsY.addActionListener(createListenerUpdateSetting(Axis.Y, textFieldSettingStepsY));

        buttonUpdateSettingsZ = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        labelEstimatedStepsZ = new JLabel("0 steps/mm");
        labelEstimatedStepsZ.setFont(labelEstimatedFont);
        labelPositionZ = new JLabel("  0.0 mm", JLabel.RIGHT);
        textFieldMeasuredZ = new JTextField("0.0");
        textFieldMeasuredZ.addKeyListener(createKeyListener(Axis.Z, labelEstimatedStepsZ));
        textFieldSettingStepsZ = new JTextField("0.0");
        textFieldSettingStepsZ.addKeyListener(createKeyListenerChangeSetting());
        buttonUpdateSettingsZ.setEnabled(false);
        buttonUpdateSettingsZ.addActionListener(createListenerUpdateSetting(Axis.Z, textFieldSettingStepsZ));
    }

    private ActionListener createListenerUpdateSetting(Axis axis, JTextField textFieldSetting) {
        return event -> {
            try {
                Position previousPosition = getBackend().getWorkPosition();

                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                double value = Double.parseDouble(textFieldSetting.getText());
                firmwareSettings.setStepsPerMillimeter(axis, value);

                // We need to issue soft reset to make the controller use the new settings
                getBackend().issueSoftReset();

                // Restore the previous position
                getBackend().setWorkPosition(PartialPosition.from(previousPosition));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

    private KeyListener createKeyListenerChangeSetting() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {

            }

            @Override
            public void keyPressed(KeyEvent event) {

            }

            @Override
            public void keyReleased(KeyEvent event) {
                checkUpdatedValues();
                checkPulseIntervalLimits();
            }
        };
    }

    private KeyListener createKeyListener(Axis axis, JLabel label) {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {

            }

            @Override
            public void keyPressed(KeyEvent event) {

            }

            @Override
            public void keyReleased(KeyEvent event) {
                JTextField source = (JTextField) event.getSource();
                if (source != null) {
                    updateEstimationFromMesurement(source, axis, label);
                }
            }
        };
    }

    private void updateEstimationFromMesurement(JTextField textFieldMesurement, Axis axis, JLabel label) {
        if (getBackend().getWorkPosition() != null) {
            try {
                DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN, Localization.dfs);
                double measured = decimalFormat.parse(textFieldMesurement.getText()).doubleValue();
                double real = getBackend().getWorkPosition().get(axis);
                double stepsPerMM = getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(axis);

                double computed = (real / measured) * stepsPerMM;
                if (measured == 0 || real == 0) {
                    computed = 0;
                }
                label.setText(decimalFormat.format(Math.abs(computed)) + " steps/mm est.");
            } catch (FirmwareSettingsException | ParseException ignored) {
                // Never mind
            }
        }
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        WizardUtils.killAlarm(getBackend());
        updateMeasurementEstimatesFields();
        updateSettingFieldsFromFirmware();
        checkPulseIntervalLimits();
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
        if (event instanceof ControllerStatusEvent) {
            ThreadHelper.invokeLater(() -> {
                WizardUtils.killAlarm(getBackend());
                Position workPosition = getBackend().getWorkPosition();
                Position machinePosition = getBackend().getMachinePosition();
                updatePosition(workPosition, machinePosition);
            });
        } else if(event instanceof FirmwareSettingEvent || event instanceof ControllerStateEvent) {
            ThreadHelper.invokeLater(() -> {
                checkUpdatedValues();
                updateMeasurementEstimatesFields();
                checkPulseIntervalLimits();
            });
        } else if (event instanceof AlarmEvent && ((AlarmEvent) event).getAlarm() == Alarm.HARD_LIMIT) {
            ThreadHelper.invokeLater(() -> {
                try {
                    getBackend().issueSoftReset();
                } catch (Exception e) {
                    // Never mind
                }
            }, TIME_BEFORE_RESET_ON_ALARM);
        }
    }

    private void updatePosition(Position currentPosition, Position machinePosition) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0", Localization.dfs);
        labelPositionX.setText(StringUtils.leftPad(decimalFormat.format(currentPosition.get(Axis.X)) + " mm", 8, ' '));
        labelPositionY.setText(StringUtils.leftPad(decimalFormat.format(currentPosition.get(Axis.Y)) + " mm", 8, ' '));
        labelPositionZ.setText(StringUtils.leftPad(decimalFormat.format(currentPosition.get(Axis.Z)) + " mm", 8, ' '));
        updateMeasurementEstimatesFields();
        navigationButtons.refresh(machinePosition);
    }

    /**
     * Check if values are updated in the text fields. It will enable the update buttons if it's a new setting.
     */
    private void checkUpdatedValues() {
        if (getBackend().getController() != null && getBackend().getController().getFirmwareSettings() != null) {
            try {
                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();

                for (Axis axis : Axis.values()) {
                    double stepsPerMillimeter = MathUtils.round(firmwareSettings.getStepsPerMillimeter(axis), 3);
                    JTextField textField = getSettingsTextField(axis);
                    JButton buttonUpdateSettings = getUpdateSettingsButton(axis);

                    if (textField == null || buttonUpdateSettings == null) {
                        continue;
                    }

                    buttonUpdateSettings.setEnabled(false);
                    try {
                        DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN, Localization.dfs);
                        double newValue = decimalFormat.parse(textField.getText()).doubleValue();
                        if (stepsPerMillimeter != newValue) {
                            buttonUpdateSettings.setEnabled(true);
                        }
                    } catch (ParseException ignored) {
                        // Never mind
                    }
                }
            } catch (FirmwareSettingsException e) {
                e.printStackTrace();
            }
        }
    }

    private JTextField getSettingsTextField(Axis axis) {
        switch (axis) {
            case X:
                return textFieldSettingStepsX;
            case Y:
                return textFieldSettingStepsY;
            case Z:
                return textFieldSettingStepsZ;
            default:
                return null;
        }
    }

    private JButton getUpdateSettingsButton(Axis axis) {
        switch (axis) {
            case X:
                return buttonUpdateSettingsX;
            case Y:
                return buttonUpdateSettingsY;
            case Z:
                return buttonUpdateSettingsZ;
            default:
                return null;
        }
    }

    /**
     * Calculates if the pulse interval would exceed the max computing speed and will set an warning message.
     * <p>
     * It's calculated as:
     * maxFeedRate / 60 * stepsPerMM
     * <p>
     * Where maxFeedRate is given in mm/minute
     */
    private void checkPulseIntervalLimits() {
        IController controller = getBackend().getController();
        IFirmwareSettings firmwareSettings = controller.getFirmwareSettings();

        int maxComputingSpeed = 0;
        for (Axis axis : Axis.values()) {
            try {
                // Get the maximum feed rate in mm/min
                double maxFeedRate = firmwareSettings.getMaximumRate(axis);

                // Fetch the current steps per mm setting
                JTextField settingsTextField = getSettingsTextField(axis);
                if (settingsTextField == null) {
                    continue;
                }

                double newStepsPerMMSetting = parseDouble(settingsTextField.getText());
                double currentStepsPerMMSetting = firmwareSettings.getStepsPerMillimeter(axis);
                double stepsPerMMSetting = Math.max(newStepsPerMMSetting, currentStepsPerMMSetting);

                // Calculate the currently needed processing speed
                double computingSpeed = (maxFeedRate / 60.0) * stepsPerMMSetting;
                maxComputingSpeed = (int) Math.max(computingSpeed, maxComputingSpeed);
            } catch (FirmwareSettingsException ignored) {
                // Never mind
            }
        }

        if (maxComputingSpeed > 30000) {
            setErrorMessage(" " + String.format(Localization.getString("platform.plugin.setupwizard.calibration.computer-speed-warning"), (maxComputingSpeed / 1000) + "kHz"));
        } else {
            setErrorMessage("");
        }
    }

    private double parseDouble(String text) {
        try {
            DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN, Localization.dfs);
            return decimalFormat.parse(text).doubleValue();
        } catch (ParseException ignored) {
            // Never mind
        }

        return 0;
    }

    private void updateMeasurementEstimatesFields() {
        if (getBackend().getController() != null && getBackend().getController().getFirmwareSettings() != null) {
            updateEstimationFromMesurement(textFieldMeasuredX, Axis.X, labelEstimatedStepsX);
            updateEstimationFromMesurement(textFieldMeasuredY, Axis.Y, labelEstimatedStepsY);
            updateEstimationFromMesurement(textFieldMeasuredZ, Axis.Z, labelEstimatedStepsZ);
        }
    }

    private void updateSettingFieldsFromFirmware() {
        if (getBackend().getController() != null && getBackend().getController().getFirmwareSettings() != null) {
            try {
                DecimalFormat decimalFormat = new DecimalFormat(DECIMAL_FORMAT_PATTERN, Localization.dfs);
                textFieldSettingStepsX.setText(decimalFormat.format(getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(Axis.X)));
                textFieldSettingStepsY.setText(decimalFormat.format(getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(Axis.Y)));
                textFieldSettingStepsZ.setText(decimalFormat.format(getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(Axis.Z)));
            } catch (FirmwareSettingsException e) {
                e.printStackTrace();
            }
        }
    }
}
