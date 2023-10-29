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
package com.willwinder.ugs.nbp.joystick.ui;

import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickDevice;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.ugs.nbp.joystick.service.JoystickServiceListener;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;

/**
 * A status line for displaying a connected device and any input status
 *
 * @author Joacim Breiler
 */
public class JoystickStatusLine extends JLabel implements JoystickServiceListener {
    private final transient JoystickService joystickService;
    private final ImageIcon joystickIcon;
    private final ImageIcon joystickIconPressed;

    public JoystickStatusLine(JoystickService joystickService) {
        this.joystickService = joystickService;
        this.joystickService.addListener(this);
        this.joystickIcon = ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/joystick/gamepad.svg", false);
        this.joystickIconPressed = ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/joystick/gamepad-pressed.svg", false);
        onControllerChanged();
    }

    @Override
    public void onUpdate(JoystickState state) {
        Optional<JoystickDevice> currentDevice = joystickService.getCurrentDevice();
        if (currentDevice.isEmpty()) {
            setVisible(false);
            return;
        }

        setVisible(true);
        setToolTipText("Connected to " + currentDevice.get().name());
        boolean isPressed = Arrays.stream(JoystickControl.values())
                .anyMatch(control -> state.getButton(control) || Math.abs(state.getAxis(control)) > Settings.getAxisThreshold());
        if (isPressed) {
            setIcon(joystickIconPressed);
        } else {
            setIcon(joystickIcon);
        }
    }

    @Override
    public void onControllerChanged() {
        onUpdate(new JoystickState());
    }
}