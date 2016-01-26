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

package com.willwinder.ugs.nbp.filebrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "File",
        id = "com.willwinder.universalgcodesender.nbp.filebrowser.OpenGcodeFile"
)
@ActionRegistration(
        displayName = "#CTL_OpenGcodeFile"
)
@ActionReference(path = "Menu/File", position = 1300)
@Messages("CTL_OpenGcodeFile=Open Gcode File...")
public final class OpenGcodeFile implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        FileBrowserTopComponent.openGcodeFileDialog();
    }
}
