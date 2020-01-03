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
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

@ActionID(
	category = "File",
	id = "com.willwinder.ugs.nbp.CloudStorageOpenAction"
)
@ActionRegistration(
	iconBase = "icons/cloud-folder.png",
	displayName = "#CTL_CloudStorageOpenAction"
)
@ActionReferences({
	@ActionReference(path = "Menu/File", position = 1300),
	@ActionReference(path = "Toolbars/File", position = 300)
})
@Messages("CTL_CloudStorageOpenAction=Open cloud file")
public final class CloudStorageOpenAction implements ActionListener {
    private final BackendAPI backend;

    public CloudStorageOpenAction() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
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
                System.out.println("Found a file! It is " + f);
                copyAndOpenFile(viewer, f);
            } else {
                System.out.println("No file selected...");
            }
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog("There was a problem setting up the S3 viewer, check your settings.");
            //Exceptions.printStackTrace(e);
        }
    }
    
    public void copyAndOpenFile(S3FileSystemView viewer, File f) throws Exception {
        File settingsDir = SettingsFactory.getSettingsDirectory();
        File cloudDir = new File(settingsDir, "cloud_files");
        cloudDir.mkdir();
        
        File target = new File(cloudDir, f.getName());
        viewer.downloadFile(f.toString(), target);
        backend.setGcodeFile(target);
    }
}
