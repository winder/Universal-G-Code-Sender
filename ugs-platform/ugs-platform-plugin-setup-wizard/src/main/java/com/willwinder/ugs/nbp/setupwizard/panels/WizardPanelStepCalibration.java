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
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A wizard step panel for configuring step length on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelStepCalibration extends AbstractWizardPanel implements UGSEventListener, ControllerStateListener {

    private static final long TIME_BEFORE_RESET_ON_ALARM = 500;
    private final DecimalFormat decimalFormat;
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
    private Timer updateTimer;

    public WizardPanelStepCalibration(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.calibration.title"));
        decimalFormat = new DecimalFormat("0.0", Localization.dfs);

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
        navigationButtons = new NavigationButtons(getBackend(), 1.0, (int)getBackend().getSettings().getJogFeedRate());

        buttonUpdateSettingsX = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        buttonUpdateSettingsX.setEnabled(false);
        labelEstimatedStepsX = new JLabel("0 steps/mm");
        labelEstimatedStepsX.setFont(labelEstimatedFont);
        labelPositionX = new JLabel("  0.0 mm", JLabel.RIGHT);
        textFieldMeasuredX = new JTextField("0");
        textFieldMeasuredX.addKeyListener(createKeyListener(Axis.X, labelEstimatedStepsX));
        textFieldSettingStepsX = new JTextField("0");
        textFieldSettingStepsX.addKeyListener(createKeyListenerChangeSetting(Axis.X, buttonUpdateSettingsX));

        buttonUpdateSettingsY = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        buttonUpdateSettingsY.setEnabled(false);
        labelEstimatedStepsY = new JLabel(Localization.getString("platform.plugin.setupwizard.calibration.setting"));
        labelEstimatedStepsY.setFont(labelEstimatedFont);
        labelPositionY = new JLabel("  0.0 mm", JLabel.RIGHT);
        textFieldMeasuredY = new JTextField("0");
        textFieldMeasuredY.addKeyListener(createKeyListener(Axis.Y, labelEstimatedStepsY));
        textFieldSettingStepsY = new JTextField("0");
        textFieldSettingStepsY.addKeyListener(createKeyListenerChangeSetting(Axis.Y, buttonUpdateSettingsY));

        buttonUpdateSettingsZ = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        buttonUpdateSettingsZ.setEnabled(false);
        labelEstimatedStepsZ = new JLabel("0 steps/mm");
        labelEstimatedStepsZ.setFont(labelEstimatedFont);
        labelPositionZ = new JLabel("  0.0 mm", JLabel.RIGHT);
        textFieldMeasuredZ = new JTextField("0");
        textFieldMeasuredZ.addKeyListener(createKeyListener(Axis.Z, labelEstimatedStepsZ));
        textFieldSettingStepsZ = new JTextField("0");
        textFieldSettingStepsZ.addKeyListener(createKeyListenerChangeSetting(Axis.Z, buttonUpdateSettingsZ));
    }

    private KeyListener createKeyListenerChangeSetting(Axis axis, JButton buttonUpdateSettings) {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {

            }

            @Override
            public void keyPressed(KeyEvent event) {

            }

            @Override
            public void keyReleased(KeyEvent event) {
                if (getBackend().getController() != null && getBackend().getController().getFirmwareSettings() != null) {
                    try {
                        JTextField source = (JTextField) event.getSource();
                        IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();

                        int stepsPerMillimeter = firmwareSettings.getStepsPerMillimeter(axis);
                        if (!StringUtils.isNumeric(source.getText()) ||
                                source.getText().trim().equalsIgnoreCase(String.valueOf(stepsPerMillimeter))) {
                            buttonUpdateSettings.setEnabled(false);
                        } else if (StringUtils.isNumeric(source.getText())) {
                            buttonUpdateSettings.setEnabled(true);
                        }
                    } catch (FirmwareSettingsException e) {
                        e.printStackTrace();
                    }
                }
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
                double measured = decimalFormat.parse(textFieldMesurement.getText()).doubleValue();
                double real = getBackend().getWorkPosition().get(axis);
                int stepsPerMM = getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(axis);

                double computed = (real / measured) * ((double) stepsPerMM);
                if (measured == 0 || real == 0) {
                    computed = 0;
                }
                label.setText(Math.abs(Math.round(computed)) + " steps/mm est.");
            } catch (FirmwareSettingsException | ParseException ignored) {
                // Never mind
            }
        }
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        getBackend().addControllerStateListener(this);
        WizardUtils.killAlarm(getBackend());
        updateMeasurementEstimatesFields();
        updateSettingFieldsFromFirmware();

        if (updateTimer != null) {
            updateTimer.cancel();
        }

        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Position currentPosition = getBackend().getWorkPosition();
                if (currentPosition != null) {
                    labelPositionX.setText(StringUtils.leftPad(decimalFormat.format(currentPosition.get(Axis.X)) + " mm", 8, ' '));
                    labelPositionY.setText(StringUtils.leftPad(decimalFormat.format(currentPosition.get(Axis.Y)) + " mm", 8, ' '));
                    labelPositionZ.setText(StringUtils.leftPad(decimalFormat.format(currentPosition.get(Axis.Z)) + " mm", 8, ' '));
                    updateMeasurementEstimatesFields();
                }
                WizardUtils.killAlarm(getBackend());
            }
        }, 0, 200);
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport();
    }

    @Override
    public void destroy() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        getBackend().removeUGSEventListener(this);
        getBackend().removeControllerStateListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (getBackend().getController() != null &&
                getBackend().isConnected() &&
                (event.isControllerStatusEvent() || event.isStateChangeEvent())) {
            WizardUtils.killAlarm(getBackend());
        } else if (event.isSettingChangeEvent() || event.isStateChangeEvent()) {
            ThreadHelper.invokeLater(() -> {
                updateMeasurementEstimatesFields();
                updateSettingFieldsFromFirmware();
            });
        } else if (event.getEventType() == UGSEvent.EventType.ALARM_EVENT && event.getAlarm() == Alarm.HARD_LIMIT) {
            ThreadHelper.invokeLater(() -> {
                try {
                    getBackend().issueSoftReset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, TIME_BEFORE_RESET_ON_ALARM);
        }

        if (event.isControllerStatusEvent()) {
            navigationButtons.refresh(event.getControllerStatus().getMachineCoord());
        }
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
                textFieldSettingStepsX.setText(String.valueOf(getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(Axis.X)));
                textFieldSettingStepsY.setText(String.valueOf(getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(Axis.Y)));
                textFieldSettingStepsZ.setText(String.valueOf(getBackend().getController().getFirmwareSettings().getStepsPerMillimeter(Axis.Z)));
            } catch (FirmwareSettingsException e) {
                e.printStackTrace();
            }
        }
    }
}
