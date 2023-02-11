/*
    Copyright 2015-2023 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.google.common.io.Files;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.gcode.ICommandCreator;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.GcodeFileWriter;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import com.willwinder.universalgcodesender.utils.IGcodeWriter;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import org.apache.commons.lang3.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wwinder
 */
public class GUIBackend implements BackendAPI {
    private static final Logger logger = Logger.getLogger(GUIBackend.class.getName());
    private static final String NEW_LINE = "\n    ";

    private final MessageService messageService = new MessageService();
    private final GcodeParser gcp = new GcodeParser();
    private final UGSEventDispatcher eventDispatcher;
    private IController controller = null;
    private Settings settings = null;
    // GUI State
    private File gcodeFile = null;
    private File processedGcodeFile = null;
    private File tempDir = null;
    private String firmware = null;

    /**
     * A temporary pointer to the active gcode stream. This is needed to make sure it is closed
     */
    private GcodeStreamReader gcodeStream;

    public GUIBackend() {
        this(new UGSEventDispatcher());
    }

    public GUIBackend(UGSEventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    /////////////
    // GUI API //
    /////////////

    @Override
    public void addUGSEventListener(UGSEventListener listener) {
        eventDispatcher.addListener(listener);
    }

    @Override
    public void removeUGSEventListener(UGSEventListener listener) {
        eventDispatcher.removeListener(listener);
    }

    @Override
    public void addMessageListener(MessageListener listener) {
        this.messageService.addListener(listener);
    }

    @Override
    public void removeMessageListener(MessageListener listener) {
        this.messageService.removeListener(listener);
    }

    @Override
    public void preprocessAndExportToFile(File f) throws Exception {
        try (IGcodeWriter gcw = new GcodeFileWriter(this.processedGcodeFile)) {
            preprocessAndExportToFile(this.gcp, this.getGcodeFile(), gcw);
        }
    }

    /**
     * Special utility to loop over a gcode file and apply any modifications made by a gcode parser. The results are
     * stored in a GcodeStream formatted file.
     * Additional rules:
     * * Comment lines are left
     */
    protected void preprocessAndExportToFile(GcodeParser gcp, File input, IGcodeWriter gcw) throws Exception {
        logger.log(Level.INFO, "Preprocessing {0} to {1}", new Object[]{input.getCanonicalPath(), gcw.getCanonicalPath()});
        GcodeParserUtils.processAndExport(gcp, input, gcw);
    }

    private void initGcodeParser() {
        // Configure gcode parser.
        gcp.clearCommandProcessors();

        try {
            List<CommandProcessor> processors = FirmwareUtils.getParserFor(firmware).orElse(null);
            for (CommandProcessor p : processors) {
                gcp.addCommandProcessor(p);
            }
        } catch (Exception e) {
            initializeWithFallbackProcessors(gcp);
        }
    }

    private void updateWithFirmware(String firmware) throws Exception {
        this.firmware = firmware;

        // Load command processors for this firmware.
        try {
            FirmwareUtils.getParserFor(firmware);
        } catch (Exception e) {
            disconnect();
            throw new Exception("Bad configuration file for: " + firmware + " (" + e.getMessage() + ")");
        }

        // Reload gcode file to use the controllers processors.
        if (this.gcodeFile != null) {
            setGcodeFile(this.gcodeFile);
        }
    }

    @Override
    public void connect(String firmware, String port, int baudRate) throws Exception {
        logger.log(Level.INFO, "Connecting to {0} on port {1}", new Object[]{firmware, port});
        updateWithFirmware(firmware);

        controller = fetchControllerFromFirmware(firmware);
        controller.setMessageService(messageService);
        applySettings(settings);

        controller.addListener(eventDispatcher);
        controller.getFirmwareSettings().addListener(eventDispatcher);

        openCommConnection(port, baudRate);
    }

    protected IController fetchControllerFromFirmware(String firmware) throws Exception {
        return FirmwareUtils.getControllerFor(firmware)
                .orElseThrow(() -> new Exception(String.format("Could not find controller implementation for the firmware \"%s\"", firmware)));
    }

    @Override
    public boolean isConnected() {
        return controller != null && controller.isCommOpen();
    }

    @Override
    public void disconnect() throws Exception {
        ControllerState previousState = controller != null ? controller.getControllerStatus().getState() : ControllerState.UNKNOWN;
        eventDispatcher.sendUGSEvent(new ControllerStateEvent(ControllerState.DISCONNECTED, previousState));
        disconnectInternal();
    }

    private void disconnectInternal() throws Exception {
        logger.log(Level.INFO, "Disconnecting.");
        if (this.controller != null) {
            this.controller.closeCommPort();
            this.controller.removeListener(eventDispatcher);
            this.controller.getFirmwareSettings().removeListener(eventDispatcher);
            this.controller = null;
        }
    }

    @Override
    public void applySettings(Settings settings) throws Exception {
        logger.log(Level.INFO, "Applying settings.");
        this.settings = settings;
        this.settings.setSettingChangeListener(eventDispatcher);
        if (this.controller != null) {
            applySettingsToController(this.settings, this.controller);
        }
        // Reload gcode file to use the controllers processors.
        if (this.gcodeFile != null) {
            setGcodeFile(this.gcodeFile);
        }
    }

    /**
     * This allows us to visualize a file without loading a controller profile.
     */
    private static void initializeWithFallbackProcessors(GcodeParser parser) {
        // Comment processor must come first otherwise we try to parse codes
        // out of the comments, like an f-code when we see "(feed rate is 100)"
        parser.addCommandProcessor(new CommentProcessor());
        parser.addCommandProcessor(new WhitespaceProcessor());
        parser.addCommandProcessor(new M30Processor());
        parser.addCommandProcessor(new DecimalProcessor(4));
    }

    @Override
    public void sendGcodeCommand(String commandText) throws Exception {
        sendGcodeCommand(false, commandText);
    }

    @Override
    public void sendGcodeCommand(boolean restoreParserState, String commandText) throws Exception {
        if (this.isConnected()) {
            GcodeCommand command = controller.createCommand(commandText);
            command.setTemporaryParserModalChange(restoreParserState);
            sendGcodeCommand(command);

            if (restoreParserState && this.isConnected()) {
                controller.restoreParserModalState();
            }
        } else {
            throw new Exception(Localization.getString("controller.log.notconnected"));
        }
    }

    @Override
    public void sendGcodeCommand(GcodeCommand command) throws Exception {
        if (this.isConnected()) {
            logger.log(Level.INFO, "Sending gcode command: {0}", command.getCommandString());
            controller.sendCommandImmediately(command);
        }
    }

    @Override
    public void adjustManualLocation(PartialPosition distance, double feedRate) throws Exception {
        // Do not allow jogging if we are not idle or already jogging
        boolean canJog = getControllerState() == ControllerState.IDLE || getControllerState() == ControllerState.JOG;
        if (!canJog) {
            logger.fine("Skipping jog as controller state was not IDLE or JOG");
            return;
        }

        boolean isEmpty = Arrays.stream(Axis.values())
                .map(axis -> distance.hasAxis(axis) ? distance.getAxis(axis) : 0)
                .allMatch(aDouble -> aDouble == 0.0);

        // Don't send empty commands.
        if (isEmpty) {
            return;
        }

        controller.jogMachine(distance, feedRate);
    }

    @Override
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception {
        controller.probe(axis, feedRate, distance, units);
    }

    @Override
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception {
        controller.offsetTool(axis, offset, units);
    }

    @Override
    public Settings getSettings() {
        logger.log(Level.FINEST, "Getting settings.");
        return this.settings;
    }

    @Override
    public CommunicatorState getControlState() {
        logger.log(Level.FINEST, "Getting control state.");
        return this.controller == null ?
                CommunicatorState.COMM_DISCONNECTED : this.controller.getCommunicatorState();
    }

    @Override
    public Position getWorkPosition() {
        return controller != null ? controller.getControllerStatus().getWorkCoord() : new Position(0, 0, 0, Units.MM);
    }

    @Override
    public Position getMachinePosition() {
        return controller != null ? controller.getControllerStatus().getMachineCoord() : new Position(0, 0, 0, Units.MM);
    }

    @Override
    public ControllerState getControllerState() {
        return controller != null && controller.getControllerStatus() != null ? controller.getControllerStatus().getState() : ControllerState.DISCONNECTED;
    }

    @Override
    public GcodeState getGcodeState() {
        if (this.controller != null) {
            return this.controller.getCurrentGcodeState();
        }
        return null;
    }

    @Override
    public IController getController() {
        logger.log(Level.FINEST, "Getting controller");
        return this.controller;
    }

    private File getTempDir() {
        if (tempDir == null) {
            tempDir = Files.createTempDir();
        }
        return tempDir;
    }

    @Override
    public void setGcodeFile(File file) throws Exception {
        if (gcodeStream != null) {
            gcodeStream.close();
        }

        logger.log(Level.INFO, "Setting gcode file.");
        eventDispatcher.sendUGSEvent(new FileStateEvent(FileState.OPENING_FILE, file.getAbsolutePath()));
        initGcodeParser();
        this.gcodeFile = file;
        processGcodeFile();
    }

    @Override
    public void unsetGcodeFile() throws Exception {
        if (gcodeStream != null) {
            gcodeStream.close();
        }

        eventDispatcher.sendUGSEvent(new FileStateEvent(FileState.FILE_UNLOADED, null));
        initGcodeParser();
        this.gcodeFile = null;
        this.processedGcodeFile = null;
    }

    @Override
    public void reloadGcodeFile() throws Exception {
        logger.log(Level.INFO, "Reloading gcode file.");
        eventDispatcher.sendUGSEvent(new FileStateEvent(FileState.OPENING_FILE, gcodeFile.getAbsolutePath()));
        processGcodeFile();
    }

    private void processGcodeFile() throws Exception {
        this.processedGcodeFile = null;

        eventDispatcher.sendUGSEvent(new FileStateEvent(FileState.FILE_LOADING,
                this.gcodeFile.getAbsolutePath()));

        initializeProcessedLines(true, this.gcodeFile, this.gcp);

        eventDispatcher.sendUGSEvent(new FileStateEvent(FileState.FILE_LOADED,
                processedGcodeFile.getAbsolutePath()));
    }

    @Override
    public List<String> getWorkspaceFileList() {
        String workspaceDirectory = settings.getWorkspaceDirectory();
        if (StringUtils.isBlank(workspaceDirectory)) {
            return Collections.emptyList();
        }

        File folder = new File(workspaceDirectory);
        if (!folder.exists() || !folder.isDirectory()) {
            return Collections.emptyList();
        }

        return Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .map(File::getName)
                .filter(name -> StringUtils.endsWithIgnoreCase(name, ".gcode") ||
                        StringUtils.endsWithIgnoreCase(name, ".nc") ||
                        StringUtils.endsWithIgnoreCase(name, ".tap"))
                .collect(Collectors.toList());
    }

    @Override
    public void openWorkspaceFile(String file) throws Exception {
        if (!getWorkspaceFileList().contains(file)) {
            throw new FileNotFoundException("Couldn't find the file '" + file + "' in workspace directory");
        }

        String workspaceDirectory = settings.getWorkspaceDirectory();
        String filename = workspaceDirectory + File.separatorChar + file;
        setGcodeFile(new File(filename));
    }

    @Override
    public void applyGcodeParser(GcodeParser parser) throws Exception {
        logger.log(Level.INFO, "Applying new parser filters.");

        if (this.processedGcodeFile == null) {
            return;
        }

        File settingsDir = SettingsFactory.getSettingsDirectory();
        File applyDir = new File(settingsDir, "apply_parser_files");
        applyDir.mkdir();

        File target = new File(applyDir, this.processedGcodeFile.getName() + ".apply.gcode");
        java.nio.file.Files.deleteIfExists(target.toPath());

        // Using a GcodeFileWriter instead of a GcodeStreamWriter so that the user can review a standard gcode file.
        try (IGcodeWriter gcw = new GcodeFileWriter(target)) {
            preprocessAndExportToFile(parser, this.processedGcodeFile, gcw);
        }

        this.setGcodeFile(target);
    }

    @Override
    public void applyCommandProcessor(CommandProcessor commandProcessor) throws Exception {
        logger.log(Level.INFO, String.format("Applying new command processor %s", commandProcessor.getClass().getSimpleName()));
        gcp.addCommandProcessor(commandProcessor);

        if (gcodeFile != null) {
            processGcodeFile();
        }
    }

    @Override
    public void removeCommandProcessor(CommandProcessor commandProcessor) throws Exception {
        gcp.removeCommandProcessor(commandProcessor);
        processGcodeFile();

        if (gcodeFile != null) {
            processGcodeFile();
        }
    }

    @Override
    public File getGcodeFile() {
        logger.log(Level.FINEST, "Getting gcode file.");
        return this.gcodeFile;
    }

    @Override
    public File getProcessedGcodeFile() {
        logger.log(Level.INFO, String.format("Getting processed gcode file (%s).", this.processedGcodeFile));
        return this.processedGcodeFile;
    }

    @Override
    public void send() throws Exception {
        logger.log(Level.INFO, String.format("Sending gcode file (%s).", this.processedGcodeFile));
        try {
            // Reset the stream and queue it again
            if (gcodeStream != null) {
                gcodeStream.close();
            }
            gcodeStream = new GcodeStreamReader(this.processedGcodeFile, getCommandCreator());

            // This will throw an exception and prevent that other stuff from
            // happening (clearing the table before it is ready for clearing.
            controller.isReadyToStreamFile();
            controller.queueStream(gcodeStream);
            controller.beginStreaming();
        } catch (Exception e) {
            logger.log(Level.SEVERE, Localization.getString("mainWindow.error.startingStream"), e);
            throw new Exception(Localization.getString("mainWindow.error.startingStream"), e);
        }
    }

    @Override
    public long getNumRows() {
        if (getControllerState() == ControllerState.RUN) {
            return this.controller.rowsInSend();
        }

        return gcodeStream == null ? 0 : gcodeStream.getNumRows();
    }

    @Override
    public long getNumSentRows() {
        logger.log(Level.FINEST, "Getting number of sent rows.");
        return controller == null ? 0 : controller.rowsSent();
    }

    @Override
    public long getNumCompletedRows() {
        logger.log(Level.FINEST, "Getting number of completed rows.");
        return controller == null ? 0 : controller.rowsCompleted();
    }

    @Override
    public long getNumRemainingRows() {
        return controller == null ? 0 : controller.rowsRemaining();
    }

    @Override
    public long getSendDuration() {
        return controller == null ? 0 : controller.getSendDuration();
    }

    @Override
    public long getSendRemainingDuration() {
        long completedRows = getNumCompletedRows();
        long numberOfRows = getNumRows();

        // Early exit condition. Can't make an estimate if we haven't started.
        if (completedRows == 0 || numberOfRows == 0) {
            return -1L;
        }

        long elapsedTime = getSendDuration();
        long timePerRow = elapsedTime / completedRows;
        long estimate = numberOfRows * timePerRow;
        return Math.max(0, estimate - elapsedTime);
    }

    @Override
    public void pauseResume() throws Exception {
        logger.log(Level.INFO, "Pause/Resume");
        try {
            switch (getControlState()) {
                case COMM_IDLE:
                default:
                    if (!isSendingFile()) {
                        throw new Exception("Cannot pause while '" + getControlState() + "'.");
                    }
                    // Fall through if we're really sending a file.
                    // This can happen at the beginning of a stream when GRBL
                    // reports an error before we send it a status request.
                case COMM_SENDING:
                    this.controller.pauseStreaming();
                    return;
                case COMM_SENDING_PAUSED:
                    this.controller.resumeStreaming();
                    return;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in pauseResume", e);
            throw new Exception(Localization.getString("mainWindow.error.pauseResume"));
        }
    }

    @Override
    public String getPauseResumeText() {
        if (isPaused())
            return Localization.getString("mainWindow.ui.resumeButton");
        else
            return Localization.getString("mainWindow.ui.pauseButton");
    }

    @Override
    public boolean isSendingFile() {
        return this.controller != null && this.controller.isStreaming();
    }

    @Override
    public boolean isIdle() {
        return isConnected() &&
                controller.getControllerStatus() != null &&
                (controller.getControllerStatus().getState() == ControllerState.IDLE || controller.getControllerStatus().getState() == ControllerState.CHECK);
    }

    @Override
    public boolean isPaused() {
        return isConnected() &&
                controller.getControllerStatus() != null &&
                (controller.getControllerStatus().getState() == ControllerState.HOLD || controller.getControllerStatus().getState() == ControllerState.DOOR);
    }

    @Override
    public boolean canPause() {
        return isConnected() &&
                controller.getControllerStatus() != null &&
                (controller.getControllerStatus().getState() == ControllerState.RUN || controller.getControllerStatus().getState() == ControllerState.JOG);
    }

    @Override
    public boolean canCancel() {
        return canPause() || isPaused();
    }

    @Override
    public boolean canSend() {
        return isIdle() &&
                this.gcodeFile != null;
    }

    @Override
    public void cancel() throws Exception {
        if (this.canCancel()) {
            this.controller.cancelSend();
        }
    }

    @Override
    public void returnToZero() throws Exception {
        double safetyHeightInMm = settings.getSafetyHeight();
        this.controller.returnToHome(safetyHeightInMm);
    }

    @Override
    public void resetCoordinatesToZero() throws Exception {
        this.controller.resetCoordinatesToZero();
    }

    @Override
    public void resetCoordinateToZero(Axis coordinate) throws Exception {
        this.controller.resetCoordinateToZero(coordinate);
    }

    @Override
    public void killAlarmLock() throws Exception {
        this.controller.killAlarmLock();
    }

    @Override
    public void performHomingCycle() throws Exception {
        this.controller.performHomingCycle();
    }

    @Override
    public void toggleCheckMode() throws Exception {
        this.controller.toggleCheckMode();
    }

    @Override
    public void issueSoftReset() throws Exception {
        this.controller.issueSoftReset();
    }

    ///////////////////////
    // Utility functions //
    ///////////////////////

    @Override
    public void requestParserState() throws Exception {
        this.controller.viewParserState();
    }

    /**
     * This would be static but I want to define it in the interface.
     *
     * @param settings   Settings to apply to the controller.
     * @param controller Controller to receive settings.
     * @throws java.lang.Exception Exception thrown if controller doesn't support some settings.
     */
    @Override
    public void applySettingsToController(Settings settings, IController controller) throws Exception {
        if (settings == null) {
            throw new Exception("Programmer error.");
        }

        // Apply settings settings to controller.
        try {
            controller.setSingleStepMode(settings.isSingleStepMode());
            controller.setStatusUpdatesEnabled(settings.isStatusUpdatesEnabled());
            controller.setStatusUpdateRate(settings.getStatusUpdateRate());
        } catch (Exception ex) {
            StringBuilder message = new StringBuilder()
                    .append(Localization.getString("mainWindow.error.firmwareSetting"))
                    .append(": \n    ")
                    .append(Localization.getString("firmware.feature.maxCommandLength")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.truncateDecimal")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.singleStep")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.removeWhitespace")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.linesToArc")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.statusUpdates")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.statusUpdateRate"));

            throw new Exception(message.toString(), ex);
        }
    }

    @Override
    public void dispatchMessage(MessageType messageType, String message) {
        messageService.dispatchMessage(messageType, message);
    }

    @Override
    public void openDoor() throws Exception {
        controller.openDoor();
    }

    @Override
    public ICommandCreator getCommandCreator() {
        if (controller == null) {
            return new DefaultCommandCreator();
        }

        return controller.getCommandCreator();
    }

    @Override
    public void setWorkPosition(PartialPosition position) throws Exception {
        controller.setWorkPosition(position);
    }

    @Override
    public void setWorkPositionUsingExpression(final Axis axis, final String expression) throws Exception {
        Units preferredUnits = getSettings().getPreferredUnits();
        String expr = StringUtils.trimToEmpty(expression);
        expr = expr.replaceAll("#", String.valueOf(getWorkPosition().getPositionIn(preferredUnits).get(axis)));

        // If the expression starts with a mathematical operation add the original position
        if (StringUtils.startsWithAny(expr, "/", "*")) {
            double value = getWorkPosition().getPositionIn(preferredUnits).get(axis);
            expr = value + " " + expr;
        }

        // Start a script engine and evaluate the expression
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            double position = Double.parseDouble(engine.eval(expr).toString());
            setWorkPosition(PartialPosition.from(axis, position, preferredUnits));
        } catch (ScriptException e) {
            throw new Exception("Invalid expression", e);
        }
    }

    ////////////////////////
    // Private functions. //
    ////////////////////////

    private boolean openCommConnection(String port, int baudRate) throws Exception {
        boolean connected;
        try {
            connected = controller.openCommPort(settings.getConnectionDriver(), port, baudRate);

            this.initializeProcessedLines(false, this.gcodeFile, this.gcp);
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception in openCommConnection.", e);
            throw new Exception(Localization.getString("mainWindow.error.connection")
                    + ": " + e.getMessage());
        }
        return connected;
    }

    private void initializeProcessedLines(boolean forceReprocess, File startFile, GcodeParser gcodeParser)
            throws Exception {
        if (startFile != null) {
            logger.info("Start preprocessing");
            long start = System.currentTimeMillis();
            if (this.processedGcodeFile == null || forceReprocess) {
                gcodeParser.reset();

                String name = startFile.getName();

                // If this is being re-processed, strip the ugs postfix and try again.
                Pattern word = Pattern.compile("(.*)_ugs_[\\d]+$");
                Matcher match = word.matcher(name);
                if (match.matches()) {
                    name = match.group(1);
                }
                this.processedGcodeFile =
                        new File(this.getTempDir(), name + "_ugs_" + System.currentTimeMillis());
                try (IGcodeWriter gcw = new GcodeStreamWriter(this.processedGcodeFile)) {
                    this.preprocessAndExportToFile(gcodeParser, startFile, gcw);
                }

                // Store gcode file stats.
                GcodeStats gs = gcodeParser.getCurrentStats();
                this.settings.setFileStats(new FileStats(
                        gs.getMin(), gs.getMax(), gs.getCommandCount()));
            }
            long end = System.currentTimeMillis();
            logger.info("Took " + (end - start) + "ms to preprocess");
        }
    }

    @Override
    public void sendOverrideCommand(Overrides override) throws Exception {
        this.controller.sendOverrideCommand(override);
    }
}
