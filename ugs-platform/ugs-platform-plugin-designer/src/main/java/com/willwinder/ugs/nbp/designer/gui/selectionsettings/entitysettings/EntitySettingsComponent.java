package com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

/**
 * Generic contract for custom entity settings panels.
 * Implementations should be self-contained and communicate via PropertyChangeSupport
 * events, and know how to apply their changes to the current selection.
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

    void addChangeListener(PropertyChangeListener l);

    void removeChangeListener(PropertyChangeListener l);
}
