/*
 * Copyright (C) 2019 will
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

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

    final static String access = "acess-token-here";
    final static String secret = "secret-key-here";

    final static S3FileSystemView viewer = new S3FileSystemView(access, secret);

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser("s3:///", viewer);
        int returnVal = chooser.showOpenDialog(new JFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {    
            File f = chooser.getSelectedFile();
            System.out.println("Found a file! It is " + f);
        } else {
            System.out.println("No file selected...");
        }
    }
}
