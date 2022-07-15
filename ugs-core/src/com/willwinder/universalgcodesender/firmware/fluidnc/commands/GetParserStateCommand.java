package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.GrblUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class GetParserStateCommand extends SystemCommand {

    public GetParserStateCommand() {
        super("$GCode/Modes");
    }

    public Optional<String> getState() {
        String state = null;
        for (String line : StringUtils.split(getResponse(), "\n")) {
            if (GrblUtils.isGrblFeedbackMessageV1(line)) {
                state = GrblUtils.parseFeedbackMessageV1(line);
            }
        }
        return Optional.ofNullable(state);
    }
}
