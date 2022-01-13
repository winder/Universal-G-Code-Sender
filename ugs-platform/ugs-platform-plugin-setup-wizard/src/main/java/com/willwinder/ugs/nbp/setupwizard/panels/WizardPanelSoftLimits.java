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
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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

/**
 * A wizard step panel for configuring soft limits on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelSoftLimits extends AbstractWizardPanel implements UGSEventListener {

    private NavigationButtons navigationButtons;

    private final DecimalFormat decimalFormat;
    private final DecimalFormat positionDecimalFormat;

    private JCheckBox checkboxEnableSoftLimits;
    private JLabel labelSoftLimitsNotSupported;
    private JLabel labelHomingIsNotEnabled;
    private JLabel labelDescription;
    private JLabel labelInstructions;

    private JTextField textFieldSoftLimitX;
    private JTextField textFieldSoftLimitY;
    private JTextField textFieldSoftLimitZ;

    private JLabel labelPositionX;
    private JButton buttonUpdateSettingsX;

    private JLabel labelPositionY;
    private JButton buttonUpdateSettingsY;

    private JLabel labelPositionZ;
    private JButton buttonUpdateSettingsZ;

    private JButton homeButton;
    private JComboBox<String> lengthComboBox;
    private JPanel softLimitPanel;

    public WizardPanelSoftLimits(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.soft-limits.title"));
        decimalFormat = new DecimalFormat("0.0##", Localization.dfs);
        positionDecimalFormat = new DecimalFormat("0.0", Localization.dfs);

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "gapbottom 5, spanx, wrap");
        panel.add(checkboxEnableSoftLimits, "gapbottom 5, spanx, wrap");
        panel.add(labelInstructions, "gapbottom 5, spanx, wrap");
        panel.add(labelSoftLimitsNotSupported, "spanx, wrap");
        panel.add(labelHomingIsNotEnabled, "spanx, wrap");

        softLimitPanel = new JPanel(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));


        addHeaderRow(softLimitPanel);

        softLimitPanel.add(new JLabel("Home your machine"));
        softLimitPanel.add(lengthComboBox, "grow, spanx 3");
        softLimitPanel.add(new JLabel("Update the max travel distance"), "grow, spanx");


        addAxisRow(softLimitPanel, homeButton, navigationButtons.getButtonXneg(), navigationButtons.getButtonXpos(), labelPositionX, textFieldSoftLimitX, buttonUpdateSettingsX);
        addAxisRow(softLimitPanel, new JLabel(), navigationButtons.getButtonYneg(), navigationButtons.getButtonYpos(), labelPositionY, textFieldSoftLimitY, buttonUpdateSettingsY);
        addAxisRow(softLimitPanel, new JLabel(), navigationButtons.getButtonZneg(), navigationButtons.getButtonZpos(), labelPositionZ, textFieldSoftLimitZ, buttonUpdateSettingsZ);

        panel.add(softLimitPanel, "grow");
        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void addAxisRow(JPanel panel,
                            JComponent firstColumn,
                            JButton buttonMoveInNegativeDirection,
                            JButton buttonMoveInPositiveDirection,
                            JLabel labelCurrentPosition,
                            JTextField textFieldSoftLimit,
                            JButton buttonUpdateSettings) {
        panel.add(firstColumn, "growx");

        panel.add(buttonMoveInNegativeDirection, "shrink, gapbottom 5");
        panel.add(labelCurrentPosition, "grow, gapbottom 5");
        panel.add(buttonMoveInPositiveDirection, "shrink, ax right, gapbottom 5");

        panel.add(textFieldSoftLimit, "wmin 80, gapbottom 5");
        panel.add(buttonUpdateSettings, "wrap, gapbottom 5");
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.soft-limits.intro") +
                "</p></body></html>");

        labelInstructions = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.soft-limits.instructions") +
                "</p></body></html>");

        navigationButtons = new NavigationButtons(getBackend(), 1.0, (int) getBackend().getSettings().getJogFeedRate());

        checkboxEnableSoftLimits = new JCheckBox("Enable soft limits");
        checkboxEnableSoftLimits.addActionListener(event -> onSoftLimitsClicked());

        labelHomingIsNotEnabled = new JLabel(Localization.getString("platform.plugin.setupwizard.soft-limits.require-homing"), ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelSoftLimitsNotSupported = new JLabel(Localization.getString("platform.plugin.setupwizard.soft-limits.not-available"), ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);

        homeButton = new JButton("Home");
        homeButton.setMinimumSize(new Dimension(40, 36));
        homeButton.addActionListener(event -> {
            try {
                getBackend().performHomingCycle();
            } catch (Exception ignored) {
                // Never mind
            }
        });

        buttonUpdateSettingsX = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        buttonUpdateSettingsX.setEnabled(false);
        buttonUpdateSettingsX.addActionListener(event -> onSave(Axis.X));

        labelPositionX = new JLabel("  0.0 mm");

        textFieldSoftLimitX = new JTextField("0.00");
        textFieldSoftLimitX.addKeyListener(createKeyListenerChangeSetting(Axis.X, buttonUpdateSettingsX));

        buttonUpdateSettingsY = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        buttonUpdateSettingsY.setEnabled(false);
        buttonUpdateSettingsY.addActionListener(event -> onSave(Axis.Y));

        labelPositionY = new JLabel("  0.0 mm");

        textFieldSoftLimitY = new JTextField("0.00");
        textFieldSoftLimitY.addKeyListener(createKeyListenerChangeSetting(Axis.Y, buttonUpdateSettingsY));

        buttonUpdateSettingsZ = new JButton(Localization.getString("platform.plugin.setupwizard.update"));
        buttonUpdateSettingsZ.setEnabled(false);
        buttonUpdateSettingsZ.addActionListener(event -> onSave(Axis.Z));
        labelPositionZ = new JLabel("  0.0 mm");

        textFieldSoftLimitZ = new JTextField("0.00");
        textFieldSoftLimitZ.addKeyListener(createKeyListenerChangeSetting(Axis.Z, buttonUpdateSettingsZ));

        lengthComboBox = new JComboBox<>();
        lengthComboBox.addItem("Step 0.1 mm");
        lengthComboBox.addItem("Step 1.0 mm");
        lengthComboBox.addItem("Step 10.0 mm");
        lengthComboBox.addItem("Step 100.0 mm");
        lengthComboBox.addItemListener(itemEvent -> {
            if (lengthComboBox.getSelectedIndex() == 0) {
                navigationButtons.setStepSize(0.1);
            } else if (lengthComboBox.getSelectedIndex() == 1) {
                navigationButtons.setStepSize(1.0);
            } else if (lengthComboBox.getSelectedIndex() == 2) {
                navigationButtons.setStepSize(10.0);
            } else if (lengthComboBox.getSelectedIndex() == 3) {
                navigationButtons.setStepSize(100.0);
            }
        });
        lengthComboBox.setSelectedIndex(2);
    }

    private void onSoftLimitsClicked() {
        try {
            getBackend().getController().getFirmwareSettings().setSoftLimitsEnabled(checkboxEnableSoftLimits.isSelected());
        } catch (FirmwareSettingsException e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't enable/disable the soft limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private void onSave(Axis axis) {
        if (getBackend().getController() != null) {
            try {
                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                switch (axis) {
                    case X:
                        double limitX = Math.abs(decimalFormat.parse(textFieldSoftLimitX.getText()).doubleValue());
                        firmwareSettings.setSoftLimit(axis, limitX);
                        buttonUpdateSettingsX.setEnabled(false);
                        break;

                    case Y:
                        double limitY = Math.abs(decimalFormat.parse(textFieldSoftLimitY.getText()).doubleValue());
                        firmwareSettings.setSoftLimit(axis, limitY);
                        buttonUpdateSettingsY.setEnabled(false);
                        break;

                    case Z:
                        double limitZ = Math.abs(decimalFormat.parse(textFieldSoftLimitZ.getText()).doubleValue());
                        firmwareSettings.setSoftLimit(axis, limitZ);
                        buttonUpdateSettingsZ.setEnabled(false);
                        break;

                    default:
                }
            } catch (ParseException | FirmwareSettingsException ignored) {
                // Never mind
            }
        }
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refeshControls();

        try {
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            textFieldSoftLimitX.setText(decimalFormat.format(firmwareSettings.getSoftLimit(Axis.X)));
            textFieldSoftLimitY.setText(decimalFormat.format(firmwareSettings.getSoftLimit(Axis.Y)));
            textFieldSoftLimitZ.setText(decimalFormat.format(firmwareSettings.getSoftLimit(Axis.Z)));
        } catch (FirmwareSettingsException e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't fetch firmware settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private void refeshControls() {
        ThreadHelper.invokeLater(() -> {
            try {
                checkboxEnableSoftLimits.setVisible(false);
                if (getBackend().getController() != null &&
                        getBackend().getController().getFirmwareSettings().isHardLimitsEnabled() &&
                        getBackend().getController().getFirmwareSettings().isHomingEnabled() &&
                        getBackend().getController().getCapabilities().hasSoftLimits()) {
                    IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                    try {
                        checkboxEnableSoftLimits.setSelected(firmwareSettings.isSoftLimitsEnabled());
                        boolean visible = firmwareSettings.isSoftLimitsEnabled();
                        setFormVisible(visible);
                    } catch (FirmwareSettingsException ignored) {
                        // Never mind..
                    }

                    checkboxEnableSoftLimits.setVisible(true);
                    labelSoftLimitsNotSupported.setVisible(false);
                    labelHomingIsNotEnabled.setVisible(false);
                } else if (getBackend().getController() != null &&
                        getBackend().getController().getCapabilities().hasSoftLimits() &&
                        (!getBackend().getController().getFirmwareSettings().isHomingEnabled() ||
                                !getBackend().getController().getFirmwareSettings().isHardLimitsEnabled())) {
                    setFormVisible(false);
                    labelHomingIsNotEnabled.setVisible(true);
                } else {
                    setFormVisible(false);
                    labelSoftLimitsNotSupported.setVisible(true);
                }

                // Redraw the panel to make sure that previously hidden fields are shown
                getPanel().revalidate();
            } catch (FirmwareSettingsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't fetch firmware settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }

        }, 100);
    }

    private void setFormVisible(boolean visible) {
        labelInstructions.setVisible(visible);
        softLimitPanel.setVisible(visible);
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

        if (getBackend().getController() != null && getBackend().isConnected() && event instanceof ControllerStatusEvent) {
            WizardUtils.killAlarm(getBackend());
            Position machineCoord = ((ControllerStatusEvent) event).getStatus().getMachineCoord();
            labelPositionX.setText(positionDecimalFormat.format(machineCoord.get(Axis.X)) + " mm");
            labelPositionY.setText(positionDecimalFormat.format(machineCoord.get(Axis.Y)) + " mm");
            labelPositionZ.setText(positionDecimalFormat.format(machineCoord.get(Axis.Z)) + " mm");
        } else if (event instanceof FirmwareSettingEvent) {
            refeshControls();
        }

        if (event instanceof ControllerStatusEvent) {
            navigationButtons.refresh(((ControllerStatusEvent) event).getStatus().getMachineCoord());
        }
    }

    private void addHeaderRow(JPanel panel) {
        Font labelHeaderFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
        JLabel headerLabel = new JLabel(Localization.getString("platform.plugin.setupwizard.home"), JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, gapbottom 5, gaptop 7");
        panel.add(new JSeparator(SwingConstants.VERTICAL), "spany 5, gapleft 5, gapright 5, wmin 10, grow");

        headerLabel = new JLabel(Localization.getString("platform.plugin.setupwizard.move"), JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 3, gapbottom 5, gaptop 7");
        panel.add(new JSeparator(SwingConstants.VERTICAL), "spany 5, gapleft 5, gapright 5, wmin 10, grow");

        headerLabel = new JLabel(Localization.getString("platform.plugin.setupwizard.update-settings"), JLabel.CENTER);
        headerLabel.setFont(labelHeaderFont);
        panel.add(headerLabel, "growx, spanx 2, wrap, gapbottom 5, gaptop 7");
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
                try {
                    if (getBackend().getController() != null && getBackend().getController().getFirmwareSettings() != null) {
                        try {
                            JTextField source = (JTextField) event.getSource();
                            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();

                            double softLimit = firmwareSettings.getSoftLimit(axis);
                            if (Math.abs(decimalFormat.parse(source.getText().trim()).doubleValue()) == Math.abs(softLimit)) {
                                buttonUpdateSettings.setEnabled(false);
                            } else {
                                buttonUpdateSettings.setEnabled(true);
                            }
                        } catch (FirmwareSettingsException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (ParseException ignored) {
                    buttonUpdateSettings.setEnabled(false);
                }
            }
        };
    }
}
