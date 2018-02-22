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
package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.uielements.panels.OverridesPanel;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=RunActionService.class)
public class RunActionService {
    private BackendAPI backend;

    public RunActionService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        initActions();
    }

    public void runAction(Overrides action) {
        if (canRunCommand()) {
            try {
                backend.sendOverrideCommand(action);
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getMessage());
            }
        }
    }

    public boolean canRunCommand() {
        return backend.getControlState() == UGSEvent.ControlState.COMM_IDLE;
    }

    final public void initActions() {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);

        try {
            String localized;
            String menuPath;
            String category;
            String localizedCategory;

            // Feed Overrides
            category = "Overrides";
            localizedCategory = Localization.getString("platform.menu.overrides");
            menuPath = "Menu/Machine/Overrides";
            localized = String.format("Menu/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.overrides"));

            String pattern = Localization.getString("overrides.feed") + " (%s)";

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrCoarseMinus", String.format(pattern, OverridesPanel.MINUS_COARSE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_FEED_OVR_COARSE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrFineMinus", String.format(pattern, OverridesPanel.MINUS_FINE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_FEED_OVR_FINE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrFinePlus", String.format(pattern, OverridesPanel.PLUS_FINE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_FEED_OVR_FINE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrCoarsePlus", String.format(pattern, OverridesPanel.PLUS_COARSE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_FEED_OVR_COARSE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".feedOvrReset", String.format(pattern, OverridesPanel.RESET_FEED),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_FEED_OVR_RESET));

            // Spindle Overrides
            menuPath = "Menu/Machine/Overrides";
            localized = String.format("Menu/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.overrides"));
            pattern = Localization.getString("overrides.spindle") + " (%s)";

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrCoarseMinus", String.format(pattern, OverridesPanel.MINUS_COARSE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_SPINDLE_OVR_COARSE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrFineMinus", String.format(pattern, OverridesPanel.MINUS_FINE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_SPINDLE_OVR_FINE_MINUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrFinePlus", String.format(pattern, OverridesPanel.PLUS_FINE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_SPINDLE_OVR_FINE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrCoarsePlus", String.format(pattern, OverridesPanel.PLUS_COARSE),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_SPINDLE_OVR_COARSE_PLUS));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".spindleOvrReset", String.format(pattern, Localization.getString("mainWindow.swing.reset")),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_SPINDLE_OVR_RESET));

            // Rapid Overrides
            menuPath = "Menu/Machine/Overrides";
            localized = String.format("Menu/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.overrides"));

            pattern = Localization.getString("overrides.rapid") + " (%s)";

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".rapidOvrLow", String.format(pattern, OverridesPanel.RAPID_LOW),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_RAPID_OVR_LOW));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".rapidOvrMedium", String.format(pattern, OverridesPanel.RAPID_MEDIUM),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_RAPID_OVR_MEDIUM));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".rapidOvrReset", String.format(pattern, OverridesPanel.RAPID_FULL),
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_RAPID_OVR_RESET));

            // Toggles
            menuPath = "Menu/Machine/Overrides/Toggles";
            localized = String.format("Menu/%s/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.overrides"),
                    Localization.getString("overrides.toggle.short"));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".toggleSpindle", OverridesPanel.SPINDLE_SHORT,
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_TOGGLE_SPINDLE));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".toogleFloodCoolant", OverridesPanel.FLOOD,
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_TOGGLE_FLOOD_COOLANT));

            ars.registerAction(OverrideAction.class.getCanonicalName() + ".toggleMistCoolant", OverridesPanel.MIST,
                    category, localizedCategory, null , menuPath, localized,
                    new OverrideAction(this, Overrides.CMD_TOGGLE_MIST_COOLANT));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected class OverrideAction extends AbstractAction {
        RunActionService gs;
        Overrides action;

        public OverrideAction(RunActionService service, Overrides action) {
            gs = service;
            this.action = action;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            gs.runAction(action);
        }

        @Override
        public boolean isEnabled() {
            return gs.canRunCommand();
        }
    }
}
