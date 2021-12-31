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
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;

/**
 * A terminal client implementation of UGS. Simply run this class with the argument -h to display run options
 *
 * @author Joacim Breiler
 */
public class TerminalClient {

    private static final long WAIT_DURATION = 1000L;
    private static final String SOFTWARE_NAME = "ugs-cli";
    private static final String SOFTWARE_DESCRIPTION = "This is a terminal version of Universal Gcode Sender used for sending gcode files to controllers using command line.";
    private final Configuration configuration;
    private BackendAPI backend;
    private PendantUI pendantUI;

    public static void main(String[] args) throws IOException {
        // Load our custom log properties preventing application to log to console
        LogManager.getLogManager().readConfiguration(TerminalClient.class.getResourceAsStream("/logging.properties"));

        Configuration configuration = new Configuration();
        configuration.parse(args);

        TerminalClient terminalClient = new TerminalClient(configuration);
        terminalClient.runJob();
    }

    /**
     * Constructor for creating a terminal client. Given the configuration object different steps in the
     * program can be switched on or off.
     *
     * @param configuration the configuration for how the job should be run.
     */
    public TerminalClient(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * Runs the job based on the options in the configuration
     */
    public void runJob() {
        try {
            if (configuration.hasOption(OptionEnum.HELP)) {
                printHelpMessage();
                System.exit(0);
            }

            if (configuration.hasOption(OptionEnum.WORKSPACE)) {
                String directory = configuration.getOptionValue(OptionEnum.WORKSPACE);
                setWorkspaceDirectory(directory);
            }

            if (configuration.hasOption(OptionEnum.DRIVER)) {
                ConnectionDriver driver = ConnectionDriver.valueOf(configuration.getOptionValue(OptionEnum.DRIVER));
                setConnectionDriver(driver);
            }

            if (configuration.hasOption(OptionEnum.LIST_PORTS)) {
                listPorts();
                System.exit(0);
            }

            initializeBackend();

            if (configuration.hasOption(OptionEnum.DAEMON)) {
                startDaemon();
            }

            if (configuration.hasOption(OptionEnum.RESET_ALARM)) {
                resetAlarm();
            }

            if (configuration.hasOption(OptionEnum.HOME)) {
                homeMachine();
            }

            if (configuration.hasOption(OptionEnum.FILE)) {
                sendFile();
            }

            while (configuration.hasOption(OptionEnum.DAEMON) && pendantUI != null) {
                Thread.sleep(100);
            }

            backend.disconnect();
        } catch (Exception e) {
            // TODO add fancy error handling
            e.printStackTrace();
            System.exit(-1);
        } finally {
            // TODO This is a hack to exit threads, find out why threads aren't killed
            System.exit(0);
        }
    }

    private void setConnectionDriver(ConnectionDriver driver) {
        Settings settings = SettingsFactory.loadSettings();
        settings.setConnectionDriver(driver);
        SettingsFactory.saveSettings(settings);
    }

    private void setWorkspaceDirectory(String directory) {
        Settings settings = SettingsFactory.loadSettings();
        settings.setWorkspaceDirectory(directory);
        SettingsFactory.saveSettings(settings);
    }

    private void startDaemon() {
        pendantUI = new PendantUI(backend);
        pendantUI.start();
    }

    /**
     * Resets an alarm in the controller
     */
    private void resetAlarm() {
        try {
            backend.killAlarmLock();
            Thread.sleep(WAIT_DURATION);
        } catch (Exception e) {
            throw new RuntimeException("The alarm couldn't be reset", e);
        }
    }

    /**
     * Lists all available ports
     */
    private void listPorts() {
        Settings settings = SettingsFactory.loadSettings();
        List<String> portNames = ConnectionFactory.getPortNames(settings.getConnectionDriver());
        System.out.println("Available ports: " + Arrays.toString(portNames.toArray()));
    }

    /**
     * Performs homing of the machine
     */
    private void homeMachine() {
        try {
            backend.performHomingCycle();
            Thread.sleep(WAIT_DURATION);
            while (backend.getControllerState() == ControllerState.HOME) {
                Thread.sleep(10);
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't home machine", e);
        }
    }

    /**
     * Prints a help message with all available properties for the program
     */
    private void printHelpMessage() {
        System.out.println(SOFTWARE_NAME + " " + Version.getVersionString());
        System.out.println();
        System.out.println(SOFTWARE_DESCRIPTION);
        System.out.println();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(SOFTWARE_NAME, configuration.getOptions());
    }

    /**
     * Starts streaming a file to the controller
     */
    private void sendFile() {
        String filename = configuration.getOptionValue(OptionEnum.FILE);
        if (StringUtils.isEmpty(filename)) {
            return;
        }

        try {
            System.out.println("Running file \"" + filename + "\"");
            File file = new File(filename);
            backend.setGcodeFile(file);

            if (!backend.canSend()) {
                System.out.println("The controller is in a state where it isn't able to process the file: " + backend.getControllerState());
                return;
            }

            backend.send();
            Thread.sleep(WAIT_DURATION);

            while (backend.isSendingFile()) {
                if (backend.getControllerState() == ControllerState.HOLD) {
                    handleResume();
                } else {
                    Thread.sleep(50);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't send file", e);
        }
    }

    private void handleResume() throws Exception {
        System.out.print("The file stream is paused, press 'ENTER' to resume ");
        while (System.in.read() != '\n') {
            Thread.sleep(10);
        }
        System.out.println("Resuming...");
        backend.pauseResume();
        Thread.sleep(WAIT_DURATION);
    }

    /**
     * Initialize and connects the backend to the controller
     */
    private void initializeBackend() {
        backend = BackendInitializerHelper.getInstance().initialize(configuration);

        // It seems like the settings are working, save them for later.
        SettingsFactory.saveSettings();

        if (configuration.hasOption(OptionEnum.PRINT_STREAM)) {
            backend.addUGSEventListener(new ProcessedLinePrinter());
        } else if (configuration.hasOption(OptionEnum.PRINT_PROGRESSBAR)) {
            ProgressBarPrinter progressBarPrinter = new ProgressBarPrinter(backend);
            backend.addUGSEventListener(progressBarPrinter);
        }
    }
}
