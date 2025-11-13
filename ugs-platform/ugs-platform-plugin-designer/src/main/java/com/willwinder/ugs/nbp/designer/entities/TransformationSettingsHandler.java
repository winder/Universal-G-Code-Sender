/*
    Copyright 2024 Albert Giro

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
package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;

/**
 * Handler for transformation settings that can be applied to entities.
 * This class encapsulates all transformation-related properties and provides
 * methods to apply them to entities. Supports bidirectional property binding
 * through PropertyChangeSupport for integration with UI components.
 *
 * @author giro-dev
 */
public class TransformationSettingsHandler {
    // Property names for change events
    public static final String PROP_POSITION_X = "positionX";
    public static final String PROP_POSITION_Y = "positionY";
    public static final String PROP_WIDTH = "width";
    public static final String PROP_HEIGHT = "height";
    public static final String PROP_ROTATION = "rotation";
    public static final String PROP_ANCHOR = "anchor";
    public static final String PROP_LOCK_RATIO = "lockRatio";
    public static final String PROP_POSITION = "position";
    public static final String PROP_SIZE = "size";

    private final PropertyChangeSupport propertyChangeSupport;

    private Point2D position;
    private Size size;
    private double rotation;
    private Anchor anchor;
    private boolean lockRatio;

    public TransformationSettingsHandler() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
        this.position = new Point2D.Double(0, 0);
        this.size = new Size(0, 0);
        this.rotation = 0;
        this.anchor = Anchor.BOTTOM_LEFT;
        this.lockRatio = false;
    }

    /**
     * Creates a transformation settings handler from an existing entity
     */
    public static TransformationSettingsHandler fromEntity(Entity entity) {
        TransformationSettingsHandler handler = new TransformationSettingsHandler();
        // Use silent setters to avoid firing events during initialization
        handler.position = entity.getPosition();
        handler.size = entity.getSize();
        handler.rotation = entity.getRotation();
        handler.anchor = Anchor.BOTTOM_LEFT; // Default anchor
        handler.lockRatio = false; // Default lock ratio
        return handler;
    }

    /**
     * Updates all properties from an entity without firing change events.
     * Useful for synchronizing the model with entity state.
     */
    public void updateFromEntity(Entity entity) {
        Point2D oldPosition = this.position;
        Size oldSize = this.size;
        double oldRotation = this.rotation;

        this.position = entity.getPosition();
        this.size = entity.getSize();
        this.rotation = entity.getRotation();

        // Fire change events for any differences
        if (!Objects.equals(oldPosition, this.position)) {
            propertyChangeSupport.firePropertyChange(PROP_POSITION, oldPosition, this.position);
            propertyChangeSupport.firePropertyChange(PROP_POSITION_X, oldPosition.getX(), this.position.getX());
            propertyChangeSupport.firePropertyChange(PROP_POSITION_Y, oldPosition.getY(), this.position.getY());
        }
        if (!Objects.equals(oldSize, this.size)) {
            propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, this.size);
            propertyChangeSupport.firePropertyChange(PROP_WIDTH, oldSize.getWidth(), this.size.getWidth());
            propertyChangeSupport.firePropertyChange(PROP_HEIGHT, oldSize.getHeight(), this.size.getHeight());
        }
        if (Double.compare(oldRotation, this.rotation) != 0) {
            propertyChangeSupport.firePropertyChange(PROP_ROTATION, oldRotation, this.rotation);
        }
    }

    /**
     * Applies the transformation settings to an entity
     */
    public void applyToEntity(Entity entity) {
        if (entity == null) {
            return;
        }

        // Apply transformations in the correct order
        entity.setPosition(anchor, position);
        entity.setSize(anchor, size);
        entity.setRotation(rotation);
    }

    /**
     * Creates a copy of this transformation settings handler
     */
    public TransformationSettingsHandler copy() {
        TransformationSettingsHandler copy = new TransformationSettingsHandler();
        copy.position = new Point2D.Double(position.getX(), position.getY());
        copy.size = new Size(size.getWidth(), size.getHeight());
        copy.rotation = rotation;
        copy.anchor = anchor;
        copy.lockRatio = lockRatio;
        return copy;
    }

    // Property change support methods
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    // Getters and setters with property change support
    public Point2D getPosition() {
        return position;
    }

    public void setPosition(Point2D position) {
        Point2D oldPosition = this.position;
        this.position = position;
        propertyChangeSupport.firePropertyChange(PROP_POSITION, oldPosition, position);
        if (oldPosition != null) {
            propertyChangeSupport.firePropertyChange(PROP_POSITION_X, oldPosition.getX(), position.getX());
            propertyChangeSupport.firePropertyChange(PROP_POSITION_Y, oldPosition.getY(), position.getY());
        }
    }

    public double getPositionX() {
        return position.getX();
    }

    public void setPositionX(double x) {
        double oldX = position.getX();
        Point2D oldPosition = this.position;
        this.position = new Point2D.Double(x, position.getY());
        propertyChangeSupport.firePropertyChange(PROP_POSITION_X, oldX, x);
        propertyChangeSupport.firePropertyChange(PROP_POSITION, oldPosition, this.position);
    }

    public double getPositionY() {
        return position.getY();
    }

    public void setPositionY(double y) {
        double oldY = position.getY();
        Point2D oldPosition = this.position;
        this.position = new Point2D.Double(position.getX(), y);
        propertyChangeSupport.firePropertyChange(PROP_POSITION_Y, oldY, y);
        propertyChangeSupport.firePropertyChange(PROP_POSITION, oldPosition, this.position);
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        Size oldSize = this.size;
        this.size = size;
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, size);
        if (oldSize != null) {
            propertyChangeSupport.firePropertyChange(PROP_WIDTH, oldSize.getWidth(), size.getWidth());
            propertyChangeSupport.firePropertyChange(PROP_HEIGHT, oldSize.getHeight(), size.getHeight());
        }
    }

    public double getWidth() {
        return size.getWidth();
    }

    public void setWidth(double width) {
        double oldWidth = size.getWidth();
        Size oldSize = this.size;

        if (lockRatio && size.getWidth() != 0) {
            double ratio = width / size.getWidth();
            this.size = new Size(width, size.getHeight() * ratio);
            propertyChangeSupport.firePropertyChange(PROP_HEIGHT, oldSize.getHeight(), this.size.getHeight());
        } else {
            this.size = new Size(width, size.getHeight());
        }

        propertyChangeSupport.firePropertyChange(PROP_WIDTH, oldWidth, width);
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, this.size);
    }

    public double getHeight() {
        return size.getHeight();
    }

    public void setHeight(double height) {
        double oldHeight = size.getHeight();
        Size oldSize = this.size;

        if (lockRatio && size.getHeight() != 0) {
            double ratio = height / size.getHeight();
            this.size = new Size(size.getWidth() * ratio, height);
            propertyChangeSupport.firePropertyChange(PROP_WIDTH, oldSize.getWidth(), this.size.getWidth());
        } else {
            this.size = new Size(size.getWidth(), height);
        }

        propertyChangeSupport.firePropertyChange(PROP_HEIGHT, oldHeight, height);
        propertyChangeSupport.firePropertyChange(PROP_SIZE, oldSize, this.size);
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        double oldRotation = this.rotation;
        this.rotation = rotation;
        propertyChangeSupport.firePropertyChange(PROP_ROTATION, oldRotation, rotation);
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        Anchor oldAnchor = this.anchor;
        this.anchor = anchor;
        propertyChangeSupport.firePropertyChange(PROP_ANCHOR, oldAnchor, anchor);
    }

    public boolean isLockRatio() {
        return lockRatio;
    }

    public void setLockRatio(boolean lockRatio) {
        boolean oldLockRatio = this.lockRatio;
        this.lockRatio = lockRatio;
        propertyChangeSupport.firePropertyChange(PROP_LOCK_RATIO, oldLockRatio, lockRatio);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformationSettingsHandler that = (TransformationSettingsHandler) o;
        return Double.compare(that.rotation, rotation) == 0 &&
                lockRatio == that.lockRatio &&
                Objects.equals(position, that.position) &&
                Objects.equals(size, that.size) &&
                anchor == that.anchor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, size, rotation, anchor, lockRatio);
    }

    @Override
    public String toString() {
        return "TransformationSettingsHandler{" +
                "position=" + position +
                ", size=" + size +
                ", rotation=" + rotation +
                ", anchor=" + anchor +
                ", lockRatio=" + lockRatio +
                '}';
    }
}
