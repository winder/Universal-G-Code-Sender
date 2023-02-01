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
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@ActionID(
        id = "com.willwinder.ugs.nbp.core.actions.OpenRecentAction",
        category = LocalizingService.OpenCategory)
@ActionRegistration(
        iconBase = OpenAction.ICON_BASE,
        lazy = false,
        displayName = "Open recent...")
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_FILE,
                position = 11),
})
public class OpenRecentAction extends AbstractAction implements DynamicMenuContent {

    public OpenRecentAction() {
        putValue(NAME, LocalizingService.OpenRecentTitle);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // does nothing, this is a popup menu
    }

    @Override
    public JComponent[] getMenuPresenters() {
        JMenu submenu = new JMenu(LocalizingService.OpenRecentTitle);
        createMenu().forEach(submenu::add);
        return new JComponent[]{submenu};
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return getMenuPresenters();
    }

    private List<JComponent> createMenu() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        return backend.getSettings().getRecentFiles().stream()
                .map(file -> Paths.get(file).toFile())
                .filter(File::exists)
                .filter(File::isFile)
                .map(file -> new JMenuItem(new OpenFileAction(file)))
                .collect(Collectors.toList());
    }
}
