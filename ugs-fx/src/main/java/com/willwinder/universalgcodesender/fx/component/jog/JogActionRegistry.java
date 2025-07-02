package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class JogActionRegistry {
    private static JogActionRegistry instance;
    Map<JogButtonEnum, Action> actions = new EnumMap<>(JogButtonEnum.class);

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
