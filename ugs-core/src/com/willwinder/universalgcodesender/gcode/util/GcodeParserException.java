/*
 */
package com.willwinder.universalgcodesender.gcode.util;

/**
 *
 * @author wwinder
 */
public class GcodeParserException extends Exception {
    public GcodeParserException(String message) {
        super(message);
    }

    public GcodeParserException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
