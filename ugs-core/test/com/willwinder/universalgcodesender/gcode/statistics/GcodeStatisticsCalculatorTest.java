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
package com.willwinder.universalgcodesender.gcode.statistics;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * @author Joacim Breiler
 */
public class GcodeStatisticsCalculatorTest {
    private static final double MAX_FEED_RATE = 1000;
    private static final double MAX_RAPID_RATE = 5000;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void calculate_shouldSeparateRapidAndCuttingDistances() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G21", "G0X10", "G1X20F1000", "G0Y10"));

        assertThat(statistics.rapidDistance()).isCloseTo(20, within(0.0001));
        assertThat(statistics.feedDistance()).isCloseTo(10, within(0.0001));
        assertThat(statistics.totalDistance()).isCloseTo(30, within(0.0001));
    }

    @Test
    public void calculate_shouldCountProcessedCommands() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G21", "", "G0X10", "; only a comment", "G1X20F1000"));

        assertThat(statistics.commandCount()).isEqualTo(3);
    }

    @Test
    public void calculate_shouldEstimateRuntimeUsingRapidAndFeedRates() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G0X100", "G1X200F1000"));

        assertThat(statistics.rapidDuration().toMillis()).isEqualTo(1200);
        assertThat(statistics.feedDuration().toMillis()).isEqualTo(6000);
        assertThat(statistics.totalDuration().toMillis()).isEqualTo(7200);
    }

    @Test
    public void calculate_shouldCapFeedRateToTheMachineMaximum() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G1X100F10000"));

        assertThat(statistics.feedDuration().toMillis()).isEqualTo(6000);
    }

    @Test
    public void calculate_shouldUseTheMaximumFeedRateWhenNoFeedRateIsSet() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G1X100"));

        assertThat(statistics.feedDuration().toMillis()).isEqualTo(6000);
    }

    @Test
    public void calculate_shouldUseTheArcLengthForArcMovements() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G17", "G2X10Y10I10J0F1000"));

        assertThat(statistics.feedDistance()).isCloseTo(10 * Math.PI / 2, within(0.0001));
    }

    @Test
    public void calculate_shouldConvertImperialProgramsToMillimeters() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G20", "G1X1F10"));

        assertThat(statistics.feedDistance()).isCloseTo(25.4, within(0.0001));
        assertThat(statistics.feedDistance(Units.INCH)).isCloseTo(1, within(0.0001));
        assertThat(statistics.feedDuration().toMillis()).isEqualTo(6000);
    }

    @Test
    public void calculate_shouldUseTheSweptAngleAsDistanceForRotations() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        GcodeStatistics statistics = calculator.calculate(commands("G1A90F1000"));

        assertThat(statistics.feedDistance()).isCloseTo(90, within(0.0001));
    }

    @Test
    public void calculate_shouldRecordCheckpointsOverTheProgram() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculator();

        // Ten movements of 1000 mm at 1000 mm/min, a minute each
        GcodeStatistics statistics = calculator.calculate(longProgram());

        assertThat(statistics.checkpoints()).hasSize(10);
        assertThat(statistics.checkpoints()).extracting(RuntimeCheckpoint::row).isSorted();
        assertThat(statistics.checkpoints()).extracting(RuntimeCheckpoint::elapsedMillis).isSorted();

        RuntimeCheckpoint last = statistics.checkpoints().get(statistics.checkpoints().size() - 1);
        assertThat(last.row()).isEqualTo(10);
        assertThat(last.elapsedMillis()).isEqualTo(statistics.totalDuration().toMillis());
    }

    @Test
    public void calculate_shouldReadPlainGcodeFilesAndIgnoreComments() throws GcodeParserException, IOException {
        GcodeStatisticsCalculator calculator = createCalculator();
        File file = createGcodeFile("(a header comment)", "G21 G90", "G0X10 ; move into position", "G1X20F1000");

        GcodeStatistics statistics = calculator.calculate(file);

        assertThat(statistics.rapidDistance()).isCloseTo(10, within(0.0001));
        assertThat(statistics.feedDistance()).isCloseTo(10, within(0.0001));
    }

    @Test
    public void calculate_shouldAddTheTimeSpentAcceleratingUpToTheFeedRate() throws GcodeParserException {
        // 100 mm at 1000 mm/min with an acceleration of 500 mm/s² reaches the feed rate, so it
        // takes 100/16.667 s of travel plus 16.667/500 s of accelerating and braking
        GcodeStatisticsCalculator calculator = createCalculatorWithAcceleration(500, 0.01);

        GcodeStatistics statistics = calculator.calculate(commands("G1X100F1000"));

        assertThat(statistics.feedDuration().toMillis()).isCloseTo(6033, within(2L));
    }

    @Test
    public void calculate_shouldNeverReachTheFeedRateOnAShortMovement() throws GcodeParserException {
        // 0.1 mm is far too short to reach 1000 mm/min, so it becomes a triangular profile taking
        // 2*sqrt(0.1/500) s instead of the 6 ms that the feed rate alone would suggest
        GcodeStatisticsCalculator calculator = createCalculatorWithAcceleration(500, 0.01);

        GcodeStatistics statistics = calculator.calculate(commands("G1X0.1F1000"));

        assertThat(statistics.feedDuration().toMillis()).isCloseTo(28, within(1L));
    }

    @Test
    public void calculate_shouldCarryTheVelocityThroughMovementsInTheSameDirection() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculatorWithAcceleration(500, 0.01);

        GcodeStatistics statistics = calculator.calculate(commands("G1X50F1000", "G1X100F1000"));

        assertThat(statistics.feedDuration().toMillis()).isCloseTo(6033, within(2L));
    }

    @Test
    public void calculate_shouldBrakeToAStandstillWhenTheDirectionIsReversed() throws GcodeParserException {
        // Four 0.1 mm movements that reverse into each other, each having to start and stop
        GcodeStatisticsCalculator calculator = createCalculatorWithAcceleration(500, 0.01);

        GcodeStatistics statistics = calculator.calculate(
                commands("G1X0.1F1000", "G1X0F1000", "G1X0.1F1000", "G1X0F1000"));

        assertThat(statistics.feedDuration().toMillis()).isCloseTo(113, within(2L));
    }

    @Test
    public void calculate_shouldAccelerateRapidMovementsTowardsTheRapidRate() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculatorWithAcceleration(500, 0.01);

        GcodeStatistics statistics = calculator.calculate(commands("G0X100"));

        assertThat(statistics.rapidDuration().toMillis()).isCloseTo(1367, within(2L));
    }

    @Test
    public void calculate_shouldNotLetTheAccelerationChangeTheDistances() throws GcodeParserException {
        GcodeStatisticsCalculator calculator = createCalculatorWithAcceleration(500, 0.01);

        GcodeStatistics statistics = calculator.calculate(commands("G0X10", "G1X20F1000"));

        assertThat(statistics.rapidDistance()).isCloseTo(10, within(0.0001));
        assertThat(statistics.feedDistance()).isCloseTo(10, within(0.0001));
    }

    private static GcodeStatisticsCalculator createCalculator() {
        return new GcodeStatisticsCalculator(
                MachineLimits.withoutAcceleration(MAX_FEED_RATE, MAX_RAPID_RATE, Units.MM));
    }

    private static GcodeStatisticsCalculator createCalculatorWithAcceleration(double acceleration, double junctionDeviation) {
        return new GcodeStatisticsCalculator(
                new MachineLimits(MAX_FEED_RATE, MAX_RAPID_RATE, acceleration, junctionDeviation, Units.MM));
    }

    private static List<String> longProgram() {
        List<String> commands = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            commands.add("G1X" + (i * 1000) + "F1000");
        }
        return commands;
    }

    private static List<String> commands(String... commands) {
        return Arrays.asList(commands);
    }

    private File createGcodeFile(String... lines) throws IOException {
        File file = temporaryFolder.newFile("program.gcode");
        Files.write(file.toPath(), Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }
}
