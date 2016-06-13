/**
 * Overrides any F commands with the given percentage.
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
public class FeedOverrideProcessor implements ICommandProcessor {
    private final double percentOverride;

    public FeedOverrideProcessor(double percentOverride) {
        this.percentOverride = percentOverride;
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) {
        List<String> ret = new ArrayList<>();
        if (percentOverride > 0) {
            ret.add(GcodePreprocessorUtils.overrideSpeed(command, percentOverride));
        } else {
            ret.add(command);
        }
        return ret;
    }
}