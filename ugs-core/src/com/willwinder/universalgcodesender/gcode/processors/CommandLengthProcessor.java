/**
 * Throws an exception if the command is too long.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class CommandLengthProcessor implements CommandProcessor {
    final private int length;
    public CommandLengthProcessor(int length) {
        this.length = length;
    }

    @Override
    public String getHelp() {
        return Localization.getString("sender.help.command.length") + "\n" +
                Localization.getString("sender.command.length")
                + ": " + length;
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        if (command.length() > length)
            throw new GcodeParserException("Command '" + command + "' is longer than " + length + " characters.");

        List<String> ret = new ArrayList<>();
        ret.add(command);
        return ret;
    }
    
}
