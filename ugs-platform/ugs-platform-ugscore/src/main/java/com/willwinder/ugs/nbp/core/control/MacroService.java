/*
    Copyright 2016-2019 Will Winder

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

import com.google.common.base.Strings;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=MacroService.class) 
public final class MacroService {
    private static final Logger logger = Logger.getLogger(MacroService.class.getName());

    public MacroService() {
        reInitActions();
    }

    public void reInitActions() {
        String menuPath = LocalizingService.MENU_MACROS;
        String actionCategory = "Macro";
        String localized = String.format("Menu/%s/%s",
                Localization.getString("platform.menu.machine"),
                Localization.getString("platform.menu.macros"));

        try {
            FileObject root = FileUtil.getConfigRoot();

            // Clear out the menu items.
            FileUtil.createFolder(root, menuPath).delete();
            FileUtil.createFolder(root, menuPath);

            String actionPath = "/Actions/" + actionCategory;
            FileUtil.createFolder(root, actionPath).delete();

            ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            Settings settings = backend.getSettings();

            List<Macro> macros = settings.getMacros();
            macros.forEach(macro -> {
                int index = macros.indexOf(macro);
                try {
                    String text;
                    if (Strings.isNullOrEmpty(macro.getNameAndDescription())){
                        text = Integer.toString(index + 1);
                    } else {
                        text = macro.getNameAndDescription();
                    }

                    ars.registerAction(MacroAction.class.getCanonicalName() + "." + macro.getName(), text, actionCategory, null, menuPath, index, localized, new MacroAction(macro));
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Couldn't register macro action: \"" + macro.getName() + "\"", e);
                }
            });
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn't register macro actions", e);
        }
    }


}
