/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.toolbars;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.uielements.actions.OpenGcodeFileAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class FileBrowsePanel extends JPanel implements UGSEventListener {
    BackendAPI backend;

    final private JTextField fileTextField = new JTextField();
    final private JButton browseButton = new JButton();

    public FileBrowsePanel(BackendAPI backend) {
        this.backend = backend;
        this.backend.addUGSEventListener(this);

        setFile();
        browseButton.setAction(new OpenGcodeFileAction(backend));

        String browse = Localization.getString("mainWindow.swing.browseButton");
        browseButton.setText(browse);

        this.setLayout(new MigLayout("inset 0, fillx"));
        this.add(new JLabel(Localization.getString("mainWindow.swing.fileLabel")));
        this.add(fileTextField, "grow, w 150!");
        this.add(browseButton);
    }

    final private void setFile() {
        if (backend.getGcodeFile() != null) {
            fileTextField.setText(backend.getGcodeFile().getName());
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isFileChangeEvent()) {
            setFile();
        }
    }
    
}
