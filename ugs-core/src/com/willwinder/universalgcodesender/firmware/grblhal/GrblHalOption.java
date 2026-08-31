/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.firmware.grblhal;

/**
 * Extended build options reported by grblHAL in the {@code [NEWOPT:...]} line of the build info
 * command ($I). These are additions to the options that GRBL reports in {@code [OPT:...]}.
 * <p>
 * The list is not exhaustive, unknown codes are still available through
 * {@link GrblHalOptions#isEnabled(String)}.
 *
 * @author Joacim Breiler
 */
public enum GrblHalOption {
    /**
     * The controller can enumerate its settings and setting groups using $ES and $EG
     */
    SETTINGS_ENUMERATION("ENUMS"),

    /**
     * The controller can enumerate its alarm and error codes using $EA and $EE
     */
    CODE_ENUMERATION("ERRS"),

    /**
     * The controller supports the extended set of real time commands
     */
    EXTENDED_REALTIME_COMMANDS("RT+"),

    /**
     * The controller supports single axis homing commands such as $HX
     */
    SINGLE_AXIS_HOMING("HOME"),

    /**
     * The controller has a tool change protocol enabled
     */
    TOOL_CHANGE("TC"),

    /**
     * The controller has a tool table
     */
    TOOL_TABLE("TT"),

    /**
     * The controller has an SD card or a similar file system attached
     */
    SD_CARD("SD"),

    /**
     * The controller is connected using ethernet
     */
    ETHERNET("ETH"),

    /**
     * The controller is connected using wifi
     */
    WIFI("WIFI"),

    /**
     * The controller is connected using bluetooth
     */
    BLUETOOTH("BT"),

    /**
     * The controller has a manual pulse generator (pendant) interface
     */
    MANUAL_PULSE_GENERATOR("MPG"),

    /**
     * The controller is configured as a lathe
     */
    LATHE("LATHE"),

    /**
     * The controller keeps odometer data
     */
    ODOMETERS("ODO"),

    /**
     * The controller can report descriptions for its settings using $SED
     */
    SETTINGS_DESCRIPTIONS("SED");

    private final String code;

    GrblHalOption(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
