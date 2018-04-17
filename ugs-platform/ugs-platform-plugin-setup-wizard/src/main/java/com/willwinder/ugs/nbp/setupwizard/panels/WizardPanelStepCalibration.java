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
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
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
public class WizardPanelStepCalibration extends AbstractWizardPanel implements UGSEventListener {

    private final DecimalFormat decimalFormat;

    private final JLabel labelEstimatedStepsX;
    private final JLabel labelPositionX;
    private final JButton buttonXneg;
    private final JButton buttonXpos;
    private final JButton buttonUpdateSettingsX;
    private final JTextField textFieldMeasuredX;
    private final JTextField textFieldSettingStepsX;
    private final JButton buttonYneg;
    private final JButton buttonYpos;
    private final JButton buttonUpdateSettingsY;
    private final JTextField textFieldMeasuredY;
    private final JLabel labelEstimatedStepsY;
    private final JTextField textFieldSettingStepsY;
    private final JLabel labelPositionY;
    private final JButton buttonZneg;
    private final JButton buttonZpos;
    private final JButton buttonUpdateSettingsZ;
    private final JTextField textFieldMeasuredZ;
    private final JLabel labelEstimatedStepsZ;
    private final JTextField textFieldSettingStepsZ;
    private final JLabel labelPositionZ;
    private Timer updateTimer;

    public WizardPanelStepCalibration(BackendAPI backend) {
        super(backend, "Step calibration");

        decimalFormat = new DecimalFormat("0.0", Localization.dfs);
        Font labelEstimatedFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Font labelHeaderFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);

        JLabel description = new JLabel("<html><body>" +
                "<p>We will now attempt to calibrate your machine. Try <b>moving</b> the machine and <b>measure</b> the results, then <b>calibrate</b> to the estimated steps.</p>" +
                "</body></html>");
        add(description);


        JPanel panel = new JPanel(new MigLayout("fill, inset 0"));
        JLabel headerLabel = new JLabel("Move", JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 3, gapbottom 5, gaptop 7");
        panel.add(new JSeparator(SwingConstants.VERTICAL), "spany 5, gapleft 5, gapright 5, wmin 10, grow");

        headerLabel = new JLabel("Measure", JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 2, gapbottom 5, gaptop 7");
        panel.add(new JSeparator(SwingConstants.VERTICAL), "spany 5, gapleft 5, gapright 5, wmin 10, grow");

        headerLabel = new JLabel("Calibrate", JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 3, wrap, gapbottom 5, gaptop 7");

        JButton resetButton = new JButton("Reset to zero");
        resetButton.setMinimumSize(new Dimension(36, 36));
        resetButton.addActionListener(event -> {
            try {
                getBackend().resetCoordinatesToZero();
            } catch (Exception ignored) {
                // Never mind
            }
        });
        panel.add(resetButton, "grow, spanx 3, gapbottom 0, gaptop 0");
        panel.add(new JLabel("Actual movement:"), "span 2, grow");
        panel.add(new JLabel("Adjust steps per millimeter:"), "spanx 5, grow, wrap");

        buttonXneg = createJogButton("X-");
        buttonXpos = createJogButton("X+");
        buttonUpdateSettingsX = new JButton("Update");
        textFieldMeasuredX = new JTextField("0");
        labelEstimatedStepsX = new JLabel("0 steps/mm");
        labelEstimatedStepsX.setFont(labelEstimatedFont);
        textFieldSettingStepsX = new JTextField("0");
        labelPositionX = new JLabel("0 mm");

        buttonUpdateSettingsX.setEnabled(false);
        buttonXneg.addActionListener(event -> moveMachine(-1, 0, 0));
        buttonXpos.addActionListener(event -> moveMachine(1, 0, 0));
        textFieldMeasuredX.addKeyListener(createKeyListener(Axis.X, labelEstimatedStepsX));
        textFieldSettingStepsX.addKeyListener(createKeyListenerChangeSetting(Axis.X, buttonUpdateSettingsX));

        panel.add(buttonXneg, "grow, gapbottom 5");
        panel.add(labelPositionX, "shrink, gapbottom 5");
        panel.add(buttonXpos, "grow, gapbottom 5");

        JPanel panelMeasureX = new JPanel(new MigLayout("fill, inset 0"));
        panelMeasureX.add(textFieldMeasuredX, "growx, wmin 50");
        panelMeasureX.add(new JLabel("mm"), "wrap");
        panel.add(panelMeasureX, "grow, span 2, gapbottom 5");

        JPanel estimationPanelX = new JPanel(new MigLayout("fill, inset 0"));
        estimationPanelX.add(labelEstimatedStepsX, "gapleft 5, span 2, wrap");
        estimationPanelX.add(textFieldSettingStepsX, "growx, wmin 50");
        estimationPanelX.add(buttonUpdateSettingsX, "growx");
        panel.add(estimationPanelX, "grow, spanx 3, wrap, gapbottom 5");


        buttonYneg = createJogButton("Y-");
        buttonYpos = createJogButton("Y+");
        buttonUpdateSettingsY = new JButton("Update");
        textFieldMeasuredY = new JTextField("0");
        labelEstimatedStepsY = new JLabel("Setting (Steps / MM)");
        labelEstimatedStepsY.setFont(labelEstimatedFont);
        textFieldSettingStepsY = new JTextField("0");
        labelPositionY = new JLabel("0 mm");

        buttonUpdateSettingsY.setEnabled(false);
        buttonYneg.addActionListener(event -> moveMachine(0, -1, 0));
        buttonYpos.addActionListener(event -> moveMachine(0, 1, 0));
        textFieldMeasuredY.addKeyListener(createKeyListener(Axis.Y, labelEstimatedStepsY));
        textFieldSettingStepsY.addKeyListener(createKeyListenerChangeSetting(Axis.Y, buttonUpdateSettingsY));

        panel.add(buttonYneg, "grow, gapbottom 5");
        panel.add(labelPositionY, "shrink, gapbottom 5");
        panel.add(buttonYpos, "grow, gapbottom 5");

        JPanel panelMeasureY = new JPanel(new MigLayout("fill, inset 0"));
        panelMeasureY.add(textFieldMeasuredY, "growx, wmin 50");
        panelMeasureY.add(new JLabel("mm"), "wrap");
        panel.add(panelMeasureY, "grow, span 2, gapbottom 5");

        JPanel estimationPanelY = new JPanel(new MigLayout("fill, inset 0"));
        estimationPanelY.add(labelEstimatedStepsY, "gapleft 5, span 2, wrap");
        estimationPanelY.add(textFieldSettingStepsY, "growx, wmin 50");
        estimationPanelY.add(buttonUpdateSettingsY, "growx");
        panel.add(estimationPanelY, "grow, spanx 3, wrap, gapbottom 5");

        buttonZneg = createJogButton("Z-");
        buttonZpos = createJogButton("Z+");
        buttonUpdateSettingsZ = new JButton("Update");
        textFieldMeasuredZ = new JTextField("0");
        labelEstimatedStepsZ = new JLabel("0 steps/mm");
        labelEstimatedStepsZ.setFont(labelEstimatedFont);
        textFieldSettingStepsZ = new JTextField("0");
        labelPositionZ = new JLabel("0 mm");

        buttonUpdateSettingsZ.setEnabled(false);
        buttonZneg.addActionListener(event -> moveMachine(0, 0, -1));
        buttonZpos.addActionListener(event -> moveMachine(0, 0, 1));
        textFieldMeasuredZ.addKeyListener(createKeyListener(Axis.Z, labelEstimatedStepsZ));
        textFieldSettingStepsZ.addKeyListener(createKeyListenerChangeSetting(Axis.Z, buttonUpdateSettingsZ));

        panel.add(buttonZneg, "grow, gapbottom 5");
        panel.add(labelPositionZ, "shrink, gapbottom 5");
        panel.add(buttonZpos, "grow, gapbottom 5");

        JPanel panelMeasureZ = new JPanel(new MigLayout("fill, inset 0"));
        panelMeasureZ.add(textFieldMeasuredZ, "growx, wmin 50");
        panelMeasureZ.add(new JLabel("mm"), "wrap");
        panel.add(panelMeasureZ, "grow, span 2, gapbottom 5");

        JPanel estimationPanelZ = new JPanel(new MigLayout("fill, inset 0"));
        estimationPanelZ.add(labelEstimatedStepsZ, "gapleft 5, span 2, wrap");
        estimationPanelZ.add(textFieldSettingStepsZ, "growx, wmin 50");
        estimationPanelZ.add(buttonUpdateSettingsZ, "growx");
        panel.add(estimationPanelZ, "grow, spanx 3, wrap, gapbottom 5");

        getPanel().add(panel, "grow");
        setValid(true);
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
                updateEstimationFromMesurement(source, axis, label);
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
            } catch (FirmwareSettingsException e) {
                e.printStackTrace();
            } catch (ParseException e) {
            }
        }
    }

    private JButton createJogButton(String text) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setMinimumSize(new Dimension(36, 36));
        return button;
    }

    private void moveMachine(int x, int y, int z) {
        try {
            getBackend().getController().jogMachine(x, y, z, 1, 100, UnitUtils.Units.MM);
        } catch (Exception e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unexpected error while moving the machine: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        killAlarm();
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
                    labelPositionX.setText(decimalFormat.format(currentPosition.get(Axis.X)) + " mm");
                    labelPositionY.setText(decimalFormat.format(currentPosition.get(Axis.Y)) + " mm");
                    labelPositionZ.setText(decimalFormat.format(currentPosition.get(Axis.Z)) + " mm");
                    updateMeasurementEstimatesFields();
                }
                killAlarm();
            }
        }, 0, 200);
    }

    @Override
    public void destroy() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        getBackend().removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (getBackend().getController() != null &&
                getBackend().isConnected() &&
                (event.isControllerStatusEvent() || event.isStateChangeEvent())) {
            killAlarm();
        }

        if (event.isSettingChangeEvent() || event.isStateChangeEvent()) {
            ThreadHelper.invokeLater(() -> {
                updateMeasurementEstimatesFields();
                updateSettingFieldsFromFirmware();
            });
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
