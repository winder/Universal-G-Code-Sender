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
    private static boolean manualModeEnabled;
    private static double manualModeStepSize;
    
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
            try {
                SettingsFactory.fileName = settings.getProperty("last.dir", System.getProperty("user.home"));
                SettingsFactory.port = settings.getProperty("port", "");
                SettingsFactory.portRate = settings.getProperty("port.rate", "9600");
                SettingsFactory.manualModeEnabled = Boolean.valueOf(settings.getProperty("manualMode.enabled", "false"));
                SettingsFactory.manualModeStepSize = Double.valueOf(settings.getProperty("manualMode.stepsize", "1"));
            } catch (Exception e) {
                e.printStackTrace();
                logger.warning("Can't load settings use defaults!");
                loadDefaults();
            } 
        } catch (Exception e) {
            logger.warning("Can't load settings file!");
            loadDefaults();
        }
    }

    public static void saveSettings() {
        logger.info("Save settings");
        try {
            try {
                settings.put("last.dir", fileName);
                settings.put("port", port);
                settings.put("port.rate", portRate);
                settings.put("manualMode.enabled", manualModeEnabled+"");
                settings.put("manualMode.stepsize", manualModeStepSize+"");
            } catch (Exception e) {
                e.printStackTrace();
            }

            settings.store(new FileOutputStream(SETTINGS_FILE), "");
        } catch (Exception e) {
            e.printStackTrace();
            logger.warning("Can't save settings!");
        }
    }
    
    private static void loadDefaults() {
        SettingsFactory.fileName = System.getProperty("user.home");
        SettingsFactory.port = "";
        SettingsFactory.portRate = "9600";
        SettingsFactory.manualModeEnabled = false;
        SettingsFactory.manualModeStepSize = 1;
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

    static void setManualControllesEnabled(boolean enabled) {
        SettingsFactory.manualModeEnabled = enabled;
    }
    
    static boolean getManualControllesEnabled() {
        return manualModeEnabled;
    }

    static void setStepSize(double stepSize) {
        SettingsFactory.manualModeStepSize = stepSize;
    }
    
    static double setStepSize() {
        return manualModeStepSize;
    }

   
    
}
