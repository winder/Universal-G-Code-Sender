/**
 * Truncates decimals to a configurable amount.
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
public class DecimalProcessor implements ICommandProcessor {
    private final int numDecimals;

    public DecimalProcessor(int numDecimals) {
        this.numDecimals = numDecimals;
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) {
        List<String> ret = new ArrayList<>();
        if (numDecimals > 0) {
            ret.add(GcodePreprocessorUtils.truncateDecimals(numDecimals, command));
        } else {
            ret.add(command);
        }
        return ret;
    }
}
