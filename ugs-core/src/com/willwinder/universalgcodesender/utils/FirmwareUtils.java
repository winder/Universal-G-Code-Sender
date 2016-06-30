/*
 * FirmwareUtils.java
 *
 * Created on April 2, 2013
 */

/*
    Copywrite 2012-2014 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.GrblController;
import com.willwinder.universalgcodesender.TinyGController;
import com.willwinder.universalgcodesender.XLCDCommunicator;
import com.willwinder.universalgcodesender.LoopBackCommunicator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author wwinder
 */
public class FirmwareUtils {
    private static final Logger logger = Logger.getLogger(FirmwareUtils.class.getName());
    final private static String FIRMWARE_CONFIG_DIRNAME = "firmware_config";

    final public static String GRBL     = "GRBL";
    final public static String Smoothie = "SmoothieBoard";
    final public static String TinyG    = "TinyG";
    final public static String XLCD     = "XLCD";
    final public static String LOOPBACK = "Loopback";
    final public static String LOOPBACK2= "Loopback_Slow";
    
    static {
        initializeFiles();
    }
    
    public static ArrayList<String> getFirmwareList() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(GRBL);
        //ret.add(Smoothie);
        ret.add(TinyG);
        ret.add(XLCD);
        ret.add(LOOPBACK);
        ret.add(LOOPBACK2);
        
        return ret;
    }
    
    public static AbstractController getControllerFor(String firmware) {
        switch(firmware) {
            case GRBL:
                return new GrblController();
            case Smoothie:
                return null;
            case TinyG:
                return new TinyGController();
            case XLCD:
                return new GrblController(new XLCDCommunicator());
            case LOOPBACK:
                return new GrblController(new LoopBackCommunicator());
            case LOOPBACK2:
                return new GrblController(new LoopBackCommunicator(10));
            default:
                break;
        }
        
        return null;
    }

    /**
     * Deletes firmware_config files from the machine then recreates them.
     */
    public static void restoreDefaults() throws IOException {
        File firmwareConfig = new File(SettingsFactory.getSettingsDirectory(),
                FIRMWARE_CONFIG_DIRNAME);

        // Delete firmware config directory so it can be re-initialized.
        FileUtils.deleteDirectory(firmwareConfig);

        initializeFiles();
    }

    /**
     * Copy any missing files from the the jar's resources/firmware_config/ dir
     * into the settings/firmware_config dir.
     */
    public static void initializeFiles() {
        File firmwareConfig = new File(SettingsFactory.getSettingsDirectory(),
                FIRMWARE_CONFIG_DIRNAME);

        // Create directory if it's missing.
        if (!firmwareConfig.exists()) {
            firmwareConfig.mkdirs();
        }

        try {
            // Loop through config files.
            String dir = "resources/firmware_config/";
            List<String> files = IOUtils.readLines(FirmwareUtils.
                    class.getClassLoader()
                    .getResourceAsStream(dir), Charsets.UTF_8);

            // Create any files which don't exist.
            for (String file : files) {
                File fwConfig = new File(firmwareConfig, file);
                if (!fwConfig.exists()) {
                    InputStream is = FirmwareUtils.class.getClassLoader().
                            getResourceAsStream(dir + file);
                    FileUtils.copyInputStreamToFile(is, new File(firmwareConfig, file));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(FirmwareUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}