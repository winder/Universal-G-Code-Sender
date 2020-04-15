package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.io.Closeable;
import java.io.IOException;

public interface IGcodeWriter extends Closeable {
    public String getCanonicalPath() throws IOException;
    public void addLine(GcodeCommand command);
    public void addLine(String original, String processed, String comment, int commandNumber);
}
