package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbp.core.actions.OutlineAction;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * An action manager that stores a custom list of actions that will be added
 * in the popup menu of the visualizer. Simply add your actions in the initialize method
 * or using the register method.
 */
public class VisualizerPopupActionsManager {
    private static final List<Action> ACTION_LIST = new ArrayList<>();

    private VisualizerPopupActionsManager() {
    }

    public static List<Action> getActionList() {
        if (ACTION_LIST.isEmpty()) {
            initialize();
        }

        return Collections.unmodifiableList(ACTION_LIST);
    }

    /**
     * Initializes the action list with default actions
     */
    private static void initialize() {
        ACTION_LIST.add(new OutlineAction());
    }

    /**
     * Registers and adds an action to the manager. If an action with the same class already exists it will not be added.
     *
     * @param action the action to add to the manager.
     */
    public static void register(Action action) {
        Optional<Action> existingAction = ACTION_LIST.stream()
                .filter(a -> a.getClass().equals(action.getClass()))
                .findAny();

        if (!existingAction.isPresent()) {
            ACTION_LIST.add(action);
        }
    }
}
