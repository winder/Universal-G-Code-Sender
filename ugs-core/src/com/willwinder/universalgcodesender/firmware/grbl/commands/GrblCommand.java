package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.GrblUtils.isGrblStatusString;

public class GrblCommand extends GcodeCommand {
    public GrblCommand(String command) {
        super(command);
    }

    public GrblCommand(String command, String originalCommand, String comment, int commandNumber) {
        super(command, originalCommand, comment, commandNumber);
    }

    @Override
    public void appendResponse(String response) {
        // Do not append status strings to non status commands
        if (!StringUtils.equals(getCommandString(), "?") && isGrblStatusString(response)) {
            return;
        }

        super.appendResponse(response);
    }
}
