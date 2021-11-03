/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.editor;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;

/**
 * @author Joacim Breiler
 */
public class EditorOptionsPanel extends AbstractUGSSettings {
    public static final String SHOW_ON_OPEN = "showOnOpen";
    private final AbstractUGSSettings.Checkbox showOnOpen = new AbstractUGSSettings.Checkbox(Localization.getString("platform.plugin.editor.showOnOpen"));

    public EditorOptionsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
    }

    @Override
    protected void updateComponentsInternal(Settings settings) {
        this.removeAll();

        Preferences prefs = NbPreferences.forModule(EditorOptionsPanel.class);
        this.showOnOpen.box.setSelected(prefs.getBoolean(SHOW_ON_OPEN, true));

        setLayout(new MigLayout("wrap 1", "grow, fill"));
        add(this.showOnOpen);
    }

    @Override
    public void save() {
        Preferences prefs = NbPreferences.forModule(EditorOptionsPanel.class);
        prefs.putBoolean(SHOW_ON_OPEN, showOnOpen.getValue());
    }

    @Override
    public String getHelpMessage() {
        return "";
    }

    @Override
    public void restoreDefaults() throws Exception {
        Preferences prefs = NbPreferences.forModule(EditorOptionsPanel.class);
        prefs.putBoolean(SHOW_ON_OPEN, true);
    }
}
