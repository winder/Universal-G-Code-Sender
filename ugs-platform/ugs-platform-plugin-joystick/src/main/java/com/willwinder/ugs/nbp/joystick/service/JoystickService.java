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
package com.willwinder.ugs.nbp.joystick.service;

import com.willwinder.ugs.nbp.joystick.action.ActionManager;

/**
 * A joystick service responsible for initializing and reading joystick data and
 * notify listeners.
 *
 * @author Joacim Breiler
 */
public interface JoystickService {

    void setActivateActionDispatcher(boolean usingActionDispatcher);

    /**
     * Starts the service by initializing the gamepad/joystick and starts to listen to
     * controller data. Any state change will be notified to registered listeners.
     */
    void initialize();

    /**
     * Stops the service and releases any gamepad/joystick.
     */
    void destroy();

    /**
     * Adds a listener for any joystick state changes
     *
     * @param listener a listener
     */
    void addListener(JoystickServiceListener listener);

    /**
     * Removes a listener for any joystick state changes
     *
     * @param listener a registered listener
     */
    void removeListener(JoystickServiceListener listener);

    /**
     * Removes all listeners
     */
    void removeAllListeners();

    /**
     * Returns the action manager with all actions that can be used for
     * mapping actions to joystick controls.
     *
     * @return the action manager
     */
    ActionManager getActionManager();
}
