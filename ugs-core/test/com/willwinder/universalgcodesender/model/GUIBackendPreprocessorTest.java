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
package com.willwinder.universalgcodesender.model;

import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeWriter;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;

/**
 *
 * @author wwinder
 */
public class GUIBackendPreprocessorTest {
    
    CommandProcessor commandDoubler = new CommandProcessor() {
        @Override
        public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
            return ImmutableList.of(command, command);
        }

        @Override
        public String getHelp() {
            return "";
        }
    };
    
    Path tempDir;
    Path inputFile, outputFile;

    @Before
    public void setup() throws IOException {
        tempDir = Files.createTempDirectory("tempfiles");
        inputFile = Files.createTempFile(tempDir, "tempfiles", ".tmp");
        outputFile = Files.createTempFile(tempDir, "tempfiles", ".tmp");
    }

    @After
    public void teardown() throws IOException {
        FileUtils.deleteDirectory(tempDir.toFile());
    }

    /**
     * Test of preprocessAndExportToFile method, of class GUIBackend.
     */
    @Test
    public void testRegularPreprocessAndExportToFile() throws Exception {
        System.out.println("regularPreprocessAndExportToFile");
        GUIBackend backend = new GUIBackend();
        GcodeParser gcp = new GcodeParser();
        // Double all the commands that go in.
        gcp.addCommandProcessor(commandDoubler);
        gcp.addCommandProcessor(new CommentProcessor());

        // Create input file, comment-only line shouldn't be processed twice.
        List<String> lines = Arrays.asList("line one", "; comment", "line two");
        Files.write(inputFile, lines, Charset.defaultCharset(), StandardOpenOption.WRITE);

        try (IGcodeWriter gcw = new GcodeStreamWriter(outputFile.toFile())) {
            backend.preprocessAndExportToFile(gcp, inputFile.toFile(), gcw);
        }

        List<String> expectedResults = Arrays.asList("line one", "line one", "", "", "line two", "line two");

        try (IGcodeStreamReader reader = new GcodeStreamReader(outputFile.toFile())) {
            Assert.assertEquals(expectedResults.size(), reader.getNumRows());

            for (String expected : expectedResults) {
                Assert.assertEquals(expected, reader.getNextCommand().getCommandString());
            }
        }
    }

    @Test
    public void testGcodeStreamPreprocessAndExportToFile() throws Exception {
        System.out.println("gcodeStreamPreprocessAndExportToFile");
        GUIBackend backend = new GUIBackend();
        GcodeParser gcp = new GcodeParser();

        // Double all the commands that go in.
        gcp.addCommandProcessor(commandDoubler);

        // Create GcodeStream input file by putting it through the preprocessor.
        List<String> lines = Arrays.asList("line one", "line two");
        Files.write(outputFile, lines, Charset.defaultCharset(), StandardOpenOption.WRITE);
        try (IGcodeWriter gcw = new GcodeStreamWriter(inputFile.toFile())) {
            backend.preprocessAndExportToFile(gcp, outputFile.toFile(), gcw);
        }

         
        // Pass a gcodestream into the function
        try (IGcodeWriter gcw = new GcodeStreamWriter(outputFile.toFile())) {
            backend.preprocessAndExportToFile(gcp, inputFile.toFile(), gcw);
        }

        List<String> expectedResults = Arrays.asList(
                "line one", "line one", "line one", "line one", 
                "line two", "line two", "line two", "line two");

        try (IGcodeStreamReader reader = new GcodeStreamReader(outputFile.toFile())) {
            Assert.assertEquals(expectedResults.size(), reader.getNumRows());

            for (String expected : expectedResults) {
                Assert.assertEquals(expected, reader.getNextCommand().getCommandString());
            }
        }
    }
}
