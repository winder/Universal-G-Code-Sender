package com.willwinder.universalgcodesender.firmware.grbl;

import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a GRBL version string
 */
public class GrblVersion {
    private static final Logger LOGGER = Logger.getLogger(GrblVersion.class.getName());
    public static final GrblVersion NO_VERSION = new GrblVersion("");
    public static final String VERSION_REGEX = "^\\[[a-zA-Z0-9:\\s]*[vV]?(?<version>(?<major>\\d+)\\.(?<minor>\\d+)(?<char>[a-zA-Z])?).*?]$";
    public static final String VERSION_1_REGEX = "^\\[VER:.*]$";

    private final double versionNumber;           // The 0.8 in '[VER:0.8c.20220620:Machine1]'
    private final Character versionLetter;  // The c in '[VER:0.8c.20220620:Machine1]'

    /**
     * Parses the GRBL version string from the format [VER:v1.1f.20170131:Some string]
     *
     * @param versionString the version string
     */
    public GrblVersion(String versionString) {
        Pattern versionPattern = Pattern.compile(VERSION_REGEX);
        Pattern version1Pattern = Pattern.compile(VERSION_1_REGEX);

        Matcher versionMatcher = versionPattern.matcher(versionString);
        if (versionMatcher.matches()) {
            versionNumber = Double.parseDouble(StringUtils.defaultString(versionMatcher.group("major"), "0") + "." + StringUtils.defaultString(versionMatcher.group("minor"), "0"));
            versionLetter = StringUtils.defaultString(versionMatcher.group("char"), "-").charAt(0);
        } else if (version1Pattern.matcher(versionString).matches()) {
            LOGGER.log(Level.SEVERE, "Controller returned an invalid version string, we will assume that it is a 1.1h version");
            versionNumber = 1.1;
            versionLetter = 'h';
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
        if (versionNumber > 0) {
            result = result + " " + versionNumber;

            if (versionLetter != '-') {
                result = result + versionLetter;
            }
        }

        return result;
    }
}
