package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.pendantui.PendantConfigBean;
import com.willwinder.universalgcodesender.types.WindowSettings;

public class Settings {
    private String firmwareVersion = "GRBL";
    private String fileName = System.getProperty("user.home");
    private String port = "";
    private String portRate = "9600";
    private boolean manualModeEnabled = false;
    private double manualModeStepSize = 1;
    private boolean scrollWindowEnabled = true;
    private boolean verboseOutputEnabled = false;
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
    private PendantConfigBean pendantConfig = new PendantConfigBean();

	public Settings() {
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
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

}
