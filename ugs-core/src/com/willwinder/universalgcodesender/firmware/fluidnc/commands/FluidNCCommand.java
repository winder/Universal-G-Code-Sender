package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * A generic command that expects it to end with a command status "ok" or "error" to consider the command done
 */
public class FluidNCCommand extends GcodeCommand {
    public FluidNCCommand(String command) {
        super(command);
    }

    @Override
    public void setResponse(String response) {
        super.setResponse("");
        appendResponse(response);
    }

    @Override
    public void appendResponse(String response) {
        // In some cases the controller will echo the commands sent, do not add those to the response.
        if (response.equals(getOriginalCommandString())) {
            return;
        }

        super.appendResponse(response);

        if (response.startsWith("ok")) {
            setDone(true);
            setOk(true);
        }

        if (response.startsWith("error")) {
            setDone(true);
            setOk(false);
            setError(true);
        }
    }
}
