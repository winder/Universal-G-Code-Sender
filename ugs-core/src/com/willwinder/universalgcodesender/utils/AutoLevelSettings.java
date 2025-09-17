package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.model.Position;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AutoLevelSettings implements Serializable {

    private double autoLevelProbeZeroHeight = 0;
    private Position autoLevelProbeOffset = Position.ZERO;
    /**
     * How long the arcs segments should be expanded in millimeters
     */
    private double autoLevelArcSliceLength = 0.01;

    /**
     * The fast probe scan rate in mm/min
     */
    private double probeScanFeedRate = 1000;

    /**
     * Probe speed in mm/min
     */
    private double probeSpeed = 10;
    // Main window
    private double stepResolution = 1;
    /**
     * Sets the corner for the minimum position to scan during auto leveling
     */
    private double minX = 0.0;
    private double minY = 0.0;
    private double minZ = 0.0;
    private double maxX = 1.0;
    private double maxY = 1.0;
    private double maxZ = 1.0;

    /**
     * The precentage of the maxZ to retract from the previous probe position
     */
    private double zRetract = 1.0;
    private double zSurface = 0;

    private boolean applyToGcode = true;
    private transient final Set<SettingChangeListener> settingChangeListeners = ConcurrentHashMap.newKeySet();

    public AutoLevelSettings() {
    }

    public AutoLevelSettings(AutoLevelSettings autoLevelSettings) {
        apply(autoLevelSettings);
    }

    public boolean equals(AutoLevelSettings obj) {
        return this.minX == obj.minX && this.minY == obj.minY && this.minZ == obj.minZ && this.maxX == obj.maxX && this.maxY == obj.maxY && this.maxZ == obj.maxZ && this.autoLevelProbeZeroHeight == obj.autoLevelProbeZeroHeight && Objects.equals(this.autoLevelProbeOffset, obj.autoLevelProbeOffset) && this.autoLevelArcSliceLength == obj.autoLevelArcSliceLength && this.stepResolution == obj.stepResolution && this.probeSpeed == obj.probeSpeed && this.probeScanFeedRate == obj.probeScanFeedRate && this.zRetract == obj.zRetract && this.zSurface == obj.zSurface;
    }

    public void setSettingChangeListener(SettingChangeListener settingChangeListener) {
        settingChangeListeners.add(settingChangeListener);
    }

    private void changed() {
        settingChangeListeners.forEach(SettingChangeListener::settingChanged);
    }

    public double getMinX() {
        return minX;
    }

    public void setMinX(double minX) {
        if (this.minX != minX) {
            this.minX = minX;
            changed();
        }
    }

    public double getMinY() {
        return minY;
    }

    public void setMinY(double minY) {
        if (this.minY != minY) {
            this.minY = minY;
            changed();
        }
    }

    public double getMinZ() {
        return minZ;
    }

    public void setMinZ(double minZ) {
        if (this.minZ != minZ) {
            this.minZ = minZ;
            changed();
        }
    }

    public double getMaxX() {
        return maxX;
    }

    public void setMaxX(double maxX) {
        if (this.maxX != maxX) {
            this.maxX = maxX;
            changed();
        }
    }

    public double getMaxY() {
        return maxY;
    }

    public void setMaxY(double maxY) {
        if (this.maxY != maxY) {
            this.maxY = maxY;
            changed();
        }
    }

    public double getMaxZ() {
        return maxZ;
    }

    public void setMaxZ(double maxZ) {
        if (this.maxZ != maxZ) {
            this.maxZ = maxZ;
            changed();
        }
    }


    public double getStepResolution() {
        return stepResolution;
    }

    public void setStepResolution(double stepResolution) {
        if (this.stepResolution != stepResolution) {
            this.stepResolution = stepResolution;
            changed();
        }
    }

    public double getZRetract() {
        return Math.min(Math.max(0.01, zRetract), 1.0);
    }

    public void setZRetract(double zRetract) {
        if (this.zRetract != zRetract) {
            this.zRetract = zRetract;
            changed();
        }
    }

    public double getZSurface() {
        return zSurface;
    }

    public void setZSurface(double zSurface) {
        if (this.zSurface != zSurface) {
            this.zSurface = zSurface;
            changed();
        }
    }

    public double getProbeScanFeedRate() {
        return probeScanFeedRate;
    }

    public void setProbeScanFeedRate(double probeScanFeedRate) {
        if (this.probeScanFeedRate != probeScanFeedRate) {
            this.probeScanFeedRate = probeScanFeedRate;
            changed();
        }
    }

    public double getProbeSpeed() {
        return probeSpeed;
    }

    public void setProbeSpeed(double probeSpeed) {
        if (this.probeSpeed != probeSpeed) {
            this.probeSpeed = probeSpeed;
            changed();
        }
    }

    public double getAutoLevelProbeZeroHeight() {
        return autoLevelProbeZeroHeight;
    }

    public void setAutoLevelProbeZeroHeight(double autoLevelProbeZeroHeight) {
        if (this.autoLevelProbeZeroHeight != autoLevelProbeZeroHeight) {
            this.autoLevelProbeZeroHeight = autoLevelProbeZeroHeight;
            changed();
        }
    }

    public Position getAutoLevelProbeOffset() {
        return autoLevelProbeOffset;
    }

    public void setAutoLevelProbeOffset(Position autoLevelProbeOffset) {
        if (this.autoLevelProbeOffset != autoLevelProbeOffset) {
            this.autoLevelProbeOffset = autoLevelProbeOffset;
            changed();
        }
    }

    public double getAutoLevelArcSliceLength() {
        return autoLevelArcSliceLength;
    }

    public void setAutoLevelArcSliceLength(double autoLevelArcSliceLength) {
        if (this.autoLevelArcSliceLength != autoLevelArcSliceLength) {
            this.autoLevelArcSliceLength = autoLevelArcSliceLength;
            changed();
        }
    }

    public boolean getApplyToGcode() {
        return applyToGcode;
    }

    public void setApplyToGcode(boolean applyToGcode) {
        if (this.applyToGcode != applyToGcode) {
            this.applyToGcode = applyToGcode;
            changed();
        }
    }

    public void apply(AutoLevelSettings settings) {
        if (!this.equals(settings)) {
            autoLevelProbeZeroHeight = settings.getAutoLevelProbeZeroHeight();
            autoLevelProbeOffset = settings.getAutoLevelProbeOffset();
            autoLevelArcSliceLength = settings.getAutoLevelArcSliceLength();
            probeScanFeedRate = settings.getProbeScanFeedRate();
            probeSpeed = settings.getProbeSpeed();
            stepResolution = settings.getStepResolution();
            minX = settings.getMinX();
            minY = settings.getMinY();
            minZ = settings.getMinZ();
            maxX = settings.getMaxX();
            maxY = settings.getMaxY();
            maxZ = settings.getMaxZ();
            zSurface = settings.getZSurface();
            zRetract = settings.getZRetract();
            applyToGcode = settings.getApplyToGcode();
            changed();
        }
    }

    public void setMin(Position position) {
        setMinX(position.getX());
        setMinY(position.getY());
        setMinZ(position.getZ());
    }

    public void setMax(Position position) {
        setMaxX(position.getX());
        setMaxY(position.getY());
        setMaxZ(position.getZ());
    }

    public void addSettingChangeListener(SettingChangeListener listener) {
        settingChangeListeners.add(listener);
    }
}
