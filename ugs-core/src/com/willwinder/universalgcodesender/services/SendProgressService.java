/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.statistics.GcodeStatistics;
import com.willwinder.universalgcodesender.gcode.statistics.GcodeStatisticsCalculator;
import com.willwinder.universalgcodesender.gcode.statistics.MachineLimits;
import com.willwinder.universalgcodesender.gcode.statistics.RuntimeCheckpoint;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Debouncer;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.MathUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Keeps track of how far a gcode program has come while it is being streamed to a controller and
 * how long it is expected to take.
 * <p>
 * The progress is built from the events dispatched while streaming, and the time estimate from the
 * {@link GcodeStatistics} of the processed file, so that a remaining time can be given before the
 * first row has even been sent.
 *
 * @author Joacim Breiler
 */
public class SendProgressService implements UGSEventListener {
    private static final Logger logger = Logger.getLogger(SendProgressService.class.getName());

    /**
     * The rate used when estimating the runtime of a program while the machine limits are unknown,
     * for instance when no controller is connected.
     */
    private static final double DEFAULT_MAX_RATE_MM_PER_MINUTE = 3000;

    /**
     * The smallest remaining duration that is reported while a program is still running, so that a
     * program that takes longer than estimated never looks like it is done while it is running.
     */
    private static final long MINIMUM_REMAINING_DURATION = 1000;

    /**
     * How many checkpoints back the progress is compared when working out whether the program is
     * running at the pace that was estimated. Comparing over more than one checkpoint evens out a
     * single part of the program that the estimate happens to be further off on.
     */
    private static final int COMPARED_CHECKPOINTS = 3;

    /**
     * How many rows at the end of a program that are left out of the pace measurement. A
     * controller reports a row as completed once it has been planned, so the rows closest to the
     * end are all reported long before they have been run and say nothing about how fast the
     * machine is going. This covers the depth of a controllers planner and receive buffers with
     * some margin.
     */
    private static final int QUEUED_ROWS_AT_END = 32;

    /**
     * How long to wait before recalculating when the machine limits change. A controller reports
     * its settings one at a time when it is connected, and there is no point in recalculating the
     * statistics for each of them.
     */
    private static final long LIMITS_SETTLE_MILLIS = 1000;

    /**
     * How far the estimate may be corrected in either direction, keeping a badly behaving sample
     * from throwing the estimate away entirely.
     */
    private static final double MIN_CORRECTION = 0.1;
    private static final double MAX_CORRECTION = 10;

    private final BackendAPI backend;
    private final StopWatch stopWatch = new StopWatch();
    private final AtomicInteger sentRows = new AtomicInteger(0);
    private final AtomicInteger dispatchedRows = new AtomicInteger(0);
    private final AtomicInteger completedRows = new AtomicInteger(0);

    private final Deque<Sample> samples = new ArrayDeque<>();

    private volatile boolean isSending = false;
    private volatile boolean hasFinishedSending = false;
    private volatile double correction = 1;

    /**
     * The expected duration of the entire program, only recalculated when a checkpoint is passed
     * so that it doesn't move around while nothing new has been measured.
     */
    private volatile long totalEstimate = 0;
    private int nextCheckpointIndex = 0;
    private Sample firstSample = null;
    private volatile int numRows = 0;
    private volatile GcodeStatistics statistics = GcodeStatistics.EMPTY;
    private volatile MachineLimits usedLimits = null;

    private final Debouncer limitsChanged = new Debouncer(
            runnable -> ThreadHelper.invokeLater(runnable, LIMITS_SETTLE_MILLIS),
            this::recalculateIfLimitsChanged);

    public SendProgressService(BackendAPI backend) {
        this.backend = backend;
        backend.addUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent) {
            handleFileState(fileStateEvent.getFileState());
        } else if (event instanceof StreamEvent streamEvent) {
            handleStreamEvent(streamEvent.getType());
        } else if (event instanceof CommandEvent commandEvent) {
            handleCommandEvent(commandEvent);
        } else if (event instanceof ControllerStateEvent || event instanceof FirmwareSettingEvent) {
            limitsChanged.call();
        }
    }

    /**
     * @return the number of rows in the loaded program
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * @return the number of rows that have been sent to the controller
     */
    public int getNumSentRows() {
        return sentRows.get();
    }

    /**
     * @return the number of rows that the controller has processed
     */
    public int getNumCompletedRows() {
        return completedRows.get();
    }

    /**
     * @return the number of rows that are left to be processed by the controller
     */
    public int getNumRemainingRows() {
        return Math.max(0, numRows - completedRows.get());
    }

    /**
     * Returns how long the current send has been running, the entire duration of the most recent
     * send, or zero if there hasn't been a send.
     *
     * @return the duration in milliseconds
     */
    public long getDuration() {
        return stopWatch.getTime();
    }

    /**
     * Returns the estimated time left of the loaded program. It is the runtime calculated from the
     * {@link GcodeStatistics} counted down against the time that the program has been running.
     * <p>
     * The completed rows are deliberately not used, a controller reports a row as completed once
     * it has been planned and not when its motion has finished, so the rows run ahead of what the
     * machine has actually done.
     *
     * @return the remaining duration in milliseconds or -1 if it can not be estimated
     */
    public long getRemainingDuration() {
        long estimatedDuration = statistics.totalDuration().toMillis();
        if (estimatedDuration == 0) {
            return -1;
        }

        if (!isSending) {
            return hasFinishedSending ? 0 : estimatedDuration;
        }

        long expectedDuration = totalEstimate > 0 ? totalEstimate : estimatedDuration;

        // The controller decides when a program is finished, so a program that runs for longer
        // than estimated should never look like it is done while it is still running
        return Math.max(MINIMUM_REMAINING_DURATION, expectedDuration - getDuration());
    }

    /**
     * Returns how long the whole program is expected to take, which is the time it has been
     * running so far plus the time that is estimated to be left of it. It follows the pace that
     * the program is actually running at, so it grows if the machine is slowed down.
     *
     * @return the total duration in milliseconds or -1 if it can not be estimated
     */
    public long getTotalDuration() {
        long remainingDuration = getRemainingDuration();
        if (remainingDuration < 0) {
            return -1;
        }

        return getDuration() + remainingDuration;
    }

    /**
     * @return how far off the estimate the program is currently running, where 2 means that it is
     * taking twice as long as estimated
     */
    public double getCorrection() {
        return correction;
    }

    /**
     * Compares how long the program has actually been running against how long it was estimated to
     * take to reach the checkpoint it just passed, and uses that to expect the same of the rest of
     * the program. Only the most recent checkpoints are compared, so that a change of pace such as
     * a feed override is picked up rather than averaged away over the whole program.
     * <p>
     * Nothing is recalculated until a checkpoint is passed, so the expected duration holds still
     * between them instead of drifting on every completed row.
     */
    private void sampleProgress() {
        // With nothing left to hand over, the controller acknowledges whatever it has room for as
        // fast as it can read it, which makes the program look like it is running far quicker than
        // it is. There is nothing left to measure once that happens.
        if (dispatchedRows.get() >= numRows) {
            return;
        }

        RuntimeCheckpoint passed = passedCheckpoint();
        if (passed == null) {
            return;
        }

        Sample sample = new Sample(getDuration(), passed.elapsedMillis());
        if (firstSample == null) {
            firstSample = sample;
        }

        samples.addLast(sample);
        while (samples.size() > COMPARED_CHECKPOINTS) {
            samples.removeFirst();
        }

        // A controller reports a row as completed once it has been planned, so the time a
        // checkpoint is reported at runs ahead of the machine by whatever it has queued up. That
        // lead is roughly the same at every checkpoint and cancels out when the time between two
        // of them is measured, which it doesn't when measuring from the start of the program.
        Sample oldest = samples.peekFirst();
        long actualDelta = sample.actualMillis() - oldest.actualMillis();
        long estimatedDelta = sample.estimatedMillis() - oldest.estimatedMillis();
        if (actualDelta > 0 && estimatedDelta > 0) {
            correction = MathUtils.clamp((double) actualDelta / estimatedDelta, MIN_CORRECTION, MAX_CORRECTION);
        }

        totalEstimate = expectedDurationOfFirstCheckpoint()
                + measuredDurationSinceFirstCheckpoint(sample)
                + expectedDurationOfWhatIsLeft(sample);
    }

    // Nothing was measured before the first checkpoint, so the estimate has to stand for it
    private long expectedDurationOfFirstCheckpoint() {
        return Math.round(correction * firstSample.estimatedMillis());
    }

    private long measuredDurationSinceFirstCheckpoint(Sample sample) {
        return sample.actualMillis() - firstSample.actualMillis();
    }

    private long expectedDurationOfWhatIsLeft(Sample sample) {
        long estimatedLeft = Math.max(0, statistics.totalDuration().toMillis() - sample.estimatedMillis());
        return Math.round(correction * estimatedLeft);
    }

    // The last checkpoint that the completed rows have reached since the previous call, or null
    // when no new checkpoint has been passed
    private RuntimeCheckpoint passedCheckpoint() {
        List<RuntimeCheckpoint> checkpoints = statistics.checkpoints();
        int completed = completedRows.get();
        RuntimeCheckpoint passed = null;
        while (nextCheckpointIndex < checkpoints.size()
                && checkpoints.get(nextCheckpointIndex).row() <= completed) {
            RuntimeCheckpoint checkpoint = checkpoints.get(nextCheckpointIndex);
            nextCheckpointIndex++;
            if (isMeasurable(checkpoint)) {
                passed = checkpoint;
            }
        }
        return passed;
    }

    // The rows a controller has queued up are reported as completed long before they have been
    // run, and a few of them can be worth a large part of a program when they are slow movements,
    // so the end of a program is left out of the measurement rather than compared against a clock
    // it was never running against
    private boolean isMeasurable(RuntimeCheckpoint checkpoint) {
        return numRows - checkpoint.row() >= QUEUED_ROWS_AT_END;
    }

    private record Sample(long actualMillis, long estimatedMillis) {
    }

    /**
     * @return the statistics of the loaded program
     */
    public GcodeStatistics getStatistics() {
        return statistics;
    }

    private void handleFileState(FileState fileState) {
        if (fileState == FileState.FILE_LOADED) {
            reset();
            loadStatistics(backend.getProcessedGcodeFile());
        } else if (fileState == FileState.FILE_UNLOADED) {
            reset();
            numRows = 0;
            statistics = GcodeStatistics.EMPTY;
            usedLimits = null;
        }
    }

    /**
     * A file that was loaded before the machine was connected has been estimated without knowing
     * how fast the machine accelerates, which is worth redoing once the machine has told us.
     */
    private void recalculateIfLimitsChanged() {
        File processedGcodeFile = backend.getProcessedGcodeFile();
        if (isSending || processedGcodeFile == null || getMachineLimits().equals(usedLimits)) {
            return;
        }

        logger.info("The machine limits have changed, recalculating the gcode statistics");
        reset();
        loadStatistics(processedGcodeFile);
    }

    private void handleStreamEvent(StreamEventType type) {
        switch (type) {
            case STREAM_STARTED -> {
                reset();
                stopWatch.start();
                isSending = true;
            }
            case STREAM_PAUSED -> {
                if (stopWatch.isStarted() && !stopWatch.isSuspended()) {
                    stopWatch.suspend();
                }
            }
            case STREAM_RESUMED -> {
                if (stopWatch.isSuspended()) {
                    stopWatch.resume();
                }
            }
            case STREAM_COMPLETE -> {
                stop();
                backend.dispatchMessage(MessageType.INFO, "*** " + Localization.getString("controller.finished.send")
                        + " " + Utils.formattedMillis(getDuration()) + "\n");
            }
            case STREAM_CANCELED -> stop();
        }
    }

    private void handleCommandEvent(CommandEvent event) {
        if (!isSending) {
            return;
        }

        GcodeCommand command = event.getCommand();
        if (event.getCommandEventType() == CommandEventType.COMMAND_SKIPPED) {
            dispatchedRows.incrementAndGet();
            completedRows.incrementAndGet();
            sampleProgress();
        } else if (command.isImmediate()) {
            // Immediate commands such as jogs aren't part of the program
        } else if (event.getCommandEventType() == CommandEventType.COMMAND_SENT) {
            sentRows.incrementAndGet();
            dispatchedRows.incrementAndGet();
        } else if (event.getCommandEventType() == CommandEventType.COMMAND_COMPLETE) {
            completedRows.incrementAndGet();
            sampleProgress();
        }
    }

    private void stop() {
        if (stopWatch.isStarted()) {
            stopWatch.stop();
        }
        isSending = false;
        hasFinishedSending = true;
    }

    private void reset() {
        stopWatch.reset();
        sentRows.set(0);
        dispatchedRows.set(0);
        completedRows.set(0);
        isSending = false;
        hasFinishedSending = false;
        correction = 1;
        totalEstimate = 0;
        nextCheckpointIndex = 0;
        firstSample = null;
        samples.clear();
    }

    private void loadStatistics(File processedGcodeFile) {
        if (processedGcodeFile == null) {
            return;
        }

        MachineLimits limits = getMachineLimits();
        usedLimits = limits;
        StopWatch calculationTime = StopWatch.createStarted();
        try (IGcodeStreamReader reader = new GcodeStreamReader(processedGcodeFile, new DefaultCommandCreator())) {
            numRows = reader.getNumRows();
            statistics = new GcodeStatisticsCalculator(limits).calculate(reader);
            logger.info(String.format(
                    "Gcode statistics: %d commands, %s mm rapid movements, %s mm cutting movements, estimated runtime %s using %s",
                    statistics.commandCount(),
                    Utils.formatter.format(statistics.rapidDistance()),
                    Utils.formatter.format(statistics.feedDistance()),
                    Utils.formattedMillis(statistics.totalDuration().toMillis()),
                    describe(limits)));
            logger.info(String.format("Took %d ms to calculate the statistics for %d rows", calculationTime.getTime(), numRows));
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format(
                    "Couldn't calculate the statistics for the processed gcode file, gave up after %d ms", calculationTime.getTime()), e);
            statistics = GcodeStatistics.EMPTY;
        }
    }

    private MachineLimits getMachineLimits() {
        if (backend.getController() == null) {
            return MachineLimits.withoutAcceleration(
                    DEFAULT_MAX_RATE_MM_PER_MINUTE, DEFAULT_MAX_RATE_MM_PER_MINUTE, Units.MM);
        }

        IFirmwareSettings firmwareSettings = backend.getController().getFirmwareSettings();
        double maximumRate = getMaximumMachineRate(firmwareSettings);
        double acceleration = getSlowestAcceleration(firmwareSettings);
        double junctionDeviation = getJunctionDeviation(firmwareSettings);
        if (acceleration > 0 && junctionDeviation <= 0) {
            junctionDeviation = MachineLimits.DEFAULT_JUNCTION_DEVIATION_MM;
        }

        return new MachineLimits(maximumRate, maximumRate, acceleration, junctionDeviation, Units.MM);
    }

    private static double getSlowestAcceleration(IFirmwareSettings firmwareSettings) {
        return Stream.of(Axis.X, Axis.Y, Axis.Z)
                .mapToDouble(axis -> getAcceleration(firmwareSettings, axis))
                .filter(acceleration -> acceleration > 0)
                .min()
                .orElse(0);
    }

    private static double getAcceleration(IFirmwareSettings firmwareSettings, Axis axis) {
        try {
            return firmwareSettings.getAcceleration(axis);
        } catch (FirmwareSettingsException e) {
            return 0;
        }
    }

    private static double getJunctionDeviation(IFirmwareSettings firmwareSettings) {
        try {
            return firmwareSettings.getJunctionDeviation();
        } catch (FirmwareSettingsException e) {
            return 0;
        }
    }

    private double getMaximumMachineRate(IFirmwareSettings firmwareSettings) {
        try {
            double maximumRate = Math.min(firmwareSettings.getMaximumRate(Axis.X),
                    Math.min(firmwareSettings.getMaximumRate(Axis.Y), firmwareSettings.getMaximumRate(Axis.Z)));
            return maximumRate > 0 ? maximumRate : DEFAULT_MAX_RATE_MM_PER_MINUTE;
        } catch (FirmwareSettingsException e) {
            return DEFAULT_MAX_RATE_MM_PER_MINUTE;
        }
    }

    private static String describe(MachineLimits limits) {
        if (!limits.hasAcceleration()) {
            return String.format("a maximum rate of %s mm/min and no known acceleration",
                    Utils.formatter.format(limits.maxFeedRate()));
        }

        return String.format("a maximum rate of %s mm/min, an acceleration of %s mm/s² and a junction deviation of %s mm",
                Utils.formatter.format(limits.maxFeedRate()),
                Utils.formatter.format(limits.acceleration()),
                Utils.formatter.format(limits.junctionDeviation()));
    }
}
