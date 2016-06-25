package com.willwinder.universalgcodesender.utils;

import com.google.gson.annotations.Expose;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.pendantui.PendantConfigBean;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.types.WindowSettings;

import java.util.*;

public class Settings {
    private String firmwareVersion = "GRBL";
    private String fileName = System.getProperty("user.home");
    private String port = "";
    private String portRate = "9600";
    private boolean manualModeEnabled = false;
    private double manualModeStepSize = 1;
    private double zJogStepSize = 1;
    private boolean scrollWindowEnabled = true;
    private boolean verboseOutputEnabled = false;
    private boolean commandTableEnabled = false;
    // Sender Settings
    private WindowSettings mainWindowSettings = new WindowSettings(0,0,640,520);
    private WindowSettings visualizerWindowSettings = new WindowSettings(0,0,640,480);
    private boolean overrideSpeedSelected = false;
    private double overrideSpeedValue = 60;
    private boolean singleStepMode = false;
    private int maxCommandLength = 50;
    private int truncateDecimalLength = 4;
    private boolean removeAllWhitespace = true;
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;
    private boolean displayStateColor = true;
    private boolean convertArcsToLines = false;
    private double smallArcThreshold = 2.0;
    private double smallArcSegmentLength = 1.3;
    private String defaultUnits = Units.MM.abbreviation;

    private boolean showNightlyWarning = true;
    private boolean showSerialPortWarning = true;

    private boolean autoConnect = false;
    private boolean autoReconnect = false;

    //vvv deprecated fields, still here to not break the old save files
    @Expose(serialize = false)
    private String customGcode1 = null;

    @Expose(serialize = false)
    private String customGcode2 = null;

    @Expose(serialize = false)
    private String customGcode3 = null;

    @Expose(serialize = false)
    private String customGcode4 = null;

    @Expose(serialize = false)
    private String customGcode5 = null;
    //^^^ deprecated fields, still here to not break the old save files

    private final Map<Integer, Macro> macros = new HashMap() {{
        put(1, new Macro(null, null, "G91 X0 Y0;"));
    }};

    private String language = "en_US";
    
    private PendantConfigBean pendantConfig = new PendantConfigBean();

    /**
     * The GSON deserialization doesn't do anything beyond initialize what's in the json document.  Call finalizeInitialization() before using the Settings.
     */
	public Settings() {
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

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getLastOpenedFilename() {
		return fileName;
	}

	public void setLastOpenedFilename(String fileName) {
		this.fileName = fileName;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getPortRate() {
		return portRate;
	}

	public void setPortRate(String portRate) {
		this.portRate = portRate;
	}

	public boolean isManualModeEnabled() {
		return manualModeEnabled;
	}

	public void setManualModeEnabled(boolean manualModeEnabled) {
		this.manualModeEnabled = manualModeEnabled;
	}

	public double getManualModeStepSize() {
		return manualModeStepSize;
	}

	public void setManualModeStepSize(double manualModeStepSize) {
		this.manualModeStepSize = manualModeStepSize;
	}

    public double getzJogStepSize() {
        return zJogStepSize;
    }

    public void setzJogStepSize(double zJogStepSize) {
        this.zJogStepSize = zJogStepSize;
    }

    public boolean isScrollWindowEnabled() {
		return scrollWindowEnabled;
	}

	public void setScrollWindowEnabled(boolean scrollWindowEnabled) {
		this.scrollWindowEnabled = scrollWindowEnabled;
	}

	public boolean isVerboseOutputEnabled() {
		return verboseOutputEnabled;
	}

	public void setVerboseOutputEnabled(boolean verboseOutputEnabled) {
		this.verboseOutputEnabled = verboseOutputEnabled;
	}

    public boolean isCommandTableEnabled() {
        return commandTableEnabled;
    }

    public void setCommandTableEnabled(boolean enabled) {
        commandTableEnabled = enabled;
    }

    public WindowSettings getMainWindowSettings() {
        return this.mainWindowSettings;
    }
    
    public void setMainWindowSettings(WindowSettings ws) {
        this.mainWindowSettings = ws;
    }

    public WindowSettings getVisualizerWindowSettings() {
        return this.visualizerWindowSettings;
    }
    
    public void setVisualizerWindowSettings(WindowSettings vw) {
        this.visualizerWindowSettings = vw;
    }

    public boolean isOverrideSpeedSelected() {
		return overrideSpeedSelected;
	}

	public void setOverrideSpeedSelected(boolean overrideSpeedSelected) {
		this.overrideSpeedSelected = overrideSpeedSelected;
	}

	public double getOverrideSpeedValue() {
		return overrideSpeedValue;
	}

	public void setOverrideSpeedValue(double overrideSpeedValue) {
		this.overrideSpeedValue = overrideSpeedValue;
	}

	public boolean isSingleStepMode() {
		return singleStepMode;
	}

	public void setSingleStepMode(boolean singleStepMode) {
		this.singleStepMode = singleStepMode;
	}

	public int getMaxCommandLength() {
		return maxCommandLength;
	}

	public void setMaxCommandLength(int maxCommandLength) {
		this.maxCommandLength = maxCommandLength;
	}

	public int getTruncateDecimalLength() {
		return truncateDecimalLength;
	}

	public void setTruncateDecimalLength(int truncateDecimalLength) {
		this.truncateDecimalLength = truncateDecimalLength;
	}

	public boolean isRemoveAllWhitespace() {
		return removeAllWhitespace;
	}

	public void setRemoveAllWhitespace(boolean removeAllWhitespace) {
		this.removeAllWhitespace = removeAllWhitespace;
	}

	public boolean isStatusUpdatesEnabled() {
		return statusUpdatesEnabled;
	}

	public void setStatusUpdatesEnabled(boolean statusUpdatesEnabled) {
		this.statusUpdatesEnabled = statusUpdatesEnabled;
	}

	public int getStatusUpdateRate() {
		return statusUpdateRate;
	}

	public void setStatusUpdateRate(int statusUpdateRate) {
		this.statusUpdateRate = statusUpdateRate;
	}

	public boolean isDisplayStateColor() {
		return displayStateColor;
	}

	public void setDisplayStateColor(boolean displayStateColor) {
		this.displayStateColor = displayStateColor;
	}

	public boolean isConvertArcsToLines() {
		return convertArcsToLines;
	}

	public void setConvertArcsToLines(boolean convertArcsToLines) {
		this.convertArcsToLines = convertArcsToLines;
	}

	public double getSmallArcThreshold() {
		return smallArcThreshold;
	}

	public void setSmallArcThreshold(double smallArcThreshold) {
		this.smallArcThreshold = smallArcThreshold;
	}

	public double getSmallArcSegmentLength() {
		return smallArcSegmentLength;
	}

	public void setSmallArcSegmentLength(double smallArcSegmentLength) {
		this.smallArcSegmentLength = smallArcSegmentLength;
	}

	public PendantConfigBean getPendantConfig() {
		return pendantConfig;
	}

	public void setPendantConfig(PendantConfigBean pendantConfig) {
		this.pendantConfig = pendantConfig;
	}
        
    public String getDefaultUnits() {
        if (Units.getUnit(defaultUnits) == null) {
            return Units.MM.abbreviation;
        }
        return defaultUnits;
    }
        
    public void setDefaultUnits(String units) {
        if (Units.getUnit(defaultUnits) != null) {
            defaultUnits = units;
        }
    }

    public boolean isShowNightlyWarning() {
        return showNightlyWarning;
    }

    public void setShowNightlyWarning(boolean showNightlyWarning) {
        this.showNightlyWarning = showNightlyWarning;
    }

    public boolean isShowSerialPortWarning() {
        return showSerialPortWarning;
    }

    public void setShowSerialPortWarning(boolean showSerialPortWarning) {
        this.showSerialPortWarning = showSerialPortWarning;
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
        //Obviously it would be more efficient to just store the max index value, but this is safer in that it's one less thing
        //to keep in sync.
        int i = -1;
        for (Integer index : macros.keySet()) {
            i = Math.max(i, index);
        }
        return i;
    }


    public void clearMacro(Integer index) {
        macros.remove(index);
    }

    @Deprecated
    public void updateMacro(Integer index, String gcode) {
        Macro macro = getMacro(index);
        updateMacro(index, macro.getName(), macro.getDescription(), gcode);
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
    }

        public String getLanguage() {
            return this.language;
        }
        
        public void setLanguage (String language) {
            this.language = language;
        }

    public boolean isAutoConnectEnabled() {
        return autoConnect;
    }

    public boolean isAutoReconnect() {
        return autoReconnect;
    }

    public void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    public void setAutoConnectEnabled(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }
}
