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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

/**
 * Generic contract for custom entity settings panels.
 * Implementations should be self-contained and communicate via PropertyChangeSupport
 * events, and know how to apply their changes to the current selection.
 * @author giro-dev
 */
public interface EntitySettingsComponent {
    /**
     * Whether this settings component should be shown for the current selection.
     */
    boolean isApplicable(Group selectionGroup);

    /**
     * Returns the UI component to mount in the entity settings area.
     */
    JComponent getComponent();

    /**
     * Sync the UI from the current selection (read from entities).
     */
    void setFromSelection(Group selectionGroup);

    /**
     * Apply a change originating from this component to the selection.
     * This avoids leaking entity-specific logic into the container.
     */
    void applyChangeToSelection(String propertyName, Object newValue, Group selectionGroup);

    /**
     * Create and execute an undoable action for a property change.
     * This method handles the undoable action creation and execution internally.
     */
    default void createAndExecuteUndoableAction(String propertyName, Object newValue, Group selectionGroup, Controller controller) {
        // Default implementation - subclasses should override for specific behavior
        applyChangeToSelection(propertyName, newValue, selectionGroup);
    }

    void addChangeListener(PropertyChangeListener l);

    void removeChangeListener(PropertyChangeListener l);
}
