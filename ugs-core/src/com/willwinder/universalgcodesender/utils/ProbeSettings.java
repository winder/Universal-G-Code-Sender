/*
    Copyright 2026 Will Winder

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

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Settings for the touch-off/center-finding probe operations (Z, single-face X/Y, X/Y-center,
 * bore/rectangle-center). All lengths are stored in millimeters and feed rates in millimeters
 * per minute, regardless of the units preferred for display.
 */
public class ProbeSettings implements Serializable {

    /**
     * The initial, fast probe feed rate in mm/min - used to quickly find the approximate contact
     * point before retracting and re-probing more slowly for precision.
     */
    private double feedRateFast = 100;

    /**
     * The slower, precise probe feed rate in mm/min used for the second (retract-and-reprobe) pass.
     */
    private double feedRateSlow = 10;

    /**
     * How far to retract between the fast and slow probe passes, in millimeters.
     */
    private double retractDistance = 3;

    /**
     * How long to pause after retracting before the slow probe pass, in seconds - gives a
     * slow-responding touch probe time to settle/release.
     */
    private double delayAfterRetract = 1;

    /**
     * The probe tip/tool diameter in millimeters, used to compensate the computed zero position
     * for single-face and center-finding probes (the reported contact position is the tool's
     * center, not its edge).
     */
    private double probeDiameter = 3.175;

    /**
     * The thickness in millimeters of a touch plate resting on the material surface for a Z
     * probe - the zeroed Z work position is offset by this amount so it lands at the material
     * surface rather than the top of the plate.
     */
    private double plateThickness = 0;

    /**
     * The default maximum travel distance in millimeters for a probe search, per operation.
     */
    private double maxTravel = 25;

    /**
     * Clamp the requested probe travel to the machine's configured soft limits (when the
     * firmware reports them as enabled) rather than sending a probe move that would exceed them.
     */
    private boolean compensateSoftLimits = true;

    private transient final Set<SettingChangeListener> settingChangeListeners = ConcurrentHashMap.newKeySet();

    public ProbeSettings() {
    }

    public ProbeSettings(ProbeSettings settings) {
        apply(settings);
    }

    public boolean equals(ProbeSettings obj) {
        return this.feedRateFast == obj.feedRateFast
                && this.feedRateSlow == obj.feedRateSlow
                && this.retractDistance == obj.retractDistance
                && this.delayAfterRetract == obj.delayAfterRetract
                && this.probeDiameter == obj.probeDiameter
                && this.plateThickness == obj.plateThickness
                && this.maxTravel == obj.maxTravel
                && this.compensateSoftLimits == obj.compensateSoftLimits;
    }

    public void addSettingChangeListener(SettingChangeListener listener) {
        settingChangeListeners.add(listener);
    }

    private void changed() {
        settingChangeListeners.forEach(SettingChangeListener::settingChanged);
    }

    public double getFeedRateFast() {
        return feedRateFast;
    }

    public void setFeedRateFast(double feedRateFast) {
        if (this.feedRateFast != feedRateFast) {
            this.feedRateFast = feedRateFast;
            changed();
        }
    }

    public double getFeedRateSlow() {
        return feedRateSlow;
    }

    public void setFeedRateSlow(double feedRateSlow) {
        if (this.feedRateSlow != feedRateSlow) {
            this.feedRateSlow = feedRateSlow;
            changed();
        }
    }

    public double getRetractDistance() {
        return retractDistance;
    }

    public void setRetractDistance(double retractDistance) {
        if (this.retractDistance != retractDistance) {
            this.retractDistance = retractDistance;
            changed();
        }
    }

    public double getDelayAfterRetract() {
        return delayAfterRetract;
    }

    public void setDelayAfterRetract(double delayAfterRetract) {
        if (this.delayAfterRetract != delayAfterRetract) {
            this.delayAfterRetract = delayAfterRetract;
            changed();
        }
    }

    public double getProbeDiameter() {
        return probeDiameter;
    }

    public void setProbeDiameter(double probeDiameter) {
        if (this.probeDiameter != probeDiameter) {
            this.probeDiameter = probeDiameter;
            changed();
        }
    }

    public double getPlateThickness() {
        return plateThickness;
    }

    public void setPlateThickness(double plateThickness) {
        if (this.plateThickness != plateThickness) {
            this.plateThickness = plateThickness;
            changed();
        }
    }

    public double getMaxTravel() {
        return maxTravel;
    }

    public void setMaxTravel(double maxTravel) {
        if (this.maxTravel != maxTravel) {
            this.maxTravel = maxTravel;
            changed();
        }
    }

    public boolean isCompensateSoftLimits() {
        return compensateSoftLimits;
    }

    public void setCompensateSoftLimits(boolean compensateSoftLimits) {
        if (this.compensateSoftLimits != compensateSoftLimits) {
            this.compensateSoftLimits = compensateSoftLimits;
            changed();
        }
    }

    public void apply(ProbeSettings settings) {
        if (!this.equals(settings)) {
            feedRateFast = settings.getFeedRateFast();
            feedRateSlow = settings.getFeedRateSlow();
            retractDistance = settings.getRetractDistance();
            delayAfterRetract = settings.getDelayAfterRetract();
            probeDiameter = settings.getProbeDiameter();
            plateThickness = settings.getPlateThickness();
            maxTravel = settings.getMaxTravel();
            compensateSoftLimits = settings.isCompensateSoftLimits();
            changed();
        }
    }
}
