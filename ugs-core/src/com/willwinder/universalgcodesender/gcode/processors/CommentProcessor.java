/**
 * Removes comments from the command.
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
public class CommentProcessor implements ICommandProcessor {

    @Override
    public List<String> processCommand(String command, GcodeState state) {
        ArrayList<String> result = new ArrayList<>();
        result.add(GcodePreprocessorUtils.removeComment(command));
        return result;
    }
}
