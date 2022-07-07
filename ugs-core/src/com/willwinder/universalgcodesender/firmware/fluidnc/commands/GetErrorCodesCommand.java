package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetErrorCodesCommand extends SystemCommand {
    public GetErrorCodesCommand() {
        super("$Errors/List");
    }

    public Map<Integer, String> getErrorCodes() {
        Map<Integer, String> errorCodes = new HashMap<>();
        Pattern errorCodePattern = Pattern.compile("([0-9]+):(.*)");
        Arrays.stream(StringUtils.split(getResponse(), "\n")).forEach(line -> {
            Matcher matcher = errorCodePattern.matcher(line);
            if (matcher.find()) {
                errorCodes.put(Integer.parseInt(matcher.group(1)), matcher.group(2));
            }
        });
        return errorCodes;
    }
}
