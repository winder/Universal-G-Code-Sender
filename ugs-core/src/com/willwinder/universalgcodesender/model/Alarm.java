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
package com.willwinder.universalgcodesender.model;

/**
 * A enum for describing alarms that can occur from the controllers
 *
 * @author Joacim Breiler
 */
public enum Alarm {

    /**
     * If the alarm is unknown
     */
    UNKONWN,

    /**
     * If a hard limit is triggered which means that the machine position is likely lost.
     * The controller needs to be reset and a re-homing is recommended
     */
    HARD_LIMIT,

    /**
     * A commanded move would have exceeded a configured soft (software) travel limit.
     */
    SOFT_LIMIT,

    /**
     * A feed hold was left unresolved (reset while paused, or reset without idling first).
     */
    ABORT_DURING_CYCLE,

    /**
     * A G38 probe cycle failed because the probe was already triggered before the move started.
     */
    PROBE_FAIL_INITIAL,

    /**
     * A G38 probe cycle failed because the probe never made contact during the move.
     */
    PROBE_FAIL_CONTACT,

    /**
     * Homing was interrupted by a reset.
     */
    HOMING_FAIL_RESET,

    /**
     * Homing failed because the safety door was opened during the homing cycle.
     */
    HOMING_FAIL_DOOR,

    /**
     * Homing failed because a limit switch stayed triggered after pulling off.
     */
    HOMING_FAIL_PULLOFF,

    /**
     * Homing failed because a limit switch never triggered during the approach.
     */
    HOMING_FAIL_APPROACH
}
