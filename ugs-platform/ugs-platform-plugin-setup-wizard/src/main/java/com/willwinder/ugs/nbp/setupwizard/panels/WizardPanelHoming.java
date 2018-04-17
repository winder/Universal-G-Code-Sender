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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

    public WizardPanelHoming(BackendAPI backend) {
        super(backend, "Homing");

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "gapbottom 10");
        panel.add(checkboxEnableHoming);
        panel.add(labelHardLimitsNotEnabled);
        panel.add(labelHomingNotSupported);
        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body>" +
                "<p>Homing is a method of of finding the absolute machine coordinates.</p>" +
                "</body></html>");

        checkboxEnableHoming = new JCheckBox("Enable homing");
        checkboxEnableHoming.addActionListener(event -> {
            try {
                getBackend().getController().getFirmwareSettings().setHomingEnabled(checkboxEnableHoming.isSelected());
            } catch (FirmwareSettingsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't enable/disable the hard limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        });

        labelHardLimitsNotEnabled = new JLabel("<html><body>Limit switches needs to be enabled before enabling homing.</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHardLimitsNotEnabled.setVisible(false);

        labelHomingNotSupported = new JLabel("<html><body>Homing is not available on your hardware.</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHomingNotSupported.setVisible(false);
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refreshControls();
    }

    private void refreshControls() {
        if (getBackend().getController() != null &&
                getBackend().getController().getCapabilities().hasHoming() &&
                getBackend().getController().getCapabilities().hasHardLimits() &&
                getBackend().getController().getFirmwareSettings().isHardLimitsEnabled()) {
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            checkboxEnableHoming.setSelected(firmwareSettings.isHomingEnabled());
            checkboxEnableHoming.setVisible(true);
            labelHomingNotSupported.setVisible(false);
            labelHardLimitsNotEnabled.setVisible(false);
        } else if (getBackend().getController() != null &&
                getBackend().getController().getCapabilities().hasHoming() &&
                !getBackend().getController().getFirmwareSettings().isHardLimitsEnabled()) {
            checkboxEnableHoming.setVisible(false);
            labelHomingNotSupported.setVisible(false);
            labelHardLimitsNotEnabled.setVisible(true);
        } else {
            checkboxEnableHoming.setVisible(false);
            labelHomingNotSupported.setVisible(true);
            labelHardLimitsNotEnabled.setVisible(false);
        }
    }

    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event.getEventType() == UGSEvent.EventType.FIRMWARE_SETTING_EVENT) {
            ThreadHelper.invokeLater(this::refreshControls);
        }
    }
}
