package com.willwinder.universalgcodesender;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Version {
    private static final String VERSION = "1.0.8 [nightly] ";
    private static String TIMESTAMP = "";

    private static boolean initialized = false;
    
    static String getVersion() {
        return VERSION;
    }
    
    synchronized static String getTimestamp() {
        if (!initialized) {
            initialize();
        }
        return TIMESTAMP;
    }
    
    private static void initialize() {
        String timestamp = "";
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
                timestamp = props.getProperty("Build-Date");
            }
        } catch (IOException e) {
            System.out.println("EXCEPTION: " + e.getMessage());
        }
        TIMESTAMP = timestamp;
    }
}
