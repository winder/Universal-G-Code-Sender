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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.settings.EntitySettingsManager;

import java.util.List;
import java.util.Optional;

/**
 * Factory for creating undoable actions for entity settings changes.
 * This centralizes action creation logic and follows the Factory pattern for better maintainability.
 *
 * @author giro-dev
 */
public final class SettingsActionFactory {

    /**
     * Creates an appropriate undoable action for a group transformation.
     *
     * @param setting        the property being changed (e.g., "width", "positionX")
     * @param newValue       the new value for the property
     * @param selectionGroup the group being transformed
     * @param anchor         the anchor point for transformations
     * @param lockRatio      whether aspect ratio should be locked for resize operations
     * @return an UndoableAction or an empty optional if the property is not supported
     */
    public static Optional<UndoableAction> createGroupTransformAction(
            EntitySetting setting,
            Object newValue,
            Group selectionGroup,
            Anchor anchor,
            boolean lockRatio) {

        if (selectionGroup == null || selectionGroup.getChildren().isEmpty()) {
            return Optional.empty();
        }

        return switch (setting) {
            case WIDTH -> Optional.of(ResizeAction.resizeWidth(
                    selectionGroup.getChildren(),
                    anchor,
                    lockRatio,
                    (Double) newValue));

            case HEIGHT -> Optional.of(ResizeAction.resizeHeight(
                    selectionGroup.getChildren(),
                    anchor,
                    lockRatio,
                    (Double) newValue));

            case POSITION_X -> Optional.of(MoveAction.moveX(
                    selectionGroup.getChildren(),
                    (Double) newValue - selectionGroup.getPosition(anchor).getX()
            ));

            case POSITION_Y -> Optional.of(MoveAction.moveY(
                    selectionGroup.getChildren(),
                    (Double) newValue - selectionGroup.getPosition(anchor).getY()));

            case ROTATION -> Optional.of(RotateAction.rotateTo(
                    selectionGroup,
                    (Double) newValue
            ));

            default -> Optional.empty();
        };
    }

    /**
     * Creates an action for changing entity settings using a settings manager.
     *
     * @param entities        the entities to modify
     * @param setting         the setting to change
     * @param newValue        the new value for the setting
     * @param settingsManager the manager that handles the setting
     * @return an UndoableAction
     */
    public static Optional<UndoableAction> createEntitySettingAction(
            List<Entity> entities,
            EntitySetting setting,
            Object newValue,
            EntitySettingsManager settingsManager) {

        if (entities == null || entities.isEmpty() || setting == null) {
            return Optional.empty();
        }

        return Optional.of(new ChangeEntitySettingsAction(entities, setting, newValue, settingsManager));
    }

    /**
     * Maps a property name to an EntitySetting enum.
     * Now delegates to EntitySetting.fromPropertyName() for cleaner, centralized lookup.
     *
     * @param propertyName the property name
     * @return the corresponding EntitySetting or null if not found
     */
    public static EntitySetting mapPropertyToEntitySetting(String propertyName) {
        return EntitySetting.fromPropertyName(propertyName);
    }

    /**
     * Creates the appropriate action based on property name and context.
     * This is a high-level method that determines which type of action to create.
     *
     * @param setting         the property being changed
     * @param newValue        the new value
     * @param selectionGroup  the group (can be null for non-group operations)
     * @param anchor          the anchor point (can be null)
     * @param lockRatio       whether to lock aspect ratio
     * @param settingsManager the settings manager to use
     * @return an appropriate UndoableAction or null
     */
    public static Optional<UndoableAction> createAction(
            EntitySetting setting,
            Object newValue,
            Group selectionGroup,
            Anchor anchor,
            boolean lockRatio,
            EntitySettingsManager settingsManager) {

        if (setting == null || selectionGroup == null || selectionGroup.getChildren().isEmpty() ) {
            return Optional.empty();
        }

        // Try to create a group transformation action first
        Optional<UndoableAction> groupAction = createGroupTransformAction(
                setting,
                newValue,
                selectionGroup,
                anchor,
                lockRatio
        );

        if (groupAction.isPresent()) {
            return groupAction;
        }

        // Fall back to entity setting action
        return createEntitySettingAction(
                selectionGroup.getChildren(),
                setting,
                newValue,
                settingsManager
        );
    }
}

