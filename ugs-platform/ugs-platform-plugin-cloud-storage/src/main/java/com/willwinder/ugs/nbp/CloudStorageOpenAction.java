/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp;

import static com.willwinder.ugs.nbp.CloudStorageSettingsPanel.S3_ID;
import static com.willwinder.ugs.nbp.CloudStorageSettingsPanel.S3_SECRET;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

@ActionID(
	category = CloudStorageOpenAction.CATEGORY,
	id = CloudStorageOpenAction.ID
)
@ActionRegistration(
    iconBase = CloudStorageOpenAction.ICON_BASE,
    displayName = "Open cloud file"
)
@ActionReferences({
    @ActionReference(
            path = LocalizingService.OpenWindowPath,
            position = 11)
})

public final class CloudStorageOpenAction implements ActionListener {
    // Localization
    public static final String CATEGORY = "File";
    public static final String ID = "com.willwinder.ugs.nbp.CloudStorageOpenAction";
    public static final String ICON_BASE = "icons/cloud-folder.png";
    public static final String LOCALIZATION_KEY = "platform.plugin.cloud.open";

    private static final Logger logger = Logger.getLogger(CloudStorageOpenAction.class.getName());

    private final BackendAPI backend;

    public CloudStorageOpenAction() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        ActionRegistrationService ars = Lookup.getDefault().lookup(ActionRegistrationService.class);
        if (ars != null) {
            ars.overrideActionName(CATEGORY, ID, Localization.getString(LOCALIZATION_KEY));
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        Preferences prefs = NbPreferences.forModule(CloudStorageSettingsPanel.class);
        String id = prefs.get(S3_ID, "");
        String secret = prefs.get(S3_SECRET, "");
        
        try {
            S3FileSystemView viewer = new S3FileSystemView(id, secret);
            JFileChooser chooser = new JFileChooser("", viewer);
            int returnVal = chooser.showOpenDialog(new JFrame());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                logger.log(Level.INFO, "Opened S3 file", f);
                copyAndOpenFile(viewer, f);
            }
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog("There was a problem setting up the S3 viewer, check your settings.");
            logger.log(Level.WARNING, "Problem working with S3.", ex);
        }
    }
    
    public void copyAndOpenFile(S3FileSystemView viewer, File f) throws Exception {
        File settingsDir = SettingsFactory.getSettingsDirectory();
        File cloudDir = new File(settingsDir, "cloud_files");
        cloudDir.mkdir();
        
        File target = new File(cloudDir, f.getName());
        logger.log(Level.INFO, "Downloading S3 file '" + f.toString() + "' to '" + cloudDir.getAbsolutePath());
        viewer.downloadFile(f.toString(), target);
        backend.setGcodeFile(target);
    }
}
