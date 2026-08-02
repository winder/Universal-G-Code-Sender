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
package com.willwinder.ugs.designer.model;

import com.google.common.collect.Sets;
import com.willwinder.ugs.designer.logic.SettingsListener;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.Set;

public class Settings {
    private final Set<SettingsListener> listeners = Sets.newConcurrentHashSet();
    private int feedSpeed = 1000;
    private int plungeSpeed = 400;
    private double toolDiameter = 3d;
    private EndmillShape toolShape = EndmillShape.UPCUT;
    private double stockThickness = 10;
    private double safeHeight = 5;
    private UnitUtils.Units preferredUnits = UnitUtils.Units.MM;
    private double toolStepOver = 0.3;
    private double vBitAngle = 60;
    private double depthPerPass = 1;
    private double laserDiameter = 0.2;
    private int maxSpindleSpeed = 255;
    private boolean detectMaxSpindleSpeed = true;
    private String spindleDirection = "M3";
    private double flatnessPrecision = 0.1;
    private boolean arcFitting = true;
    private boolean useToolChanges = false;
    private int toolNumber = ToolDefinition.UNASSIGNED_TOOL_NUMBER;
    private String currentToolId;
    private ToolDefinition currentToolSnapshot;

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
     * Returns the shape of the currently configured tool
     *
     * @return the tool shape, never {@code null}
     */
    public EndmillShape getToolShape() {
        return toolShape == null ? EndmillShape.UPCUT : toolShape;
    }

    public void setToolShape(EndmillShape toolShape) {
        this.toolShape = toolShape;
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

    /**
     * Returns the included angle of the V-shaped bit in degrees. This is what decides how much
     * wider a carved line gets for every millimeter the tool is lowered.
     *
     * @return the included angle in degrees
     */
    public double getVBitAngle() {
        return vBitAngle;
    }

    /**
     * Sets the included angle of the V-shaped bit given a value between 1 and 179 degrees. Angles
     * outside that range would either never widen the cut or never reach any depth.
     *
     * @param vBitAngle the included angle in degrees
     */
    public void setVBitAngle(double vBitAngle) {
        this.vBitAngle = Math.min(Math.max(1, Math.abs(vBitAngle)), 179);
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
        setToolShape(settings.getToolShape());
        setToolStepOver(settings.getToolStepOver());
        setVBitAngle(settings.getVBitAngle());
        setPreferredUnits(settings.getPreferredUnits());
        setSafeHeight(settings.getSafeHeight());
        setLaserDiameter(settings.getLaserDiameter());
        setMaxSpindleSpeed(settings.getMaxSpindleSpeed());
        setDetectMaxSpindleSpeed(settings.getDetectMaxSpindleSpeed());
        setSpindleDirection(settings.getSpindleDirection());
        setFlatnessPrecision(settings.getFlatnessPrecision());
        setArcFitting(settings.getArcFitting());
        setUseToolChanges(settings.getUseToolChanges());
        setToolNumber(settings.getToolNumber());
        setCurrentToolId(settings.getCurrentToolId());
        ToolDefinition snapshot = settings.getCurrentToolSnapshot();
        setCurrentToolSnapshot(snapshot == null ? null : new ToolDefinition(snapshot));
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

    /**
     * Returns whether runs of straight lines in generated tool paths should be replaced by arcs
     * where the lines follow a circle. The arcs are held to the same precision as
     * {@link #getFlatnessPrecision()}.
     */
    public boolean getArcFitting() {
        return arcFitting;
    }

    public void setArcFitting(boolean arcFitting) {
        this.arcFitting = arcFitting;
        notifyListeners();
    }

    /**
     * Returns whether generated programs should carry a tool change. Off by default — most hobby
     * machines have a single spindle with no changer, and an unexpected {@code M6} either halts
     * the job or is rejected outright.
     */
    public boolean getUseToolChanges() {
        return useToolChanges;
    }

    public void setUseToolChanges(boolean useToolChanges) {
        this.useToolChanges = useToolChanges;
        notifyListeners();
    }

    /**
     * Returns the physical tool slot of the active tool, emitted as the T word of a tool change.
     * {@link ToolDefinition#UNASSIGNED_TOOL_NUMBER} means no tool change should be written.
     */
    public int getToolNumber() {
        return toolNumber;
    }

    public void setToolNumber(int toolNumber) {
        this.toolNumber = Math.max(ToolDefinition.UNASSIGNED_TOOL_NUMBER, toolNumber);
        notifyListeners();
    }

    public boolean hasToolNumber() {
        return toolNumber > ToolDefinition.UNASSIGNED_TOOL_NUMBER;
    }

    public String getCurrentToolId() {
        return currentToolId;
    }

    public void setCurrentToolId(String currentToolId) {
        this.currentToolId = currentToolId;
        notifyListeners();
    }

    public ToolDefinition getCurrentToolSnapshot() {
        return currentToolSnapshot;
    }

    public void setCurrentToolSnapshot(ToolDefinition currentToolSnapshot) {
        this.currentToolSnapshot = currentToolSnapshot;
        notifyListeners();
    }
}
