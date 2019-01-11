package com.willwinder.ugs.cli;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper for initializing the backend. It will attempt to connect to controller using the given
 * configuration and wait until it is idling or returns an alarm.
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
            backend.connect(firmware, port, baudRate);

            // TODO Wait until controller is finnished and in state IDLE or ALARM
            Thread.sleep(3000);

            System.out.println("Connected to \"" + backend.getController().getFirmwareVersion() + "\" on " + port + " baud " + baudRate);
        } catch (Exception e) {
            System.err.println("Couldn't connect to controller with firmware \"" + firmware + "\" on " + port + " baud " + baudRate);

            if (StringUtils.isNotEmpty(e.getMessage())) {
                System.err.println(e.getMessage());
            } else {
                e.printStackTrace();
            }
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
