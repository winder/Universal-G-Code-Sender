/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.models;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;

/**
 * Model for cuttable-specific properties (cutting parameters, depths, speeds, etc.)
 * Extends EntitySettingsModel to include transformation properties.
 */
public class CuttableSettingsModel extends EntitySettingsModel {

    private CutType cutType = CutType.NONE;
    private double targetDepth = 0d;
    private double startDepth = 0d;
    private int spindleSpeed = 100;
    private int passes = 1;
    private int feedRate = getDefaultFeedRate();
    private int leadInPercent = 0;
    private int leadOutPercent = 0;
    private boolean includeInExport = true;

    // Getters and setters for cuttable properties
    public CutType getCutType() {
        return cutType;
    }

    public void setCutType(CutType cutType) {
        if (!this.cutType.equals(cutType)) {
            this.cutType = cutType;
            notifyListeners(EntitySetting.CUT_TYPE);
        }
    }

    public double getTargetDepth() {
        return targetDepth;
    }

    public void setTargetDepth(double targetDepth) {
        if (!valuesEquals(this.targetDepth, targetDepth)) {
            this.targetDepth = targetDepth;
            notifyListeners(EntitySetting.TARGET_DEPTH);
        }
    }

    public double getStartDepth() {
        return startDepth;
    }

    public void setStartDepth(double startDepth) {
        if (!valuesEquals(this.startDepth, startDepth)) {
            this.startDepth = startDepth;
            notifyListeners(EntitySetting.START_DEPTH);
        }
    }

    public int getSpindleSpeed() {
        return spindleSpeed;
    }

    public void setSpindleSpeed(Integer speed) {
        if (!valuesEquals(this.spindleSpeed, speed)) {
            if (speed == 0) speed = 100;
            this.spindleSpeed = speed;
            notifyListeners(EntitySetting.SPINDLE_SPEED);
        }
    }

    public int getPasses() {
        return passes;
    }

    public void setPasses(Integer passes) {
        if (!valuesEquals(this.passes, passes)) {
            passes = Math.max(1, passes);
            this.passes = passes;
            notifyListeners(EntitySetting.PASSES);
        }
    }

    public int getFeedRate() {
        return feedRate;
    }

    public void setFeedRate(Integer feedRate) {
        if (!valuesEquals(this.feedRate, feedRate)) {
            if (feedRate == 0) feedRate = getDefaultFeedRate();
            this.feedRate = feedRate;
            notifyListeners(EntitySetting.FEED_RATE);
        }
    }

    public int getLeadInPercent() {
        return leadInPercent;
    }

    public void setLeadInPercent(int leadInPercent) {
        if (!valuesEquals(this.leadInPercent, leadInPercent)) {
            this.leadInPercent = leadInPercent;
            notifyListeners(EntitySetting.LEAD_IN_PERCENT);
        }
    }

    public int getLeadOutPercent() {
        return leadOutPercent;
    }

    public void setLeadOutPercent(int leadOutPercent) {
        if (!valuesEquals(this.leadOutPercent, leadOutPercent)) {
            this.leadOutPercent = leadOutPercent;
            notifyListeners(EntitySetting.LEAD_OUT_PERCENT);
        }
    }

    public boolean getIncludeInExport() {
        return includeInExport;
    }

    public void setIncludeInExport(boolean includeInExport) {
        if (this.includeInExport != includeInExport) {
            this.includeInExport = includeInExport;
            notifyListeners(EntitySetting.INCLUDE_IN_EXPORT);
        }
    }

    private int getDefaultFeedRate() {
        Controller controller = ControllerFactory.getController();
        if (controller != null && controller.getSettings() != null) {
            return controller.getSettings().getFeedSpeed();
        }
        return 50;
    }

    @Override
    public void reset() {
        super.reset(); // Reset entity properties
        setCutType(CutType.NONE);
        setStartDepth(0);
        setTargetDepth(0);
        setSpindleSpeed(100);
        setFeedRate(getDefaultFeedRate());
        setLeadInPercent(0);
        setLeadOutPercent(0);
        setPasses(1);
        setIncludeInExport(true);
    }

    @Override
    public void updateFromGroup(Group selectionGroup) {
        super.updateFromGroup(selectionGroup); // Update entity properties

        if (selectionGroup.getSettings().contains(EntitySetting.START_DEPTH)) {
            setStartDepth(selectionGroup.getStartDepth());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.TARGET_DEPTH)) {
            setTargetDepth(selectionGroup.getTargetDepth());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.CUT_TYPE)) {
            setCutType(selectionGroup.getCutType());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.SPINDLE_SPEED)) {
            setSpindleSpeed(selectionGroup.getSpindleSpeed());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.PASSES)) {
            setPasses(selectionGroup.getPasses());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.FEED_RATE)) {
            setFeedRate(selectionGroup.getFeedRate());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.LEAD_IN_PERCENT)) {
            setLeadInPercent(selectionGroup.getLeadInPercent());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.LEAD_OUT_PERCENT)) {
            setLeadOutPercent(selectionGroup.getLeadOutPercent());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.INCLUDE_IN_EXPORT)) {
            setIncludeInExport(selectionGroup.getIncludeInExport());
        }
    }

    public Object getValueFor(EntitySetting setting) {
        return switch (setting) {
            case CUT_TYPE -> getCutType();
            case START_DEPTH -> getStartDepth();
            case TARGET_DEPTH -> getTargetDepth();
            case SPINDLE_SPEED -> getSpindleSpeed();
            case PASSES -> getPasses();
            case FEED_RATE -> getFeedRate();
            case LEAD_IN_PERCENT -> getLeadInPercent();
            case LEAD_OUT_PERCENT -> getLeadOutPercent();
            case INCLUDE_IN_EXPORT -> getIncludeInExport();
            default -> super.getValueFor(setting);
        };
    }

    public void updateValueFor(EntitySetting setting, Object newValue) {
        switch (setting) {
            case CUT_TYPE -> setCutType((CutType) newValue);
            case START_DEPTH -> setStartDepth((Double) newValue);
            case TARGET_DEPTH -> setTargetDepth((Double) newValue);
            case SPINDLE_SPEED -> setSpindleSpeed((Integer) newValue);
            case PASSES -> setPasses((Integer) newValue);
            case FEED_RATE -> setFeedRate((Integer) newValue);
            case LEAD_IN_PERCENT -> setLeadInPercent((Integer) newValue);
            case LEAD_OUT_PERCENT -> setLeadOutPercent((Integer) newValue);
            case INCLUDE_IN_EXPORT -> setIncludeInExport((Boolean) newValue);
            default -> super.updateValueFor(setting, newValue);
        }
    }
}
