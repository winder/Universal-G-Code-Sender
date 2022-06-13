package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

/**
 * The system command is used for backend operations. This will cause the command to only log with verbose logging.
 */
public class SystemCommand extends FluidNCCommand {
    public SystemCommand(String command) {
        super(command);
        setGenerated(true);
    }
}
