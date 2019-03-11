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

import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * The configuration works as a wrapper for the command line arguments handled by apache commons.
 * It will build all commands based on the enum values in {@link OptionEnum}.
 *
 * @author Joacim Breiler
 */
public class Configuration {

    /**
     * The available options for the program
     */
    private Options options;

    /**
     * Contains the parsed options from the command line arguments
     */
    private CommandLine commandLine;

    /**
     * Default constructor
     * <p>
     * Will attempt to create all options based on the values in {@link OptionEnum}.
     */
    public Configuration() {
        options = new Options();

        // Create all options
        Arrays.stream(OptionEnum.values()).forEach(o ->
                options.addOption(Option.builder(o.getOptionShort())
                        .longOpt(o.getOptionName())
                        .hasArg(o.hasArgument())
                        .argName(o.getArgumentName())
                        .optionalArg(true)
                        .desc(o.getDescription())
                        .build()));
    }

    /**
     * Parses the command line arguments and makes them available using
     * {@link Configuration#hasOption(OptionEnum)} and {@link Configuration#getOptionValue(OptionEnum)}.
     *
     * @param arguments command line arguments
     */
    public void parse(String[] arguments) {
        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, arguments);
        } catch (ParseException e) {
            throw new RuntimeException("Couldn't parse the command commandLine arguments", e);
        }
    }

    /**
     * Returns true if the given option enum was entered as a command line argument
     *
     * @param option the option to check for
     * @return true if it was given
     */
    public boolean hasOption(OptionEnum option) {
        return commandLine.hasOption(option.getOptionName());
    }

    /**
     * Retuns the options extra argument value if present
     *
     * @param option the option value to fetch
     * @return the value as string if found, otherwise null is returned
     */
    public String getOptionValue(OptionEnum option) {
        return commandLine.getOptionValue(option.getOptionName());
    }

    /**
     * Return all configured options
     *
     * @return all options
     */
    public Options getOptions() {
        return options;
    }
}
