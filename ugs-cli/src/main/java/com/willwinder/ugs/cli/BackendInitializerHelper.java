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

import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Helper for initializing the backend. It will attempt to connect to controller using the given
 * configuration and wait until it is idling or returns an alarm.
 *
 * @author Joacim Breiler
 */
public class BackendInitializerHelper implements UGSEventListener {

    private static BackendInitializerHelper instance;

    private BackendInitializerHelper() {
    }

    public static BackendInitializerHelper getInstance() {
        if (instance == null) {
            instance = new BackendInitializerHelper();
        }
        return instance;
    }

    public BackendAPI initialize(Configuration configuration) {
        Settings backendSettings = SettingsFactory.loadSettings();

        String firmwareArgument = configuration.getOptionValue(OptionEnum.CONTROLLER_FIRMWARE);
        String firmware = StringUtils.defaultIfEmpty(firmwareArgument, backendSettings.getFirmwareVersion());

        String portArgument = configuration.getOptionValue(OptionEnum.PORT);
        String port = StringUtils.defaultIfEmpty(portArgument, backendSettings.getPort());

        String baudRateArgument = configuration.getOptionValue(OptionEnum.BAUD);
        int baudRate = Integer.parseInt(StringUtils.defaultIfEmpty(baudRateArgument, backendSettings.getPortRate()));

        BackendAPI backend = new GUIBackend();
        try {
            backend.addUGSEventListener(this);
            backend.applySettings(backendSettings);
            backend.getSettings().setFirmwareVersion(firmware);

            // Only connect if port is available
            Settings settings = SettingsFactory.loadSettings();
            List<String> portNames = ConnectionFactory.getPortNames(settings.getConnectionDriver());
            if (portNames.contains(port)) {
                backend.connect(firmware, port, baudRate);
            }

            // TODO Wait until controller is finnished and in state IDLE or ALARM
            Thread.sleep(3000);

            if (backend.isConnected()) {
                System.out.println("Connected to \"" + backend.getController().getFirmwareVersion() + "\" on " + port + " baud " + baudRate);
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            System.err.println("Couldn't connect to controller with firmware \"" + firmware + "\" on " + port + " baud " + baudRate);

            if (StringUtils.isNotEmpty(e.getMessage())) {
                System.err.println(e.getMessage());
            }
            System.exit(-1);
        } finally {
            backend.removeUGSEventListener(this);
        }

        return backend;
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        // TODO handle controller status events
    }
}
