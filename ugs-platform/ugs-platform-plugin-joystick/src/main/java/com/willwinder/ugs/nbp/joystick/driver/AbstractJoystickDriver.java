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
package com.willwinder.ugs.nbp.joystick.driver;

import com.willwinder.ugs.nbp.joystick.model.JoystickState;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract driver class with some base functionalities
 *
 * @author Joacim Breiler
 */
public abstract class AbstractJoystickDriver implements JoystickDriver {
    protected final JoystickState joystickState;
    private final Set<JoystickDriverListener> listeners;

    protected AbstractJoystickDriver() {
        listeners = ConcurrentHashMap.newKeySet();
        joystickState = new JoystickState();
    }

    @Override
    public final void addListener(JoystickDriverListener listener) {
        listeners.add(listener);
    }

    @Override
    public JoystickState getState() {
        return joystickState;
    }

    protected void notifyJoystickUpdated() {
        listeners.forEach(JoystickDriverListener::onJoystickUpdated);
    }

    protected void notifyDeviceChanged() {
        listeners.forEach(JoystickDriverListener::onDeviceChanged);
    }
}
