package com.willwinder.universalgcodesender.firmware.grbl;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a GRBL version string
 */
public class GrblVersion {
    public static final GrblVersion NO_VERSION = new GrblVersion("");
    public static final String VERSION_REGEX = "^\\[[a-zA-Z0-9:\\s]*[vV]?(?<version>(?<major>\\d+)\\.(?<minor>\\d+)(?<char>[a-zA-Z])?).*?]$";
    private final double versionNumber;           // The 0.8 in '[VER:0.8c.20220620:Machine1]'
    private final Character versionLetter;  // The c in '[VER:0.8c.20220620:Machine1]'

    /**
     * Parses the GRBL version string from the format [VER:v1.1f.20170131:Some string]
     *
     * @param versionString the version string
     */
    public GrblVersion(String versionString) {
        Pattern versionPattern = Pattern.compile(VERSION_REGEX);
        Matcher matcher = versionPattern.matcher(versionString);
        if (matcher.matches()) {
            versionNumber = Double.parseDouble(StringUtils.defaultString(matcher.group("major"), "0") + "." + StringUtils.defaultString(matcher.group("minor"), "0"));
            versionLetter = StringUtils.defaultString(matcher.group("char"), "-").charAt(0);
        } else {
            versionNumber = 0;
            versionLetter = '-';
        }
    }

    public double getVersionNumber() {
        return versionNumber;
    }

    public Character getVersionLetter() {
        return versionLetter;
    }

    @Override
    public String toString() {
        String result = "GRBL";
        if(versionNumber > 0) {
            result = result + " " + versionNumber;

            if(versionLetter != '-') {
                result = result + versionLetter;
            }
        }

        return result;
    }
}
