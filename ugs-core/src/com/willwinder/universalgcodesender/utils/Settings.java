/*
    Copyright 2014-2017 Will Winder

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
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.pendantui.PendantConfigBean;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.types.WindowSettings;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class Settings {
    // Transient, don't serialize or deserialize.
    transient private SettingChangeListener listener = null;

    private String firmwareVersion = "GRBL";
    private String fileName = System.getProperty("user.home");
    private String port = "";
    private String portRate = "115200";
    private boolean manualModeEnabled = false;
    private double manualModeStepSize = 1;
    private boolean useZStepSize = false;
    private double zJogStepSize = 1;
    private double jogFeedRate = 10;
    private boolean scrollWindowEnabled = true;
    private boolean verboseOutputEnabled = false;
    private boolean commandTableEnabled = false;
    // Sender Settings
    private WindowSettings mainWindowSettings = new WindowSettings(0,0,640,520);
    private WindowSettings visualizerWindowSettings = new WindowSettings(0,0,640,480);
    private boolean singleStepMode = false;
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;
    private boolean displayStateColor = true;
    private String defaultUnits = Units.MM.abbreviation;

    private boolean showNightlyWarning = true;
    private boolean showSerialPortWarning = true;

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
    
    private PendantConfigBean pendantConfig = new PendantConfigBean();

    private String connectionDriver;

    private String clientId = UUID.randomUUID().toString();

    /**
     * A setting the enables usage statistics to be reported to a central server
     */
    private TrackingSetting trackingSetting = TrackingSetting.NOT_ANSWERED;

    /**
     * The GSON deserialization doesn't do anything beyond initialize what's in the json document.  Call finalizeInitialization() before using the Settings.
     */
    public Settings() {
        System.out.println("Initializing...");

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
        listener.settingChanged();
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

    public void setLastOpenedFilename(String fileName) {
        this.fileName = fileName;
        changed();
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

    public double getzJogStepSize() {
        return zJogStepSize;
    }

    public void setzJogStepSize(double zJogStepSize) {
        this.zJogStepSize = zJogStepSize;
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

    public boolean isDisplayStateColor() {
        return displayStateColor;
    }

    public void setDisplayStateColor(boolean displayStateColor) {
        this.displayStateColor = displayStateColor;
        changed();
    }

    public PendantConfigBean getPendantConfig() {
        return pendantConfig;
    }

    public void setPendantConfig(PendantConfigBean pendantConfig) {
        this.pendantConfig = pendantConfig;
        changed();
    }
        
    public Units getPreferredUnits() {
        Units u = Units.getUnit(defaultUnits);

        return (u == null) ? Units.MM : u;
    }

    @Deprecated
    public String getDefaultUnits() {
        if (Units.getUnit(defaultUnits) == null) {
            return Units.MM.abbreviation;
        }
        return defaultUnits;
    }
        
    public void setDefaultUnits(String units) {
        if (Units.getUnit(defaultUnits) != null) {
            defaultUnits = units;
            changed();
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

    public Macro getMacro(Integer index) {
        Macro macro = macros.get(index);
        if (macro == null) {
            macro = new Macro(index.toString(), null, null);
        }
        return macro;
    }

    public Integer getNumMacros() {
        return macros.size();
    }

    public Integer getLastMacroIndex() {
        // Obviously it would be more efficient to just store the max index
        // value, but this is safer in that it's one less thing to keep in sync.
        int i = -1;
        for (Integer index : macros.keySet()) {
            i = Math.max(i, index);
        }
        return i;
    }

    public void clearMacros() {
        macros.clear();
        changed();
    }

    public void clearMacro(Integer index) {
        macros.remove(index);
        changed();
    }

    public void updateMacro(Integer index, Macro macro) {
        updateMacro(index, macro.getName(), macro.getDescription(), macro.getGcode());
    }

    public void updateMacro(Integer index, String name, String description, String gcode) {
        if (gcode == null || gcode.trim().isEmpty()) {
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
        ConnectionDriver connectionDriver = ConnectionDriver.JSSC;
        if (StringUtils.isNotEmpty(this.connectionDriver)) {
            try {
                connectionDriver = ConnectionDriver.valueOf(this.connectionDriver);
            } catch (IllegalArgumentException | NullPointerException ignored) {
                // Never mind, we'll use the default
            }
        }
        return connectionDriver;
    }

    public void setConnectionDriver(ConnectionDriver connectionDriver) {
        this.connectionDriver = connectionDriver.name();
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Returns if usage statistics should be enabled.
     *
     * @return a tracking setting
     */
    public TrackingSetting getTrackingSetting() {
        return trackingSetting;
    }

    /**
     * Sets a tracking setting. If {@link TrackingSetting#NOT_ANSWERED} a popup dialog will be shown
     * letting the user to select if tracking should be enabled.
     *
     * @param trackingSetting the tracking setting if usage statistics should be reported
     */
    public void setTrackingSetting(TrackingSetting trackingSetting) {
        this.trackingSetting = trackingSetting;
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
