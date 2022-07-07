package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetAlarmCodesCommand extends SystemCommand {
    public GetAlarmCodesCommand() {
        super("$Alarms/List");
    }

    public Map<Integer, String> getErrorCodes() {
        Map<Integer, String> alarmCodes = new HashMap<>();
        Pattern alarmCodePattern = Pattern.compile("([0-9]+):(.*)");
        Arrays.stream(StringUtils.split(getResponse(), "\n")).forEach(line -> {
            Matcher matcher = alarmCodePattern.matcher(line);
            if (matcher.find()) {
                alarmCodes.put(Integer.parseInt(matcher.group(1)), matcher.group(2));
            }
        });
        return alarmCodes;
    }
}
