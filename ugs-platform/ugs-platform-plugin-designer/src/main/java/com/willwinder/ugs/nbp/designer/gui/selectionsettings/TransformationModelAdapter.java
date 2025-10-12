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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.TransformationSettingsHandler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * Adapter that bridges the TransformationSettingsHandler with the SelectionSettingsModel
 * to provide bidirectional property binding. This allows the UI components to work
 * with the transformation model while maintaining compatibility with the existing
 * selection settings system.
 *
 * @author Joacim Breiler
 */
public class TransformationModelAdapter implements PropertyChangeListener {
    private final SelectionSettingsModel selectionModel;
    private final TransformationSettingsHandler transformationModel;
    private boolean updating = false; // Prevent circular updates

    public TransformationModelAdapter(SelectionSettingsModel selectionModel, List<Entity> entities) {
        this.selectionModel = selectionModel;
        
        // Create transformation model from the first entity (or empty if no entities)
        if (!entities.isEmpty()) {
            this.transformationModel = TransformationSettingsHandler.fromEntity(entities.get(0));
        } else {
            this.transformationModel = new TransformationSettingsHandler();
        }
        
        // Set up bidirectional binding
        setupBindings();
        
        // DO NOT sync here - this was causing the circular dependency!
        // The SelectionSettingsModel should already have the correct values
        // from the entity that was just selected.
    }

    /**
     * Sets up bidirectional property bindings between the models
     */
    private void setupBindings() {
        // Listen to transformation model changes and update selection model
        transformationModel.addPropertyChangeListener(this);
        
        // Listen to selection model changes and update transformation model
        selectionModel.addListener(this::onSelectionModelChange);
    }

    /**
     * Updates the transformation model from a list of entities
     */
    public void updateFromEntities(List<Entity> entities) {
        if (updating || entities.isEmpty()) {
            return;
        }

        updating = true;
        try {
            // Update the transformation model silently (without firing events)
            Entity firstEntity = entities.get(0);
            transformationModel.updateFromEntity(firstEntity);
        } finally {
            updating = false;
        }
    }

    /**
     * Applies the current transformation settings to a list of entities
     */
    public void applyToEntities(List<Entity> entities) {
        if (updating) return;
        
        for (Entity entity : entities) {
            transformationModel.applyToEntity(entity);
        }
    }

    /**
     * Handles property changes from the transformation model
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (updating) return;
        
        updating = true;
        try {
            // Only sync the specific property that changed to avoid full sync
            String propertyName = evt.getPropertyName();
            Object newValue = evt.getNewValue();

            switch (propertyName) {
                case TransformationSettingsHandler.PROP_POSITION_X ->
                    selectionModel.setPositionX((Double) newValue);
                case TransformationSettingsHandler.PROP_POSITION_Y ->
                    selectionModel.setPositionY((Double) newValue);
                case TransformationSettingsHandler.PROP_WIDTH ->
                    selectionModel.setWidth((Double) newValue);
                case TransformationSettingsHandler.PROP_HEIGHT ->
                    selectionModel.setHeight((Double) newValue);
                case TransformationSettingsHandler.PROP_ROTATION ->
                    selectionModel.setRotation((Double) newValue);
                case TransformationSettingsHandler.PROP_ANCHOR ->
                    selectionModel.setAnchor((Anchor) newValue);
                case TransformationSettingsHandler.PROP_LOCK_RATIO ->
                    selectionModel.setLockRatio((Boolean) newValue);
            }
        } finally {
            updating = false;
        }
    }

    /**
     * Handles changes from the selection model
     */
    private void onSelectionModelChange(EntitySetting setting) {
        if (updating) return;
        
        updating = true;
        try {
            syncFromSelectionModel(setting);
        } finally {
            updating = false;
        }
    }

    /**
     * Syncs a specific value from selection model to transformation model
     */
    private void syncFromSelectionModel(EntitySetting setting) {
        switch (setting) {
            case POSITION_X -> transformationModel.setPositionX(selectionModel.getPositionX());
            case POSITION_Y -> transformationModel.setPositionY(selectionModel.getPositionY());
            case WIDTH -> transformationModel.setWidth(selectionModel.getWidth());
            case HEIGHT -> transformationModel.setHeight(selectionModel.getHeight());
            case ROTATION -> transformationModel.setRotation(selectionModel.getRotation());
            case ANCHOR -> transformationModel.setAnchor(selectionModel.getAnchor());
            case LOCK_RATIO -> transformationModel.setLockRatio(selectionModel.getLockRatio());
        }
    }

    /**
     * Gets the transformation model for direct access
     */
    public TransformationSettingsHandler getTransformationModel() {
        return transformationModel;
    }

    /**
     * Adds a property change listener to the transformation model
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        transformationModel.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener from the transformation model
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        transformationModel.removePropertyChangeListener(listener);
    }

    /**
     * Adds a property change listener for a specific property
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        transformationModel.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a property change listener for a specific property
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        transformationModel.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Cleanup method to remove all listeners
     */
    public void dispose() {
        transformationModel.removePropertyChangeListener(this);
    }
}
