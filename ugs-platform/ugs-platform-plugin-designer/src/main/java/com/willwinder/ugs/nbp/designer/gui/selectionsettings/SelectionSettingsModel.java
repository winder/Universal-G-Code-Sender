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

import java.awt.Font;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SelectionSettingsModel implements Serializable {
    private final transient Set<SelectionSettingsModelListener> listeners = ConcurrentHashMap.newKeySet();
    private final Map<EntitySetting, Object> settings = new ConcurrentHashMap<>();

    public void addListener(SelectionSettingsModelListener listener) {
        listeners.add(listener);
    }

    public void put(EntitySetting key, Object object) {
        if (key == EntitySetting.WIDTH) {
            setWidth(parseDouble(object));
        } else if (key == EntitySetting.HEIGHT) {
            setHeight(parseDouble(object));
        } else if (key == EntitySetting.ROTATION) {
            setRotation(parseDouble(object));
        } else if (key == EntitySetting.POSITION_X) {
            setPositionX(parseDouble(object));
        } else if (key == EntitySetting.POSITION_Y) {
            setPositionY(parseDouble(object));
        } else if (key == EntitySetting.CUT_TYPE) {
            setCutType(parseCutType(object));
        } else if (key == EntitySetting.TARGET_DEPTH) {
            setTargetDepth(parseDouble(object));
        } else if (key == EntitySetting.START_DEPTH) {
            setStartDepth(parseDouble(object));
        } else if (key == EntitySetting.TEXT) {
            setText(object.toString());
        } else if (key == EntitySetting.FONT_FAMILY) {
            setFontFamily(object.toString());
        } else if (key == EntitySetting.ANCHOR) {
            setAnchor((Anchor) object);
        } else if (key == EntitySetting.LOCK_RATIO) {
            setLockRatio((Boolean) object);
        }
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
            default -> throw new SelectionSettingsModelException("Unknown setting " + key);
        };
    }

    private CutType parseCutType(Object object) {
        if (object instanceof CutType cutType) {
            return cutType;
        }

        throw new SelectionSettingsModelException("Incorrect type");
    }

    private double parseDouble(Object object) {
        if (object instanceof Double doubleValue) {
            return doubleValue;
        }

        throw new SelectionSettingsModelException("Incorrect type");
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
        setText("");
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
        return  (Boolean) settings.getOrDefault(EntitySetting.LOCK_RATIO, true);
    }

    public void setLockRatio(boolean lockRatio) {
        if (getLockRatio() != lockRatio) {
            settings.put(EntitySetting.LOCK_RATIO, lockRatio);
            notifyListeners(EntitySetting.LOCK_RATIO);
        }
    }
}
