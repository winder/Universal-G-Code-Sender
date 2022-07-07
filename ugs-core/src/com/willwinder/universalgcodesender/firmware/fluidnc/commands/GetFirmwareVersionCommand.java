package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.utils.SemanticVersion;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFirmwareVersionCommand extends SystemCommand {
    private SemanticVersion version;
    private String firmware;

    public GetFirmwareVersionCommand() {
        super("$Build/Info");
    }

    @Override
    public void appendResponse(String response) {
        super.appendResponse(response);

        if (isDone() && isOk()) {
            Pattern pattern = Pattern.compile("\\[VER:[0-9.]+ (?<variant>[a-zA-Z0-9]+) v(?<version>(?<major>[0-9]*)(.(?<minor>[0-9]+)(.(?<patch>[0-9]+))?)?([a-zA-Z]+)?)(:.*)*]", Pattern.CASE_INSENSITIVE);
            Arrays.stream(StringUtils.split(getResponse(), "\n")).forEach(line -> {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    try {
                        version = new SemanticVersion(matcher.group("version"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    firmware = matcher.group("variant");
                }
            });
        }
    }

    public String getFirmware() {
        return firmware;
    }

    public SemanticVersion getVersion() {
        return version;
    }
}
