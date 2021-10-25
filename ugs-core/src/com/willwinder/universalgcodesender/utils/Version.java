package com.willwinder.universalgcodesender.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Version {
    private static final Logger LOGGER = Logger.getLogger(Version.class.getName());
    private static final String BUILD_DATE_FORMAT = "yyyy-MM-dd";
    private static final String BUILD_DATE_NUMBER_FORMAT = "yyyyMMdd";
    private static String VERSION = "2.0-SNAPSHOT ";
    private static String BUILD_DATE = "";

    private static boolean initialized = false;

    /**
     * Returns if this is a snapshot/nightly build
     *
     * @return true if it is a nightly build
     */
    public static boolean isNightlyBuild() {
        return VERSION.contains("-SNAPSHOT");
    }

    /**
     * Returns the version as a string.
     *
     * If it is a snapshot/nightly build it will include a build date: "2.0.6-SNAPSHOT / 2020-10-06"
     * If it is a release build it will only include the version: "2.0.6"
     *
     * @return the build version as a string
     */
    public static String getVersionString() {
        if (!initialized) {
            initialize();
        }

        String versionString = Version.getVersion();
        if (isNightlyBuild()) {
            return Version.getVersion() + " / " + Version.getBuildDate();
        }
        return versionString;
    }

    public static synchronized String getVersion() {
        if (!initialized) {
            initialize();
        }

        return VERSION;
    }

    /**
     * Fetches the the build date for this version
     *
     * @return the build date in the format yyyy-MM-dd
     */
    public static synchronized String getBuildDate() {
        if (!initialized) {
            initialize();
        }
        return BUILD_DATE;
    }

    /**
     * Fetches the the build date for this version
     *
     * @return the build date in the format yyyyMMdd
     */
    public static long getBuildDateAsNumber() {
        String buildDate = getBuildDate();
        try {
            SimpleDateFormat parser = new SimpleDateFormat(BUILD_DATE_FORMAT);
            Date date = parser.parse(buildDate);

            SimpleDateFormat formatter = new SimpleDateFormat(BUILD_DATE_NUMBER_FORMAT);
            return Long.parseLong(formatter.format(date));
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,"Couldn't convert the " + buildDate + " from date format \"" + BUILD_DATE_FORMAT + "\" to format \"" + BUILD_DATE_NUMBER_FORMAT + "\"");
            return 0;
        }
    }

    private static void initialize() {
        try {
            Properties props;
            try (InputStream is = Version.class.getResourceAsStream("/resources/build.properties")) {
                props = new Properties();
                props.load(is);
            }
            VERSION = props.getProperty("Version");
            BUILD_DATE = props.getProperty("Build-Date");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Couldn't parse the build date or version from the properties file: " + e.getMessage());
        }
        initialized = true;
    }
}
