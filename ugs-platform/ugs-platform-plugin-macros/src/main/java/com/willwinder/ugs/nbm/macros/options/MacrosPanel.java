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
package com.willwinder.ugs.nbm.macros.options;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.macros.MacroPanel;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.swing.*;
import java.awt.*;

final class MacrosPanel extends AbstractOptionsPanel {

    MacroPanel mp;
    BackendAPI backend;

    MacrosPanel(MacrosOptionsPanelController controller) {
        super(controller);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        super.setLayout(new BorderLayout());
    }

    @Override
    public void load() {
        if (mp != null) {
            this.remove(mp);
        }
        mp = new MacroPanel(backend);
        super.add(mp, BorderLayout.CENTER);
        SwingUtilities.invokeLater(changer::changed);
    }

    @Override
    public void store() {
        SettingsFactory.saveSettings(backend.getSettings());
    }

    @Override
    public boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
