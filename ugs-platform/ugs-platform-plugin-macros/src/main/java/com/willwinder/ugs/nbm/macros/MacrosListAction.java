/*
    Copywrite 2015-2016 Will Winder

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

package com.willwinder.ugs.nbm.macros;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.Macro;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.*;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * An action for registering macro actions as menu items
 *
 * @author Joacim Breiler
 */
@ActionID(
        id = "MacrosListAction",
        category = "Machine")
@ActionRegistration(
        lazy = false,
        iconBase = MacrosListAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.MacrosTitleKey
)
@ActionReferences({
        @ActionReference(
                path = "Menu/Machine",
                position = 800)
})
public class MacrosListAction extends AbstractAction implements DynamicMenuContent, UGSEventListener {

    public static final String ICON_BASE = "icons/macros.png";
    private final BackendAPI backend;
    private JMenu menu;

    public MacrosListAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.MacrosTitle);
        putValue(NAME, LocalizingService.MacrosTitle);

        reloadMenu();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
    }

    @Override
    public JComponent[] getMenuPresenters() {
        return new JComponent[]{menu};
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return new JComponent[]{menu};
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private void reloadMenu() {
        menu = new JMenu(this);
        menu.setText(LocalizingService.MacrosTitle);
        menu.setIcon(ImageUtilities.loadImageIcon(ICON_BASE, false));

        Collection<Macro> macros = backend.getSettings().getMacros();
        macros.stream()
                .map(this::makeMenuItem)
                .forEach(menu::add);

        menu.add(new JSeparator());

        JMenuItem openSettingsAction = new JMenuItem(Localization.getString("platform.menu.macros.edit"));
        openSettingsAction.addActionListener(l -> OptionsDisplayer.getDefault().open("UGS/macros"));
        menu.add(openSettingsAction);
    }

    private JComponent makeMenuItem(Macro macro) {
        JMenuItem item = new JMenuItem();
        Actions.connect(item, new MacroAction(backend, macro), false);
        return item;
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event.isSettingChangeEvent()) {
            reloadMenu();
        }
    }
}
