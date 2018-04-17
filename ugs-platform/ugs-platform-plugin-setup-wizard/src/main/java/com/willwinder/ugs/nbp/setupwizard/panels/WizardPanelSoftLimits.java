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
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * A wizard step panel for configuring soft limits on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelSoftLimits extends AbstractWizardPanel implements UGSEventListener, KeyListener {

    private final DecimalFormat decimalFormat;

    private JCheckBox checkboxEnableSoftLimits;
    private JLabel labelSoftLimitsNotSupported;
    private JLabel labelHomingIsNotEnabled;
    private JLabel labelDescription;
    private JTextField textFieldSoftLimitX;
    private JTextField textFieldSoftLimitY;
    private JTextField textFieldSoftLimitZ;
    private JLabel labelSoftLimitX;
    private JLabel labelSoftLimitY;
    private JLabel labelSoftLimitZ;
    private JButton buttonSave;


    public WizardPanelSoftLimits(BackendAPI backend) {
        super(backend, "Soft limits");
        decimalFormat = new DecimalFormat("0.0", Localization.dfs);

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "gapbottom 10");
        panel.add(checkboxEnableSoftLimits, "gapbottom 15");
        panel.add(labelSoftLimitsNotSupported);
        panel.add(labelHomingIsNotEnabled);

        panel.add(labelSoftLimitX, "gapleft 10");
        panel.add(textFieldSoftLimitX, "w 60, gapleft 10, gapbottom 10");

        panel.add(labelSoftLimitY, "gapleft 10");
        panel.add(textFieldSoftLimitY, "w 60, gapleft 10, gapbottom 10");

        panel.add(labelSoftLimitZ, "gapleft 10");
        panel.add(textFieldSoftLimitZ, "w 60, gapleft 10, gapbottom 10");
        panel.add(buttonSave);
        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body>" +
                "<p>Soft limits will prevent the machine to move beyond it's safe work area.</p>" +
                "</body></html>");

        checkboxEnableSoftLimits = new JCheckBox("Enable soft limits");
        checkboxEnableSoftLimits.addActionListener(event -> onSoftLimitsClicked());

        labelHomingIsNotEnabled = new JLabel("Homing needs to be enabled before enabling soft limits.", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHomingIsNotEnabled.setVisible(false);

        labelSoftLimitsNotSupported = new JLabel("Soft limits is not available on your hardware", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelSoftLimitsNotSupported.setVisible(false);

        labelSoftLimitX = new JLabel("X max travel");
        labelSoftLimitX.setVisible(false);
        textFieldSoftLimitX = new JTextField("0");
        textFieldSoftLimitX.setVisible(false);
        textFieldSoftLimitX.addKeyListener(this);

        labelSoftLimitY = new JLabel("Y max travel");
        labelSoftLimitY.setVisible(false);
        textFieldSoftLimitY = new JTextField("0");
        textFieldSoftLimitY.setVisible(false);
        textFieldSoftLimitY.addKeyListener(this);

        labelSoftLimitZ = new JLabel("Z max travel");
        labelSoftLimitZ.setVisible(false);
        textFieldSoftLimitZ = new JTextField("0");
        textFieldSoftLimitZ.setVisible(false);
        textFieldSoftLimitZ.addKeyListener(this);

        buttonSave = new JButton("Save");
        buttonSave.setEnabled(false);
        buttonSave.addActionListener(event -> onSave());
    }

    private void onSoftLimitsClicked() {
        try {
            getBackend().getController().getFirmwareSettings().setSoftLimitsEnabled(checkboxEnableSoftLimits.isSelected());
        } catch (FirmwareSettingsException e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't enable/disable the soft limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    private void onSave() {
        if (getBackend().getController() != null) {
            try {
                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                double limitX = decimalFormat.parse(textFieldSoftLimitX.getText()).doubleValue();
                double limitY = decimalFormat.parse(textFieldSoftLimitY.getText()).doubleValue();
                double limitZ = decimalFormat.parse(textFieldSoftLimitZ.getText()).doubleValue();

                firmwareSettings.setSoftLimitX(limitX);
                firmwareSettings.setSoftLimitY(limitY);
                firmwareSettings.setSoftLimitZ(limitZ);
                updateSaveButton();
            } catch (ParseException | FirmwareSettingsException ignored) {
                // Never mind
            }
        }
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refeshControls();
        updateSaveButton();
    }

    private void refeshControls() {
        ThreadHelper.invokeLater(() -> {
            if (getBackend().getController() != null &&
                    getBackend().getController().getFirmwareSettings().isHardLimitsEnabled() &&
                    getBackend().getController().getFirmwareSettings().isHomingEnabled() &&
                    getBackend().getController().getCapabilities().hasSoftLimits()) {
                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                try {
                    checkboxEnableSoftLimits.setSelected(firmwareSettings.isSoftLimitsEnabled());
                    textFieldSoftLimitX.setText(decimalFormat.format(firmwareSettings.getSoftLimitX()));
                    textFieldSoftLimitY.setText(decimalFormat.format(firmwareSettings.getSoftLimitY()));
                    textFieldSoftLimitZ.setText(decimalFormat.format(firmwareSettings.getSoftLimitZ()));

                    labelSoftLimitX.setVisible(firmwareSettings.isSoftLimitsEnabled());
                    labelSoftLimitY.setVisible(firmwareSettings.isSoftLimitsEnabled());
                    labelSoftLimitZ.setVisible(firmwareSettings.isSoftLimitsEnabled());
                    textFieldSoftLimitX.setVisible(firmwareSettings.isSoftLimitsEnabled());
                    textFieldSoftLimitY.setVisible(firmwareSettings.isSoftLimitsEnabled());
                    textFieldSoftLimitZ.setVisible(firmwareSettings.isSoftLimitsEnabled());
                    buttonSave.setVisible(firmwareSettings.isSoftLimitsEnabled());
                } catch (FirmwareSettingsException ignored) {
                    // Never mind..
                }

                checkboxEnableSoftLimits.setVisible(true);
                labelSoftLimitsNotSupported.setVisible(false);
                labelHomingIsNotEnabled.setVisible(false);

                updateSaveButton();
            } else if (getBackend().getController() != null &&
                    getBackend().getController().getCapabilities().hasSoftLimits() &&
                    (!getBackend().getController().getFirmwareSettings().isHomingEnabled() ||
                    !getBackend().getController().getFirmwareSettings().isHardLimitsEnabled())) {
                checkboxEnableSoftLimits.setVisible(false);
                labelSoftLimitsNotSupported.setVisible(false);
                labelHomingIsNotEnabled.setVisible(true);
                labelSoftLimitX.setVisible(false);
                labelSoftLimitY.setVisible(false);
                labelSoftLimitZ.setVisible(false);
                textFieldSoftLimitX.setVisible(false);
                textFieldSoftLimitY.setVisible(false);
                textFieldSoftLimitZ.setVisible(false);
                buttonSave.setVisible(false);
            } else {
                checkboxEnableSoftLimits.setVisible(false);
                labelSoftLimitsNotSupported.setVisible(true);
                labelHomingIsNotEnabled.setVisible(false);
                labelSoftLimitX.setVisible(false);
                labelSoftLimitY.setVisible(false);
                labelSoftLimitZ.setVisible(false);
                textFieldSoftLimitX.setVisible(false);
                textFieldSoftLimitY.setVisible(false);
                textFieldSoftLimitZ.setVisible(false);
                buttonSave.setVisible(false);
            }
        });
    }

    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.getEventType() == UGSEvent.EventType.FIRMWARE_SETTING_EVENT) {
            ThreadHelper.invokeLater(this::refeshControls);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        updateSaveButton();
    }

    private void updateSaveButton() {
        if (getBackend().getController() != null) {
            try {
                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                boolean enabled = decimalFormat.parse(textFieldSoftLimitX.getText()).doubleValue() != firmwareSettings.getSoftLimitX() ||
                        decimalFormat.parse(textFieldSoftLimitY.getText()).doubleValue() != firmwareSettings.getSoftLimitY() ||
                        decimalFormat.parse(textFieldSoftLimitZ.getText()).doubleValue() != firmwareSettings.getSoftLimitZ();
                buttonSave.setEnabled(enabled);
            } catch (ParseException | FirmwareSettingsException e) {
                e.printStackTrace();
            }
        }
    }
}
