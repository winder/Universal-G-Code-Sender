package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.ConnectionWatchTimer;
import com.willwinder.universalgcodesender.GrblCapabilitiesConstants;
import com.willwinder.universalgcodesender.GrblCommunicator;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.ICommunicator;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.IFileService;
import com.willwinder.universalgcodesender.StatusPollTimer;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.ConnectionException;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.FluidNCCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetAlarmCodesCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetErrorCodesCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetFirmwareVersionCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetParserStateCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetStatusCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.SystemCommand;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.SemanticVersion;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.willwinder.universalgcodesender.Utils.formatter;
import static com.willwinder.universalgcodesender.firmware.fluidnc.FluidNCUtils.GRBL_COMPABILITY_VERSION;
import static com.willwinder.universalgcodesender.model.CommunicatorState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static com.willwinder.universalgcodesender.model.UnitUtils.scaleUnits;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletion;

public class FluidNCController implements IController, CommunicatorListener {

    private static final Logger LOGGER = Logger.getLogger(FluidNCController.class.getSimpleName());
    private static final SemanticVersion MINIMUM_VERSION = new SemanticVersion(3, 3, 0);

    private final GcodeParser gcodeParser = new GcodeParser();
    private final Set<ControllerListener> listeners = Collections.synchronizedSet(new HashSet<>());
    private final List<GcodeCommand> activeCommands = new ArrayList<>();
    private final ICommunicator communicator;
    private final FluidNCSettings firmwareSettings = new FluidNCSettings(this);
    private final Capabilities capabilities = new Capabilities();
    private final StatusPollTimer positionPollTimer = new StatusPollTimer(this);
    private final ConnectionWatchTimer connectionWatchTimer = new ConnectionWatchTimer(this);
    private final StopWatch streamStopWatch = new StopWatch();
    private final IFileService fileService;

    private MessageService messageService = new MessageService();
    private ControllerStatus controllerStatus;
    private SemanticVersion semanticVersion = new SemanticVersion();
    private String firmwareVariant;
    private IGcodeStreamReader streamCommands;
    private String distanceModeCode;
    private String unitsCode;

    public FluidNCController() {
        this(new GrblCommunicator());
    }

    public FluidNCController(ICommunicator communicator) {
        this.controllerStatus = ControllerStatusBuilder.newInstance()
                .setState(ControllerState.DISCONNECTED)
                .build();
        this.communicator = communicator;
        this.communicator.addListener(this);
        this.fileService = new FluidNCFileService(this, positionPollTimer);
    }

    @Override
    public void addListener(ControllerListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ControllerListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public void performHomingCycle() throws Exception {
        sendCommandImmediately(new FluidNCCommand(GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C));
    }

    @Override
    public void returnToHome(double safetyHeightInMm) throws Exception {
        if (isIdle()) {
            // Convert the safety height to the same units as the current gcode state
            UnitUtils.Units currentUnit = getCurrentGcodeState().getUnits();
            double safetyHeight = safetyHeightInMm * UnitUtils.scaleUnits(MM, currentUnit);

            // If Z is less than zero, raise it before further movement.
            double currentZPosition = getControllerStatus().getWorkCoord().getPositionIn(currentUnit).get(Axis.Z);
            if (currentZPosition < safetyHeight) {
                String moveToSafetyHeightCommand = GcodeUtils.GCODE_RETURN_TO_Z_ZERO_LOCATION;
                if (safetyHeight > 0) {
                    moveToSafetyHeightCommand = GcodeUtils.generateMoveCommand("G90 G0", 0, new PartialPosition(null, null, safetyHeight, currentUnit));
                }
                sendCommandImmediately(new SystemCommand(moveToSafetyHeightCommand));
            }
            sendCommandImmediately(new SystemCommand(GcodeUtils.GCODE_RETURN_TO_XY_ZERO_LOCATION));
            sendCommandImmediately(new SystemCommand(GcodeUtils.GCODE_RETURN_TO_Z_ZERO_LOCATION));
        }
    }

    @Override
    public void resetCoordinatesToZero() throws Exception {
        if (isCommOpen()) {
            String gcode = GrblUtils.getResetCoordsToZeroCommand(GRBL_COMPABILITY_VERSION, 'a');
            sendCommandImmediately(new SystemCommand(gcode));
        }
    }

    @Override
    public void resetCoordinateToZero(Axis axis) throws Exception {
        if (isCommOpen()) {
            String gcode = GrblUtils.getResetCoordToZeroCommand(axis, getCurrentGcodeState().getUnits(), GRBL_COMPABILITY_VERSION, 'a');
            sendCommandImmediately(new SystemCommand(gcode));
        }
    }

    @Override
    public void setWorkPosition(PartialPosition axisPosition) throws Exception {
        if (isCommOpen()) {
            UnitUtils.Units currentUnits = getCurrentGcodeState().getUnits();
            PartialPosition position = axisPosition.getPositionIn(currentUnits);
            String gcode = GrblUtils.getSetCoordCommand(position, GRBL_COMPABILITY_VERSION, 'a');
            if (StringUtils.isNotEmpty(gcode)) {
                sendCommandImmediately(new SystemCommand(gcode));
            }
        }
    }

    @Override
    public void openDoor() throws Exception {

    }

    @Override
    public void killAlarmLock() throws Exception {
        sendCommandImmediately(new SystemCommand("$Alarm/Disable"));
    }

    @Override
    public void toggleCheckMode() throws Exception {

    }

    @Override
    public void viewParserState() throws Exception {
        if (isCommOpen()) {
            sendCommandImmediately(new FluidNCCommand(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND));
        }
    }

    @Override
    public void issueSoftReset() throws Exception {
        messageService.dispatchMessage(MessageType.INFO, "*** Resetting controller\n");
        positionPollTimer.stop();
        setControllerState(ControllerState.DISCONNECTED);
        resetBuffers();
        communicator.cancelSend();
        communicator.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
    }

    @Override
    public void requestStatusReport() throws Exception {
        communicator.sendByteImmediately(GrblUtils.GRBL_STATUS_COMMAND);
    }

    @Override
    public void jogMachine(PartialPosition distance, double feedRate) throws Exception {
        String commandString = GcodeUtils.generateMoveCommand("G91", feedRate, distance);
        GcodeCommand command = createCommand("$J=" + commandString);
        sendCommandImmediately(command);
    }

    @Override
    public void jogMachineTo(PartialPosition position, double feedRate) throws Exception {
        String commandString = GcodeUtils.generateMoveToCommand("G90", position, feedRate);
        GcodeCommand command = createCommand("$J=" + commandString);
        sendCommandImmediately(command);
    }

    @Override
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception {
        LOGGER.log(Level.INFO,
                String.format("Probing. axis: %s, feedRate: %s, distance: %s, units: %s",
                        axis, feedRate, distance, units));

        String probePattern = "G38.2 %s%s F%s";
        String probeCommand = String.format(probePattern, axis, formatter.format(distance), formatter.format(feedRate));

        GcodeCommand state = createCommand(GcodeUtils.unitCommand(units) + " G91 G49");
        state.setTemporaryParserModalChange(true);

        GcodeCommand probe = createCommand(probeCommand);
        probe.setTemporaryParserModalChange(true);

        sendCommandImmediately(state);
        sendCommandImmediately(probe);

        restoreParserModalState();
    }

    @Override
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception {
        String offsetPattern = "G43.1 %s%s";
        String offsetCommand = String.format(offsetPattern,
                axis,
                formatter.format(offset * scaleUnits(units, MM)));

        GcodeCommand state = createCommand("G21 G90");
        state.setTemporaryParserModalChange(true);

        sendCommandImmediately(state);
        sendCommandImmediately(createCommand(offsetCommand));

        restoreParserModalState();
    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {
        Byte realTimeCommand = GrblUtils.getOverrideForEnum(command, capabilities);
        if (realTimeCommand != null) {
            messageService.dispatchMessage(MessageType.INFO, String.format("> 0x%02x\n", realTimeCommand));
            communicator.sendByteImmediately(realTimeCommand);
        }
    }

    @Override
    public boolean getSingleStepMode() {
        return true;
    }

    @Override
    public void setSingleStepMode(boolean enabled) {
    }

    @Override
    public boolean getStatusUpdatesEnabled() {
        return positionPollTimer.isEnabled();
    }

    @Override
    public void setStatusUpdatesEnabled(boolean enabled) {
        positionPollTimer.setEnabled(enabled);
    }

    @Override
    public int getStatusUpdateRate() {
        return positionPollTimer.getUpdateInterval();
    }

    @Override
    public void setStatusUpdateRate(int rate) {
        // 100 ms is the maximum rate to request status reports
        positionPollTimer.setUpdateInterval(Math.max(100, rate));
    }

    @Override
    public Boolean openCommPort(ConnectionDriver connectionDriver, String port, int portRate) throws Exception {
        if (isCommOpen()) {
            throw new Exception("Communication port is already open.");
        }

        positionPollTimer.stop();
        communicator.connect(connectionDriver, port, portRate);
        setControllerState(ControllerState.CONNECTING);
        messageService.dispatchMessage(MessageType.INFO, "*** Connecting to " + connectionDriver.getProtocol() + port + ":" + portRate + "\n");

        ThreadHelper.invokeLater(() -> {
            if (StringUtils.isEmpty(firmwareVariant) || semanticVersion == null) {
                initializeController();
            }
        }, 1000);
        connectionWatchTimer.start();
        return isCommOpen();
    }

    private void setControllerState(ControllerState controllerState) {
        ControllerStatus newControllerStatus = ControllerStatusBuilder
                .newInstance(controllerStatus)
                .setState(controllerState)
                .build();

        if (!newControllerStatus.equals(this.controllerStatus)) {
            this.controllerStatus = newControllerStatus;
            listeners.forEach(l -> l.statusStringListener(controllerStatus));
        }
    }

    @Override
    public Boolean closeCommPort() throws Exception {
        positionPollTimer.stop();
        connectionWatchTimer.stop();
        if (!isCommOpen() && getControllerStatus().getState() == ControllerState.DISCONNECTED) {
            return true;
        }

        communicator.disconnect();
        setControllerState(ControllerState.DISCONNECTED);
        messageService.dispatchMessage(MessageType.INFO, "*** Connection closed\n");
        return true;
    }

    @Override
    public Boolean isCommOpen() {
        return communicator != null && communicator.isConnected();
    }

    @Override
    public Boolean isReadyToReceiveCommands() throws Exception {
        return isCommOpen() && !this.isStreaming();
    }

    @Override
    public Boolean isReadyToStreamFile() throws Exception {
        return controllerStatus.getState() == ControllerState.IDLE;
    }

    @Override
    public Boolean isStreaming() {
        return controllerStatus.getState() == ControllerState.RUN && streamCommands != null;
    }

    @Override
    public long getSendDuration() {
        return streamStopWatch.getTime();
    }

    @Override
    public int rowsInSend() {
        if (streamCommands == null) {
            return 0;
        }
        return streamCommands.getNumRows();
    }

    @Override
    public int rowsSent() {
        if (streamCommands == null) {
            return 0;
        }
        return streamCommands.getNumRows() - streamCommands.getNumRowsRemaining() - activeCommands.size();
    }

    @Override
    public int rowsCompleted() {
        if (streamCommands == null) {
            return 0;
        }
        return streamCommands.getNumRows() - streamCommands.getNumRowsRemaining();
    }

    @Override
    public int rowsRemaining() {
        if (streamCommands == null) {
            return 0;
        }
        return streamCommands.getNumRowsRemaining();
    }

    @Override
    public Optional<GcodeCommand> getActiveCommand() {
        if (activeCommands.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(activeCommands.get(0));
    }

    @Override
    public GcodeState getCurrentGcodeState() {
        return gcodeParser.getCurrentState();
    }

    @Override
    public void beginStreaming() {
        // Send all queued commands and streams then kick off the stream.
        try {
            if (streamCommands != null) {
                streamStopWatch.reset();
                streamStopWatch.start();
                setControllerState(ControllerState.RUN);
                communicator.queueStreamForComm(streamCommands);
                communicator.streamCommands();
            }
        } catch (Exception e) {
            this.streamStopWatch.reset();
            throw e;
        }
    }

    @Override
    public void pauseStreaming() throws Exception {
        communicator.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
        communicator.pauseSend();

        if (streamStopWatch.isStarted() && !streamStopWatch.isSuspended()) {
            streamStopWatch.suspend();
        }
    }

    @Override
    public void resumeStreaming() throws Exception {
        communicator.sendByteImmediately(GrblUtils.GRBL_RESUME_COMMAND);
        communicator.resumeSend();

        if (streamStopWatch.isSuspended()) {
            streamStopWatch.resume();
        }
    }

    @Override
    public Boolean isPaused() {
        return communicator.isPaused();
    }

    @Override
    public Boolean isIdle() {
        return controllerStatus.getState() == ControllerState.IDLE;
    }

    @Override
    public void cancelSend() throws Exception {
        communicator.cancelSend();
        resetBuffers();

        if (controllerStatus.getState() == ControllerState.JOG) {
            communicator.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
        } else {
            messageService.dispatchMessage(MessageType.INFO, "*** Canceling command stream\n");
            if (!communicator.isPaused()) {
                try {
                    pauseStreaming();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Exception while trying to issue a soft reset", e);
                }
            }

            ThreadHelper.invokeLater(() -> {
                try {
                    // Make sure we reached HOLD (1 -> Starting hold, 0 => On hold)
                    while (!(getControllerStatus().getState() == ControllerState.HOLD && StringUtils.equalsIgnoreCase(getControllerStatus().getSubState(), "0"))) {
                        Thread.sleep(getStatusUpdateRate());
                    }
                    issueSoftReset();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed waiting for hold and issue soft reset", e);
                }
            });
        }
    }

    @Override
    public void cancelJog() throws Exception {
        communicator.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
    }

    @Override
    public CommunicatorState getCommunicatorState() {
        ControllerState state = this.controllerStatus == null ? ControllerState.DISCONNECTED : this.controllerStatus.getState();
        switch (state) {
            case JOG:
            case RUN:
                return CommunicatorState.COMM_SENDING;
            case HOLD:
            case DOOR:
                return CommunicatorState.COMM_SENDING_PAUSED;
            case IDLE:
                if (isStreaming()) {
                    return CommunicatorState.COMM_SENDING_PAUSED;
                } else {
                    return CommunicatorState.COMM_IDLE;
                }
            case ALARM:
                return CommunicatorState.COMM_IDLE;
            case CHECK:
                if (isStreaming() && communicator.isPaused()) {
                    return CommunicatorState.COMM_SENDING_PAUSED;
                } else if (isStreaming() && !communicator.isPaused()) {
                    return CommunicatorState.COMM_SENDING;
                } else {
                    return COMM_CHECK;
                }
            default:
                return CommunicatorState.COMM_IDLE;
        }
    }

    private void initializeController() {
        positionPollTimer.stop();
        gcodeParser.reset();
        resetBuffers();

        setControllerState(ControllerState.CONNECTING);
        try {
            GetStatusCommand statusCommand = FluidNCUtils.queryForStatusReport(this, messageService);
            if (!statusCommand.isDone() || statusCommand.isError()) {
                throw new IllegalStateException("Could not query the device status");
            }

            if (statusCommand.getControllerStatus().getState() == ControllerState.HOLD || statusCommand.getControllerStatus().getState() == ControllerState.ALARM) {
                messageService.dispatchMessage(MessageType.INFO, "*** Device is in a holding or alarm state and needs to be reset\n");
                issueSoftReset();
                setControllerState(ControllerState.CONNECTING);
                Thread.sleep(1000);
                FluidNCUtils.queryForStatusReport(this, messageService);
            }

            messageService.dispatchMessage(MessageType.INFO, "*** Fetching device firmware version\n");
            GetFirmwareVersionCommand getFirmwareVersionCommand = sendAndWaitForCompletion(this, new GetFirmwareVersionCommand());
            firmwareVariant = getFirmwareVersionCommand.getFirmware();
            semanticVersion = getFirmwareVersionCommand.getVersion();
            capabilities.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
            if (!firmwareVariant.equalsIgnoreCase("FluidNC") || semanticVersion.compareTo(MINIMUM_VERSION) < 0) {
                messageService.dispatchMessage(MessageType.INFO, String.format("*** Expected a 'FluidNC %s' or later but got '%s %s'\n", MINIMUM_VERSION, firmwareVariant, semanticVersion));
                throw new IllegalStateException("Unknown controller version: " + this.semanticVersion.toString());
            }

            messageService.dispatchMessage(MessageType.INFO, "*** Fetching device status codes\n");
            sendAndWaitForCompletion(this, new GetErrorCodesCommand(), 3000);
            sendAndWaitForCompletion(this, new GetAlarmCodesCommand(), 3000);

            // Fetch the gcode state
            messageService.dispatchMessage(MessageType.INFO, "*** Fetching device state\n");
            GetParserStateCommand getParserStateCommand = sendAndWaitForCompletion(this, new GetParserStateCommand());
            String state = getParserStateCommand.getState().orElseThrow(() -> new ConnectionException("Could not get controller state"));
            gcodeParser.addCommand(state);

            refreshFirmwareSettings();
            FluidNCUtils.addCapabilities(capabilities, semanticVersion, firmwareSettings);

            // Toggle the state to force UI update
            setControllerState(ControllerState.CONNECTING);
            setControllerState(ControllerState.IDLE);

            messageService.dispatchMessage(MessageType.INFO, String.format("*** Connected to %s\n", getFirmwareVersion()));
            requestStatusReport();
            positionPollTimer.start();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not initialize the connection", e);
            messageService.dispatchMessage(MessageType.INFO, "*** Could not establish connection with the controller\n");
            try {
                closeCommPort();
            } catch (Exception ex) {
                // Never mind...
            }
        }
    }

    private void refreshFirmwareSettings() throws FirmwareSettingsException {
        messageService.dispatchMessage(MessageType.INFO, "*** Fetching device settings\n");
        firmwareSettings.refresh();
    }

    @Override
    public void resetBuffers() {
        activeCommands.clear();
        communicator.resetBuffers();
    }

    @Override
    public boolean handlesAllStateChangeEvents() {
        return true;
    }

    @Override
    public GcodeCommand createCommand(String command) throws Exception {
        return new FluidNCCommand(command);
    }

    @Override
    public void sendCommandImmediately(GcodeCommand cmd) throws Exception {
        communicator.queueCommand(cmd);
        communicator.streamCommands();
    }

    @Override
    public void queueStream(IGcodeStreamReader reader) {
        streamCommands = reader;
    }

    @Override
    public void cancelCommands() {
        communicator.cancelSend();
    }

    @Override
    public void restoreParserModalState() {
        StringBuilder cmd = new StringBuilder();
        if (distanceModeCode != null) {
            cmd.append(distanceModeCode).append(" ");
        }
        if (unitsCode != null) {
            cmd.append(unitsCode).append(" ");
        }

        try {
            GcodeCommand command = createCommand(cmd.toString());
            command.setTemporaryParserModalChange(true);
            sendCommandImmediately(command);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateParserModalState(GcodeCommand command) {
        if (command.isError() || command.isTemporaryParserModalChange()) {
            return;
        }

        try {
            gcodeParser.addCommand(command.getCommandString());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Problem parsing command: " + command, e);
        }

        String gcode = command.getCommandString().toUpperCase();
        if (gcode.contains("G90")) {
            distanceModeCode = "G90";
        }
        if (gcode.contains("G91")) {
            distanceModeCode = "G91";
        }
        if (gcode.contains("G20")) {
            unitsCode = "G20";
        }
        if (gcode.contains("G21")) {
            unitsCode = "G21";
        }
    }

    @Override
    public ICommunicator getCommunicator() {
        return communicator;
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
    }

    @Override
    public String getFirmwareVersion() {
        return firmwareVariant + " " + semanticVersion.toString();
    }

    @Override
    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }

    @Override
    public void rawResponseListener(String response) {
        if (GrblUtils.isGrblStatusString(response)) {
            getActiveCommand().filter(command -> command instanceof GetStatusCommand || command.getCommandString().contains("?")).ifPresent(command -> {
                command.appendResponse(response);
                activeCommands.remove(0);
                listeners.forEach(l -> l.commandComplete(command));

                if (command instanceof SystemCommand) {
                    messageService.dispatchMessage(MessageType.VERBOSE, command.getResponse() + "\n");
                } else {
                    messageService.dispatchMessage(MessageType.INFO, command.getResponse() + "\n");
                }
            });
            positionPollTimer.receivedStatus();

            // Don't update the state from status command if we are connecting
            if (controllerStatus.getState() == ControllerState.CONNECTING) {
                return;
            }

            controllerStatus = FluidNCUtils.getStatusFromStatusResponse(controllerStatus, response, getFirmwareSettings().getReportingUnits());
            setControllerState(controllerStatus.getState());
            listeners.forEach(l -> l.statusStringListener(controllerStatus));
            messageService.dispatchMessage( MessageType.VERBOSE, response + "\n");
        } else if (getActiveCommand().isPresent()) {
            GcodeCommand command = getActiveCommand().get();
            command.appendResponse(response);

            if (command instanceof FluidNCCommand) {
                if (command.isDone()) {
                    activeCommands.remove(0);
                    updateParserModalState(command);

                    listeners.forEach(l -> l.commandComplete(command));

                    if (command instanceof GetStatusCommand) {
                        messageService.dispatchMessage(MessageType.VERBOSE, command.getResponse() + "\n");
                    } else if (command instanceof SystemCommand) {
                        messageService.dispatchMessage(MessageType.VERBOSE, command.getResponse() + "\n");
                    } else {
                        messageService.dispatchMessage(MessageType.INFO, command.getResponse() + "\n");
                    }
                }
            } else {
                if (response.startsWith("ok") || response.startsWith("error") || response.startsWith("alarm")) {
                    command.setDone(true);

                    activeCommands.remove(0);
                    listeners.forEach(l -> l.commandComplete(command));
                    messageService.dispatchMessage(MessageType.INFO, command.getResponse() + "\n");
                }
            }
        } else if (FluidNCUtils.isWelcomeResponse(response)) {
            messageService.dispatchMessage(MessageType.VERBOSE, response + "\n");
            if (controllerStatus.getState() == ControllerState.CONNECTING) {
                LOGGER.info("We got a welcome string, but are already in a connection phase. Do not initialize another connection phase...");
                return;
            }
            ThreadHelper.invokeLater(this::initializeController);
        }

        if (FluidNCUtils.isProbeMessage(response)) {
            Position p = FluidNCUtils.parseProbePosition(response, getFirmwareSettings().getReportingUnits());
            listeners.forEach(l -> l.probeCoordinates(p));
        }

        if (FluidNCUtils.isMessageResponse(response)) {
            MessageType messageType = MessageType.INFO;
            if (controllerStatus.getState() == ControllerState.CONNECTING) {
                messageType = MessageType.VERBOSE;
            }
            messageService.dispatchMessage(messageType, FluidNCUtils.parseMessageResponse(response).orElse("") + "\n");
        } else {
            messageService.dispatchMessage(MessageType.VERBOSE, "Other: " + response + "\n");
        }
    }

    @Override
    public void commandSent(GcodeCommand command) {
        command.setSent(true);

        if (command instanceof SystemCommand) {
            messageService.dispatchMessage(MessageType.VERBOSE, String.format("> %s\n", command.getCommandString()));
        } else {
            messageService.dispatchMessage(MessageType.INFO, String.format("> %s\n", command.getCommandString()));
        }
        activeCommands.add(command);
        listeners.forEach(l -> l.commandSent(command));
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        listeners.forEach(l -> l.commandSkipped(command));
    }

    @Override
    public void communicatorPausedOnError() {
        messageService.dispatchMessage(MessageType.INFO, "*** The communicator has been paused\n");
        try {
            // Synchronize the controller <> communicator state.
            if (!this.isStreaming()) {
                communicator.resumeSend();
            } else {
                this.pauseStreaming();
            }
        } catch (Exception ignored) {
            LOGGER.log(Level.SEVERE, "Couldn't set the state to paused.");
        }
    }

    @Override
    public IFileService getFileService() {
        return fileService;
    }
}
