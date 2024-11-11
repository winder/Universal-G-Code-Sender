/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.ToggleUnitCategory,
        id = LocalizingService.ToggleUnitActionId)
@ActionRegistration(
        iconBase = UnlockAction.ICON_BASE,
        displayName = "resources/MessagesBundle#" + LocalizingService.ToggleUnitTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.ToggleUnitWindowPath,
                position = 1051)
})
public class ToggleUnitAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE_MM = "resources/icons/mm.svg";
    public static final String ICON_BASE_INCH = "resources/icons/inch.svg";

    private final BackendAPI backend;

    public ToggleUnitAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE_MM);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE_MM, false));
        putValue("menuText", LocalizingService.ToggleUnitTitle);
        putValue(NAME, LocalizingService.ToggleUnitTitle);
        putValue(Action.SHORT_DESCRIPTION, LocalizingService.ToggleUnitTooltip);
        setEnabled(isEnabled());
        update();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UnitUtils.Units units = backend.getSettings().getPreferredUnits() == UnitUtils.Units.MM ? UnitUtils.Units.INCH : UnitUtils.Units.MM;
        backend.getSettings().setPreferredUnits(units);
    }

    @Override
    public boolean isEnabled() {
        return !backend.isConnected() || (backend.isConnected() && backend.isIdle());
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            update();
        } else if (evt instanceof ControllerStateEvent) {
            setEnabled(isEnabled());
        }
    }

    private void update() {
        if(backend.getSettings().getPreferredUnits() == UnitUtils.Units.MM) {
            putValue("iconBase", ICON_BASE_MM);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE_MM, false));
        } else {
            putValue("iconBase", ICON_BASE_INCH);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE_INCH, false));
        }
    }
}
