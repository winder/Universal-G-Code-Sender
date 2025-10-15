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

import com.willwinder.ugs.nbp.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.nbp.designer.actions.ChangeFontAction;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
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
    private final List<EntitySettingsComponent> activeComponents = new ArrayList<>();
    private final List<PropertyChangeListener> activeComponentListeners = new ArrayList<>();

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
        activeComponents.forEach(component -> {
            if (component.getComponent() != null) {
                component.getComponent().setEnabled(enabled);
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

        activeComponents.add(component);
        mountComponent(component.getComponent());

        PropertyChangeListener listener = evt -> {
            Group currentGroup = controller.getSelectionManager().getSelectionGroup();
            createAndExecuteUndoableAction(evt.getPropertyName(), evt.getNewValue(), currentGroup);
            controller.getDrawing().invalidate();
        };

        component.addChangeListener(listener);
        activeComponentListeners.add(listener);
        component.setFromSelection(selectionGroup);
    }

    private void createAndExecuteUndoableAction(String propertyName, Object newValue, Group selectionGroup) {
        List<Entity> entities = selectionGroup.getChildren();
        if (entities.isEmpty()) return;

        UndoableAction action = createAction(propertyName, newValue, entities);
        if (action != null) {
            action.redo();
            controller.getUndoManager().addAction(action);
        }
    }

    private UndoableAction createAction(String propertyName, Object newValue, List<Entity> entities) {
        return switch (propertyName) {
            case "text" -> new ChangeEntitySettingsAction(entities, EntitySetting.TEXT, newValue);
            case "fontFamily" -> createFontAction(entities, newValue);
            default -> {
                EntitySetting setting = mapPropertyToEntitySetting(propertyName);
                yield setting != null ? new ChangeEntitySettingsAction(entities, setting, newValue) : null;
            }
        };
    }

    private UndoableAction createFontAction(List<Entity> entities, Object newValue) {
        List<Text> textEntities = entities.stream()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .toList();

        if (textEntities.isEmpty()) return null;

        return textEntities.size() == 1
                ? new ChangeFontAction(textEntities.get(0), String.valueOf(newValue))
                : new ChangeEntitySettingsAction(entities, EntitySetting.FONT_FAMILY, newValue);
    }

    private EntitySetting mapPropertyToEntitySetting(String propertyName) {
        return switch (propertyName) {
            case "positionX" -> EntitySetting.POSITION_X;
            case "positionY" -> EntitySetting.POSITION_Y;
            case "width" -> EntitySetting.WIDTH;
            case "height" -> EntitySetting.HEIGHT;
            case "rotation" -> EntitySetting.ROTATION;
            case "anchor" -> EntitySetting.ANCHOR;
            case "lockRatio" -> EntitySetting.LOCK_RATIO;
            case "cutType" -> EntitySetting.CUT_TYPE;
            case "startDepth" -> EntitySetting.START_DEPTH;
            case "targetDepth" -> EntitySetting.TARGET_DEPTH;
            case "spindleSpeed" -> EntitySetting.SPINDLE_SPEED;
            case "passes" -> EntitySetting.PASSES;
            case "feedRate" -> EntitySetting.FEED_RATE;
            case "leadInPercent" -> EntitySetting.LEAD_IN_PERCENT;
            case "leadOutPercent" -> EntitySetting.LEAD_OUT_PERCENT;
            case "includeInExport" -> EntitySetting.INCLUDE_IN_EXPORT;
            case "fontFamily" -> EntitySetting.FONT_FAMILY;
            default -> null;
        };
    }

    private void mountComponent(JComponent componentUI) {
        if (componentUI != null && componentUI.getParent() != contentPanel) {
            contentPanel.add(componentUI, "growx, spanx, wrap");
        }
    }

    private void clearAllComponents() {
        for (int i = 0; i < activeComponents.size(); i++) {
            activeComponents.get(i).removeChangeListener(activeComponentListeners.get(i));
        }

        activeComponents.clear();
        activeComponentListeners.clear();
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
