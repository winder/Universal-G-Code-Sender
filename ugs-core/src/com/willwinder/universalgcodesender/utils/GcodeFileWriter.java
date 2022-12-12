package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class GcodeFileWriter implements IGcodeWriter {
    private final File file;
    private final PrintWriter fileWriter;

    public GcodeFileWriter(File f) throws FileNotFoundException, UnsupportedEncodingException {
        file = f;
        fileWriter = new PrintWriter(f, StandardCharsets.UTF_8.name());
    }

    @Override
    public String getCanonicalPath() throws IOException {
        return file.getCanonicalPath();
    }

    @Override
    public void addLine(GcodeCommand command) {
        addLine(command.getOriginalCommandString(), command.getCommandString(), command.getComment(), command.getCommandNumber());
    }

    @Override
    public void addLine(String original, String processed, String comment, int commandNumber) {
        if (StringUtils.isNotEmpty(comment)) {
            fileWriter.println((processed + " (" + comment + ")").trim());
        } else {
            fileWriter.println(processed);
        }
    }

    @Override
    public void close() throws IOException {
        fileWriter.close();
    }
}
