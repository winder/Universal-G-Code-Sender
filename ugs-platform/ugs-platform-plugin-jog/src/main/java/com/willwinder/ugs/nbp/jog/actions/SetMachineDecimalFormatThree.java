/*
 * Copyright (C) 2025 Damian Nikodem
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.jog.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import static javax.swing.Action.NAME;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * An action to set the number of Decimal Places in the backend.
 *
 * @author Damian Nikodem
 */
@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.nbp.jog.actions.SetMachineDecimalFormatThree")
@ActionRegistration(
        displayName = "resources.MessagesBundle#platform.plugin.jog.setMachineDecimalFormatThree",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE_JOG_DECIMAL_PLACES_3,
                position = 10000,
                separatorAfter = 10001)
})
@ServiceProvider(service = SetMachineDecimalFormatThree.class)
public class SetMachineDecimalFormatThree extends AbstractAction implements Presenter.Menu {
    private final String DECIMAL_FORMAT_THREE = "0.000";
    private final BackendAPI backend;
    private final JCheckBoxMenuItem menuItem;

    public SetMachineDecimalFormatThree() {
        String title = Localization.getString("platform.plugin.jog.setMachineDecimalFormatThree");
        putValue(NAME, title);

        menuItem = new JCheckBoxMenuItem(title);
        menuItem.setAction(this);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onBackendEvent);
        setEnabled(isEnabled());
        onBackendEvent( new SettingChangedEvent()) ;
    }

    private void onBackendEvent(UGSEvent event) {
        if (event instanceof SettingChangedEvent) {
            menuItem.setSelected(backend.getSettings().getMachineDecimalFormat().equals(DECIMAL_FORMAT_THREE));            
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
        backend.getSettings().setMachineDecimalFormat(DECIMAL_FORMAT_THREE);
        backend.getSettings().changed();        
    }
}
