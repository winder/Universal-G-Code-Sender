/*
    Copyright 2016-2018 Will Winder

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

import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.utils.ControllerSettings;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.LogManager;

public class TerminalClient {

    private static final String SOFTWARE_NAME = "ugs-cli";
    private static final String OPTION_HELP = "help";
    private static final String OPTION_VERSION = "version";
    private static final String OPTION_FILE = "file";
    private static final String OPTION_CONTROLLER_FIRMWARE = "firmware";
    private static final String OPTION_PORT = "port";
    private static final String OPTION_BAUD = "baud";
    private static final String OPTION_HOME = "home";

    private Options options;
    private GUIBackend backend;
    private String firmware = "grbl";
    private String port = "/dev/ttsUSB0";
    private int baudRate = 9600;
    private String filename;
    private boolean running;

    private static String getVersion() {
        String version = "unknown";
        try {
            final Properties properties = new Properties();
            properties.load(TerminalClient.class.getResourceAsStream("/project.properties"));
            version = properties.getProperty("project.version", version);
        } catch (IOException e) {
            // Never mind...
        }
        return version;
    }

    public static void main(String[] args) throws IOException {
        LogManager.getLogManager().readConfiguration(TerminalClient.class.getResourceAsStream("/logging.properties"));


        TerminalClient terminalClient = new TerminalClient();

        terminalClient.initializeOptions();
        terminalClient.handleArguments(args);
        terminalClient.initializeBackend();

        terminalClient.runJob();
    }

    private void runJob() {
        try {
            if (StringUtils.isNotEmpty(filename)) {
                System.out.println("Running file \"" + filename + "\"");
                File file = new File(filename);
                backend.setGcodeFile(file);
                backend.addControllerListener(new ProcessedLinePrinter());
                if (!backend.canSend()) {
                    System.out.println("The controller is in a state where it isn't able to process the file: " + backend.getControlState());
                }

                if(!backend.isConnected() || !backend.isIdle() || !backend.getController().isReadyToReceiveCommands()) {
                    System.out.println("Connecting");
                }

                Thread.sleep(5000);
                /*ProgressBar pb = new ProgressBarBuilder()
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .setInitialMax(100)
                        .setTaskName(file.getName())
                        .setPrintStream(System.out)
                        .build();*/
                running = true;
                ThreadHelper.invokeLater(() -> {
                    try {
                        backend.send();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    running = false;
                });



                while (backend.isSendingFile() || running) {
                    Thread.sleep(50);
                }
                /*pb.maxHint(backend.getNumRows());

                while(backend.isSendingFile()) {
                    pb.stepTo(backend.getNumCompletedRows());
                }
                pb.stepTo(backend.getNumCompletedRows());
                System.out.println();*/

            }

            backend.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {
            // TODO This is a hack to exit threads, find out why threads aren't killed
            System.exit(0);
        }
    }

    private void initializeBackend() {
        try {
            backend = new GUIBackend();
            backend.applySettings(SettingsFactory.loadSettings());
            backend.getSettings().setFirmwareVersion(firmware);
            backend.connect(firmware, port, baudRate);

            System.out.println("Connected to \"" + firmware + "\" on " + port + " baud " + baudRate);
        } catch (Exception e) {
            System.err.println("Couldn't connect to controller with firmware \"" + firmware + "\" on " + port + " baud " + baudRate);

            if (StringUtils.isNotEmpty(e.getMessage())) {
                System.err.println(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
    }

    private void handleArguments(String[] args) {
        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.getOptions().length <= 0) {
                System.out.println("Use argument -h or --help for usage instructions");
                System.exit(0);
            }

            if (line.hasOption(OPTION_HELP)) {
                System.out.println(SOFTWARE_NAME + " " + getVersion());
                System.out.println();
                System.out.println("This is a terminal version of Universal Gcode Sender used for sending gcode files to controllers using command line.");
                System.out.println();

                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(SOFTWARE_NAME, options);
                System.exit(0);
            }

            if (line.hasOption(OPTION_PORT)) {
                port = line.getOptionValue(OPTION_PORT);
            }

            if (line.hasOption(OPTION_BAUD)) {
                baudRate = Integer.valueOf(line.getOptionValue(OPTION_BAUD));
            }

            if (line.hasOption(OPTION_VERSION)) {
                System.out.println(getVersion());
            }

            if (line.hasOption(OPTION_CONTROLLER_FIRMWARE)) {
                String firmware = line.getOptionValue(OPTION_CONTROLLER_FIRMWARE);
                try {
                    this.firmware = firmware;
                } catch (Exception e) {
                    System.err.println("No controller type was found with the name \"" + firmware + "\", available controllers are: " + FirmwareUtils.getFirmwareList());
                    System.exit(-1);
                }
            }

            if (line.hasOption(OPTION_FILE)) {
                filename = line.getOptionValue(OPTION_FILE);
                File file = new File(filename);
                if (!file.exists() || !file.isFile()) {
                    System.err.println("File does not exist: \"" + filename + "\"");
                    System.exit(-1);
                }
            }
        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
        }
    }

    private Options initializeOptions() {
        options = new Options();

        options.addOption(Option.builder("h")
                .longOpt(OPTION_HELP)
                .argName(OPTION_HELP)
                .hasArg(false)
                .optionalArg(true)
                .desc("Prints the help information.")
                .build());

        options.addOption(Option.builder("v")
                .longOpt(OPTION_VERSION)
                .argName(OPTION_VERSION)
                .hasArg(false)
                .optionalArg(true)
                .desc("Prints the software version.")
                .build());

        options.addOption(Option.builder("f")
                .longOpt(OPTION_FILE)
                .argName(OPTION_FILE)
                .hasArg(true)
                .optionalArg(true)
                .desc("Opens a file for streaming to controller and will exit upon completion.")
                .build());

        options.addOption(Option.builder("c")
                .longOpt(OPTION_CONTROLLER_FIRMWARE)
                .argName(OPTION_CONTROLLER_FIRMWARE)
                .hasArg(true)
                .type(ControllerSettings.CONTROLLER.class)
                .optionalArg(true)
                .desc("What type of controller firmware we are connecting to, defaults to \"" + ControllerSettings.CONTROLLER.GRBL.name() + "\". These are the available firmwares: " + FirmwareUtils.getFirmwareList())
                .build());

        options.addOption(Option.builder("p")
                .longOpt(OPTION_PORT)
                .argName(OPTION_PORT)
                .hasArg(true)
                .type(String.class)
                .optionalArg(true)
                .desc("Which port for the controller to connect to. I.e /dev/ttyUSB0 (on Unix-like systems or COM4 (on windows).")
                .build());

        options.addOption(Option.builder("b")
                .longOpt(OPTION_BAUD)
                .argName(OPTION_BAUD)
                .hasArg(true)
                .type(Integer.class)
                .optionalArg(true)
                .desc("Baud rate to connect with.")
                .build());

        options.addOption(Option.builder("ho")
                .longOpt(OPTION_HOME)
                .argName(OPTION_HOME)
                .hasArg(false)
                .type(Integer.class)
                .optionalArg(true)
                .desc("If a homing process should be done before any gcode files are sent to the controller.")
                .build());

        options.addOption(Option.builder("ho")
                .longOpt(OPTION_HOME)
                .argName(OPTION_HOME)
                .hasArg(false)
                .type(Integer.class)
                .optionalArg(true)
                .desc("If a homing process should be done before any gcode files are sent to the controller.")
                .build());

        return options;
    }
}
