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
    HARD_LIMIT
}
