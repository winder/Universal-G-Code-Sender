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
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;

/**
 * Common utils to be used for the Joystick implementation
 *
 * @author Joacim Breiler
 */
public class Utils {
    public static final String ACTION_Z_DOWN = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.zMinus.instance";
    public static final String ACTION_Z_UP = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.zPlus.instance" ;
    public static final String ACTION_JOG_Z = "continuousJogZAction";
    public static final String ACTION_JOG_X = "continuousJogXAction";
    public static final String ACTION_JOG_Y = "continuousJogYAction";
    public static final String ACTION_DIVIDE_FEED = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.JogSizeAction.divide.feed.instance";
    public static final String ACTION_MULTIPLY_FEED = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.JogSizeAction.multiply.feed.instance";
    public static final String ACTION_START = "Actions/Machine/com-willwinder-ugs-nbp-core-actions-StartAction.instance";
    public static final String ACTION_JOG_Y_PLUS = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.yPlus.instance";
    public static final String ACTION_JOG_X_PLUS = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.xPlus.instance";
    public static final String ACTION_JOG_X_MINUS = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.xMinus.instance";
    public static final String ACTION_JOG_Y_MINUS = "Actions/Machine/com.willwinder.ugs.nbp.core.services.JogActionService.yMinus.instance";
    public static final String ACTION_STOP = "Actions/Machine/com-willwinder-ugs-nbp-core-actions-StopAction.instance";

    /**
     * Translates from Jamepad controller button to a JoystickButton.
     *
     * @param controllerButton the controller button to translate from
     * @return a joystick button if it could be translated, or throws an illegalargumentexception
     */
    public static JoystickControl getJoystickButtonFromControllerButton(ControllerButton controllerButton) {
        switch (controllerButton) {
            case A:
                return JoystickControl.A;
            case B:
                return JoystickControl.B;
            case X:
                return JoystickControl.X;
            case Y:
                return JoystickControl.Y;
            case BACK:
                return JoystickControl.BACK;
            case GUIDE:
                return JoystickControl.SELECT;
            case START:
                return JoystickControl.START;
            case LEFTSTICK:
                return JoystickControl.L3;
            case RIGHTSTICK:
                return JoystickControl.R3;
            case LEFTBUMPER:
                return JoystickControl.L1;
            case RIGHTBUMPER:
                return JoystickControl.R1;
            case DPAD_UP:
                return JoystickControl.DPAD_UP;
            case DPAD_DOWN:
                return JoystickControl.DPAD_DOWN;
            case DPAD_LEFT:
                return JoystickControl.DPAD_LEFT;
            case DPAD_RIGHT:
                return JoystickControl.DPAD_RIGHT;
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
    public static JoystickControl getJoystickAxisFromControllerAxis(ControllerAxis controllerAxis) {
        switch (controllerAxis) {
            case LEFTX:
                return JoystickControl.LEFT_X;
            case LEFTY:
                return JoystickControl.LEFT_Y;
            case RIGHTX:
                return JoystickControl.RIGHT_X;
            case RIGHTY:
                return JoystickControl.RIGHT_Y;
            case TRIGGERLEFT:
                return JoystickControl.L2;
            case TRIGGERRIGHT:
                return JoystickControl.R2;
            default:
                throw new IllegalArgumentException("Unknown axis type: " + controllerAxis);
        }
    }
}
