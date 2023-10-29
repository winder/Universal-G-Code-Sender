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

    private Utils() {}

    /**
     * Translates from Jamepad controller button to a JoystickButton.
     *
     * @param controllerButton the controller button to translate from
     * @return a joystick button if it could be translated, or throws an illegalargumentexception
     */
    public static JoystickControl getJoystickButtonFromControllerButton(ControllerButton controllerButton) {
        return switch (controllerButton) {
            case A -> JoystickControl.A;
            case B -> JoystickControl.B;
            case X -> JoystickControl.X;
            case Y -> JoystickControl.Y;
            case BACK -> JoystickControl.BACK;
            case GUIDE -> JoystickControl.SELECT;
            case START -> JoystickControl.START;
            case LEFTSTICK -> JoystickControl.L3;
            case RIGHTSTICK -> JoystickControl.R3;
            case LEFTBUMPER -> JoystickControl.L1;
            case RIGHTBUMPER -> JoystickControl.R1;
            case DPAD_UP -> JoystickControl.DPAD_UP;
            case DPAD_DOWN -> JoystickControl.DPAD_DOWN;
            case DPAD_LEFT -> JoystickControl.DPAD_LEFT;
            case DPAD_RIGHT -> JoystickControl.DPAD_RIGHT;
            case BUTTON_MISC1 -> JoystickControl.MISC1;
            case BUTTON_PADDLE1 -> JoystickControl.PADDLE1;
            case BUTTON_PADDLE2 -> JoystickControl.PADDLE2;
            case BUTTON_PADDLE3 -> JoystickControl.PADDLE3;
            case BUTTON_PADDLE4 -> JoystickControl.PADDLE4;
            case BUTTON_TOUCHPAD -> JoystickControl.BUTTON_TOUCHPAD;
        };
    }

    /**
     * Translates from Jamepad controller axis to a JoystickAxis.
     *
     * @param controllerAxis the controller axis to translate from
     * @return a joystick axis if it could be translated, or throws an illegalargumentexception
     */
    public static JoystickControl getJoystickAxisFromControllerAxis(ControllerAxis controllerAxis) {
        return switch (controllerAxis) {
            case LEFTX -> JoystickControl.LEFT_X;
            case LEFTY -> JoystickControl.LEFT_Y;
            case RIGHTX -> JoystickControl.RIGHT_X;
            case RIGHTY -> JoystickControl.RIGHT_Y;
            case TRIGGERLEFT -> JoystickControl.L2;
            case TRIGGERRIGHT -> JoystickControl.R2;
        };
    }
}
