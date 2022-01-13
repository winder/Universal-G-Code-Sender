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
package com.willwinder.ugs.nbp.jog.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * An action for activating/deactivating separate step values for XY and Z axis.
 *
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.nbp.jog.actions.ShowABCStepSizeAction")
@ActionRegistration(
        displayName = "resources.MessagesBundle#platform.plugin.jog.showABCStepSize",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE_JOG_STEP_SIZE,
                position = 10000,
                separatorAfter = 10001)
})
@ServiceProvider(service = ShowABCStepSizeAction.class)
public class ShowABCStepSizeAction extends AbstractAction implements Presenter.Menu {

    private final BackendAPI backend;
    private final JCheckBoxMenuItem menuItem;

    public ShowABCStepSizeAction() {
        String title = Localization.getString("platform.plugin.jog.showABCStepSize");
        putValue(NAME, title);

        menuItem = new JCheckBoxMenuItem(title);
        menuItem.setAction(this);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onBackendEvent);
        setEnabled(isEnabled());
    }

    private void onBackendEvent(UGSEvent event) {
        if (event instanceof SettingChangedEvent) {
            menuItem.setSelected(backend.getSettings().showABCStepSize());
        } else if (event instanceof ControllerStateEvent) {
            EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend != null && backend.isConnected();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return menuItem;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Toggle the usage separate Z step size
        backend.getSettings().setShowABCStepSize(!backend.getSettings().showABCStepSize());
    }
}
