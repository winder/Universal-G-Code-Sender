/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.ugs.cli;

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.utils.ControllerSettings;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;

import java.util.Arrays;

/**
 * An enum with all option attributes that can be used in the configuration. {@link Configuration}
 *
 * @author Joacim Breiler
 */
public enum OptionEnum {

    HELP("help", "h", false, "", "Prints the help information."),
    VERSION("version", "v", false, "", "Prints the software version."),
    FILE("file", "f", true, "filename", "Opens a file for streaming to controller and will exit upon completion."),
    CONTROLLER_FIRMWARE("controller", "c", true, "controller", "What type of controller firmware we are connecting to, defaults to \"" + ControllerSettings.CONTROLLER.GRBL.name() + "\". These are the available firmwares: " + FirmwareUtils.getFirmwareList()),
    PORT("port", "p", true, "port", "Which port for the controller to connect to. I.e /dev/ttyUSB0 (on Unix-like systems or COM4 (on windows)."),
    BAUD("baud", "b", true, "baudrate", "Baud rate to connect with."),
    HOME("home", "ho", false, "", "If a homing process should be done before any gcode files are sent to the controller."),
    LIST_PORTS("list", "l", false, "", "Lists all available ports."),
    PRINT_STREAM("print-stream", "ps", false, "", "Prints the streamed lines to console"),
    PRINT_PROGRESSBAR("print-progressbar", "pp", false, "", "Prints the progress of the file stream"),
    RESET_ALARM("reset-alarm", "r", false, "", "Resets any alarm"),
    DAEMON("daemon", "d", false, "", "Starts in daemon mode providing a web pendant UI"),
    WORKSPACE("workspace", "w", true, "dir", "Sets and saves the workspace directory setting"),
    DRIVER("driver", "dr", true, "driver", "Sets and saves the connection driver setting. These are the available drivers: " + Arrays.toString(ConnectionDriver.values()));

    /**
     * The long option name that will be displayed like this: --file
     */
    private final String optionName;

    /**
     * The short name of the option, usually one character and will be available like this: -f
     */
    private final String optionShort;

    /**
     * If the option should contain an argument value as well.
     * This will display the argument name in help menu, ie: -f &lt;filename&gt;
     */
    private final boolean hasArgument;

    /**
     * The name of the argument value. For instance "filename" will be displayed as  -f &lt;filename&gt; using the help.
     */
    private final String argumentName;


    /**
     * The description of the option that will be displayed in the help text.
     */
    private final String description;

    /**
     * A constructor for creating an configuration option.
     *
     * @param optionName   the long name of the option and will be available like this: --file
     * @param optionShort  the short name of the option, usually one character, and will be available like this: -f
     * @param hasArgument  if set to true the argument name will also be used. In the help description it will be
     *                     available with lt and gt like this: --file &lt;filename&gt;
     * @param argumentName the name of the argument. In the help description it will be
     *                     available with lt and gt like this: --file &lt;filename&gt;
     * @param description  the description of the option that will be displayed in the help text.
     */
    OptionEnum(String optionName, String optionShort, boolean hasArgument, String argumentName, String description) {
        this.optionName = optionName;
        this.optionShort = optionShort;
        this.description = description;
        this.hasArgument = hasArgument;
        this.argumentName = argumentName;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasArgument() {
        return hasArgument;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public String getOptionName() {
        return optionName;
    }

    public String getOptionShort() {
        return optionShort;
    }
}
