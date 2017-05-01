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
import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.*;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
                position = 1000)
})
public class MacrosListAction extends AbstractAction implements DynamicMenuContent, Presenter.Popup, Presenter.Menu {

    public static final String ICON_BASE = "icons/macros.png";
    private final BackendAPI backend;
    private static final Logger logger = Logger.getLogger(MacrosListAction.class.getCanonicalName());

    public MacrosListAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.MacrosTitle);
        putValue(NAME, LocalizingService.MacrosTitle);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
        Action action = Actions.forID(LocalizingService.MacrosCategory, LocalizingService.MacrosActionId);
        if(action != null) {
            action.actionPerformed(e);
        }
    }

    @Override
    public JComponent[] getMenuPresenters() {
        return createMenu();
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return createMenu();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu(this);
        menu.setText(LocalizingService.MacrosTitle);
        menu.setIcon(ImageUtilities.loadImageIcon(ICON_BASE, false));

        JComponent[] menuItems = createMenu();
        Arrays.stream(menuItems)
                .forEach(menu::add);
        return menu;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private JComponent[] createMenu() {
        Collection<Macro> macros = backend.getSettings().getMacros();
        List<JComponent> actions = macros.stream()
                .map(this::makeMenuItem)
                .collect(Collectors.toList());

        actions.add(new JSeparator());
        JMenuItem openSettingsAction = new JMenuItem(Localization.getString("platform.menu.macros.edit"));
        openSettingsAction.addActionListener(l -> OptionsDisplayer.getDefault().open("UGS/macros"));
        actions.add(openSettingsAction);

        return actions.toArray(new JComponent[actions.size()]);
    }

    private JComponent makeMenuItem(Macro macro) {
        JMenuItem item = new JMenuItem();
        Actions.connect(item, new MacroAction(backend, macro), false);
        return item;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    private class MacroAction extends AbstractAction {
        private BackendAPI backend;
        private Macro macro;

        public MacroAction(BackendAPI backend, Macro macro) {
            this.backend = backend;
            this.macro = macro;
            putValue(NAME, macro.getName());
            putValue("menuText", macro.getName());
            putValue("displayName", macro.getName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (macro != null && macro.getGcode() != null) {
                    MacroHelper.executeCustomGcode(macro.getGcode(), backend);
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
