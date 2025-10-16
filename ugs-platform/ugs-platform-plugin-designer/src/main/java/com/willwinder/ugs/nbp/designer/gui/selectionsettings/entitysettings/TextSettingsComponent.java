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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings;

import com.willwinder.ugs.nbp.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.gui.FontCombo;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import net.miginfocom.swing.MigLayout;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;


/*
 * @Author giro-dev
 */
@ServiceProvider(service = EntitySettingsComponent.class, position = 1)
public class TextSettingsComponent extends JPanel implements EntitySettingsComponent {
    public static final String PROP_TEXT = "text";
    public static final String PROP_FONT_FAMILY = "fontFamily";

    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36, wrap, spanx";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private JTextField textField;
    private FontCombo fontCombo;
    private boolean updating = false;

    public TextSettingsComponent() {
        super(new MigLayout("insets 0, gap 10, fillx", "[sg label,right] 10 [grow]"));
        initializeComponents();
        buildLayout();
        setupListeners();
    }

    private void initializeComponents() {
        textField = new JTextField();
        fontCombo = new FontCombo();
    }

    private void buildLayout() {
        add(new JLabel("Text Properties", SwingConstants.LEFT), "spanx, gaptop 5, gapbottom 0, wrap");
        add(new JSeparator(), "spanx, growx, gaptop 0, gapbottom 5, wrap");

        add(new JLabel("Text", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(textField, FIELD_CONSTRAINTS);

        add(new JLabel("Font", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(fontCombo, FIELD_CONSTRAINTS);
    }

    private void setupListeners() {
        // Only fire property change when focus is lost, not on every keystroke
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!updating) {
                    pcs.firePropertyChange(PROP_TEXT, null, textField.getText());
                }
            }
        });

        fontCombo.addActionListener(e -> {
            if (!updating) {
                pcs.firePropertyChange(PROP_FONT_FAMILY, null, fontCombo.getSelectedItem());
            }
        });
    }

    public String getTitle() {
        return "Text Properties";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Component[] components = {textField, fontCombo};
        for (Component component : components) {
            if (component != null) component.setEnabled(enabled);
        }
    }

    @Override
    public boolean isApplicable(Group selectionGroup) {
        return selectionGroup.getChildren().stream()
                .allMatch(Text.class::isInstance);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setFromSelection(Group selectionGroup) {
        Text firstText = selectionGroup.getChildren().stream()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .findFirst()
                .orElse(null);

        if (firstText != null) {
            updating = true;
            try {
                textField.setText(firstText.getText());
                fontCombo.setSelectedItem(firstText.getFontFamily());
            } finally {
                updating = false;
            }
        }
    }

    @Override
    public void applyChangeToSelection(String propertyName, Object newValue, Group selectionGroup) {
        String value = newValue != null ? String.valueOf(newValue) : "";
        selectionGroup.getChildren().stream()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .forEach(text -> {
                    switch (propertyName) {
                        case PROP_TEXT -> text.setText(value);
                        case PROP_FONT_FAMILY -> text.setFontFamily(value);
                    }
                });
    }

    @Override
    public void addChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removeChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    public void createAndExecuteUndoableAction(String propertyName, Object newValue, Group selectionGroup, Controller controller) {
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
            case PROP_TEXT -> new ChangeEntitySettingsAction(entities, EntitySetting.TEXT, newValue);
            case PROP_FONT_FAMILY -> createFontAction(entities, newValue);
            default -> {
                EntitySetting setting = mapPropertyToEntitySetting(propertyName);
                yield setting != null ? new ChangeEntitySettingsAction(entities, setting, newValue) : null;
            }
        };
    }

    private UndoableAction createFontAction(List<Entity> entities, Object newValue) {
        return new ChangeEntitySettingsAction(entities, EntitySetting.FONT_FAMILY, newValue);
    }

    private EntitySetting mapPropertyToEntitySetting(String propertyName) {
        return switch (propertyName) {
            case PROP_TEXT -> EntitySetting.TEXT;
            case PROP_FONT_FAMILY -> EntitySetting.FONT_FAMILY;
            default -> null;
        };
    }
}
