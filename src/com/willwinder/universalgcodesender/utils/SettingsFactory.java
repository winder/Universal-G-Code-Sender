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

/**
 *
 * @author moll
 */
public class SettingsFactory {
    private static final Logger logger = Logger.getLogger(SettingsFactory.class.getName());
    
    public static File getSettingsFolder(){
    	File settingsFolder = new File(System.getProperty("user.home"));
    	if (System.getProperty("os.name").toLowerCase().contains("mac")) {
    		settingsFolder = new File(System.getProperty("user.home")+"/Library/Preferences/");
    	}
    	return settingsFolder;
    }
    
    public static Settings loadSettings() {
    	// the defaults are now in the settings bean
    	Settings out = new Settings();
    	
    	File propertiesFile = new File(getSettingsFolder(), "UniversalGcodeSender.properties");
    	File jsonFile = new File(getSettingsFolder(), "UniversalGcodeSender.json");
    	
        logger.info(Localization.getString("settings.log.loading"));
        try {
	    	if(jsonFile.exists()){
	            logger.log(Level.INFO, "{0}: {1}", new Object[]{Localization.getString("settings.log.location"), jsonFile});
	    		out = new Gson().fromJson(new FileReader(jsonFile), Settings.class);
	    	} else if(propertiesFile.exists()){
                    logger.log(Level.INFO, "{0}: {1}", new Object[]{Localization.getString("settings.log.location"), propertiesFile});
                    Properties properties = new Properties();
                    properties.load(new FileInputStream(propertiesFile));
                    out.setLastOpenedFilename(properties.getProperty("last.dir", System.getProperty("user.home")));
                    out.setPort(properties.getProperty("port", ""));
                    out.setPortRate(properties.getProperty("port.rate", "9600"));
                    out.setManualModeEnabled(Boolean.valueOf(properties.getProperty("manualMode.enabled", "false")));
                    out.setManualModeStepSize(Double.valueOf(properties.getProperty("manualMode.stepsize", "1")));
                    out.setScrollWindowEnabled(Boolean.valueOf(properties.getProperty("scrollWindow.enabled", "true")));
                    out.setVerboseOutputEnabled(Boolean.valueOf(properties.getProperty("verboseOutput.enabled", "false")));
                    out.setOverrideSpeedSelected(Boolean.valueOf(properties.getProperty("overrideSpeed.enabled", "false")));
                    out.setOverrideSpeedValue(Double.valueOf(properties.getProperty("overrideSpeed.value", "60")));
                    out.setFirmwareVersion(properties.getProperty("firmwareVersion", "GRBL"));
                    out.setSingleStepMode(Boolean.valueOf(properties.getProperty("singleStepMode", "false")));
                    out.setMaxCommandLength(Integer.valueOf(properties.getProperty("maxCommandLength", "50")));
                    out.setTruncateDecimalLength(Integer.valueOf(properties.getProperty("truncateDecimalLength", "4")));
                    out.setRemoveAllWhitespace(Boolean.valueOf(properties.getProperty("removeAllWhitespace", "true")));
                    out.setStatusUpdatesEnabled(Boolean.valueOf(properties.getProperty("statusUpdatesEnabled", "true")));
                    out.setStatusUpdateRate(Integer.valueOf(properties.getProperty("statusUpdateRate", "200")));
                    out.setDisplayStateColor(Boolean.valueOf(properties.getProperty("displayStateColor", "true")));
                    out.setConvertArcsToLines(Boolean.valueOf(properties.getProperty("convertArcsToLines", "false")));
                    out.setSmallArcThreshold(Double.valueOf(properties.getProperty("smallArcThreshold", "2.0")));
                    out.setSmallArcSegmentLength(Double.valueOf(properties.getProperty("smallArcSegmentLength", "1.3")));
                    out.setCustomGcode1(properties.getProperty("customGcode1", "G0 X0 Y0;"));
                    out.setCustomGcode2(properties.getProperty("customGcode2", "G0 G91 X10;G0 G91 Y10;"));
                    out.setCustomGcode3(properties.getProperty("customGcode3", ""));
                    out.setCustomGcode4(properties.getProperty("customGcode4", ""));
                    out.setCustomGcode5(properties.getProperty("customGcode5", ""));
                    out.setLanguage(properties.getProperty("language", "en_US"));
	    	}
        } catch (Exception e) {
            logger.warning(Localization.getString("settings.log.error"));
        }
        
        if (out == null) return new Settings();
        return out;
    }

    public static void saveSettings(Settings settings) {
        logger.info(Localization.getString("settings.log.saving"));
        try {
            File jsonFile = new File(getSettingsFolder(), "UniversalGcodeSender.json");
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                fileWriter.write(gson.toJson(settings, Settings.class));
            }
        	
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
