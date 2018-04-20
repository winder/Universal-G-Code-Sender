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
 * A wizard step panel for configuring hard limits on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelHardLimits extends AbstractWizardPanel implements UGSEventListener {
    private JCheckBox checkboxEnableHardLimits;
    private JLabel labelHardLimitssNotSupported;
    private JLabel labelDescription;
    private JLabel labelExtendedDescription;

    public WizardPanelHardLimits(BackendAPI backend) {
        super(backend, "Limit switches");

        initComponents();
        initLayout();
        setValid(true);
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "growx, wrap, gapbottom 10");
        panel.add(labelExtendedDescription, "wrap, gapbottom 10");
        panel.add(checkboxEnableHardLimits, "wrap");
        panel.add(labelHardLimitssNotSupported, "wrap");
        add(panel);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body>" +
                "<p>Limit switches will prevent the machine to move beyond its physical limits.</p>" +
                "</body></html>");

        labelExtendedDescription = new JLabel("<html><body>" +
                "<p>Before enabling make sure that you have equipped your machine with switches on at least one end for each axis.</p>" +
                "</body></html>");

        checkboxEnableHardLimits = new JCheckBox("Enable limit switches");
        checkboxEnableHardLimits.addActionListener(event -> onHardLimitsClicked());

        labelHardLimitssNotSupported = new JLabel("<html><body>Limit switches are unfortunately not available on your hardware.</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHardLimitssNotSupported.setVisible(false);
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
        if (getBackend().getController() != null && getBackend().getController().getCapabilities().hasHardLimits()) {
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            checkboxEnableHardLimits.setSelected(firmwareSettings.isHardLimitsEnabled());
            checkboxEnableHardLimits.setVisible(true);
            labelExtendedDescription.setVisible(true);
            labelHardLimitssNotSupported.setVisible(false);
        } else {
            labelExtendedDescription.setVisible(false);
            checkboxEnableHardLimits.setVisible(false);
            labelHardLimitssNotSupported.setVisible(true);
        }
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
        if (evt.getEventType() == UGSEvent.EventType.FIRMWARE_SETTING_EVENT) {
            ThreadHelper.invokeLater(() -> {
                IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                checkboxEnableHardLimits.setSelected(firmwareSettings.isHardLimitsEnabled());
            });
        }
    }
}
