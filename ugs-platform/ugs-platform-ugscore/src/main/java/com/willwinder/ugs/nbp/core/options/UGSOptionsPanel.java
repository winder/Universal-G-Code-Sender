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
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.BorderLayout;

abstract class UGSOptionsPanel extends javax.swing.JPanel {

    protected final AbstractOptionsPanelController controller;
    private AbstractUGSSettings settingsPanel;
    protected final Settings settings;

    UGSOptionsPanel(AbstractOptionsPanelController controller) {
        this.controller = controller;
        settings = CentralLookup.getDefault().lookup(Settings.class);
        settingsPanel = initPanel(controller);

        initComponents();
        // TODO listen to changes in form fields and call controller.changed()
    }

    protected abstract AbstractUGSSettings initPanel(IChanged changer);

    private void initComponents() {
        setLayout(new BorderLayout());
        add(settingsPanel, BorderLayout.CENTER);
    }

    void load() {
        settingsPanel.updateComponents(settings);
    }

    void store() {
        settingsPanel.save();
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
