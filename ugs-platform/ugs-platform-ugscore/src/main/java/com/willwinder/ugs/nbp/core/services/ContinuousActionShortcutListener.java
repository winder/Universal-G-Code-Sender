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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.core.actions.ContinuousAction;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lib.services.ShortcutService;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.logging.Logger;

import static java.awt.event.KeyEvent.KEY_PRESSED;
import static java.awt.event.KeyEvent.KEY_RELEASED;

/**
 * A shortcut listener that will attempt to intercept shortcuts to continuous actions.
 * If key was pressed shorter than a certain threshold the normal
 * {@link Action#actionPerformed(ActionEvent)} will be executed. However, if the action was activated longer than
 * the threshold it will be considered a continuous activation which will first execute
 * {@link ContinuousAction#actionActivate()} and then {@link ContinuousAction#actionDeactivated()} when the key
 * is released.
 *
 * @author Joacim Breiler
 */
@OnStart
@ServiceProvider(service = ContinuousActionShortcutListener.class)
public class ContinuousActionShortcutListener implements Runnable, KeyEventDispatcher {
    private static final Logger LOGGER = Logger.getLogger(ContinuousActionShortcutListener.class.getSimpleName());
    private static final String PROPERTY_ACTIVE_WINDOW = "activeWindow";

    /**
     * How long should the shortcut be pressed before continuous
     * action is activated. Given in milliseconds
     */
    private static final int LONG_PRESS_DELAY = 500;

    private final ShortcutService shortcutService;
    private final ActionRegistrationService actionRegistrationService;
    private final ContinuousActionExecutor executor;

    public ContinuousActionShortcutListener() {
        shortcutService = Lookup.getDefault().lookup(ShortcutService.class);
        actionRegistrationService = Lookup.getDefault().lookup(ActionRegistrationService.class);
        executor = new ContinuousActionExecutor(LONG_PRESS_DELAY);
    }

    @Override
    public void run() {
        registerListener();
    }

    protected void registerListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(PROPERTY_ACTIVE_WINDOW, propertyChangeEvent -> {
            if (propertyChangeEvent.getNewValue() == null) {
                LOGGER.fine("Window lost focus, release all keys");
                executor.release();
            }
        });

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }

    private String getKeyAsString(KeyEvent e) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(e.getKeyCode(), e.getModifiersEx());
        return Utilities.keyToString(keyStroke, true);
    }

    private Optional<ActionReference> getContinuousActionByShortcut(String keyAsString) {
        return  shortcutService.getActionIdForShortcut(keyAsString)
                .flatMap(actionRegistrationService::getActionById)
                .filter(action -> action.getAction() instanceof ContinuousAction);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (SwingUtilities.getRoot(keyEvent.getComponent()) instanceof JDialog) {
            return false;
        }

        if (keyEvent.getID() != KEY_PRESSED && keyEvent.getID() != KEY_RELEASED) {
            return false;
        }

        // Try to find an continuous action for the current keys
        String keyAsString = getKeyAsString(keyEvent);
        Optional<ActionReference> actionById = getContinuousActionByShortcut(keyAsString);
        if (!actionById.isPresent()) {
            return false;
        }

        // We found a continuous action, consume the event to prevent
        // the ordinary shortcut handler to execute
        keyEvent.consume();

        // Execute the
        if (keyEvent.getID() == KEY_PRESSED && executor.getCurrentAction() == null) {
            executor.setCurrentAction(actionById.get());
            executor.keyPressed(keyEvent);
        } else if (keyEvent.getID() == KEY_RELEASED) {
            executor.keyReleased(keyEvent);
        }

        return false;
    }
}
