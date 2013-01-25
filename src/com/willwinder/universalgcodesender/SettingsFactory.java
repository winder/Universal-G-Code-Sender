/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author moll
 */
class SettingsFactory {
    private static File SETTINGS_FILE  = new File(System.getProperty("user.home")+"/Library/Preferences/",".UniversalGcodeSender.properties");;
    
    private static Properties settings = new Properties();
    private static Logger logger = Logger.getLogger(SettingsFactory.class.getName());
    
    private static String fileName = "";
    private static String port = "";
    private static String portRate;
    
    static {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            SETTINGS_FILE = new File(System.getProperty("user.home")+"/Library/Preferences/","UniversalGcodeSender.properties");
        }
        logger.info("Settings file location: " + SETTINGS_FILE);
    }

    public static void loadSettings() {
        logger.info("Load settings");
        try {
            settings.load(new FileInputStream(SETTINGS_FILE));
            SettingsFactory.fileName = settings.getProperty("last.dir", System.getProperty("user.home"));
            SettingsFactory.port = settings.getProperty("port", "");
            SettingsFactory.portRate = settings.getProperty("port.rate", "9600");
        } catch (Exception e) {
            logger.warning("Can't load settings!");
        }
    }

    public static void saveSettings() {
        logger.info("Save settings");
        try {
            try {
                settings.put("last.dir", fileName);
                settings.put("port", port);
                settings.put("port.rate", portRate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            settings.store(new FileOutputStream(SETTINGS_FILE), "");
        } catch (Exception e) {
            logger.warning("Can't save settings!");
        }
    }

    public static void setLastPath(String fileName) {
        SettingsFactory.fileName = fileName;
    }
    
    public static String getLastPath() {
        return fileName;
    }

    static void setPort(String port) {
        SettingsFactory.port = port;
    }

    static String getPort() {
        return port;
    }
    
    static void setPortRate(String rate) {
        SettingsFactory.portRate = rate;
    }

    static String getPortRate() {
        return portRate;
    }

   
    
}
