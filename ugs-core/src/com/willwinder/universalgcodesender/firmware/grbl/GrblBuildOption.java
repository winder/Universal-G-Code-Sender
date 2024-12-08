/*
    Copyright 2024 Will Winder

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
package com.willwinder.universalgcodesender.firmware.grbl;

/**
 * GRBL options are reported by the build info command ($I). This enum contains the known options.
 */
public enum GrblBuildOption {
    VARIABLE_SPINDLE_ENABLED("V"),
    LINE_NUMBERS_ENABLED("N"),
    MIST_COOLANT_ENABLED("M"),
    CORE_XY_ENABLED("C"),
    PARKING_MOTION_ENABLED("P"),
    HOMING_FORCE_ORIGIN_ENABLED("Z"),
    HOMING_SINGLE_AXIS_COMMAND_ENABLED("H"),
    TWO_LIMIT_SWITCHES_ON_AXIS_ENABLED("T"),
    TWO_ALLOW_OVERRIDE_ON_PROBING_ENABLED("A"),
    USE_SPINDLE_DIRECTION_AS_ENABLE_PIN_ENABLED("D"),
    SPINDLE_OFF_ON_ZERO_SPEED_ENABLED("0"),
    SOFTWARE_LIMIT_PIN_DEBOUNCING_ENABLED("S"),
    PARKING_OVERRIDE_CONTROL_ENABLED("R"),
    SAFETY_DOOR_INPUT_ENABLED("+"),
    RESTORE_ALL_EEPROM_DISABLED("*"),
    RESTORE_EEPROM_SETTINGS_DISABLED("$"),
    RESTORE_EEPROM_PARAMETER_DATA_DISABLED("#"),
    BUILD_INFO_USER_STRING_DISABLED("I"),
    FORCE_SYNC_ON_EEPROM_WRITE_DISABLED("E"),
    FORCE_SYNC_ON_WORK_COORDINATE_CHANGE_DISABLED("W"),
    HOMING_INITIALIZATION_LOCK_DISABLED("L"),
    DUAL_AXIS_MOTORS_ENABLED("2");

    private final String code;

    GrblBuildOption(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
