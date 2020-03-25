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
package com.willwinder.ugs.nbp.joystick;

import com.studiohartman.jamepad.ControllerAxis;
import com.studiohartman.jamepad.ControllerButton;
import com.willwinder.ugs.nbp.joystick.model.JoystickAxis;
import com.willwinder.ugs.nbp.joystick.model.JoystickButton;

/**
 * Common utils to be used for the Joystick implementation
 *
 * @author Joacim Breiler
 */
public class Utils {

    /**
     * Translates from Jamepad controller button to a JoystickButton.
     *
     * @param controllerButton the controller button to translate from
     * @return a joystick button if it could be translated, or throws an illegalargumentexception
     */
    public static JoystickButton getJoystickButtonFromControllerButton(ControllerButton controllerButton) {
        switch (controllerButton) {
            case A:
                return JoystickButton.A;
            case B:
                return JoystickButton.B;
            case X:
                return JoystickButton.X;
            case Y:
                return JoystickButton.Y;
            case BACK:
                return JoystickButton.BACK;
            case GUIDE:
                return JoystickButton.GUIDE;
            case START:
                return JoystickButton.START;
            case LEFTSTICK:
                return JoystickButton.LEFT_STICK;
            case RIGHTSTICK:
                return JoystickButton.RIGHT_STICK;
            case LEFTBUMPER:
                return JoystickButton.LEFT_BUMPER;
            case RIGHTBUMPER:
                return JoystickButton.RIGHT_BUMPER;
            case DPAD_UP:
                return JoystickButton.DPAD_UP;
            case DPAD_DOWN:
                return JoystickButton.DPAD_DOWN;
            case DPAD_LEFT:
                return JoystickButton.DPAD_LEFT;
            case DPAD_RIGHT:
                return JoystickButton.DPAD_RIGHT;
            default:
                throw new IllegalArgumentException("Unknown button type: " + controllerButton);
        }
    }

    /**
     * Translates from Jamepad controller axis to a JoystickAxis.
     *
     * @param controllerAxis the controller axis to translate from
     * @return a joystick axis if it could be translated, or throws an illegalargumentexception
     */
    public static JoystickAxis getJoystickAxisFromControllerAxis(ControllerAxis controllerAxis) {
        switch (controllerAxis) {
            case LEFTX:
                return JoystickAxis.LEFT_X;
            case LEFTY:
                return JoystickAxis.LEFT_Y;
            case RIGHTX:
                return JoystickAxis.RIGHT_X;
            case RIGHTY:
                return JoystickAxis.RIGHT_Y;
            case TRIGGERLEFT:
                return JoystickAxis.TRIGGER_LEFT;
            case TRIGGERRIGHT:
                return JoystickAxis.TRIGGER_RIGHT;
            default:
                throw new IllegalArgumentException("Unknown axis type: " + controllerAxis);
        }
    }
}
