/**
 * Removes any M30 commands.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class M30Processor implements ICommandProcessor {
    @Override
    public List<String> processCommand(String command, GcodeState state) {
        List<String> ret = new ArrayList<>();
        ret.add(GcodePreprocessorUtils.removeM30(command));
        return ret;
    }
}
