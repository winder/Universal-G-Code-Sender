/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.model.toollibrary;

import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.Objects;
import java.util.UUID;

public class ToolDefinition {
    private String id;
    private String name;
    private EndmillShape shape;
    private Double vBitAngleDegrees;
    private double diameter;
    private UnitUtils.Units diameterUnit;
    private int feedSpeed;
    private int plungeSpeed;
    private double depthPerPass;
    private double stepOverPercent;
    private int maxSpindleSpeed;
    private String spindleDirection;
    private boolean builtIn;
    private boolean isCustomSentinel;

    public ToolDefinition() {
        this.id = UUID.randomUUID().toString();
        this.shape = EndmillShape.UPCUT;
        this.diameterUnit = UnitUtils.Units.MM;
        this.spindleDirection = "M3";
    }

    public ToolDefinition(ToolDefinition other) {
        this.id = other.id;
        this.name = other.name;
        this.shape = other.shape;
        this.vBitAngleDegrees = other.vBitAngleDegrees;
        this.diameter = other.diameter;
        this.diameterUnit = other.diameterUnit;
        this.feedSpeed = other.feedSpeed;
        this.plungeSpeed = other.plungeSpeed;
        this.depthPerPass = other.depthPerPass;
        this.stepOverPercent = other.stepOverPercent;
        this.maxSpindleSpeed = other.maxSpindleSpeed;
        this.spindleDirection = other.spindleDirection;
        this.builtIn = other.builtIn;
        this.isCustomSentinel = other.isCustomSentinel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EndmillShape getShape() {
        return shape == null ? EndmillShape.UPCUT : shape;
    }

    public void setShape(EndmillShape shape) {
        this.shape = shape;
    }

    public Double getVBitAngleDegrees() {
        return vBitAngleDegrees;
    }

    public void setVBitAngleDegrees(Double vBitAngleDegrees) {
        this.vBitAngleDegrees = vBitAngleDegrees;
    }

    public double getDiameter() {
        return diameter;
    }

    public void setDiameter(double diameter) {
        this.diameter = diameter;
    }

    public UnitUtils.Units getDiameterUnit() {
        return diameterUnit == null ? UnitUtils.Units.MM : diameterUnit;
    }

    public void setDiameterUnit(UnitUtils.Units diameterUnit) {
        this.diameterUnit = diameterUnit;
    }

    public double getDiameterInMm() {
        return diameter * UnitUtils.scaleUnits(getDiameterUnit(), UnitUtils.Units.MM);
    }

    public int getFeedSpeed() {
        return feedSpeed;
    }

    public void setFeedSpeed(int feedSpeed) {
        this.feedSpeed = feedSpeed;
    }

    public int getPlungeSpeed() {
        return plungeSpeed;
    }

    public void setPlungeSpeed(int plungeSpeed) {
        this.plungeSpeed = plungeSpeed;
    }

    public double getDepthPerPass() {
        return depthPerPass;
    }

    public void setDepthPerPass(double depthPerPass) {
        this.depthPerPass = depthPerPass;
    }

    public double getStepOverPercent() {
        return stepOverPercent;
    }

    public void setStepOverPercent(double stepOverPercent) {
        this.stepOverPercent = stepOverPercent;
    }

    public int getMaxSpindleSpeed() {
        return maxSpindleSpeed;
    }

    public void setMaxSpindleSpeed(int maxSpindleSpeed) {
        this.maxSpindleSpeed = maxSpindleSpeed;
    }

    public String getSpindleDirection() {
        return spindleDirection == null ? "M3" : spindleDirection;
    }

    public void setSpindleDirection(String spindleDirection) {
        this.spindleDirection = spindleDirection;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public boolean isCustomSentinel() {
        return isCustomSentinel;
    }

    public void setCustomSentinel(boolean customSentinel) {
        isCustomSentinel = customSentinel;
    }

    /**
     * Copies this tool's library-managed fields onto a {@link Settings} instance while leaving
     * project/session-level fields (safe height, stock thickness, laser diameter, flatness,
     * preferredUnits, detectMaxSpindleSpeed) untouched. Diameter is converted to mm; feed, plunge
     * and depth are already stored in mm / mm-per-minute.
     */
    public Settings applyToSettings(Settings base) {
        Settings result = new Settings(base);
        result.setToolDiameter(getDiameterInMm());
        result.setFeedSpeed(feedSpeed);
        result.setPlungeSpeed(plungeSpeed);
        result.setDepthPerPass(depthPerPass);
        result.setToolStepOver(stepOverPercent);
        result.setMaxSpindleSpeed(maxSpindleSpeed);
        result.setSpindleDirection(getSpindleDirection());
        result.setCurrentToolId(id);
        result.setCurrentToolSnapshot(new ToolDefinition(this));
        return result;
    }

    /**
     * Equality on user-visible fields — excludes {@code builtIn} and {@code isCustomSentinel}
     * so a project-embedded snapshot matches a library entry that was re-tagged.
     */
    public boolean matchesValues(ToolDefinition other) {
        if (other == null) return false;
        return Objects.equals(name, other.name)
                && shape == other.shape
                && Objects.equals(vBitAngleDegrees, other.vBitAngleDegrees)
                && Double.compare(diameter, other.diameter) == 0
                && diameterUnit == other.diameterUnit
                && feedSpeed == other.feedSpeed
                && plungeSpeed == other.plungeSpeed
                && Double.compare(depthPerPass, other.depthPerPass) == 0
                && Double.compare(stepOverPercent, other.stepOverPercent) == 0
                && maxSpindleSpeed == other.maxSpindleSpeed
                && Objects.equals(getSpindleDirection(), other.getSpindleDirection());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ToolDefinition that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name == null ? id : name;
    }
}
