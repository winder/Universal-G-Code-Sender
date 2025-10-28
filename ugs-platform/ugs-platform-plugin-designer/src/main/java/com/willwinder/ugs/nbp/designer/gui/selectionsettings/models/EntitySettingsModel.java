/*
    Copyright 2024 Albert Giro Quer

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

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base model for entity transformation properties (position, size, rotation, anchor)
 *
 * @author giro-dev
 */
public class EntitySettingsModel implements Serializable {
    private final transient Set<EntitySettingsModelListener> listeners = ConcurrentHashMap.newKeySet();

    private double width = 0d;
    private double height = 0d;
    private double positionX = 0d;
    private double positionY = 0d;
    private double rotation = 0d;
    private boolean lockRatio = false;
    private Anchor anchor = Anchor.CENTER;

    public void addListener(EntitySettingsModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(EntitySettingsModelListener listener) {
        listeners.remove(listener);
    }

    protected void notifyListeners(EntitySetting setting) {
        listeners.forEach(l -> l.onModelUpdate(setting));
    }

    protected boolean valuesEquals(double value1, double value2) {
        return Utils.roundToDecimals(value1, Utils.MAX_DECIMALS) == Utils.roundToDecimals(value2, Utils.MAX_DECIMALS);
    }

    // Getters and setters for entity properties
    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (!valuesEquals(this.width, width)) {
            this.width = width;
            notifyListeners(EntitySetting.WIDTH);
        }
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (!valuesEquals(this.height, height)) {
            this.height = height;
            notifyListeners(EntitySetting.HEIGHT);
        }
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        if (!valuesEquals(this.positionX, positionX)) {
            this.positionX = positionX;
            notifyListeners(EntitySetting.POSITION_X);
        }
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        if (!valuesEquals(this.positionY, positionY)) {
            this.positionY = positionY;
            notifyListeners(EntitySetting.POSITION_Y);
        }
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        if (!valuesEquals(this.rotation, rotation)) {
            this.rotation = rotation;
            notifyListeners(EntitySetting.ROTATION);
        }
    }

    public boolean getLockRatio() {
        return lockRatio;
    }

    public void setLockRatio(boolean lockRatio) {
        this.lockRatio = lockRatio;
        notifyListeners(EntitySetting.LOCK_RATIO);

    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        if (!this.anchor.equals(anchor)) {
            this.anchor = anchor;
            notifyListeners(EntitySetting.ANCHOR);
        }
    }

    public void setSize(double width, double height) {
        boolean updatedWidth = false;
        boolean updatedHeight = false;

        if (!valuesEquals(this.width, width)) {
            this.width = width;
            updatedWidth = true;
        }

        if (!valuesEquals(this.height, height)) {
            this.height = height;
            updatedHeight = true;
        }

        if (updatedWidth) notifyListeners(EntitySetting.WIDTH);
        if (updatedHeight) notifyListeners(EntitySetting.HEIGHT);
    }

    public void reset() {
        setWidth(0);
        setHeight(0);
        setPositionX(0);
        setPositionY(0);
        setRotation(0);
        setLockRatio(true);
        setAnchor(Anchor.CENTER);
    }

    public void updateFromGroup(Group selectionGroup) {
        if (selectionGroup.getSettings().contains(EntitySetting.POSITION_X)) {
            setPositionX(selectionGroup.getPosition(getAnchor()).getX());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.POSITION_Y)) {
            setPositionY(selectionGroup.getPosition(getAnchor()).getY());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.WIDTH)) {
            setWidth(selectionGroup.getSize().getWidth());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.HEIGHT)) {
            setHeight(selectionGroup.getSize().getHeight());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.ROTATION)) {
            setRotation(selectionGroup.getRotation());
        }
        if (selectionGroup.getSettings().contains(EntitySetting.ANCHOR)) {
            setAnchor(selectionGroup.getAnchor());
        }
        if (selectionGroup.getSettings().contains(EntitySetting.LOCK_RATIO)) {
            setLockRatio(selectionGroup.isLockRatio());
        }
    }

    public Object getValueFor(EntitySetting setting) {
        return switch (setting) {
            case POSITION_X -> getPositionX();
            case POSITION_Y -> getPositionY();
            case WIDTH -> getWidth();
            case HEIGHT -> getHeight();
            case ROTATION -> getRotation();
            case ANCHOR -> getAnchor();
            case LOCK_RATIO -> getLockRatio();
            default ->
                    throw new IllegalArgumentException("Unexpected value: " + setting + " (valid settings are: " + EntitySetting.TRANSFORMATION_SETTINGS + ")");
        };
    }

    public void updateValueFor(EntitySetting setting, Object newValue) {
        switch (setting) {
            case WIDTH -> setWidth((Double) newValue);
            case HEIGHT -> setHeight((Double) newValue);
            case POSITION_X -> setPositionX((Double) newValue);
            case POSITION_Y -> setPositionY((Double) newValue);
            case ROTATION -> setRotation((Double) newValue);
            case ANCHOR -> setAnchor((com.willwinder.ugs.nbp.designer.entities.Anchor) newValue);
            case LOCK_RATIO -> setLockRatio((Boolean) newValue);
            default ->
                    throw new IllegalArgumentException("Unexpected value: " + setting + " (valid settings are: " + EntitySetting.TRANSFORMATION_SETTINGS + ")");
        }
    }
}
