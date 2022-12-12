/*
    Copyright 2012-2022 Will Winder

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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JOptionPane;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author wwinder
 */
public class FirmwareUtils {
    final private static String FIRMWARE_CONFIG_DIRNAME = "firmware_config";
    private static final Logger logger = Logger.getLogger(FirmwareUtils.class.getName());
    private static final Map<String, ConfigTuple> configFiles = new HashMap<>();
    private static boolean userNotified = false;
    private static boolean overwriteOldFiles = false;

    static {
        initialize();
    }

    public static Map<String, ConfigTuple> getConfigFiles() {
        return configFiles;
    }

    public static List<String> getFirmwareList() {
        List<String> firmwares = new ArrayList<>(configFiles.keySet());
        firmwares.sort(StringUtils::compareIgnoreCase);
        return firmwares;
    }

    /**
     * Gets a list of command processors initialized with user settings.
     */
    public static Optional<List<CommandProcessor>> getParserFor(String firmware)
            throws Exception {
        if (!configFiles.containsKey(firmware)) {
            throw new Exception("Missing config file.");
        }
        return Optional.of(configFiles.get(firmware).loader.getProcessors());
    }

    /**
     * Gets a new controller object from a firmware config.
     *
     * @param firmware
     * @return
     */
    public static Optional<IController> getControllerFor(String firmware) {
        if (!configFiles.containsKey(firmware) || configFiles.get(firmware).loader == null) {
            return Optional.empty();
        }

        return configFiles.get(firmware).loader.getController();
    }

    /**
     * Deletes firmware_config file from the machine then recreate it.
     */
    public static void restoreDefaults(String firmware) throws IOException {
        ConfigTuple tuple = configFiles.get(firmware);
        configFiles.remove(firmware);
        tuple.file.delete();

        // Reload missing file.
        initialize();
    }

    private static ControllerSettings getSettingsForStream(InputStream is)
            throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return new Gson().fromJson(br, ControllerSettings.class);
        }
    }

    /**
     * Copy any missing files from the the jar's resources/firmware_config/ dir
     * into the settings/firmware_config dir.
     */
    public synchronized static void initialize() {
        logger.info("Initializing firmware... ...");
        File firmwareConfig = new File(SettingsFactory.getSettingsDirectory(),
                FIRMWARE_CONFIG_DIRNAME);

        // Create directory if it's missing.
        if (!firmwareConfig.exists()) {
            firmwareConfig.mkdirs();
        }

        FileSystem fileSystem = null;

        // Copy firmware config files.
        try {
            final String dir = "/resources/firmware_config/";

            URI location = FirmwareUtils.class.getResource(dir).toURI();

            Path myPath;
            if (location.getScheme().equals("jar")) {
                try {
                    // In case the filesystem already exists.
                    fileSystem = FileSystems.getFileSystem(location);
                } catch (FileSystemNotFoundException e) {
                    // Otherwise create the new filesystem.
                    fileSystem = FileSystems.newFileSystem(location,
                            Collections.<String, String>emptyMap());
                }

                myPath = fileSystem.getPath(dir);
            } else {
                myPath = Paths.get(location);
            }

            Stream<Path> files = Files.walk(myPath, 1);
            for (Path path : (Iterable<Path>) files::iterator) {
                logger.info(path.toString());
                final String name = path.getFileName().toString();
                File fwConfig = new File(firmwareConfig, name);
                if (name.endsWith(".json")) {
                    boolean copyFile = !fwConfig.exists();
                    ControllerSettings jarSetting =
                            getSettingsForStream(Files.newInputStream(path));

                    // If the file is outdated... ask the user (once).
                    if (fwConfig.exists()) {
                        ControllerSettings current =
                                getSettingsForStream(new FileInputStream(fwConfig));
                        boolean outOfDate =
                                current.getVersion() < jarSetting.getVersion();
                        if (outOfDate && !userNotified && !overwriteOldFiles) {
                            int result = NarrowOptionPane.showNarrowConfirmDialog(
                                    200,
                                    Localization.getString("settings.file.outOfDate.message"),
                                    Localization.getString("settings.file.outOfDate.title"),
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);
                            overwriteOldFiles = result == JOptionPane.OK_OPTION;
                            userNotified = true;
                        }

                        if (overwriteOldFiles) {
                            copyFile = true;
                            jarSetting.getProcessorConfigs().Custom
                                    = current.getProcessorConfigs().Custom;
                        }
                    }

                    // Copy file from jar to firmware_config directory.
                    if (copyFile) {
                        try {
                            save(fwConfig, jarSetting);
                        } catch (IOException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            String errorMessage = String.format("%s %s",
                    Localization.getString("settings.file.generalError"),
                    ex.getLocalizedMessage());
            GUIHelpers.displayErrorDialog(errorMessage);
            logger.log(Level.SEVERE, errorMessage, ex);
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Problem closing filesystem.", ex);
                }
            }
        }

        configFiles.clear();
        for (File f : firmwareConfig.listFiles()) {
            try (InputStream fileInputStream = new FileInputStream(f)) {
                ControllerSettings config = new Gson().fromJson(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8), ControllerSettings.class);
                configFiles.put(config.getName(), new ConfigTuple(config, f));
            } catch (JsonSyntaxException | JsonIOException | IOException ex) {
                GUIHelpers.displayErrorDialog("Unable to load configuration files: " + f.getAbsolutePath());
            }
        }
    }

    public static void save(File f, ControllerSettings cs) throws IOException {
        if (f.exists()) {
            f.delete();
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(cs, ControllerSettings.class);
        FileUtils.writeStringToFile(f, json, StandardCharsets.UTF_8);
    }

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

        public void reload() {
            try (InputStream fileInputStream = new FileInputStream(file)){
                loader = new Gson().fromJson(IOUtils.toString(fileInputStream, StandardCharsets.UTF_8), ControllerSettings.class);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }
}
