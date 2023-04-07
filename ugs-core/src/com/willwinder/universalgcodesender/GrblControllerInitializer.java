package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetBuildInfoCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetParserStateCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetSettingsCommand;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.services.MessageService;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletion;

/**
 * A class that implements an initialization protocol for GRBL and keeps an internal state of the
 * connection process. The query process will not require the controller to be reset which is needed
 * for controllers such as grblHAL or GRBL_ESP32.
 * <p/>
 * 1. It will first to query the machine for a status report 10 times, if the status is HOLD or ALARM
 *    a blank line will be sent to see if the controller is responsive
 * 2. Fetch the build info for the controller
 * 3. Fetch the parser state
 * 4. Start the status poller
 *
 * @author Joacim Breiler
 */
public class GrblControllerInitializer implements IControllerInitializer {
    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final MessageService messageService;
    private final GrblController controller;
    private GrblVersion version = GrblVersion.NO_VERSION;

    public GrblControllerInitializer(GrblController controller, MessageService messageService) {
        this.controller = controller;
        this.messageService = messageService;
    }

    @Override
    public boolean initialize() {
        // Only allow one initialization at a time
        if (isInitializing.get() || isInitialized.get()) {
            return false;
        }

        controller.resetBuffers();

        controller.setControllerState(ControllerState.CONNECTING);
        isInitializing.set(true);
        try {
            Thread.sleep(2000);
            if (!GrblUtils.isControllerResponsive(controller, messageService)) {
                isInitializing.set(false);
                messageService.dispatchMessage(MessageType.INFO, "*** Device is in a holding or alarm state and needs to be reset\n");
                controller.issueSoftReset();
                return false;
            }

            // Toggle the state to force UI update
            controller.setControllerState(ControllerState.CONNECTING);
            controller.setControllerState(ControllerState.IDLE);

            fetchControllerState();

            messageService.dispatchMessage(MessageType.INFO, String.format("*** Connected to %s\n", version.toString()));
            controller.requestStatusReport();
            isInitialized.set(true);
            isInitializing.set(false);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void fetchControllerState() throws Exception {
        // Send commands to get the state of the controller
        GetBuildInfoCommand getBuildInfoCommand = sendAndWaitForCompletion(controller, new GetBuildInfoCommand());
        version = getBuildInfoCommand.getVersion().orElse(GrblVersion.NO_VERSION);

        sendAndWaitForCompletion(controller, new GetSettingsCommand());
        sendAndWaitForCompletion(controller, new GetParserStateCommand());
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

    public GrblVersion getVersion() {
        return version;
    }
}
