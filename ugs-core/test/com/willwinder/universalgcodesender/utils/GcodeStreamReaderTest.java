package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GcodeStreamReaderTest {

    private InputStream stringToStream(String data) {
        return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = GcodeStreamReader.NotGcodeStreamFile.class)
    public void readingEmptyFileShouldThrowNotGcodeStreamException() throws GcodeStreamReader.NotGcodeStreamFile {
        new GcodeStreamReader(stringToStream(""), new DefaultCommandCreator());
    }

    @Test
    public void readingEmptyPreprocessedFile() throws GcodeStreamReader.NotGcodeStreamFile, IOException {
        GcodeStreamReader gcodeStreamReader = new GcodeStreamReader(stringToStream("gsw_meta:0"), new DefaultCommandCreator());
        assertEquals(0, gcodeStreamReader.getNumRows());
        assertEquals(0, gcodeStreamReader.getNumRowsRemaining());
        assertNull(gcodeStreamReader.getNextCommand());
    }

    @Test
    public void readingPreprocessedFileShouldReturnCommand() throws GcodeStreamReader.NotGcodeStreamFile, IOException {
        GcodeStreamReader gcodeStreamReader = new GcodeStreamReader(stringToStream("gsw_meta:1\n" + "G01; test" + GcodeStream.FIELD_SEPARATOR + "G01" + GcodeStream.FIELD_SEPARATOR + "1" + GcodeStream.FIELD_SEPARATOR + "test"), new DefaultCommandCreator());
        assertEquals(1, gcodeStreamReader.getNumRows());
        assertEquals(1, gcodeStreamReader.getNumRowsRemaining());

        GcodeCommand nextCommand = gcodeStreamReader.getNextCommand();
        assertEquals(1, nextCommand.getCommandNumber());
        assertEquals("G01; test", nextCommand.getOriginalCommandString());
        assertEquals("G01", nextCommand.getCommandString());
        assertEquals("test", nextCommand.getComment());

        assertEquals(1, gcodeStreamReader.getNumRows());
        assertEquals(0, gcodeStreamReader.getNumRowsRemaining());
    }
}
