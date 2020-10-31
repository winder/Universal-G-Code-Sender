package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class CommandProcessorList implements CommandProcessor, Iterable<CommandProcessor> {

    private List<CommandProcessor> commandProcessors = new ArrayList<>();

    /**
     * Applies all command processors to a given command and returns the
     * resulting GCode. Does not change the parser state.
     *
     * TODO: Rather than have a separate 'preprocessCommand' which needs to be
     * followed up with calls to addCommand, it would be great to have addCommand
     * also do the preprocessing. This is challenging because they have different
     * return types.
     *
     * This is also needed for some very particular processing in GUIBackend which
     * gathers comments as a separate step outside the GcodeParser.
     */
    @Override
    public List<String> processCommand(String command, final GcodeState initialState) throws GcodeParserException {
        List<String> ret = new ArrayList<>();
        ret.add(command);
        GcodeState tempState;
        for (CommandProcessor p : commandProcessors) {
            // Reset point segments after each pass. The final pass is what we will return.
            tempState = initialState.copy();
            // Process each command in the list and add results to the end.
            // Don't re-process the results with the same preprocessor.
            for (int i = ret.size(); i > 0; i--) {
                // The arc expander changes the lastGcodeCommand which causes the following to fail:
                // G2 Y-0.7 J-14.7
                // Y28.7 J14.7 (this line treated as a G1)
                tempState.currentMotionMode = initialState.currentMotionMode;
                List<String> intermediate = p.processCommand(ret.remove(0), tempState);

                // process results to update the state and collect PointSegments
                for(String c : intermediate) {
                    tempState = testState(c, tempState);
                }

                ret.addAll(intermediate);
            }
        }

        return ret;
    }

    @Override
    public String getHelp() {
        return "Combines several processors and runs them in sequence";
    }

    /**
     * Helper to statically process the next step in a program without modifying the parser.
     */
    static private GcodeState testState(String command, GcodeState state) throws GcodeParserException {
        GcodeState ret = state;

        // Add command get meta doesn't update the state, so we need to do that manually.
        Collection<GcodeParser.GcodeMeta> metaObjects = GcodeParserUtils.processCommand(command, 0, state);
        if (metaObjects != null) {
            for (GcodeParser.GcodeMeta c : metaObjects) {
                if (c.state != null) {
                    ret = c.state;
                }
            }
        }

        return ret;
    }

    public void add(CommandProcessor processor) {
        if(!commandProcessors.contains(processor)) {
            commandProcessors.add(processor);
        }
    }

    public void remove(CommandProcessor processor) {
        commandProcessors.remove(processor);
    }

    public int size() {
        return commandProcessors.size();
    }

    public void clear() {
        commandProcessors.clear();
    }

    @Override
    public Iterator<CommandProcessor> iterator() {
        return commandProcessors.iterator();
    }

    @Override
    public void forEach(Consumer<? super CommandProcessor> action) {
        commandProcessors.forEach(action);
    }

    @Override
    public Spliterator<CommandProcessor> spliterator() {
        return commandProcessors.spliterator();
    }
}
