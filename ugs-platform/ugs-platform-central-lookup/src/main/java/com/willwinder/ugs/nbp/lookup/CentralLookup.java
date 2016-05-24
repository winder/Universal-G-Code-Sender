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

/*
 * Based on this:
 * http://wadechandler.blogspot.com/2007/12/central-lookup-creating-central.html
 */
package com.willwinder.ugs.nbp.lookup;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author will
 */
public class CentralLookup extends AbstractLookup {
    private static CentralLookup def = new CentralLookup();

    private InstanceContent content = null;
    
    public CentralLookup(InstanceContent content) {
        super(content);
        this.content = content;
    }
    
    public CentralLookup() {
        this(new InstanceContent());
        try {
            GUIBackend backend = new GUIBackend();
            Settings settings = SettingsFactory.loadSettings();
            //Localization.initialize(settings.getLanguage());

            boolean fullyLocalized = Localization.initialize(settings.getLanguage());
            if (!fullyLocalized) {
                GUIHelpers.displayErrorDialog(Localization.getString("incomplete.localization"));
            }


            backend.applySettings(settings);
            this.add(backend);
            this.add(settings);
        } catch (Exception ex) {
            Logger.getLogger(CentralLookup.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }
    
    final public void add(Object instance) {
        content.add(instance);
    }

    public void remove(Object instance) {
        content.remove(instance);
    }

    public static CentralLookup getDefault(){
        return def;
    }
}
