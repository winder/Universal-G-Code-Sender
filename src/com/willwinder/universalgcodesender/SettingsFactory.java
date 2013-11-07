/*
    Copywrite 2013 Christian Moll, Will Winder

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
    private static final Logger logger = Logger.getLogger(SettingsFactory.class.getName());
    
    private static String firmwareVersion = "";
    private static String fileName = "";
    private static String port = "";
    private static String portRate;
    private static boolean manualModeEnabled;
    private static double manualModeStepSize;
    private static boolean scrollWindowEnabled;
    private static boolean verboseOutputEnabled;
    // Sender Settings
    private static boolean overrideSpeedSelected;
    private static double overrideSpeedValue;
    private static boolean singleStepMode;
    private static int maxCommandLength;
    private static int truncateDecimalLength;
    private static boolean removeAllWhitespace;
    private static boolean statusUpdatesEnabled;
    private static int statusUpdateRate;
    private static boolean displayStateColor;
    
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
            
            // V1 Settings - These are settings from the very beginning.
            try {
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
            
            // V1.0.7 Settings - New settings, load separately to avoid
            //                   overwriting them with loadDefaults.
            try {
                SettingsFactory.firmwareVersion = settings.getProperty("firmwareVersion", "GRBL");
                SettingsFactory.singleStepMode = Boolean.valueOf(settings.getProperty("singleStepMode", "false"));
                SettingsFactory.maxCommandLength = Integer.valueOf(settings.getProperty("maxCommandLength", "50"));
                SettingsFactory.truncateDecimalLength = Integer.valueOf(settings.getProperty("truncateDecimalLength", "4"));
                SettingsFactory.removeAllWhitespace = Boolean.valueOf(settings.getProperty("removeAllWhitespace", "true"));
                SettingsFactory.statusUpdatesEnabled = Boolean.valueOf(settings.getProperty("statusUpdatesEnabled", "true"));
                SettingsFactory.statusUpdateRate = Integer.valueOf(settings.getProperty("statusUpdateRate", "200"));
                SettingsFactory.displayStateColor = Boolean.valueOf(settings.getProperty("displayStateColor", "true"));

            } catch (Exception e) {
                logger.warning("Can't load settings file!");
                loadDefaults2();
            }
        } catch (Exception e) {
            logger.warning("Can't load settings file!");
            loadDefaults();
            loadDefaults2();
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
                
                // Controller settings
                settings.put("singleStepMode", singleStepMode+"");
                settings.put("maxCommandLength", maxCommandLength+"");
                settings.put("truncateDecimalLength", truncateDecimalLength+"");
                settings.put("removeAllWhitespace", removeAllWhitespace+"");
                settings.put("statusUpdatesEnabled", statusUpdatesEnabled+"");
                settings.put("statusUpdateRate", statusUpdateRate+"");
                settings.put("displayStateColor", displayStateColor+"");
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
        SettingsFactory.scrollWindowEnabled = true;
        SettingsFactory.verboseOutputEnabled = false;
        SettingsFactory.overrideSpeedSelected = false;
        SettingsFactory.overrideSpeedValue = 60;
    }
    
    private static void loadDefaults2() {
        SettingsFactory.firmwareVersion = "GRBL";
        SettingsFactory.singleStepMode = false;
        SettingsFactory.maxCommandLength = 50;
        SettingsFactory.truncateDecimalLength = 4;
        SettingsFactory.removeAllWhitespace = true;
        SettingsFactory.statusUpdatesEnabled = true;
        SettingsFactory.statusUpdateRate = 200;
        SettingsFactory.displayStateColor = true;
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

    public static String getPort() {
        return port;
    }
    
    public static void setPortRate(String rate) {
        SettingsFactory.portRate = rate;
    }

    public static String getPortRate() {
        return portRate;
    }

    public static void setManualControllesEnabled(boolean enabled) {
        SettingsFactory.manualModeEnabled = enabled;
    }
    
    public static boolean getManualControllesEnabled() {
        return manualModeEnabled;
    }

    public static void setStepSize(double stepSize) {
        SettingsFactory.manualModeStepSize = stepSize;
    }
    
    public static double getStepSize() {
        return manualModeStepSize;
    }

    public static void setScrollWindow(boolean selected) {
        SettingsFactory.scrollWindowEnabled = selected;
    }
     
    public static boolean isScrollWindow() {
        return scrollWindowEnabled;
    }
    
    public static void setVerboseOutput(boolean selected) {
        SettingsFactory.verboseOutputEnabled = selected;
    }

    public static boolean isVerboseOutput() {
        return verboseOutputEnabled;
    }
   
    public static void setOverrideSpeedSelected(boolean selected) {
        SettingsFactory.overrideSpeedSelected = selected;
    }

    public static boolean isOverrideSpeedSelected() {
        return overrideSpeedSelected;
    }
    
    public static void setOverrideSpeedValue(double value) {
        SettingsFactory.overrideSpeedValue = value;
    }
    
    public static double getOverrideSpeedValue() {
        return overrideSpeedValue;
    }

    public static void setFirmware(String value) {
        firmwareVersion = value;
    }
    
    public static String getFirmware() {
        return firmwareVersion;
    }
    
    public static void setSingleStepMode(boolean enabled) {
        singleStepMode = enabled;
    }
    
    public static boolean getSingleStepMode() {
        return singleStepMode;
    }
    
    public static void setMaxCommandLength(int length) {
        maxCommandLength = length;
    }
    
    public static int getMaxCommandLength() {
        return maxCommandLength;
    }
    
    public static void setTruncateDecimalLength(int length) {
        truncateDecimalLength = length;
    }
    
    public static int getTruncateDecimalLength() {
        return truncateDecimalLength;
    }
    
    public static void setRemoveAllWhitespace(boolean enabled) {
        removeAllWhitespace = enabled;
    }
    
    public static boolean getRemoveAllWhitespace() {
        return removeAllWhitespace;
    }
    
    public static void setStatusUpdatesEnabled(boolean enabled) {
        statusUpdatesEnabled = enabled;         
    }
    
    public static boolean getStatusUpdatesEnabled() {
        return statusUpdatesEnabled;
    }
    
    public static void setStatusUpdateRate(int rate) {
        statusUpdateRate = rate;
    }
    
    public static int getStatusUpdateRate() {
        return statusUpdateRate;
    }
    
    public static void setDisplayStateColor(boolean enabled) {
        displayStateColor = enabled;
    }
    
    public static boolean getDisplayStateColor() {
        return displayStateColor;
    }
}
