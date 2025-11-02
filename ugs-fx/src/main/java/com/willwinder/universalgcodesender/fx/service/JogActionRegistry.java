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
import com.willwinder.universalgcodesender.fx.component.jog.JogAction;
import com.willwinder.universalgcodesender.fx.component.jog.JogButtonEnum;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class JogActionRegistry {
    private static JogActionRegistry instance;
    private final Map<JogButtonEnum, Action> actions = new EnumMap<>(JogButtonEnum.class);

    private JogActionRegistry() {}

    public static JogActionRegistry getInstance() {
        if (instance == null) {
            instance = new JogActionRegistry();
            instance.loadActions();
        }
        return instance;
    }

    public static void registerActions() {
        getInstance();
    }

    private void loadActions() {
        ActionRegistry actionRegistry = ActionRegistry.getInstance();
        Arrays.stream(JogButtonEnum.values()).forEach(jogButtonEnum -> {
            JogAction jogAction = new JogAction(jogButtonEnum);
            actionRegistry.registerAction(jogAction);
            actions.put(jogButtonEnum, jogAction);
        });
    }

    public Optional<Action> getAction(JogButtonEnum button) {
        return Optional.ofNullable(actions.get(button));
    }
}
