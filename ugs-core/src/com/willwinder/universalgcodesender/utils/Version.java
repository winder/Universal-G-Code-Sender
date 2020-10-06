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
    private static final String BUILD_DATE_FORMAT = "MMM dd, yyyy";
    private static final String BUILD_DATE_NUMBER_FORMAT = "yyyyMMdd";
    private static String VERSION = "2.0 [nightly] ";
    private static String BUILD_DATE = "";

    private static boolean initialized = false;
    
    static public Boolean isNightlyBuild() {
        return VERSION.contains("nightly");
    }

    static public String getVersionString() {
        return Version.getVersion() + " / " + Version.getBuildDate();
    }

    static public String getVersion() {
        return VERSION;
    }

    /**
     * Fetches the the build date for this version
     *
     * @return the build date in the format MMM dd, yyyy
     */
    synchronized public static String getBuildDate() {
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
            return Long.valueOf(formatter.format(date));
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE,"Couldn't convert the " + buildDate + " from date format \"" + BUILD_DATE_FORMAT + "\" to format \"" + BUILD_DATE_NUMBER_FORMAT + "\"");
            return 0;
        }
    }

    private static void initialize() {
        String buildDate = "";
        String version = "";
        try {
            Class clazz = Version.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();

            if (classPath.startsWith("jar")) {
                Properties props;
                try (InputStream is = clazz.getResourceAsStream("/resources/build.properties")) {
                    props = new Properties();
                    props.load(is);
                }
                buildDate = props.getProperty("Build-Date");
                version = props.getProperty("Version");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Couldn't parse the build date from the properties file: " + e.getMessage());
        }
        BUILD_DATE = buildDate;
        VERSION = version;
        initialized = true;
    }
}
