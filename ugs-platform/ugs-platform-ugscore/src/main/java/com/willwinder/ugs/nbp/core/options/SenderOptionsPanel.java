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
package com.willwinder.ugs.nbp.core.options;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.uielements.ConnectionSettingsPanel;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.BorderLayout;

final class SenderOptionsPanel extends javax.swing.JPanel {

    private final SenderOptionsOptionsPanelController controller;
    private ConnectionSettingsPanel settingsPanel;
    private final Settings settings;

    SenderOptionsPanel(SenderOptionsOptionsPanelController controller) {
        this.controller = controller;
        settings = CentralLookup.getDefault().lookup(Settings.class);
        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    private void initComponents() {
        settingsPanel = new ConnectionSettingsPanel(settings);
        setLayout(new BorderLayout());
        add(settingsPanel, BorderLayout.CENTER);
    }

    void load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(SenderOptionsPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(SenderOptionsPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
    }

    void store() {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(SenderOptionsPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(SenderOptionsPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
