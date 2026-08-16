/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.ugsd.v1;

import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.io.Serializable;

/**
 * The settings of a design as they are stored in a design file. Every value is boxed and treated
 * as optional so that a file written by a version that did not know about a setting still loads,
 * leaving that setting at its default instead of at zero.
 *
 * @author Joacim Breiler
 */
public class SettingsV1 implements Serializable {
    private Integer feedSpeed;
    private Integer plungeSpeed;
    private Double toolDiameter;
    private EndmillShape toolShape;
    private Double stockThickness;
    private Double safeHeight;
    private UnitUtils.Units preferredUnits;
    private Double toolStepOver;
    private Double vBitAngle;
    private Double depthPerPass;
    private Double laserDiameter;
    private Integer maxSpindleSpeed;
    private Boolean detectMaxSpindleSpeed;
    private String spindleDirection;
    private Double flatnessPrecision;
    private Boolean arcFitting;
    private Boolean useToolChanges;
    private Integer toolNumber;
    private String currentToolId;

    public static SettingsV1 fromInternal(Settings settings) {
        SettingsV1 result = new SettingsV1();
        result.feedSpeed = settings.getFeedSpeed();
        result.plungeSpeed = settings.getPlungeSpeed();
        result.toolDiameter = settings.getToolDiameter();
        result.toolShape = settings.getToolShape();
        result.stockThickness = settings.getStockThickness();
        result.safeHeight = settings.getSafeHeight();
        result.preferredUnits = settings.getPreferredUnits();
        result.toolStepOver = settings.getToolStepOver();
        result.vBitAngle = settings.getVBitAngle();
        result.depthPerPass = settings.getDepthPerPass();
        result.laserDiameter = settings.getLaserDiameter();
        result.maxSpindleSpeed = settings.getMaxSpindleSpeed();
        result.detectMaxSpindleSpeed = settings.getDetectMaxSpindleSpeed();
        result.spindleDirection = settings.getSpindleDirection();
        result.flatnessPrecision = settings.getFlatnessPrecision();
        result.arcFitting = settings.getArcFitting();
        result.useToolChanges = settings.getUseToolChanges();
        result.toolNumber = settings.getToolNumber();
        result.currentToolId = settings.getCurrentToolId();
        return result;
    }

    public Settings toInternal() {
        Settings settings = new Settings();
        if (feedSpeed != null) {
            settings.setFeedSpeed(feedSpeed);
        }
        if (plungeSpeed != null) {
            settings.setPlungeSpeed(plungeSpeed);
        }
        if (toolDiameter != null) {
            settings.setToolDiameter(toolDiameter);
        }
        if (toolShape != null) {
            settings.setToolShape(toolShape);
        }
        if (stockThickness != null) {
            settings.setStockThickness(stockThickness);
        }
        if (safeHeight != null) {
            settings.setSafeHeight(safeHeight);
        }
        if (preferredUnits != null) {
            settings.setPreferredUnits(preferredUnits);
        }
        if (toolStepOver != null) {
            settings.setToolStepOver(toolStepOver);
        }
        if (vBitAngle != null) {
            settings.setVBitAngle(vBitAngle);
        }
        if (depthPerPass != null) {
            settings.setDepthPerPass(depthPerPass);
        }
        if (laserDiameter != null) {
            settings.setLaserDiameter(laserDiameter);
        }
        if (maxSpindleSpeed != null) {
            settings.setMaxSpindleSpeed(maxSpindleSpeed);
        }
        if (detectMaxSpindleSpeed != null) {
            settings.setDetectMaxSpindleSpeed(detectMaxSpindleSpeed);
        }
        if (spindleDirection != null) {
            settings.setSpindleDirection(spindleDirection);
        }
        if (flatnessPrecision != null) {
            settings.setFlatnessPrecision(flatnessPrecision);
        }
        if (arcFitting != null) {
            settings.setArcFitting(arcFitting);
        }
        if (useToolChanges != null) {
            settings.setUseToolChanges(useToolChanges);
        }
        if (toolNumber != null) {
            settings.setToolNumber(toolNumber);
        }
        settings.setCurrentToolId(currentToolId);
        return settings;
    }
}
