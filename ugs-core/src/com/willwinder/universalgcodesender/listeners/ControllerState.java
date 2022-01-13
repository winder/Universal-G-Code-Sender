/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.listeners;

/**
 * The different states of a controller.
 *
 * @author Joacim Breiler
 */
public enum ControllerState {
    /**
     * When a serious error has occurred on the controller. This occurs when a limit switch
     * has been triggered or if an operation would make the machine travel beyond soft limits.
     * This types of errors often requires a controller reset or maybe unlocked
     */
    ALARM,

    /**
     * When the machine controller is paused.
     */
    HOLD,

    /**
     * When the machine door is open
     */
    DOOR,

    /**
     * When the machine controller is processing commands
     */
    RUN,

    /**
     * When the machine controller is jogging
     */
    JOG,

    /**
     * When the machine controller is in a check mode to parse commands without moving
     * the machine.
     */
    CHECK,

    /**
     * When the machine controller is in idle mode.
     */
    IDLE,

    /**
     * When the machine controller is performing a homing sequence
     */
    HOME,

    /**
     * When the machine is in sleep mode
     */
    SLEEP,

    /**
     * When not connected to the controller
     */
    DISCONNECTED,

    /**
     * When the machine is in an unknown state
     */
    UNKNOWN
}