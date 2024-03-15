/*
    Copyright 2023-2024 Will Winder

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
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.util.ImageUtilities;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
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
    private final boolean showDetails;
    private final ImageIcon joystickIconDisconnected;

    public JoystickStatusLine(JoystickService joystickService) {
        this(joystickService, false);
    }

    public JoystickStatusLine(JoystickService joystickService, boolean showDetails) {
        this.showDetails = showDetails;
        this.joystickService = joystickService;
        this.joystickService.addListener(this);
        this.joystickIcon = ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/joystick/gamepad.svg", false);
        this.joystickIconPressed = ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/joystick/gamepad-pressed.svg", false);
        this.joystickIconDisconnected = ImageUtilities.loadImageIcon("com/willwinder/ugs/nbp/joystick/gamepad-disconnected.svg", false);
        onControllerChanged();
    }

    @Override
    public void onUpdate(JoystickState state) {
        setVisible(Settings.isActive());

        Optional<JoystickDevice> currentDevice = joystickService.getCurrentDevice();
        if (currentDevice.isEmpty()) {
            setDisconnected();
            return;
        }

        setConnected(state, currentDevice.get());
    }

    private void setConnected(JoystickState state, JoystickDevice currentDevice) {
        String text = Localization.getString("platform.plugin.joystick.connectedTo") + " " + currentDevice.name();
        setToolTipText(text);

        if (showDetails) {
            setText(text);
        }

        boolean isPressed = Arrays.stream(JoystickControl.values())
                .anyMatch(control -> state.getButton(control) || Math.abs(state.getAxis(control)) > Settings.getAxisThreshold());
        if (isPressed) {
            setIcon(joystickIconPressed);
        } else {
            setIcon(joystickIcon);
        }
    }

    private void setDisconnected() {
        String text = Localization.getString("platform.plugin.joystick.notConnected");
        if (showDetails) {
            setText(text);
        }
        setToolTipText(text);
        setIcon(joystickIconDisconnected);
    }

    @Override
    public void onControllerChanged() {
        onUpdate(new JoystickState());
    }
}