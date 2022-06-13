package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.GrblUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public class GetParserStateCommand extends SystemCommand {
    private String state;

    public GetParserStateCommand() {
        super("$GCode/Modes");
    }

    @Override
    public void appendResponse(String response) {
        super.appendResponse(response);
        if (isDone() && isOk()) {
            Arrays.stream(StringUtils.split(getResponse(), "\n")).forEach(line -> {
                if(GrblUtils.isGrblFeedbackMessageV1(line)) {
                    this.state = GrblUtils.parseFeedbackMessageV1(line);
                }
            });
        }
    }

    public Optional<String> getState() {
        return Optional.ofNullable(state);
    }
}
