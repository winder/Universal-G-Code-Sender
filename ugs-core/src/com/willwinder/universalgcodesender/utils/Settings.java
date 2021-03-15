/*
    Copyright 2014-2021 Will Winder

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

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.types.WindowSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Settings {
    private static final Logger logger = Logger.getLogger(Settings.class.getName());

    // Transient, don't serialize or deserialize.
    transient private SettingChangeListener listener = null;
    transient public static int HISTORY_SIZE = 20;

    private String firmwareVersion = "GRBL";
    private String fileName = System.getProperty("user.home");

    // Welcome screen
    private Deque<String> fileHistory = new ArrayDeque<>();
    private Deque<String> dirHistory = new ArrayDeque<>();

    // Connection
    private String port = "";
    private String portRate = "115200";

    // Jogging / JogService
    private boolean manualModeEnabled = false;
    private double manualModeStepSize = 1;
    private boolean useZStepSize = true;
    private boolean showABCStepSize = true;
    private double zJogStepSize = 1;
    private double abcJogStepSize = 1;
    private double jogFeedRate = 10;

    // Console
    private boolean scrollWindowEnabled = true;
    private boolean verboseOutputEnabled = false;
    private boolean commandTableEnabled = false;

    // Sender Settings
    private WindowSettings mainWindowSettings = new WindowSettings(0,0,640,520);
    private WindowSettings visualizerWindowSettings = new WindowSettings(0,0,640,480);
    private boolean singleStepMode = false;
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;
    private Units preferredUnits = Units.MM;
    private Set<Axis> disabledAxes = new HashSet<>();

    private boolean showNightlyWarning = true;
    private boolean showSerialPortWarning = true;
    private boolean autoStartPendant = false;
    private boolean autoConnect = false;
    private boolean autoReconnect = false;

    private AutoLevelSettings autoLevelSettings = new AutoLevelSettings();

    private FileStats fileStats = new FileStats();

    //vvv deprecated fields, still here to not break the old save files
    // Transient, don't serialize or deserialize.
    transient private String customGcode1 = null;
    transient private String customGcode2 = null;
    transient private String customGcode3 = null;
    transient private String customGcode4 = null;
    transient private String customGcode5 = null;
    //^^^ deprecated fields, still here to not break the old save files

    private Map<Integer, Macro> macros = new HashMap<>();

    private String language = "en_US";

    private String connectionDriver;

    /**
     * A directory with gcode files for easy access through pendant
     */
    private String workspaceDirectory;

    /**
     * The safety height to clear when returning to home given in mm.
     */
    private Double safetyHeight = 5d;

    /**
     * The GSON deserialization doesn't do anything beyond initialize what's in the json document.  Call finalizeInitialization() before using the Settings.
     */
    public Settings() {
        logger.fine("Initializing...");

        // Initialize macros with a default macro
        macros.put(1, new Macro(null, null, "G91 X0 Y0;"));
    }

    /**
     * Null legacy fields and move data to current data structures
     */
    public void finalizeInitialization() {
        if (customGcode1 != null) {
            updateMacro(1, null, null, customGcode1);
            customGcode1 = null;
        }
        if (customGcode2 != null) {
            updateMacro(2, null, null, customGcode2);
            customGcode2 = null;
        }
        if (customGcode3 != null) {
            updateMacro(3, null, null, customGcode3);
            customGcode3 = null;
        }
        if (customGcode4 != null) {
            updateMacro(4, null, null, customGcode4);
            customGcode4 = null;
        }
        if (customGcode5 != null) {
            updateMacro(5, null, null, customGcode5);
            customGcode5 = null;
        }
    }

    /**
     * This method should only be called once during setup, a runtime exception
     * will be thrown if that contract is violated.
     */
    public void setSettingChangeListener(SettingChangeListener listener) {
        this.listener = listener;
    }

    private void changed() {
        if (listener != null) {
            listener.settingChanged();
        }
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        if (StringUtils.equals(firmwareVersion, this.firmwareVersion)) return;
        this.firmwareVersion = firmwareVersion;
        changed();
    }

    public String getLastOpenedFilename() {
        return fileName;
    }

    public void setLastOpenedFilename(String absolutePath) {
        Path p = Paths.get(absolutePath).toAbsolutePath();
        this.fileName = p.toString();
        updateRecentFiles(p.toString());
        updateRecentDirectory(p.getParent().toString());
        changed();
    }

    public Collection<String> getRecentFiles() {
      return Collections.unmodifiableCollection(fileHistory);
    }

    public void updateRecentFiles(String absolutePath) {
      updateRecent(this.fileHistory, HISTORY_SIZE, absolutePath);
    }

    public Collection<String> getRecentDirectories() {
      return Collections.unmodifiableCollection(dirHistory);
    }

    public void updateRecentDirectory(String absolutePath) {
      updateRecent(this.dirHistory, HISTORY_SIZE, absolutePath);
    }

    private static void updateRecent(Deque<String> stack, int maxSize, String element) {
      stack.remove(element);
      stack.push(element);
      while( stack.size() > maxSize)
        stack.removeLast();
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        if (StringUtils.equals(port, this.port)) return;
        this.port = port;
        changed();
    }

    public String getPortRate() {
        return portRate;
    }

    public void setPortRate(String portRate) {
        if (StringUtils.equals(portRate, this.portRate)) return;
        this.portRate = portRate;
        changed();
    }

    public boolean isManualModeEnabled() {
        return manualModeEnabled;
    }

    public void setManualModeEnabled(boolean manualModeEnabled) {
        this.manualModeEnabled = manualModeEnabled;
        changed();
    }

    public double getManualModeStepSize() {
        return manualModeStepSize;
    }

    public void setManualModeStepSize(double manualModeStepSize) {
        this.manualModeStepSize = manualModeStepSize;
        changed();
    }

    public boolean useZStepSize() {
        return this.useZStepSize;
    }

    public void setUseZStepSize(boolean useZStepSize) {
        this.useZStepSize = useZStepSize;
        changed();
    }

    public boolean showABCStepSize() {
        return this.showABCStepSize;
    }

    public void setShowABCStepSize(boolean showABCStepSize) {
        this.showABCStepSize = showABCStepSize;
        changed();
    }

    public double getZJogStepSize() {
        return zJogStepSize;
    }

    public void setZJogStepSize(double zJogStepSize) {
        this.zJogStepSize = zJogStepSize;
        changed();
    }

    public double getABCJogStepSize() {
        return abcJogStepSize;
    }

    public void setABCJogStepSize(double abcJogStepSize) {
        this.abcJogStepSize = abcJogStepSize;
        changed();
    }

    public double getJogFeedRate() {
        return jogFeedRate;
    }

    public void setJogFeedRate(double jogFeedRate) {
        this.jogFeedRate = jogFeedRate;
        changed();
    }

    public boolean isScrollWindowEnabled() {
        return scrollWindowEnabled;
    }

    public void setScrollWindowEnabled(boolean scrollWindowEnabled) {
        this.scrollWindowEnabled = scrollWindowEnabled;
        changed();
    }

    public boolean isVerboseOutputEnabled() {
        return verboseOutputEnabled;
    }

    public void setVerboseOutputEnabled(boolean verboseOutputEnabled) {
        this.verboseOutputEnabled = verboseOutputEnabled;
        changed();
    }

    public boolean isCommandTableEnabled() {
        return commandTableEnabled;
    }

    public void setCommandTableEnabled(boolean enabled) {
        commandTableEnabled = enabled;
        changed();
    }

    public WindowSettings getMainWindowSettings() {
        return this.mainWindowSettings;
    }
    
    public void setMainWindowSettings(WindowSettings ws) {
        this.mainWindowSettings = ws;
        changed();
    }

    public WindowSettings getVisualizerWindowSettings() {
        return this.visualizerWindowSettings;
    }
    
    public void setVisualizerWindowSettings(WindowSettings vw) {
        this.visualizerWindowSettings = vw;
        changed();
    }

    public boolean isSingleStepMode() {
        return singleStepMode;
    }

    public void setSingleStepMode(boolean singleStepMode) {
        this.singleStepMode = singleStepMode;
        changed();
    }

    public boolean isStatusUpdatesEnabled() {
        return statusUpdatesEnabled;
    }

    public void setStatusUpdatesEnabled(boolean statusUpdatesEnabled) {
        this.statusUpdatesEnabled = statusUpdatesEnabled;
        changed();
    }

    public int getStatusUpdateRate() {
        return statusUpdateRate;
    }

    public void setStatusUpdateRate(int statusUpdateRate) {
        this.statusUpdateRate = statusUpdateRate;
        changed();
    }
        
    public Units getPreferredUnits() {
        return (preferredUnits == null) ? Units.MM : preferredUnits;
    }

    public boolean isAxisEnabled(Axis a) {
        return !this.disabledAxes.contains(a);
    }

    public void setAxisEnabled(Axis a, boolean enabled) {
        if (enabled ? this.disabledAxes.remove(a) : this.disabledAxes.add(a)) {
            changed();
        }
    }

    public void setPreferredUnits(Units units) {
        if (units != null) {
            double scaleUnits = UnitUtils.scaleUnits(preferredUnits, units);
            preferredUnits = units;
            changed();

            // Change
            setManualModeStepSize(manualModeStepSize * scaleUnits);
            setZJogStepSize(zJogStepSize * scaleUnits);
            setJogFeedRate(Math.round(jogFeedRate * scaleUnits));
        }
    }

    public boolean isShowNightlyWarning() {
        return showNightlyWarning;
    }

    public void setShowNightlyWarning(boolean showNightlyWarning) {
        this.showNightlyWarning = showNightlyWarning;
        changed();
    }

    public boolean isShowSerialPortWarning() {
        return showSerialPortWarning;
    }

    public void setShowSerialPortWarning(boolean showSerialPortWarning) {
        this.showSerialPortWarning = showSerialPortWarning;
        changed();
    }

    public List<Macro> getMacros() {
        return Collections.unmodifiableList(new ArrayList<>(macros.values()));
    }

    public void updateMacro(Integer index, String name, String description, String gcode) {
        if (gcode == null) {
            macros.remove(index);
        } else {
            if (name == null) {
                name = index.toString();
            }
            macros.put(index, new Macro(name, description, gcode));
        }
        changed();
    }

    public String getLanguage() {
        // "zh_CHS" is a legacy format that has been renamed.
        if ("zh_CHS".equals(this.language)) {
          this.language = "zh_CN";
        }

        return this.language;
    }
    
    public void setLanguage (String language) {
        this.language = language;
        changed();
    }

    public boolean isAutoConnectEnabled() {
        return autoConnect;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoLevelSettings(AutoLevelSettings settings) {
        if (! settings.equals(this.autoLevelSettings)) {
            this.autoLevelSettings = settings;
            changed();
        }
    }

    public AutoLevelSettings getAutoLevelSettings() {
        return this.autoLevelSettings;
    }

    public void setFileStats(FileStats settings) {
        this.fileStats = settings;
        changed();
    }

    public FileStats getFileStats() {
        return this.fileStats;
    }

    public ConnectionDriver getConnectionDriver() {
        if (StringUtils.isNotEmpty(this.connectionDriver)) {
            try {
                return ConnectionDriver.valueOf(this.connectionDriver);
            } catch (IllegalArgumentException | NullPointerException ignored) {
                // Never mind, we'll use the default
            }
        }

        return getDefaultDriver();
    }

    private ConnectionDriver getDefaultDriver() {
        ConnectionDriver result = ConnectionDriver.JSERIALCOMM;
        if (SystemUtils.IS_OS_LINUX) {
            result = ConnectionDriver.JSSC;
        }
        return result;
    }

    public void setConnectionDriver(ConnectionDriver connectionDriver) {
        this.connectionDriver = connectionDriver.name();
    }

    public void setAutoStartPendant(boolean autoStartPendant) {
        this.autoStartPendant = autoStartPendant;
        changed();
    }

    public boolean isAutoStartPendant() {
        return this.autoStartPendant;
    }

    public void setWorkspaceDirectory(String workspaceDirectory) {
        this.workspaceDirectory = workspaceDirectory;
    }

    public String getWorkspaceDirectory() {
        return this.workspaceDirectory;
    }

    public void addMacro(Macro macro) {
        int newIndex = macros.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        macros.put(newIndex, macro);
        changed();
    }

    public void setMacros(List<Macro> macros) {
        this.macros.clear();
        macros.forEach(this::addMacro);
        changed();
    }

    public double getSafetyHeight() {
        return this.safetyHeight;
    }

    public void setSafetyHeight(double safetyHeight) {
        this.safetyHeight = safetyHeight;
    }

    public static class AutoLevelSettings {
        // Setting window
        public double autoLevelProbeZeroHeight = 0;
        public Position autoLevelProbeOffset = new Position(0, 0, 0, Units.UNKNOWN);
        public double autoLevelArcSliceLength = 0.01;

        // Main window
        public double stepResolution = 10;
        public double probeSpeed = 10;
        public double zSurface = 0;

        public boolean equals(AutoLevelSettings obj) {
            return
                    this.autoLevelProbeZeroHeight == obj.autoLevelProbeZeroHeight &&
                            Objects.equals(this.autoLevelProbeOffset, obj.autoLevelProbeOffset) &&
                            this.autoLevelArcSliceLength == obj.autoLevelArcSliceLength &&
                            this.stepResolution == obj.stepResolution &&
                            this.probeSpeed == obj.probeSpeed &&
                            this.zSurface == obj.zSurface;
        }
    }

    public static class FileStats {
        public Position minCoordinate;
        public Position maxCoordinate;
        public long numCommands;

        public FileStats() {
            this.minCoordinate = new Position(0, 0, 0, Units.MM);
            this.maxCoordinate = new Position(0, 0, 0, Units.MM);
            this.numCommands = 0;
        }

        public FileStats(Position min, Position max, long num) {
            this.minCoordinate = min;
            this.maxCoordinate = max;
            this.numCommands = num;
        }
    }
}
