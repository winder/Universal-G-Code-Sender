/*
    Copyright 2016-2018 Will Winder

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

import com.willwinder.ugs.nbp.core.control.MacroService;
import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.macros.MacroSettingsPanel;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import org.openide.util.Lookup;

final class MacrosPanel extends AbstractOptionsPanel {

    private MacroSettingsPanel mp;
    private BackendAPI backend;
    private MacroService macroService;

    MacrosPanel(MacrosOptionsPanelController controller) {
        super(controller);

        macroService = Lookup.getDefault().lookup(MacroService.class);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        super.setLayout(new BorderLayout());
    }

    @Override
    public void load() {
        if (mp != null) {
            this.remove(mp);
        }
        mp = new MacroSettingsPanel(backend);
        super.add(mp, BorderLayout.CENTER);
        SwingUtilities.invokeLater(changer::changed);
    }

    @Override
    public void store() {
        mp.save();
        SettingsFactory.saveSettings(backend.getSettings());
        macroService.reInitActions();
    }

    @Override
    public boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
