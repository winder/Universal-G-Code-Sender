package com.willwinder.ugs.nbp.setupwizard.panels;

import com.willwinder.ugs.nbp.setupwizard.AbstractWizardPanel;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingUtils;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsFile;
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
 * A settings step for importing settings
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
        super(backend, "Import settings", false);

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
        labelDescription = new JLabel("<html><body>" +
                "<p>If your machine came with a configuration file or if you previously made a backup of your settings you can load them here. Otherwise click next to continue.</p>" +
                "</body></html>");

        fileInfoPanel = new RoundedPanel(8);
        fileInfoPanel.setBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
        fileInfoPanel.setForeground(ThemeColors.LIGHT_GREY);

        labelNameValue = new JLabel("");
        labelNameValue.setFont(labelNameValue.getFont().deriveFont(Font.BOLD, 16));

        labelFirmware = new JLabel("Firmware:", JLabel.RIGHT);
        labelFirmware.setFont(labelFirmware.getFont().deriveFont(Font.BOLD));
        labelFirmwareValue = new JLabel("");

        labelCreatedBy = new JLabel("Created by:", JLabel.RIGHT);
        labelCreatedBy.setFont(labelCreatedBy.getFont().deriveFont(Font.BOLD));
        labelCreatedByValue = new JLabel("");

        labelDate = new JLabel("Date:", JLabel.RIGHT);
        labelDate.setFont(labelDate.getFont().deriveFont(Font.BOLD));
        labelDateValue = new JLabel("");

        labelSettingsImported = new JLabel("<html><body>" +
                "<h3>Finished imported settings</h3>" +
                "<p>Please continue to verify your configuration using the following steps.</p>" +
                "</body></html>");
        labelSettingsImported.setVerticalAlignment(SwingConstants.CENTER);
        labelSettingsImported.setIcon(ImageUtilities.loadImageIcon("icons/checked32.png", false));

        buttonOpen = new JButton("Open settings file...");
        buttonOpen.addActionListener(event -> {
            JFileChooser fileChooser = FirmwareSettingsFileTypeFilter.getSettingsFileChooser();
            int returnVal = fileChooser.showOpenDialog(new JFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                loadSettingsFile(fileChooser.getSelectedFile());
            }
        });

        buttonImport = new JButton("Import settings");
        buttonImport.addActionListener(event -> importSettings());
    }

    private void importSettings() {
        String message = "Are you sure you want to overwrite the current firmware settings?";
        int result = JOptionPane.showConfirmDialog(this.getComponent(), message,
                "Overwrite firmware settings?", JOptionPane.YES_NO_OPTION);
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
