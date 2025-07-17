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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;

import java.awt.Font;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionSettingsModel implements Serializable {
    private final transient Set<SelectionSettingsModelListener> listeners = ConcurrentHashMap.newKeySet();
    private final Map<EntitySetting, Object> settings = new ConcurrentHashMap<>();

    public void addListener(SelectionSettingsModelListener listener) {
        listeners.add(listener);
    }

    public Object get(EntitySetting key) {
        return switch (key) {
            case WIDTH -> getWidth();
            case HEIGHT -> getHeight();
            case ROTATION -> getRotation();
            case POSITION_X -> getPositionX();
            case POSITION_Y -> getPositionY();
            case CUT_TYPE -> getCutType();
            case TARGET_DEPTH -> getTargetDepth();
            case START_DEPTH -> getStartDepth();
            case TEXT -> getText();
            case FONT_FAMILY -> getFontFamily();
            case ANCHOR -> getAnchor();
            case LOCK_RATIO -> getLockRatio();
            case SPINDLE_SPEED -> getSpindleSpeed();
            case PASSES -> getPasses();
            case FEED_RATE -> getFeedRate();
            case LEAD_IN_PERCENT -> getLeadInPercent();
            case LEAD_OUT_PERCENT -> getLeadOutPercent();
            case INCLUDE_IN_EXPORT -> getIncludeInExport();
            default -> throw new SelectionSettingsModelException("Unknown setting " + key);
        };
    }

    public double getWidth() {
        return (Double) settings.getOrDefault(EntitySetting.WIDTH, 0d);
    }

    public void setWidth(double width) {
        if (!valuesEquals(getWidth(), width)) {
            settings.put(EntitySetting.WIDTH, width);
            notifyListeners(EntitySetting.WIDTH);
        }
    }

    public double getHeight() {
        return (Double) settings.getOrDefault(EntitySetting.HEIGHT, 0d);
    }

    public void setHeight(double height) {
        if (!valuesEquals(getHeight(), height)) {
            settings.put(EntitySetting.HEIGHT, height);
            notifyListeners(EntitySetting.HEIGHT);
        }
    }

    public double getPositionX() {
        return (Double) settings.getOrDefault(EntitySetting.POSITION_X, 0d);
    }

    public void setPositionX(double positionX) {
        if (!valuesEquals(getPositionX(), positionX)) {
            settings.put(EntitySetting.POSITION_X, positionX);
            notifyListeners(EntitySetting.POSITION_X);
        }
    }

    public double getPositionY() {
        return (Double) settings.getOrDefault(EntitySetting.POSITION_Y, 0d);
    }

    public void setPositionY(double positionY) {
        if (!valuesEquals(getPositionY(), positionY)) {
            settings.put(EntitySetting.POSITION_Y, positionY);
            notifyListeners(EntitySetting.POSITION_Y);
        }
    }

    private boolean valuesEquals(double value1, double value2) {
        return Utils.roundToDecimals(value1, Utils.MAX_DECIMALS) == Utils.roundToDecimals(value2, Utils.MAX_DECIMALS);
    }

    public double getRotation() {
        return (Double) settings.getOrDefault(EntitySetting.ROTATION, 0d);
    }

    public void setRotation(double rotation) {
        if (!valuesEquals(getRotation(), rotation)) {
            settings.put(EntitySetting.ROTATION, rotation);
            notifyListeners(EntitySetting.ROTATION);
        }
    }

    public int getLeadInPercent() {
        return (Integer) settings.getOrDefault(EntitySetting.LEAD_IN_PERCENT, 0);
    }

    public void setLeadInPercent(int leadInPercent) {
        if (!valuesEquals(getLeadInPercent(), leadInPercent)) {
            settings.put(EntitySetting.LEAD_IN_PERCENT, leadInPercent);
            notifyListeners(EntitySetting.LEAD_IN_PERCENT);
        }
    }

    public int getLeadOutPercent() {
        return (Integer) settings.getOrDefault(EntitySetting.LEAD_OUT_PERCENT, 0);
    }

    public void setLeadOutPercent(int leadOut) {
        if (!valuesEquals(getLeadOutPercent(), leadOut)) {
            settings.put(EntitySetting.LEAD_OUT_PERCENT, leadOut);
            notifyListeners(EntitySetting.LEAD_OUT_PERCENT);
        }
    }
    public boolean getIncludeInExport() {
        return (Boolean) settings.getOrDefault(EntitySetting.INCLUDE_IN_EXPORT, true);
    }

    public void setIncludeInExport(boolean aValue) {
        if (getIncludeInExport() != aValue) {
            settings.put(EntitySetting.INCLUDE_IN_EXPORT, aValue);
            notifyListeners(EntitySetting.INCLUDE_IN_EXPORT);
        }
    }
    private void notifyListeners(EntitySetting setting) {
        listeners.forEach(l -> l.onModelUpdate(setting));
    }

    public void reset() {
        setWidth(0);
        setHeight(0);
        setPositionX(0);
        setPositionY(0);
        setRotation(0);
        setCutType(CutType.NONE);
        setStartDepth(0);
        setTargetDepth(0);
        setSpindleSpeed(0);
        setFeedRate(0);
        setLeadInPercent(0);
        setText("");
        setIncludeInExport(true);        
        setFontFamily(Font.SANS_SERIF);
    }

    public Anchor getAnchor() {
        return (Anchor) settings.getOrDefault(EntitySetting.ANCHOR, Anchor.CENTER);
    }

    public void setAnchor(Anchor anchor) {
        if (!getAnchor().equals(anchor)) {
            settings.put(EntitySetting.ANCHOR, anchor);
            notifyListeners(EntitySetting.ANCHOR);
        }
    }

    public CutType getCutType() {
        return (CutType) settings.getOrDefault(EntitySetting.CUT_TYPE, CutType.NONE);
    }

    public void setCutType(CutType cutType) {
        if (!getCutType().equals(cutType)) {
            settings.put(EntitySetting.CUT_TYPE, cutType);
            notifyListeners(EntitySetting.CUT_TYPE);
        }
    }

    public double getTargetDepth() {
        return (Double) settings.getOrDefault(EntitySetting.TARGET_DEPTH, 0d);
    }

    public void setTargetDepth(double targetDepth) {
        if (!valuesEquals(getTargetDepth(), targetDepth)) {
            settings.put(EntitySetting.TARGET_DEPTH, targetDepth);
            notifyListeners(EntitySetting.TARGET_DEPTH);
        }
    }

    public double getStartDepth() {
        return (Double) settings.getOrDefault(EntitySetting.START_DEPTH, 0d);
    }

    public void setStartDepth(double startDepth) {
        if (!valuesEquals(getStartDepth(), startDepth)) {
            settings.put(EntitySetting.START_DEPTH, startDepth);
            notifyListeners(EntitySetting.START_DEPTH);
        }
    }

    public int getSpindleSpeed() {
        return (Integer) settings.getOrDefault(EntitySetting.SPINDLE_SPEED, 100);
    }

    public void setSpindleSpeed(Integer speed) {
        if (!valuesEquals(getSpindleSpeed(), speed)) {
            if (speed == 0) {
                speed = 100;
            }
            settings.put(EntitySetting.SPINDLE_SPEED, speed);
            notifyListeners(EntitySetting.SPINDLE_SPEED);
        }
    }

    public int getPasses() {
        return (Integer) settings.getOrDefault(EntitySetting.PASSES, 1);
    }

    public void setPasses(Integer passes) {
        if (!valuesEquals(getPasses(), passes)) {
            passes = Math.max(1, passes);
            settings.put(EntitySetting.PASSES, passes);
            notifyListeners(EntitySetting.PASSES);
        }
    }

    public int getFeedRate() {
        return (Integer) settings.getOrDefault(EntitySetting.FEED_RATE, getDefaultFeedRate());
    }

    public void setFeedRate(Integer feedRate) {
        if (!valuesEquals(getFeedRate(), feedRate)) {
            if (feedRate == 0) {
                feedRate = getDefaultFeedRate();
            }
            settings.put(EntitySetting.FEED_RATE, feedRate);
            notifyListeners(EntitySetting.FEED_RATE);
        }
    }

    private int getDefaultFeedRate() {
        Controller controller = ControllerFactory.getController();
        if (controller != null && controller.getSettings() != null) {
            return controller.getSettings().getFeedSpeed();
        }
        return 50;
    }

    public String getFontFamily() {
        return (String) settings.getOrDefault(EntitySetting.FONT_FAMILY, Font.SANS_SERIF);
    }

    public void setFontFamily(String fontFamily) {
        if (!getFontFamily().equals(fontFamily)) {
            settings.put(EntitySetting.FONT_FAMILY, fontFamily);
            notifyListeners(EntitySetting.FONT_FAMILY);
        }
    }

    public String getText() {
        return (String) settings.getOrDefault(EntitySetting.TEXT, "");
    }

    public void setText(String text) {
        if (!getText().equals(text)) {
            settings.put(EntitySetting.TEXT, text);
            notifyListeners(EntitySetting.TEXT);
        }
    }

    public void setSize(double width, double height) {
        boolean updatedWidth = false;
        boolean updatedHeight = false;
        if (!valuesEquals(getWidth(), width)) {
            settings.put(EntitySetting.WIDTH, width);
            updatedWidth = true;
        }

        if (!valuesEquals(getHeight(), height)) {
            settings.put(EntitySetting.HEIGHT, height);
            updatedHeight = true;
        }

        if (updatedWidth) {
            notifyListeners(EntitySetting.WIDTH);
        }

        if (updatedHeight) {
            notifyListeners(EntitySetting.HEIGHT);
        }
    }

    public boolean getLockRatio() {
        return (Boolean) settings.getOrDefault(EntitySetting.LOCK_RATIO, true);
    }

    public void setLockRatio(boolean lockRatio) {
        if (getLockRatio() != lockRatio) {
            settings.put(EntitySetting.LOCK_RATIO, lockRatio);
            notifyListeners(EntitySetting.LOCK_RATIO);
        }
    }

    public void updateFromEntity(Group selectionGroup) {
        List<EntitySetting> settings = selectionGroup.getSettings();
        if (settings.contains(EntitySetting.TEXT)) {
            Text textEntity = (Text) selectionGroup.getChildren().get(0);
            setText(textEntity.getText());
        }

        if (settings.contains(EntitySetting.FONT_FAMILY)) {
            Text textEntity = (Text) selectionGroup.getChildren().get(0);
            setFontFamily(textEntity.getFontFamily());
        }

        if (settings.contains(EntitySetting.POSITION_X)) {
            setPositionX(selectionGroup.getPosition(getAnchor()).getX());
        }

        if (settings.contains(EntitySetting.POSITION_X)) {
            setPositionY(selectionGroup.getPosition(getAnchor()).getY());
        }

        if (settings.contains(EntitySetting.WIDTH)) {
            setWidth(selectionGroup.getSize().getWidth());
        }

        if (settings.contains(EntitySetting.HEIGHT)) {
            setHeight(selectionGroup.getSize().getHeight());
        }

        if (settings.contains(EntitySetting.ROTATION)) {
            setRotation(selectionGroup.getRotation());
        }

        if (settings.contains(EntitySetting.START_DEPTH)) {
            setStartDepth(selectionGroup.getStartDepth());
        }
        if (settings.contains(EntitySetting.TARGET_DEPTH)) {
            setTargetDepth(selectionGroup.getTargetDepth());
        }

        if (settings.contains(EntitySetting.CUT_TYPE)) {
            setCutType(selectionGroup.getCutType());
        }

        if (settings.contains(EntitySetting.SPINDLE_SPEED)) {
            setSpindleSpeed(selectionGroup.getSpindleSpeed());
        }

        if (settings.contains(EntitySetting.PASSES)) {
            setPasses(selectionGroup.getPasses());
        }

        if (settings.contains(EntitySetting.FEED_RATE)) {
            setFeedRate(selectionGroup.getFeedRate());
        }

        if (settings.contains(EntitySetting.LEAD_IN_PERCENT)) {
            setLeadInPercent(selectionGroup.getLeadInPercent());
        }

        if (settings.contains(EntitySetting.LEAD_OUT_PERCENT)) {
            setLeadOutPercent(selectionGroup.getLeadOutPercent());
        }
        if (settings.contains(EntitySetting.INCLUDE_IN_EXPORT)) {
            setIncludeInExport(selectionGroup.getIncludeInExport());
        }
    }
}
