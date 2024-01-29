/*
    Copyright 2016-2024 Will Winder

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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Overrides;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.FLOOD;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.MINUS_COARSE;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.MINUS_FINE;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.MIST;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.PLUS_COARSE;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.PLUS_FINE;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.RAPID_FULL;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.RAPID_LOW;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.RAPID_MEDIUM;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.RESET_FEED;
import static com.willwinder.universalgcodesender.uielements.panels.OverrideLabels.SPINDLE_SHORT;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;

/**
 * A service for registering override actions as shortcuts
 *
 * @author wwinder
 */
@ServiceProvider(service = OverrideActionService.class)
public class OverrideActionService {
    public static final String MENU_OVERRIDE_LABEL = Localization.getString("platform.menu.overrides");
    public static final String MENU_MACHINE_LABEL = Localization.getString("platform.menu.machine");
    public static final String MENU_TOGGLE_LABEL = Localization.getString("overrides.toggle.short");
    public static final String MENU_PATH = "Menu/Machine/Overrides";
    public static final String MENU_OVERRIDES_TOGGLE_PATH = "Menu/Machine/Overrides/Toggles";
    private final BackendAPI backend;

    public OverrideActionService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        initActions();
    }

    public void runAction(Overrides action) {
        if (canRunAction()) {
            try {
                backend.getController().getOverrideManager().sendOverrideCommand(action);
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getMessage());
            }
        }
    }

    public boolean canRunAction() {
        return backend.isConnected() && backend.getController().getCapabilities().hasOverrides();
    }

    public final void initActions() {
        ActionRegistrationService ars = Lookup.getDefault().lookup(ActionRegistrationService.class);

        try {
            // Feed Overrides
            String category = "Overrides";
            String localized = String.format("Menu/%s/%s",
                    MENU_MACHINE_LABEL,
                    MENU_OVERRIDE_LABEL);

            String pattern = Localization.getString("overrides.feed") + " (%s)";

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrCoarseMinus", String.format(pattern, MINUS_COARSE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_FEED_OVR_COARSE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrFineMinus", String.format(pattern, MINUS_FINE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_FEED_OVR_FINE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrFinePlus", String.format(pattern, PLUS_FINE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_FEED_OVR_FINE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrCoarsePlus", String.format(pattern, PLUS_COARSE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_FEED_OVR_COARSE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrReset", String.format(pattern, RESET_FEED),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_FEED_OVR_RESET));

            // Spindle Overrides
            localized = String.format("Menu/%s/%s",
                    MENU_MACHINE_LABEL,
                    MENU_OVERRIDE_LABEL);
            pattern = Localization.getString("overrides.spindle") + " (%s)";

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrCoarseMinus", String.format(pattern, MINUS_COARSE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_SPINDLE_OVR_COARSE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrFineMinus", String.format(pattern, MINUS_FINE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_SPINDLE_OVR_FINE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrFinePlus", String.format(pattern, PLUS_FINE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_SPINDLE_OVR_FINE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrCoarsePlus", String.format(pattern, PLUS_COARSE),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_SPINDLE_OVR_COARSE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrReset", String.format(pattern, Localization.getString("mainWindow.swing.reset")),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_SPINDLE_OVR_RESET));

            // Rapid Overrides
            localized = String.format("Menu/%s/%s",
                    MENU_MACHINE_LABEL,
                    MENU_OVERRIDE_LABEL);

            pattern = Localization.getString("overrides.rapid") + " (%s)";

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".rapidOvrLow", String.format(pattern, RAPID_LOW),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_RAPID_OVR_LOW));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".rapidOvrMedium", String.format(pattern, RAPID_MEDIUM),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_RAPID_OVR_MEDIUM));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".rapidOvrReset", String.format(pattern, RAPID_FULL),
                    category, null, MENU_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_RAPID_OVR_RESET));

            // Toggles
            localized = String.format("Menu/%s/%s/%s",
                    MENU_MACHINE_LABEL,
                    MENU_OVERRIDE_LABEL,
                    MENU_TOGGLE_LABEL);

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".toggleSpindle", SPINDLE_SHORT,
                    category, null, MENU_OVERRIDES_TOGGLE_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_TOGGLE_SPINDLE));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".toogleFloodCoolant", FLOOD,
                    category, null, MENU_OVERRIDES_TOGGLE_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_TOGGLE_FLOOD_COOLANT));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".toggleMistCoolant", MIST,
                    category, null, MENU_OVERRIDES_TOGGLE_PATH, 0, localized,
                    new OverrideAction(Overrides.CMD_TOGGLE_MIST_COOLANT));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
