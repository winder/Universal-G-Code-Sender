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
    
    Refactored by Bob Jones 2014
 */

package com.willwinder.universalgcodesender.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author moll
 */
public class SettingsFactory {
    private static final Logger logger = Logger.getLogger(SettingsFactory.class.getName());
    private static final String USER_HOME = "user.home";
    private static final String FALSE = "false";
    public static final String PROPERTIES_FILENAME = "UniversalGcodeSender.properties";
    public static final String JSON_FILENAME = "UniversalGcodeSender.json";
    public static final String MAC_LIBRARY = "/Library/Preferences/";

    private static File getSettingsFile() {
        File properties = null;
        File json = null;

        String homeDir = System.getProperty(USER_HOME);
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("mac")) {
            homeDir = homeDir + MAC_LIBRARY;
            properties = new File(homeDir + PROPERTIES_FILENAME);
            json       = new File(homeDir + JSON_FILENAME);
        }
        else if (osName.contains("windows")) {
            if (!homeDir.endsWith(File.separator)) {
                homeDir = homeDir + File.separator;
            }

            properties = new File(homeDir + PROPERTIES_FILENAME);
            json       = new File(homeDir + JSON_FILENAME);
        }
        // Unix
        else {
            properties = new File(homeDir + PROPERTIES_FILENAME);

            // Check homedir for hidden / not hidden files
            json = new File(homeDir + File.separator + JSON_FILENAME);
            if (!json.exists()) {
                // Default to hidden if none.
                json = new File(homeDir + File.separator + "." + JSON_FILENAME);
            }
        }

        return properties.exists() ? properties : json;
    }

    @Deprecated
    private static File getSettingsFolder(){
        File settingsFolder = new File(System.getProperty(USER_HOME));
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            settingsFolder = new File(System.getProperty(USER_HOME)+MAC_LIBRARY);
        }
        return settingsFolder;
    }

    public static Settings loadSettings() {
        // the defaults are now in the settings bean
        Settings out = new Settings();

        File settingsFile = getSettingsFile();

        // Localized setting not available here.
        //logger.info(Localization.getString("settings.log.loading"));
        logger.info("Loading settings.");
        try {
            if(settingsFile.getName().endsWith("json") && settingsFile.exists()){
                //logger.log(Level.INFO, "{0}: {1}", new Object[]{Localization.getString("settings.log.location"), settingsFile});
                logger.log(Level.INFO, "Log location: {0}", settingsFile.getAbsolutePath());
                out = new Gson().fromJson(new FileReader(settingsFile), Settings.class);
            } else if(settingsFile.getName().endsWith("properties")){
                    //logger.log(Level.INFO, "{0}: {1}", new Object[]{Localization.getString("settings.log.location"), settingsFile});
                    logger.log(Level.INFO, "Log location: {0}", settingsFile.getAbsolutePath());
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(settingsFile));
                    out.setLastOpenedFilename(properties.getProperty("last.dir", System.getProperty(USER_HOME)));
                    out.setPort(properties.getProperty("port", ""));
                    out.setPortRate(properties.getProperty("port.rate", "9600"));
                    out.setManualModeEnabled(Boolean.valueOf(properties.getProperty("manualMode.enabled", FALSE)));
                    out.setManualModeStepSize(Double.valueOf(properties.getProperty("manualMode.stepsize", "1")));
                    out.setScrollWindowEnabled(Boolean.valueOf(properties.getProperty("scrollWindow.enabled", "true")));
                    out.setVerboseOutputEnabled(Boolean.valueOf(properties.getProperty("verboseOutput.enabled", FALSE)));
                    out.setOverrideSpeedSelected(Boolean.valueOf(properties.getProperty("overrideSpeed.enabled", FALSE)));
                    out.setOverrideSpeedValue(Double.valueOf(properties.getProperty("overrideSpeed.value", "60")));
                    out.setFirmwareVersion(properties.getProperty("firmwareVersion", "GRBL"));
                    out.setSingleStepMode(Boolean.valueOf(properties.getProperty("singleStepMode", FALSE)));
                    out.setMaxCommandLength(Integer.valueOf(properties.getProperty("maxCommandLength", "50")));
                    out.setTruncateDecimalLength(Integer.valueOf(properties.getProperty("truncateDecimalLength", "4")));
                    out.setRemoveAllWhitespace(Boolean.valueOf(properties.getProperty("removeAllWhitespace", "true")));
                    out.setStatusUpdatesEnabled(Boolean.valueOf(properties.getProperty("statusUpdatesEnabled", "true")));
                    out.setStatusUpdateRate(Integer.valueOf(properties.getProperty("statusUpdateRate", "200")));
                    out.setDisplayStateColor(Boolean.valueOf(properties.getProperty("displayStateColor", "true")));
                    out.setConvertArcsToLines(Boolean.valueOf(properties.getProperty("convertArcsToLines", FALSE)));
                    out.setSmallArcThreshold(Double.valueOf(properties.getProperty("smallArcThreshold", "2.0")));
                    out.setSmallArcSegmentLength(Double.valueOf(properties.getProperty("smallArcSegmentLength", "1.3")));
                    out.updateMacro(1, null, null, properties.getProperty("customGcode1", "G0 X0 Y0;"));
                    out.updateMacro(2, null, null, properties.getProperty("customGcode2", "G0 G91 X10;G0 G91 Y10;"));
                    out.updateMacro(3, null, null, properties.getProperty("customGcode3", ""));
                    out.updateMacro(4, null, null, properties.getProperty("customGcode4", ""));
                    out.updateMacro(5, null, null, properties.getProperty("customGcode5", ""));
                    out.setLanguage(properties.getProperty("language", "en_US"));
            }
            out.finalizeInitialization();
        } catch (Exception e) {
            //logger.warning(Localization.getString("settings.log.error"));
            logger.warning("Can't load settings, using defaults.");
        }
        
        if (out == null) return new Settings();
        return out;
    }

    public static void saveSettings(Settings settings) {
        logger.info(Localization.getString("settings.log.saving"));
        try {
            // Save json file.
            File jsonFile = getSettingsFile();
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                fileWriter.write(gson.toJson(settings, Settings.class));
            }

            // Delete the old settings file if it exists.
            File propertiesFile = new File(getSettingsFolder(), "UniversalGcodeSender.properties");
            if(propertiesFile.exists()){
                    propertiesFile.delete();
            }
         } catch (Exception e) {
            e.printStackTrace();
            logger.warning(Localization.getString("settings.log.saveerror"));
        }
    }
}
