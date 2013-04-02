/*
    Copywrite 2013 Christian Moll

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
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
    private static File SETTINGS_FILE  = new File(System.getProperty("user.home"),".UniversalGcodeSender.properties");
    
    private static Properties settings = new Properties();
    private static Logger logger = Logger.getLogger(SettingsFactory.class.getName());
    
    private static String firmwareVersion = "";
    private static String fileName = "";
    private static String port = "";
    private static String portRate;
    private static boolean manualModeEnabled;
    private static double manualModeStepSize;
    private static boolean scrollWindowEnabled;
    private static boolean verboseOutputEnabled;
    private static boolean overrideSpeedSelected;
    private static double overrideSpeedValue;
    
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
                SettingsFactory.firmwareVersion = settings.getProperty("firmwareVersion", "GRBL");
                SettingsFactory.fileName = settings.getProperty("last.dir", System.getProperty("user.home"));
                SettingsFactory.port = settings.getProperty("port", "");
                SettingsFactory.portRate = settings.getProperty("port.rate", "9600");
                SettingsFactory.manualModeEnabled = Boolean.valueOf(settings.getProperty("manualMode.enabled", "false"));
                SettingsFactory.manualModeStepSize = Double.valueOf(settings.getProperty("manualMode.stepsize", "1"));
                SettingsFactory.scrollWindowEnabled = Boolean.valueOf(settings.getProperty("scrollWindow.enabled", "true"));
                SettingsFactory.verboseOutputEnabled = Boolean.valueOf(settings.getProperty("verboseOutput.enabled", "false"));
                SettingsFactory.overrideSpeedSelected = Boolean.valueOf(settings.getProperty("overrideSpeed.enabled", "false"));
                SettingsFactory.overrideSpeedValue = Double.valueOf(settings.getProperty("overrideSpeed.value", "60"));
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
                settings.put("firmwareVersion", firmwareVersion);
                settings.put("last.dir", fileName);
                settings.put("port", port);
                settings.put("port.rate", portRate);
                settings.put("manualMode.enabled", manualModeEnabled+"");
                settings.put("manualMode.stepsize", manualModeStepSize+"");
                settings.put("scrollWindow.enabled", scrollWindowEnabled+"");
                settings.put("verboseOutput.enabled", verboseOutputEnabled+"");
                settings.put("overrideSpeed.enabled", overrideSpeedSelected+"");
                settings.put("overrideSpeed.value", overrideSpeedValue+"");
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
        SettingsFactory.firmwareVersion = "GRBL";
        SettingsFactory.fileName = System.getProperty("user.home");
        SettingsFactory.port = "";
        SettingsFactory.portRate = "9600";
        SettingsFactory.manualModeEnabled = false;
        SettingsFactory.manualModeStepSize = 1;
        SettingsFactory.scrollWindowEnabled = true;
        SettingsFactory.verboseOutputEnabled = false;
        SettingsFactory.overrideSpeedSelected = false;
        SettingsFactory.overrideSpeedValue = 60;
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
    
    static double getStepSize() {
        return manualModeStepSize;
    }

    static void setScrollWindow(boolean selected) {
        SettingsFactory.scrollWindowEnabled = selected;
    }
     
    static boolean isScrollWindow() {
        return scrollWindowEnabled;
    }
    
    static void setVerboseOutput(boolean selected) {
        SettingsFactory.verboseOutputEnabled = selected;
    }

    static boolean isVerboseOutput() {
        return verboseOutputEnabled;
    }
   
    static void setOverrideSpeedSelected(boolean selected) {
        SettingsFactory.overrideSpeedSelected = selected;
    }

    static boolean isOverrideSpeedSelected() {
        return overrideSpeedSelected;
    }
    
    static void setOverrideSpeedValue(double value) {
        SettingsFactory.overrideSpeedValue = value;
    }
    
    static double getOverrideSpeedValue() {
        return overrideSpeedValue;
    }

    static void setFirmware(String value) {
        firmwareVersion = value;
    }
    
    static String getFirmware() {
        return firmwareVersion;
    }
}
