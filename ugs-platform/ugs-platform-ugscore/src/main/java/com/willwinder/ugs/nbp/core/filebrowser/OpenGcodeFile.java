/*
    Copywrite 2015 Will Winder

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

package com.willwinder.ugs.nbp.core.filebrowser;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(
        category = "File",
        id = "com.willwinder.universalgcodesender.nbp.filebrowser.OpenGcodeFile"
)
@ActionRegistration(
        displayName = "#CTL_OpenGcodeFile"
)
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 1300),
    @ActionReference(path="Shortcuts", name="M-O")
})
@Messages("CTL_OpenGcodeFile=Open Gcode File...")
public final class OpenGcodeFile implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        openGcodeFileDialog();
    }

    public static void openGcodeFile(File f) {
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        try {
            backend.setGcodeFile(f);
            settings.setLastOpenedFilename(f.getAbsolutePath());
            SettingsFactory.saveSettings(settings);
        } catch (Exception e) {

        }
    }

    public static void openGcodeFileDialog() {
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        
        JFileChooser fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(settings.getLastOpenedFilename());
        int returnVal = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File gcodeFile = fileChooser.getSelectedFile();
                openGcodeFile(gcodeFile);
            } catch (Exception ex) {
                //MainWindow.displayErrorDialog(ex.getMessage());
            }
        } else {
            // Canceled file open.
        }  
    }
}
