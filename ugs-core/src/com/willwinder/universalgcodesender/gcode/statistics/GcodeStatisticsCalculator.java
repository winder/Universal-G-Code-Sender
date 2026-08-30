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

import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

/**
 * Calculates statistics for a gcode program, such as how far the machine will travel using rapid
 * and cutting movements and roughly how long the program will take to run.
 * <p>
 * Every movement has a target velocity, which is the programmed feed rate capped by the machines
 * maximum feed rate, or the maximum rapid rate for rapid movements. When the machines acceleration
 * is known the movement is planned as a trapezoidal velocity profile that accelerates towards that
 * target and brakes for the next corner, which matters a great deal for programs built from short
 * segments where the target velocity is never reached. Without an acceleration the movement is
 * assumed to be made at its target velocity from end to end.
 *
 * @author Joacim Breiler
 */
public class GcodeStatisticsCalculator {

    private final MachineLimits limits;

    /**
     * Creates a calculator.
     *
     * @param limits the motion limits of the machine
     */
    public GcodeStatisticsCalculator(MachineLimits limits) {
        if (limits.maxFeedRate() <= 0 || limits.maxRapidRate() <= 0) {
            throw new IllegalArgumentException("The maximum feed rate and rapid rate must be larger than zero");
        }

        this.limits = inMillimeters(limits);
    }

    /**
     * Calculates the statistics for a file, which may either be in the gcode stream format or
     * plain gcode text.
     *
     * @param file the file to calculate statistics for
     * @return the statistics for the program
     */
    public GcodeStatistics calculate(File file) throws IOException, GcodeParserException {
        try (InputStream inputStream = new FileInputStream(file);
             IGcodeStreamReader reader = new GcodeStreamReader(inputStream, new DefaultCommandCreator())) {
            return calculate(reader);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            // The file exists but isn't a gcode stream, parse it as plain gcode text instead
        }

        Accumulator accumulator = new Accumulator(limits);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null; ) {
                accumulator.add(GcodePreprocessorUtils.removeComment(line));
            }
        }
        return accumulator.getStatistics();
    }

    /**
     * Calculates the statistics for a loaded gcode stream. The stream will be consumed but not
     * closed by this method.
     *
     * @param reader the stream to calculate statistics for
     * @return the statistics for the program
     */
    public GcodeStatistics calculate(IGcodeStreamReader reader) throws IOException, GcodeParserException {
        Accumulator accumulator = new Accumulator(limits);
        while (reader.getNumRowsRemaining() > 0) {
            accumulator.add(reader.getNextCommand().getCommandString());
        }
        return accumulator.getStatistics();
    }

    /**
     * Calculates the statistics for a list of gcode commands.
     *
     * @param commands the commands to calculate statistics for
     * @return the statistics for the program
     */
    public GcodeStatistics calculate(Collection<String> commands) throws GcodeParserException {
        Accumulator accumulator = new Accumulator(limits);
        for (String command : commands) {
            accumulator.add(GcodePreprocessorUtils.removeComment(command));
        }
        return accumulator.getStatistics();
    }

    // Everything is measured in millimeters once a program is being read, so the limits are
    // converted once instead of for every movement
    private static MachineLimits inMillimeters(MachineLimits limits) {
        double scale = UnitUtils.scaleUnits(limits.units(), Units.MM);
        return new MachineLimits(
                limits.maxFeedRate() * scale,
                limits.maxRapidRate() * scale,
                Math.max(0, limits.acceleration()) * scale,
                Math.max(0, limits.junctionDeviation()) * scale,
                Units.MM);
    }
}
