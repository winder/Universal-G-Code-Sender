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
package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=MacroService.class) 
public class MacroService {
    public MacroService() {
        initActions();
    }

    
    private void initActions() {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        try {
            String menuPath = "Menu/Machine/Macros";
            int numMacros = settings.getLastMacroIndex() + 1;
            for (int i = 0; i < numMacros; i++) {
                Macro m = settings.getMacro(i);
                ars.registerAction(m.getName(), "Macro", null, menuPath, new MacroAction(settings, backend, i));
            }
        } catch (Exception e) {

        }
    }


    protected class MacroAction extends AbstractAction {
        BackendAPI backend;
        Settings settings;
        int macroIdx;

        public MacroAction(Settings s, BackendAPI b, int macro) {
            backend = b;
            settings = s;
            macroIdx = macro;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Macro m = settings.getMacro(macroIdx);
            try {
                if (m != null && m.getGcode() != null) {
                    backend.sendGcodeCommand(m.getGcode());
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public boolean isEnabled() {
            return backend.isConnected() && backend.isIdle();
        }
    }
}
