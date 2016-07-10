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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Need a simple way to map the config loader (JSON in POJO format) to the
     * file it was generated from.
     */
    public static class ConfigTuple {
        public ControllerSettings loader;
        public File file;
        public ConfigTuple(ControllerSettings l, File f) {
            this.loader = l;
            this.file = f;
        }
    }

    private static Map<String,ConfigTuple> configFiles = new HashMap<>();

    public static Map<String,ConfigTuple> getConfigFiles() {
        return configFiles;
    }

    static {
        initialize();
    }
    
    public static ArrayList<String> getFirmwareList() {
        ArrayList<String> ret = new ArrayList<>();
        for (String fw : configFiles.keySet()) {
            ret.add(fw);
        }
        return ret;
    }
    
    /**
     * Gets a list of command processors initialized with user settings.
     */
    public static Optional<List<ICommandProcessor>> getParserFor(String firmware, Settings settings) {
        if (!configFiles.containsKey(firmware)) {
            return Optional.empty();
        }
        return Optional.of(configFiles.get(firmware).loader.getProcessors(settings));
    }

    /**
     * Gets a new controller object from a firmware config.
     * @param firmware
     * @return 
     */
    public static Optional<AbstractController> getControllerFor(String firmware) {
        if (!configFiles.containsKey(firmware)) {
            return Optional.empty();
        }

        /*
        ConfigLoader config = new Gson().fromJson(new FileReader(configFiles.get(firmware).configFile), ConfigLoader.class);
        File f = configFiles.get(firmware).configFile;
        File next = new File(f.getParent(), f.getName() + ".out");
        try (FileWriter fileWriter = new FileWriter(next)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
             fileWriter.write(gson.toJson(config, ConfigLoader2.class));
        } catch (IOException ex) {
            Logger.getLogger(FirmwareUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Have config: " + config.toString());
        */
        return Optional.of(configFiles.get(firmware).loader.getController());
    }

    /**
     * Deletes firmware_config files from the machine then recreates them.
     */
    public static void restoreDefaults() throws IOException {
        File firmwareConfig = new File(SettingsFactory.getSettingsDirectory(),
                FIRMWARE_CONFIG_DIRNAME);

        // Delete firmware config directory so it can be re-initialized.
        FileUtils.deleteDirectory(firmwareConfig);

        initialize();
    }

    /**
     * Copy any missing files from the the jar's resources/firmware_config/ dir
     * into the settings/firmware_config dir.
     */
    public static void initialize() {
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

        configFiles = new HashMap<>();
        for (File f : firmwareConfig.listFiles()) {
            try {
                ControllerSettings config = new Gson().fromJson(new FileReader(f), ControllerSettings.class);
                //ConfigLoader config = new ConfigLoader(f);
                configFiles.put(config.getName(), new ConfigTuple(config, f));
            } catch (FileNotFoundException | JsonSyntaxException | JsonIOException ex) {
                GUIHelpers.displayErrorDialog("Unable to load configuration files: " + f.getAbsolutePath());
            }
        }
    }
}