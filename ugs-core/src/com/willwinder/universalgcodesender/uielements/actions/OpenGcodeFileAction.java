/*
    Copywrite 2015-2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.actions;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 *
 * @author wwinder
 */
public class OpenGcodeFileAction extends AbstractAction {
    BackendAPI backend;

    public OpenGcodeFileAction(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        openGcodeFileDialog();
    }
    
    public void openGcodeFile(File f) {
        try {
            backend.setGcodeFile(f);
            Settings settings = backend.getSettings();
            settings.setLastOpenedFilename(f.getAbsolutePath());
            SettingsFactory.saveSettings(settings);
        } catch (Exception e) {

        }
    }

    public void openGcodeFileDialog() {
        JFileChooser fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(
                backend.getSettings().getLastOpenedFilename());
        int returnVal = fileChooser.showOpenDialog(new JFrame());
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
