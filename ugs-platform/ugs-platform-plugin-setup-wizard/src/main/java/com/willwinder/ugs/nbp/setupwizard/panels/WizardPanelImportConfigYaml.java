/*
    Copyright 2025 Damian Nikodem

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
import com.willwinder.universalgcodesender.IFileService;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.types.CommandException;
import com.willwinder.universalgcodesender.uielements.FileOpenDialog;
import com.willwinder.universalgcodesender.uielements.FileSaveDialog;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.components.YamlSettingsFileTypeFilter;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.JComboBox;

/**
 * A wizard step panel for importing settings
 *
 * @author Joacim Breiler
 */
public class WizardPanelImportConfigYaml extends AbstractWizardPanel implements UGSEventListener {
   
    private JLabel labelDescription;
    private JLabel labelSettingsImported;
    private JLabel labelConfigFileToUse;
        
    private JButton buttonOpen;
    private JButton buttonSave;
    private JComboBox<String> comboSettingsFileToUse;
    private RoundedPanel fileInfoPanel;
    private String settingsFileToUse;
    private int updateCount=0;
        
    public WizardPanelImportConfigYaml(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.import-config-yaml.title"), false);

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 1, fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "gapbottom 5, spanx");
        panel.add(buttonOpen, "gaptop 5, gapbottom 10, wmin 200, hmin 36, spanx");
        panel.add(buttonSave, "gaptop 5, gapbottom 10, wmin 200, hmin 36, spanx");        
        fileInfoPanel.setLayout(new MigLayout("wrap 2, fillx, inset 20, gap 5, hidemode 3", "[20%][80%]"));
        fileInfoPanel.add(labelConfigFileToUse, "gapleft 10, grow");
        fileInfoPanel.add(comboSettingsFileToUse);

        panel.add(fileInfoPanel, "wmin 400, gapleft 2, spanx");
        panel.add(labelSettingsImported, "gaptop 20, grow");

        getPanel().add(panel, "grow");
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.import-config-yaml.intro") +
                "</p></body></html>");

        fileInfoPanel = new RoundedPanel(8);
        fileInfoPanel.setBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
        fileInfoPanel.setForeground(ThemeColors.LIGHT_GREY);

        labelSettingsImported = new JLabel("<html><body>" +
                "<h3>" + Localization.getString("platform.plugin.setupwizard.import-config-yaml.finished-title") + "</h3>" +
                "<p>" + Localization.getString("platform.plugin.setupwizard.import-config-yaml.finished-description") + "</p>" +
                "</body></html>");
        labelSettingsImported.setVerticalAlignment(SwingConstants.CENTER);
        labelSettingsImported.setIcon(ImageUtilities.loadImageIcon("icons/checked32.png", false));

        buttonOpen = new JButton(Localization.getString("platform.plugin.setupwizard.import-config-yaml.open-settings"));
        buttonOpen.addActionListener(event -> {
            FileSaveDialog fileChooser = YamlSettingsFileTypeFilter.getSettingsFileSaveChooser();
            fileChooser.setFile(settingsFileToUse);            
            fileChooser.setVisible(true);
            fileChooser.getSelectedFile().ifPresent(this::downloadSettingsFile);
        });
        
        buttonSave = new JButton(Localization.getString("platform.plugin.setupwizard.import-config-yaml.save-settings"));
        buttonSave.addActionListener(event -> {
            FileOpenDialog fileChooser = YamlSettingsFileTypeFilter.getSettingsFileChooser();
            fileChooser.setFile(settingsFileToUse);
            fileChooser.setVisible(true);
            fileChooser.getSelectedFile().ifPresent(this::uploadSettingsFile);
            refreshComponents();
        });
        
        comboSettingsFileToUse = new JComboBox<>();
        comboSettingsFileToUse.addActionListener(event -> {
            if (!canUpdate()) {
                return;
            }
            try {
                beginUpdate();
                IFirmwareSettings firmware = getBackend().getController().getFirmwareSettings();
                try {
                    String newConfigFilename= ""+comboSettingsFileToUse.getSelectedItem();
                    if ( (newConfigFilename == null) || (newConfigFilename.equals("null")) ) {
                        return;
                    }
                    if (!newConfigFilename.equals(settingsFileToUse)) {                    

                        String title = Localization.getString("platform.plugin.setupwizard.import-config-yaml.overwrite-config-filename-title");
                        String message = Localization.getString("platform.plugin.setupwizard.import-config-yaml.overwrite-config-filename-message");
                        title = String.format(title, settingsFileToUse);
                        message = String.format(message,newConfigFilename);
                        int result = JOptionPane.showConfirmDialog(this.getComponent(), message,
                                title, JOptionPane.YES_NO_OPTION);

                        if (result == JOptionPane.YES_OPTION) {
                            firmware.setConfigFilename(newConfigFilename);
                            // Rebooot Controller
                            firmwareSettingsUpdated();
                        }                        
                    }                
                } catch (FirmwareSettingsException e) {
                    GUIHelpers.displayErrorDialog("Couldn't Update Active Configuration file: " + e.getMessage(), true);
                }
            } finally {
                endUpdate();
            }
            
            refreshComponents();
            
        });
        
        labelConfigFileToUse = new JLabel(Localization.getString("platform.plugin.setupwizard.import-config-yaml.config-to-use"));
        refreshComponents();
    }
    
    private void beginUpdate() {
        updateCount++;
    }
    
    private void endUpdate() {
        if (updateCount == 0) {
            throw new RuntimeException("endUpdate called more times than beginUpdate");
        }
        updateCount--;
    }
    
    private boolean canUpdate() {
        return updateCount == 0;
    }
    
    private void refreshComponents() {
        ControllerState controllerState = getBackend().getControllerState();
        boolean isConnected = getBackend().isConnected() && (controllerState == ControllerState.IDLE || controllerState == ControllerState.ALARM || controllerState == ControllerState.HOLD);
        try {
            beginUpdate();        
            if (isConnected) {
                try {
                    settingsFileToUse = getBackend().getController().getFirmwareSettings().getConfigFilename();
                    List<com.willwinder.universalgcodesender.model.File> files = getBackend().getController().getFileService().getFiles();
                    comboSettingsFileToUse.removeAllItems();
                    for (com.willwinder.universalgcodesender.model.File f : files) {
                        if (f.getAbsolutePath().startsWith("/localfs/")) {                        
                            comboSettingsFileToUse.addItem(f.getName());
                        }                                        
                    }
                    comboSettingsFileToUse.setEnabled(comboSettingsFileToUse.getItemCount() != 1);

                    comboSettingsFileToUse.setSelectedItem(settingsFileToUse);

                } catch (FirmwareSettingsException | IOException ex) {
                    comboSettingsFileToUse.removeAllItems();
                    comboSettingsFileToUse.addItem("ERROR");
                    comboSettingsFileToUse.setEnabled(false);
                }     

            }
        } finally {
            endUpdate();
        }        
    }

    private void downloadSettingsFile(File file) {
        boolean downloaded = false;
        if (file != null) {            
            try {
                IFileService ifs = getBackend().getController().getFileService();
                List<com.willwinder.universalgcodesender.model.File> files = ifs.getFiles();
                for (com.willwinder.universalgcodesender.model.File remoteFile: files) {
                    if (remoteFile.getAbsolutePath().equals("/localfs/"+this.settingsFileToUse )) {
                        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));
                        fos.write(ifs.downloadFile(remoteFile));
                        fos.flush();                        
                        downloaded = true;
                    }
                }
                
                if (!downloaded) {
                    throw new Exception("Could not find: /localfs/"+this.settingsFileToUse + " To Download");
                }
            } catch (Exception fse) {
                GUIHelpers.displayErrorDialog("Couldn't download settings file: " + fse.getMessage(), true);
            }
        }
    }
    
    private void uploadSettingsFile(File file) {
        boolean rebootAfterUpload = false;
        if (file != null) {
            try {
                 IFileService ifs = getBackend().getController().getFileService();
                 
                 if (file.getName().equalsIgnoreCase(settingsFileToUse)) {
                    String title = Localization.getString("platform.plugin.setupwizard.import-config-yaml.overwrite-config-file-title");
                    String message = Localization.getString("platform.plugin.setupwizard.import-config-yaml.overwrite-config-file-message");
                    message = String.format(message,settingsFileToUse);
                    int result = JOptionPane.showConfirmDialog(this.getComponent(), message,
                            title, JOptionPane.YES_NO_OPTION);

                    if (result == JOptionPane.NO_OPTION) {
                        return;
                    } else {
                        rebootAfterUpload = true;
                        
                    }                                        
                 }
                 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file),(int)file.length());
                 
                 byte[] data = new byte[(int)file.length()];
                 bis.read(data);
                 ifs.uploadFile("/localfs/"+file.getName(), data);
                 if (rebootAfterUpload) {
                    getBackend().getController().issueHardReset();
                    firmwareSettingsUpdated();        
                 }
                 
            } catch (Exception e) {
                GUIHelpers.displayErrorDialog("Couldn't upload configuration file: " + e.getMessage(), true);                
            }
        }
        refreshComponents();
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);        
        refreshComponents();        
        labelSettingsImported.setVisible(false);
    }
    
    
    public void firmwareSettingsUpdated() {     
        try {
            getBackend().getController().getFirmwareSettings().refresh();        
        } catch ( FirmwareSettingsException | CommandException e) {
            
        }
        labelSettingsImported.setVisible(true);
    }
    
    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport() && 
                getBackend().getController().getCapabilities().hasConfigPersistence();                
    }
    
    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent controllerStateEvent) {
            ControllerState state = controllerStateEvent.getState();
            if (state == ControllerState.DISCONNECTED) {
//               Future Revisions may want to reboot the controller and reconnect.                
//               isErrorConnecting = true;
//               Settings settings = getBackend().getSettings();
//               getBackend().connect(settings.getFirmwareVersion(), settings.getPort(), Integer.parseInt(settings.getPortRate()));
            } 
            if (canUpdate()) {
                refreshComponents();
            }
        }
    }
}
