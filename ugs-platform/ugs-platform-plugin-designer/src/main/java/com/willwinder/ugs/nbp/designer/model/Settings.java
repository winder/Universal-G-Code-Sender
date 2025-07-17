/*
    Copyright 2021-2025 Will Winder

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
package com.willwinder.ugs.nbp.designer.model;

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.logic.SettingsListener;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.Set;

public class Settings {
    private final Set<SettingsListener> listeners = Sets.newConcurrentHashSet();
    private int feedSpeed = 1000;
    private int plungeSpeed = 400;
    private double toolDiameter = 3d;
    private double stockThickness = 10;
    private double safeHeight = 5;
    private UnitUtils.Units preferredUnits = UnitUtils.Units.MM;
    private double toolStepOver = 0.3;
    private double depthPerPass = 1;
    private double laserDiameter = 0.2;
    private int maxSpindleSpeed = 255;
    private boolean detectMaxSpindleSpeed = true;
    private String spindleDirection = "M3";
    private double flatnessPrecision = 0.1;

    public Settings() {
    }

    public Settings(Settings settings) {
        applySettings(settings);
    }

    public int getFeedSpeed() {
        return feedSpeed;
    }

    public void setFeedSpeed(int feedSpeed) {
        this.feedSpeed = feedSpeed;
        notifyListeners();
    }

    public int getPlungeSpeed() {
        return plungeSpeed;
    }

    public void setPlungeSpeed(int plungeSpeed) {
        this.plungeSpeed = plungeSpeed;
        notifyListeners();
    }

    public String getSpindleDirection() {
        return spindleDirection;
    }

    public void setSpindleDirection(String newSpindleDirection) {
        this.spindleDirection = newSpindleDirection;
        notifyListeners();
    }

    /**
     * Returns the tool diameter in millimeters
     *
     * @return the tool diameter
     */
    public double getToolDiameter() {
        return toolDiameter;
    }

    public void setToolDiameter(double toolDiameter) {
        this.toolDiameter = Math.abs(toolDiameter);
        notifyListeners();
    }

    /**
     * Returns the stock thickness in millimeters
     *
     * @return the stock thickness
     */
    public double getStockThickness() {
        return stockThickness;
    }

    public void setStockThickness(double thickness) {
        this.stockThickness = thickness;
        notifyListeners();
    }

    private void notifyListeners() {
        listeners.forEach(SettingsListener::onSettingsChanged);
    }

    public void addListener(SettingsListener settingsListener) {
        listeners.add(settingsListener);
    }

    /**
     * Returns the general safety height in millimeters
     *
     * @return the safety height
     */
    public double getSafeHeight() {
        return safeHeight;
    }

    public void setSafeHeight(double safeHeight) {
        this.safeHeight = safeHeight;
        notifyListeners();
    }

    /**
     * Returns the preferred units to view in the UI
     *
     * @return the preferred viewing units
     */
    public UnitUtils.Units getPreferredUnits() {
        return preferredUnits;
    }

    public void setPreferredUnits(UnitUtils.Units preferredUnits) {
        this.preferredUnits = preferredUnits;
        notifyListeners();
    }

    /**
     * Returns the percentage of how much the tool should cut each time given a value between 0.01 and 1.
     *
     * @return the percentage
     */
    public double getToolStepOver() {
        return toolStepOver;
    }

    /**
     * Sets the percentage of how much the tool should step over given a value between 0.01 and 1.
     *
     * @param toolStepOver a value between 0.01 and 1
     */
    public void setToolStepOver(double toolStepOver) {
        toolStepOver = Math.abs(toolStepOver);
        if (toolStepOver == 0) {
            toolStepOver = 0.01;
        } else if (toolStepOver > 1) {
            toolStepOver = 1;
        }

        this.toolStepOver = toolStepOver;
        notifyListeners();
    }

    public String getStockSizeDescription() {
        double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, getPreferredUnits());
        return Utils.formatter.format(getStockThickness() * scale) + " " + getPreferredUnits().abbreviation;
    }

    public double getDepthPerPass() {
        return depthPerPass;
    }

    public void setDepthPerPass(double depthPerPass) {
        if (depthPerPass == 0) {
            depthPerPass = 0.001;
        }

        this.depthPerPass = Math.abs(depthPerPass);
        notifyListeners();
    }

    public void applySettings(Settings settings) {
        if (settings == null) {
            return;
        }

        setDepthPerPass(settings.getDepthPerPass());
        setFeedSpeed(settings.getFeedSpeed());
        setPlungeSpeed(settings.getPlungeSpeed());
        setStockThickness(settings.getStockThickness());
        setToolDiameter(settings.getToolDiameter());
        setToolStepOver(settings.getToolStepOver());
        setPreferredUnits(settings.getPreferredUnits());
        setSafeHeight(settings.getSafeHeight());
        setLaserDiameter(settings.getLaserDiameter());
        setMaxSpindleSpeed(settings.getMaxSpindleSpeed());
        setDetectMaxSpindleSpeed(settings.getDetectMaxSpindleSpeed());
        setSpindleDirection(settings.getSpindleDirection());
        setFlatnessPrecision(settings.getFlatnessPrecision());
    }

    public double getLaserDiameter() {
        return laserDiameter;
    }

    public void setLaserDiameter(double laserDiameter) {
        this.laserDiameter = laserDiameter;
        notifyListeners();
    }

    public int getMaxSpindleSpeed() {
        return maxSpindleSpeed;
    }

    public void setMaxSpindleSpeed(int maxSpindleSpeed) {
        if (this.maxSpindleSpeed == Math.abs(maxSpindleSpeed)) {
            return;
        }
        this.maxSpindleSpeed = Math.abs(maxSpindleSpeed);
        notifyListeners();
    }

    public boolean getDetectMaxSpindleSpeed() {
        return detectMaxSpindleSpeed;
    }

    public void setDetectMaxSpindleSpeed(boolean detectMaxSpindleSpeed) {
        this.detectMaxSpindleSpeed = detectMaxSpindleSpeed;
        notifyListeners();
    }

    public double getFlatnessPrecision() {
        return flatnessPrecision;
    }

    public void setFlatnessPrecision(double flatnessPrecision) {
        this.flatnessPrecision = flatnessPrecision;
        notifyListeners();
    }
}
