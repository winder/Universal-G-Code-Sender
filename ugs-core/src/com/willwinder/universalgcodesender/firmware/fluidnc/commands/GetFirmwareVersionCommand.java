package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.utils.SemanticVersion;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetFirmwareVersionCommand extends SystemCommand {
    private static final Pattern VERSION_PATTERN = Pattern.compile("\\[VER:[0-9.]+ (?<variant>[a-zA-Z0-9]+) v(?<version>(?<major>[0-9]*)(.(?<minor>[0-9]+)(.(?<patch>[0-9]+))?)?([a-zA-Z]+)?)(:.*)*]", Pattern.CASE_INSENSITIVE);

    public GetFirmwareVersionCommand() {
        super("$Build/Info");
    }

    public String getFirmware() {
        String firmware = "Unknown";
        for (String line : StringUtils.split(getResponse(), "\n")) {
            Matcher matcher = VERSION_PATTERN.matcher(line);
            if (matcher.find()) {
                firmware = matcher.group("variant");
            }
        }
        return firmware;
    }

    public SemanticVersion getVersion() {
        SemanticVersion result = new SemanticVersion();
        for (String line : StringUtils.split(getResponse(), "\n")) {
            Matcher matcher = VERSION_PATTERN.matcher(line);
            if (matcher.find()) {
                try {
                    result = new SemanticVersion(matcher.group("version"));
                } catch (ParseException e) {
                    // Never mind
                }
            }
        }
        return result;
    }
}
