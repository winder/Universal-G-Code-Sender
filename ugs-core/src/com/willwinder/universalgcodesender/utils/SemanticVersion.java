package com.willwinder.universalgcodesender.utils;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SemanticVersion implements Comparable<SemanticVersion> {

    private static final String VERSION_REGEX = "(?<major>[0-9]*)(.(?<minor>[0-9]+)(.(?<patch>[0-9]+))?)?";
    private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX, Pattern.CASE_INSENSITIVE);

    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Construct a fully featured version object with all bells and whistles.
     *
     * @param major major version number
     * @param minor minor version number
     * @param patch patch level
     */
    public SemanticVersion(int major, int minor, int patch) {
        if (major < 0 || minor < 0 || patch < 0) {
            throw new IllegalArgumentException("Version numbers must be positive!");
        }

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    /**
     * Construct a version object by parsing a string.
     *
     * @param version version in flat string format
     * @throws IllegalArgumentException if the version string can not be parsed
     */
    public SemanticVersion(String version) throws ParseException {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.find()) { // Start recursive descend
            throw new IllegalArgumentException(String.format("Could not parse version from string \"%s\"", version));
        }
        major = Integer.parseInt(StringUtils.defaultString(matcher.group("major"), "0"));
        minor = Integer.parseInt(StringUtils.defaultString(matcher.group("minor"), "0"));
        patch = Integer.parseInt(StringUtils.defaultString(matcher.group("patch"), "0"));
    }

    public SemanticVersion() {
        major = 0;
        minor = 0;
        patch = 0;
    }

    @Override
    public String toString() {
        return String.valueOf(major) + '.' + minor + '.' + patch;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SemanticVersion)) {
            return false;
        }
        SemanticVersion ov = (SemanticVersion) other;
        return ov.major == major && ov.minor == minor && ov.patch == patch;
    }

    @Override
    public int compareTo(SemanticVersion v) {
        int result = major - v.major;
        if (result == 0) { // Same major
            result = minor - v.minor;
            if (result == 0) { // Same minor
                result = patch - v.patch;
            }
        }
        return result;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }
}
