package com.willwinder.ugs.nbp.core.actions;

import javax.swing.Action;

/**
 * This action will make it possible to activate/deactivate. This will make it possible
 * to create an action that can continuously execute as opposed to a standard action
 * that is only fired once.
 * <p>
 * This class provides such methods and can be used together with the
 * {@link com.willwinder.ugs.nbp.core.services.ContinuousActionShortcutListener}.
 */
public interface ContinuousAction extends Action {

    /**
     * Starts the execution of the action.
     */
    void actionActivate();

    /**
     * Stops the execution of the action.
     */
    void actionDeactivated();
}
