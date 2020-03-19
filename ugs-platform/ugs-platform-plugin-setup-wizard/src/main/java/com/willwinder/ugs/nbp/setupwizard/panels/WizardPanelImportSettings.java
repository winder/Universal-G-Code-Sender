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
import com.willwinder.universalgcodesender.firmware.FirmwareSettingUtils;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsFile;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.components.FirmwareSettingsFileTypeFilter;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wizard step panel for importing settings
 *
 * @author Joacim Breiler
 */
public class WizardPanelImportSettings extends AbstractWizardPanel {

    private static final Logger LOGGER = Logger.getLogger(WizardPanelImportSettings.class.getSimpleName());
    private JLabel labelDescription;
    private JLabel labelNameValue;
    private JLabel labelCreatedBy;
    private JLabel labelCreatedByValue;
    private JLabel labelDate;
    private JLabel labelDateValue;
    private JLabel labelFirmware;
    private JLabel labelFirmwareValue;
    private JLabel labelSettingsImported;
    private JButton buttonOpen;
    private JButton buttonImport;
    private FirmwareSettingsFile firmwareSettingsFile;
    private RoundedPanel fileInfoPanel;

    public WizardPanelImportSettings(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.import-settings.title"), false);

        initComponents();
        initLayout();
        loadSettingsFile(null);
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "gapbottom 5, spanx");
        panel.add(buttonOpen, "gaptop 5, gapbottom 10, wmin 200, hmin 36, spanx");

        fileInfoPanel.setLayout(new MigLayout("wrap 2, fillx, inset 20, gap 5, hidemode 3", "[20%][80%]"));
        fileInfoPanel.add(labelNameValue, "gapleft 10, gapbottom 10, spanx");
        fileInfoPanel.add(labelFirmware, "gapleft 10, grow");
        fileInfoPanel.add(labelFirmwareValue);
        fileInfoPanel.add(labelCreatedBy, "gapleft 10, grow");
        fileInfoPanel.add(labelCreatedByValue);
        fileInfoPanel.add(labelDate, "gapleft 10, grow");
        fileInfoPanel.add(labelDateValue);
        fileInfoPanel.add(buttonImport, "gaptop 10, growx, spanx, hmin 36");
        panel.add(fileInfoPanel, "wmin 400, gapleft 2, spanx");
        panel.add(labelSettingsImported, "gaptop 20, grow");

        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.import-settings.intro") +
                "</p></body></html>");

        fileInfoPanel = new RoundedPanel(8);
        fileInfoPanel.setBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
        fileInfoPanel.setForeground(ThemeColors.LIGHT_GREY);

        labelNameValue = new JLabel("");
        labelNameValue.setFont(labelNameValue.getFont().deriveFont(Font.BOLD, 16));

        labelFirmware = new JLabel(Localization.getString("platform.plugin.setupwizard.import-settings.firmware"), JLabel.RIGHT);
        labelFirmware.setFont(labelFirmware.getFont().deriveFont(Font.BOLD));
        labelFirmwareValue = new JLabel("");

        labelCreatedBy = new JLabel(Localization.getString("platform.plugin.setupwizard.import-settings.created-by"), JLabel.RIGHT);
        labelCreatedBy.setFont(labelCreatedBy.getFont().deriveFont(Font.BOLD));
        labelCreatedByValue = new JLabel("");

        labelDate = new JLabel(Localization.getString("platform.plugin.setupwizard.import-settings.created-date"), JLabel.RIGHT);
        labelDate.setFont(labelDate.getFont().deriveFont(Font.BOLD));
        labelDateValue = new JLabel("");

        labelSettingsImported = new JLabel("<html><body>" +
                "<h3>" + Localization.getString("platform.plugin.setupwizard.import-settings.finished-title") + "</h3>" +
                "<p>" + Localization.getString("platform.plugin.setupwizard.import-settings.finished-description") + "</p>" +
                "</body></html>");
        labelSettingsImported.setVerticalAlignment(SwingConstants.CENTER);
        labelSettingsImported.setIcon(ImageUtilities.loadImageIcon("icons/checked32.png", false));

        buttonOpen = new JButton(Localization.getString("platform.plugin.setupwizard.import-settings.open-settings"));
        buttonOpen.addActionListener(event -> {
            JFileChooser fileChooser = FirmwareSettingsFileTypeFilter.getSettingsFileChooser();
            int returnVal = fileChooser.showOpenDialog(new JFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                loadSettingsFile(fileChooser.getSelectedFile());
            }
        });

        buttonImport = new JButton(Localization.getString("platform.plugin.setupwizard.import-settings.import"));
        buttonImport.addActionListener(event -> importSettings());
    }

    private void importSettings() {
        String title = Localization.getString("platform.plugin.setupwizard.import-settings.overwrite-title");
        String message = Localization.getString("platform.plugin.setupwizard.import-settings.overwrite-message");
        int result = JOptionPane.showConfirmDialog(this.getComponent(), message,
                title, JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                getBackend().getController().getFirmwareSettings().setSettings(firmwareSettingsFile.getSettings());
            } catch (FirmwareSettingsException e) {
                LOGGER.log(Level.SEVERE, "Couldn't set firmware settings", e);
            }
            labelSettingsImported.setVisible(true);
        }

        loadSettingsFile(null);
    }

    private void loadSettingsFile(File file) {
        if (file == null) {
            firmwareSettingsFile = null;
        } else {
            try {
                firmwareSettingsFile = FirmwareSettingUtils.readSettings(file);
            } catch (IOException e) {
                GUIHelpers.displayErrorDialog("Couldn't read settings file: " + e.getMessage(), true);
                firmwareSettingsFile = null;
            }
        }
        showSettingsInformation();
    }

    private void showSettingsInformation() {
        // Wait until we show the information so that the user notices that something happened
        ThreadHelper.invokeLater(() -> {
            boolean showSettingsInformation = false;
            if (firmwareSettingsFile != null) {
                showSettingsInformation = true;
                labelNameValue.setText(firmwareSettingsFile.getName());
                labelFirmwareValue.setText(firmwareSettingsFile.getFirmwareName());
                labelCreatedByValue.setText(firmwareSettingsFile.getCreatedBy());
                labelDateValue.setText(firmwareSettingsFile.getDate());
            }

            fileInfoPanel.setVisible(showSettingsInformation);
        }, 500);
    }

    @Override
    public void initialize() {
        loadSettingsFile(null);
        labelSettingsImported.setVisible(false);
    }

    @Override
    public void destroy() {
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport();
    }
}
