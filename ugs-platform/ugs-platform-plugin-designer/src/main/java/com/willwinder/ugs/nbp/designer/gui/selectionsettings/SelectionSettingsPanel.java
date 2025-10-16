/*
    Copyright 2021-2024 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings.ComponentWithListener;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings.EntitySettingsComponent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import net.miginfocom.swing.MigLayout;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SelectionSettingsPanel extends JPanel implements SelectionListener, EntityListener {
    private transient Controller controller;
    private final JPanel contentPanel;
    private final List<EntitySettingsComponent> availableComponents = new ArrayList<>();
    private final List<ComponentWithListener> activeComponents = new ArrayList<>();

    public SelectionSettingsPanel(Controller controller) {
        setLayout(new MigLayout("fillx, insets 10, gap 0", "[grow]"));

        contentPanel = new JPanel(new MigLayout("fillx, insets 0, gap 10", "[grow]"));
        add(contentPanel, "growx, wrap");

        Collection<? extends EntitySettingsComponent> components = Lookup.getDefault().lookupAll(EntitySettingsComponent.class);
        availableComponents.addAll(components);

        setController(controller);
        setEnabled(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setEnabledRecursive(this, enabled);
        activeComponents.forEach(activeComponent -> {
            if (activeComponent.component().getComponent() != null) {
                activeComponent.component().getComponent().setEnabled(enabled);
            }
        });
    }

    private void setEnabledRecursive(Container container, boolean enabled) {
        for (Component c : container.getComponents()) {
            c.setEnabled(enabled);
            if (c instanceof Container child) {
                setEnabledRecursive(child, enabled);
            }
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        onEvent(new EntityEvent(controller.getSelectionManager(), EventType.SELECTED));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        Group selectionGroup = controller.getSelectionManager().getSelectionGroup();
        if (selectionGroup.getChildren().isEmpty()) {
            clearAllComponents();
            setEnabled(false);
            return;
        }

        setEnabled(true);
        updateActiveComponents(selectionGroup);
        controller.getDrawing().invalidate();
    }

    private void updateActiveComponents(Group selectionGroup) {
        clearAllComponents();

        availableComponents.stream()
                .filter(component -> component.isApplicable(selectionGroup))
                .forEach(component -> activateComponent(component, selectionGroup));
    }

    private void activateComponent(EntitySettingsComponent component, Group selectionGroup) {
        if (component == null || component.getComponent() == null) return;

        mountComponent(component.getComponent());

        PropertyChangeListener listener = evt -> {
            Group currentGroup = controller.getSelectionManager().getSelectionGroup();
            component.createAndExecuteUndoableAction(evt.getPropertyName(), evt.getNewValue(), currentGroup, controller);
            controller.getDrawing().invalidate();
        };

        component.addChangeListener(listener);
        activeComponents.add(new ComponentWithListener(component, listener));
        component.setFromSelection(selectionGroup);
    }

    private void mountComponent(JComponent componentUI) {
        if (componentUI != null && componentUI.getParent() != contentPanel) {
            contentPanel.add(componentUI, "growx, spanx, wrap");
        }
    }

    private void clearAllComponents() {
        activeComponents.forEach(activeComponent ->
            activeComponent.component().removeChangeListener(activeComponent.listener())
        );

        activeComponents.clear();
        contentPanel.removeAll();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public void release() {
        clearAllComponents();
        if (controller != null) {
            controller.getSelectionManager().removeSelectionListener(this);
            controller.getSelectionManager().removeListener(this);
        }
    }

    private void setController(Controller controller) {
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
        this.controller.getSelectionManager().addListener(this);
    }
}
