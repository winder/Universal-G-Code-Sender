/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import java.util.prefs.Preferences;
import net.miginfocom.swing.MigLayout;
import org.openide.util.NbPreferences;

/**
 *
 * @author will
 */
public class CloudStorageSettingsPanel extends AbstractUGSSettings {
    // TODO: Localize settings
    private final AbstractUGSSettings.Textfield s3Id = new AbstractUGSSettings.Textfield("AWS Access Key ID");
    private final AbstractUGSSettings.Textfield s3Secret = new AbstractUGSSettings.Textfield("AWS Secret Access Key");

    public static String S3_ID = "s3Id";
    public static String S3_SECRET = "s3Secret";
    
    public CloudStorageSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();

        Preferences prefs = NbPreferences.forModule(CloudStorageSettingsPanel.class);
        this.s3Id.text.setText(prefs.get(S3_ID, ""));
        this.s3Secret.text.setText(prefs.get(S3_SECRET, ""));

        setLayout(new MigLayout("wrap 1", "grow, fill"));
        add(this.s3Id);
        add(this.s3Secret);        
    }

    @Override
    public void save() {
        Preferences prefs = NbPreferences.forModule(CloudStorageSettingsPanel.class);
        prefs.put(S3_ID, s3Id.text.getText());
        prefs.put(S3_SECRET, s3Secret.text.getText());
    }

    @Override
    public String getHelpMessage() {
        return "";
    }

    @Override
    public void restoreDefaults() throws Exception {
    }
}
