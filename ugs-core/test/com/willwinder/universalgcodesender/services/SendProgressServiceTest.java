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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.gcode.statistics.GcodeStatistics;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.within;

/**
 * @author Joacim Breiler
 */
public class SendProgressServiceTest {

    private static final int PROGRAM_ROWS = 100;
    private static final int ROWS_PER_CHECKPOINT = 10;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private GUIBackend backend;
    private SendProgressService service;
    private IFirmwareSettings firmwareSettings;

    @Before
    public void setUp() throws Exception {
        backend = spy(new GUIBackend());
        backend.applySettings(new Settings());

        firmwareSettings = mock(IFirmwareSettings.class);
        IController controller = mock(IController.class);
        when(controller.getFirmwareSettings()).thenReturn(firmwareSettings);
        connectedController = controller;

        service = new SendProgressService(backend);
    }

    private IController connectedController;

    private void connectMachine() {
        doReturn(connectedController).when(backend).getController();
    }

    @Test
    public void getNumRows_shouldBeZeroWhenNoFileIsLoaded() {
        assertThat(service.getNumRows()).isZero();
        assertThat(service.getNumCompletedRows()).isZero();
        assertThat(service.getNumRemainingRows()).isZero();
    }

    @Test
    public void getNumRows_shouldBeTheRowsOfTheLoadedFile() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));

        assertThat(service.getNumRows()).isEqualTo(3);
        assertThat(service.getNumRemainingRows()).isEqualTo(3);
    }

    @Test
    public void getNumCompletedRows_shouldCountCompletedAndSkippedRowsWhileStreaming() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));
        startStream();

        service.UGSEvent(commandEvent(CommandEventType.COMMAND_COMPLETE));
        service.UGSEvent(commandEvent(CommandEventType.COMMAND_SKIPPED));

        assertThat(service.getNumCompletedRows()).isEqualTo(2);
        assertThat(service.getNumRemainingRows()).isEqualTo(1);
    }

    @Test
    public void getNumSentRows_shouldCountSentRowsWhileStreaming() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));
        startStream();

        service.UGSEvent(commandEvent(CommandEventType.COMMAND_SENT));
        service.UGSEvent(commandEvent(CommandEventType.COMMAND_SENT));

        assertThat(service.getNumSentRows()).isEqualTo(2);
    }

    @Test
    public void getNumCompletedRows_shouldNotCountCommandsSentOutsideOfAStream() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));

        service.UGSEvent(commandEvent(CommandEventType.COMMAND_COMPLETE));

        assertThat(service.getNumCompletedRows()).isZero();
    }

    @Test
    public void getNumCompletedRows_shouldNotCountImmediateCommands() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));
        startStream();

        service.UGSEvent(new CommandEvent(CommandEventType.COMMAND_COMPLETE, immediateCommand()));

        assertThat(service.getNumCompletedRows()).isZero();
    }

    @Test
    public void getDuration_shouldBeZeroBeforeAnyStream() {
        assertThat(service.getDuration()).isZero();
    }

    @Test
    public void getDuration_shouldStopIncreasingWhenTheStreamCompletes() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100"));
        startStream();
        service.UGSEvent(new StreamEvent(StreamEventType.STREAM_COMPLETE));

        long duration = service.getDuration();
        Thread.sleep(50);

        assertThat(service.getDuration()).isEqualTo(duration);
    }

    @Test
    public void getRemainingDuration_shouldNotBeEstimatedWithoutAFile() {
        assertThat(service.getRemainingDuration()).isEqualTo(-1);
    }

    @Test
    public void getRemainingDuration_shouldEstimateTheEntireFileBeforeStreaming() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));

        long remainingDuration = service.getRemainingDuration();

        assertThat(remainingDuration).isEqualTo(service.getStatistics().totalDuration().toMillis());
    }

    @Test
    public void getRemainingDuration_shouldNotBeCorrectedFromASingleCheckpoint() throws Exception {
        backend.setGcodeFile(createLongProgram());
        long estimatedDuration = service.getStatistics().totalDuration().toMillis();
        startStream();

        // A single checkpoint says nothing about the pace, as the controller reports rows as
        // completed ahead of running them
        service.UGSEvent(commandEvent(CommandEventType.COMMAND_COMPLETE));

        assertThat(service.getRemainingDuration()).isCloseTo(estimatedDuration, within(500L));
    }

    @Test
    public void getCorrection_shouldBeMeasuredBetweenCheckpoints() throws Exception {
        backend.setGcodeFile(createLongProgram());
        startStream();

        // Each row is estimated to take a minute but is completed straight away
        passCheckpoints(2);

        assertThat(service.getCorrection()).isLessThan(1);
    }

    @Test
    public void getCorrection_shouldNotBeMeasuredFromASingleCheckpoint() throws Exception {
        backend.setGcodeFile(createLongProgram());
        startStream();

        passCheckpoints(1);

        assertThat(service.getCorrection()).isEqualTo(1);
    }

    @Test
    public void getCorrection_shouldOnlyBeRecalculatedWhenACheckpointIsPassed() throws Exception {
        backend.setGcodeFile(createLongProgram());
        startStream();
        passCheckpoints(2);
        double correction = service.getCorrection();

        Thread.sleep(100);

        assertThat(service.getCorrection()).isEqualTo(correction);
    }

    @Test
    public void getTotalDuration_shouldHoldStillBetweenCheckpoints() throws Exception {
        backend.setGcodeFile(createLongProgram());
        startStream();
        service.UGSEvent(commandEvent(CommandEventType.COMMAND_COMPLETE));
        long totalDuration = service.getTotalDuration();

        Thread.sleep(200);

        assertThat(service.getTotalDuration()).isEqualTo(totalDuration);
    }

    @Test
    public void getTotalDuration_shouldNotBeRecalculatedOnceEverythingHasBeenSent() throws Exception {
        backend.setGcodeFile(createLongProgram());
        startStream();
        passCheckpoints(2);
        long totalDuration = service.getTotalDuration();

        // Everything has been handed over, so the controller acknowledges the rest as fast as it
        // can read it rather than as fast as it runs it
        for (int i = 0; i < PROGRAM_ROWS; i++) {
            service.UGSEvent(commandEvent(CommandEventType.COMMAND_SENT));
        }
        Thread.sleep(50);
        service.UGSEvent(commandEvent(CommandEventType.COMMAND_COMPLETE));

        assertThat(service.getTotalDuration()).isEqualTo(totalDuration);
    }

    @Test
    public void getTotalDuration_shouldNotBeRecalculatedFromTheRowsAtTheEndOfTheProgram() throws Exception {
        backend.setGcodeFile(createLongProgram());
        startStream();
        passCheckpoints(6);
        long totalDuration = service.getTotalDuration();

        // The remaining checkpoints are all within the rows that a controller keeps queued up
        passCheckpoints(3);

        assertThat(service.getTotalDuration()).isEqualTo(totalDuration);
    }

    @Test
    public void getTotalDuration_shouldNotBeEstimatedWithoutAFile() {
        assertThat(service.getTotalDuration()).isEqualTo(-1);
    }

    @Test
    public void getTotalDuration_shouldBeTheEstimatedRuntimeBeforeStreaming() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));

        assertThat(service.getTotalDuration())
                .isEqualTo(service.getStatistics().totalDuration().toMillis());
    }

    @Test
    public void getTotalDuration_shouldCoverTheTimeSpentAndTheTimeLeft() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));
        startStream();

        Thread.sleep(100);

        assertThat(service.getTotalDuration())
                .isEqualTo(service.getDuration() + service.getRemainingDuration());
    }

    @Test
    public void connectingAMachineShouldRecalculateTheStatistics() throws Exception {
        backend.setGcodeFile(createLongProgram());
        long withoutMachineLimits = service.getStatistics().totalDuration().toMillis();

        // The machine reports that it accelerates slowly, which makes the program take longer
        when(firmwareSettings.getAcceleration(any())).thenReturn(20d);
        when(firmwareSettings.getJunctionDeviation()).thenReturn(0.01);
        connectMachine();
        service.UGSEvent(new ControllerStateEvent(ControllerState.IDLE, ControllerState.CONNECTING));

        ThreadHelper.waitUntil(
                () -> service.getStatistics().totalDuration().toMillis() != withoutMachineLimits,
                5, TimeUnit.SECONDS);

        assertThat(service.getStatistics().totalDuration().toMillis()).isGreaterThan(withoutMachineLimits);
    }

    @Test
    public void connectingAMachineShouldNotRecalculateWhenTheLimitsAreUnchanged() throws Exception {
        backend.setGcodeFile(createLongProgram());
        GcodeStatistics statistics = service.getStatistics();

        service.UGSEvent(new ControllerStateEvent(ControllerState.IDLE, ControllerState.CONNECTING));
        Thread.sleep(1500);

        assertThat(service.getStatistics()).isSameAs(statistics);
    }

    @Test
    public void unloadingAFileShouldResetTheProgress() throws Exception {
        backend.setGcodeFile(createGcodeFile("G21", "G0X100", "G1X200F1000"));

        backend.unsetGcodeFile();

        assertThat(service.getNumRows()).isZero();
        assertThat(service.getStatistics().totalDuration()).isZero();
        assertThat(service.getRemainingDuration()).isEqualTo(-1);
    }

    private void passCheckpoints(int count) throws InterruptedException {
        for (int i = 0; i < count; i++) {
            Thread.sleep(50);
            for (int row = 0; row < ROWS_PER_CHECKPOINT; row++) {
                service.UGSEvent(commandEvent(CommandEventType.COMMAND_COMPLETE));
            }
        }
    }

    private void startStream() {
        service.UGSEvent(new FileStateEvent(FileState.FILE_LOADED));
        service.UGSEvent(new StreamEvent(StreamEventType.STREAM_STARTED));
    }

    private static CommandEvent commandEvent(CommandEventType type) {
        return new CommandEvent(type, new GcodeCommand("G0X1"));
    }

    private static GcodeCommand immediateCommand() {
        GcodeCommand command = new GcodeCommand("G0X1");
        command.setImmediate(true);
        return command;
    }

    /**
     * Creates a program of a hundred rows taking six seconds each, so that a checkpoint lands
     * every ten rows and there are rows enough for most of them to be measurable.
     */
    private File createLongProgram() throws IOException {
        List<String> commands = new ArrayList<>();
        for (int i = 1; i <= PROGRAM_ROWS; i++) {
            commands.add("G1X" + (i * 300) + "F6000");
        }
        return createGcodeFile(commands.toArray(new String[0]));
    }

    private File createGcodeFile(String... lines) throws IOException {
        File file = temporaryFolder.newFile("program.gcode");
        Files.write(file.toPath(), Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }
}
