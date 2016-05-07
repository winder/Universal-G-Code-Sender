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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BackendAPI.ACTIONS;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=RunActionService.class) 
public class RunActionService {
    BackendAPI backend;

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

    public void runAction(ACTIONS action) {
        if (canRunCommand()) {
            try {
                backend.performAction(action);
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
            String localized = String.format("Menu/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.actions"));
            String menuPath = "Menu/Machine/Actions";
            String machine = "Machine";
            String localMachine = Localization.getString("platform.menu.machine");
            
            ars.registerAction(Localization.getString("mainWindow.swing.returnToZeroButton"),
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.RETURN_TO_ZERO));
            ars.registerAction(Localization.getString("mainWindow.swing.softResetMachineControl"),
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.ISSUE_SOFT_RESET));
            ars.registerAction(Localization.getString("mainWindow.swing.resetCoordinatesButton"),
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.RESET_COORDINATES_TO_ZERO));
            ars.registerAction("$X",
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.KILL_ALARM_LOCK));
            ars.registerAction("$C",
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.TOGGLE_CHECK_MODE));
            ars.registerAction("$G",
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.REQUEST_PARSER_STATE));
            ars.registerAction(Localization.getString("mainWindow.swing.homeMachine"),
                    machine, localMachine, null , menuPath, localized,
                    new GcodeAction(this, ACTIONS.HOMING_CYCLE));

            /*
            TODO: Override actions
            localized = String.format("Menu/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.overrides"));
            menuPath = "Menu/Machine/Overrides";
            machine = "Machine";

            // Overrides
            */

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
    protected class GcodeAction extends AbstractAction {
        RunActionService gs;
        ACTIONS action;

        public GcodeAction(RunActionService service, ACTIONS action) {
            //super(name);
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
