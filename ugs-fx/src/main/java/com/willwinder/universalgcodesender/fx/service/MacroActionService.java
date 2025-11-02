/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.MacroAction;
import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import javafx.collections.ListChangeListener;

import java.util.List;

/**
 * Registers macros as actions and keep them updated
 *
 * @author Joacim Breiler
 */
public class MacroActionService {
    public static void registerMacros() {
        MacroRegistry.getInstance().getMacros().addListener((ListChangeListener<MacroAdapter>) c -> {
            List<Action> allActionsOfClass = ActionRegistry.getInstance().getAllActionsOfClass(MacroAction.class);
            ActionRegistry.getInstance().unregisterActions(allActionsOfClass);
            MacroRegistry.getInstance().getMacros().forEach(macro -> ActionRegistry.getInstance().registerAction(new MacroAction(macro)));
        });

        MacroRegistry.getInstance().getMacros().forEach(macro -> ActionRegistry.getInstance().registerAction(new MacroAction(macro)));
    }
}
