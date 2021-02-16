/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.joystick.ui;

import com.willwinder.ugs.nbp.joystick.action.ActionManager;
import com.willwinder.ugs.nbp.joystick.action.AnalogAction;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.JScrollMenu;
import com.willwinder.universalgcodesender.uielements.JScrollPopupMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A button that can bind a joystick button to an action using a popup menu.
 *
 * @author Joacim Breiler
 */
public class BindActionButton extends JButton {
    private static final List<String> EXCLUDED_CATEGORIES = Arrays.asList(
            "Actions/Source",
            "Actions/View",
            "Actions/Window",
            "Actions/File",
            "Actions/Refactoring",
            "Actions/Tools",
            "Actions/Edit",
            "Actions/Window/SelectDocumentNode",
            "Actions/Profile",
            "Actions/Diff",
            "Actions/Help",
            "Actions/System");

    private final JPopupMenu popupMenu;
    private final JoystickService joystickService;

    public BindActionButton(JoystickService joystickService, JoystickControl joystickControl) {
        this.joystickService = joystickService;
        this.popupMenu = createPopupMenu(joystickControl);

        addActionListener(this::showPopupMenu);

        joystickService.getActionManager().getMappedAction(joystickControl)
                .ifPresent(actionReference -> setText(actionReference.getName()));
    }

    private void showPopupMenu(ActionEvent actionEvent) {
        Component b = (Component) actionEvent.getSource();
        Point p = b.getLocationOnScreen();
        popupMenu.show(this, 0, 0);
        popupMenu.setLocation(p.x, p.y + b.getHeight());
    }

    private JPopupMenu createPopupMenu(JoystickControl joystickControl) {
        ActionManager actionManager = joystickService.getActionManager();
        JPopupMenu popupMenu = new JScrollPopupMenu();

        popupMenu.add(createClearActionMenuItem(joystickControl, actionManager));
        popupMenu.add(new JSeparator());
        actionManager.getCategories().stream()
                .filter(category -> !EXCLUDED_CATEGORIES.contains(category)) // Remove categories
                .map(category -> createActionsCategoryMenu(joystickControl, category)) // Map to a category menu item
                .flatMap(optionalMenu -> optionalMenu.map(Stream::of).orElseGet(Stream::empty)) // Remove empty optionals
                .forEach(popupMenu::add); // Add to popup menu

        return popupMenu;
    }

    private JMenuItem createClearActionMenuItem(JoystickControl joystickControl, ActionManager actionManager) {
        JMenuItem clear = new JMenuItem(Localization.getString("platform.plugin.joystick.clear"));
        clear.addActionListener((item) -> {
            setText("");
            actionManager.clearMappedAction(joystickControl);
        });
        return clear;
    }

    private Optional<JMenu> createActionsCategoryMenu(JoystickControl joystickControl, String category) {
        JMenu categoryMenuItem = new JScrollMenu(category);
        joystickService.getActionManager()
                .getActionsByCategory(category).stream()
                .filter(actionReference -> !(actionReference.getAction() instanceof AnalogAction && !joystickControl.isAnalog())) // Analog actions doesn't work with digital buttons, do not add those actions
                .forEach(actionReference -> categoryMenuItem.add(createSelectActionMenuItem(joystickControl, actionReference)));

        if (categoryMenuItem.getItemCount() > 0) {
            return Optional.of(categoryMenuItem);
        }
        return Optional.empty();
    }

    private JMenuItem createSelectActionMenuItem(JoystickControl joystickControl, ActionReference actionReference) {
        JMenuItem jMenuItem = new JMenuItem(actionReference.getName());
        jMenuItem.addActionListener((item) -> {
            setText(actionReference.getName());
            joystickService.getActionManager().setMappedAction(joystickControl, actionReference);
        });
        return jMenuItem;
    }
}
