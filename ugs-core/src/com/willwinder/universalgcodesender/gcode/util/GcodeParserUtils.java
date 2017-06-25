/*
    Copyright 2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.util;

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author wwinder
 */
public class GcodeParserUtils {
    private static final Logger logger = Logger.getLogger(GcodeParserUtils.class.getName());

    /**
     * Helper method to apply processors to gcode.
     */
    public static void processAndExport(GcodeParser gcp, File input, File output)
            throws IOException, GcodeParserException {
        try(BufferedReader br = new BufferedReader(new FileReader(input))) {
            if (processAndExportGcodeStream(gcp, br, output)) {
                return;
            }
        }

        try(BufferedReader br = new BufferedReader(new FileReader(input))) {
            processAndExportText(gcp, br, output);
        }
    }

    /**
     * Common logic in processAndExport* methods.
     */
    private static void write(GcodeParser gcp, GcodeStreamWriter gsw, String original, String command, String comment, int idx) throws GcodeParserException {
        if (idx % 100000 == 0) {
            logger.log(Level.FINE, "gcode processing line: " + idx);
        }

        if (StringUtils.isEmpty(command)) {
            gsw.addLine(original, command, comment, idx);
        }
        else {
            // Parse the gcode for the buffer.
            Collection<String> lines = gcp.preprocessCommand(command, gcp.getCurrentState());

            for(String processedLine : lines) {
                gsw.addLine(original, processedLine, comment, idx);
            }

            gcp.addCommand(command);
        }
    }

    /**
     * Attempts to read the input file in GcodeStream format.
     * @return whether or not we succeed processing the file.
     */
    private static boolean processAndExportGcodeStream(GcodeParser gcp, BufferedReader input, File output)
            throws IOException, GcodeParserException {

        // Preprocess a GcodeStream file.
        try {
            GcodeStreamReader gsr = new GcodeStreamReader(input);
            try (GcodeStreamWriter gsw = new GcodeStreamWriter(output)) {
                int i = 0;
                while (gsr.getNumRowsRemaining() > 0) {
                    i++;
                    GcodeCommand gc = gsr.getNextCommand();
                    write(gcp, gsw, gc.getOriginalCommandString(), gc.getCommandString(), gc.getComment(), i);
                }

                // Done processing GcodeStream file.
                return true;
            }
        } catch (GcodeStreamReader.NotGcodeStreamFile ex) {
            // File exists, but isn't a stream reader. So go ahead and try parsing it as a raw gcode file.
        }
        return false;
    }

    /**
     * Attempts to read the input file in gcode-text format.
     * @return whether or not we succeed processing the file.
     */
    private static void processAndExportText(GcodeParser gcp, BufferedReader input, File output)
            throws IOException, GcodeParserException {
        // Preprocess a regular gcode file.
        try(BufferedReader br = input) {
            try (GcodeStreamWriter gsw = new GcodeStreamWriter(output)) {
                int i = 0;
                for(String line; (line = br.readLine()) != null; ) {
                    i++;

                    String comment = GcodePreprocessorUtils.parseComment(line);
                    String commentRemoved = GcodePreprocessorUtils.removeComment(line);

                    write(gcp, gsw, line, commentRemoved, comment, i);
                }
            }
        }
    }
}
