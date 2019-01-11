package com.willwinder.ugs.cli;

import org.apache.commons.cli.*;

import java.util.Arrays;

/**
 * The configuration works as a wrapper for the command line arguments handled by apache commons.
 * It will build all commands based on the enum values in {@link OptionEnum}.
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
     * Will attempt top create all options based on the values in {@link OptionEnum}.
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
