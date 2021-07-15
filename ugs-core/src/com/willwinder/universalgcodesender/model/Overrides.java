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
 *
 * @author wwinder
 */
public enum Overrides {
    //CMD_DEBUG_REPORT, // 0x85 // Only when DEBUG enabled, sends debug report in '{}' braces.
    CMD_FEED_OVR_RESET, // 0x90         // Restores feed override value to 100%.
    CMD_FEED_OVR_COARSE_PLUS, // 0x91
    CMD_FEED_OVR_COARSE_MINUS, // 0x92
    CMD_FEED_OVR_FINE_PLUS , // 0x93
    CMD_FEED_OVR_FINE_MINUS , // 0x94
    CMD_RAPID_OVR_RESET, // 0x95        // Restores rapid override value to 100%.
    CMD_RAPID_OVR_MEDIUM, // 0x96
    CMD_RAPID_OVR_LOW, // 0x97
    CMD_SPINDLE_OVR_RESET, // 0x99      // Restores spindle override value to 100%.
    CMD_SPINDLE_OVR_COARSE_PLUS, // 0x9A
    CMD_SPINDLE_OVR_COARSE_MINUS, // 0x9B
    CMD_SPINDLE_OVR_FINE_PLUS, // 0x9C
    CMD_SPINDLE_OVR_FINE_MINUS, // 0x9D
    CMD_TOGGLE_SPINDLE, // 0x9E
    CMD_TOGGLE_FLOOD_COOLANT, // 0xA0
    CMD_TOGGLE_MIST_COOLANT, // 0xA1
}
