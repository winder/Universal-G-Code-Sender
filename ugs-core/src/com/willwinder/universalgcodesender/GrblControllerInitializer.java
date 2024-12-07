package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetBuildInfoCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetParserStateCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetSettingsCommand;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageType;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletion;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that implements an initialization protocol for GRBL and keeps an internal state of the
 * connection process. The query process will not require the controller to be reset which is needed
 * for controllers such as grblHAL or GRBL_ESP32.
 * <p/>
 * 1. It will first to query the machine for a status report 10 times, if the status is HOLD or ALARM
 * a blank line will be sent to see if the controller is responsive
 * 2. Fetch the build info for the controller
 * 3. Fetch the parser state
 * 4. Start the status poller
 *
 * @author Joacim Breiler
 */
public class GrblControllerInitializer implements IControllerInitializer {
    private static final Logger LOGGER = Logger.getLogger(GrblControllerInitializer.class.getSimpleName());
    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final GrblController controller;
    private GrblVersion version = GrblVersion.NO_VERSION;
    private GrblBuildOptions options = new GrblBuildOptions();

    public GrblControllerInitializer(GrblController controller) {
        this.controller = controller;
    }

    @Override
    public boolean initialize() throws ControllerException {
        // Only allow one initialization at a time
        if (isInitializing.get() || isInitialized.get()) {
            return false;
        }

        controller.resetBuffers();

        controller.setControllerState(ControllerState.CONNECTING);
        isInitializing.set(true);
        try {
            // Some controllers need this wait before we can query its status
            Thread.sleep(2000);
            if (!GrblUtils.isControllerResponsive(controller)) {
                isInitializing.set(false);
                controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Device is in a holding or alarm state and needs to be reset\n");
                controller.issueSoftReset();
                return false;
            }

            // Some controllers need this wait before we can query the rest of its information
            Thread.sleep(2000);
            fetchControllerVersion();
            fetchControllerState();

            controller.getMessageService().dispatchMessage(MessageType.INFO, String.format("*** Connected to %s\n", version.toString()));
            isInitialized.set(true);
            isInitializing.set(false);
            return true;
        } catch (Exception e) {
            isInitialized.set(false);
            isInitializing.set(false);
            closeConnection();
            throw new ControllerException(e.getMessage());
        }
    }

    private void closeConnection() {
        try {
            controller.closeCommPort();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not properly close the connection", e);
        }
    }

    private void fetchControllerState() throws InterruptedException {
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device settings\n");
        sendAndWaitForCompletion(controller, new GetSettingsCommand());
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device state\n");
        sendAndWaitForCompletion(controller, new GetParserStateCommand());
    }

    private void fetchControllerVersion() throws InterruptedException {
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device version\n");
        GetBuildInfoCommand getBuildInfoCommand = sendAndWaitForCompletion(controller, new GetBuildInfoCommand());
        Optional<GrblVersion> optionalVersion = getBuildInfoCommand.getVersion();
        if (optionalVersion.isEmpty()) {
            controller.getMessageService().dispatchMessage(MessageType.ERROR, "*** Could not detect the GRBL version\n");
            throw new ControllerException("Could not detect the GRBL version");
        }

        version = optionalVersion.get();
        options = getBuildInfoCommand.getBuildOptions();
    }

    @Override
    public void reset() {
        isInitializing.set(false);
        isInitialized.set(false);
        version = GrblVersion.NO_VERSION;
    }

    @Override
    public boolean isInitialized() {
        return isInitialized.get();
    }

    public boolean isInitializing() {
        return isInitializing.get();
    }

    public GrblVersion getVersion() {
        return version;
    }

    public GrblBuildOptions getOptions() {
        return options;
    }
}
