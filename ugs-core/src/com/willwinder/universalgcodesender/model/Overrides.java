/*
    Copyright 2016 Will Winder

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
 * @author wwinder
 */
public enum Overrides {
    /**
     * Restores feed override value to 100%.
     */
    CMD_FEED_OVR_RESET,
    CMD_FEED_OVR_COARSE_PLUS,
    CMD_FEED_OVR_COARSE_MINUS,
    CMD_FEED_OVR_FINE_PLUS,
    CMD_FEED_OVR_FINE_MINUS,
    /**
     * Restores rapid override value to 100%.
     */
    CMD_RAPID_OVR_RESET,
    CMD_RAPID_OVR_MEDIUM,
    CMD_RAPID_OVR_LOW,
    /**
     * Restores spindle override value to 100%.
     */
    CMD_SPINDLE_OVR_RESET,
    CMD_SPINDLE_OVR_COARSE_PLUS,
    CMD_SPINDLE_OVR_COARSE_MINUS,
    CMD_SPINDLE_OVR_FINE_PLUS,
    CMD_SPINDLE_OVR_FINE_MINUS,
    CMD_TOGGLE_SPINDLE,
    CMD_TOGGLE_FLOOD_COOLANT,
    CMD_TOGGLE_MIST_COOLANT;
}
