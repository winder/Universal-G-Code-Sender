/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.nbp.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.CORNER_RADIUS;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.settings.RectangleSettingsManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import com.willwinder.universalgcodesender.uielements.components.SeparatorLabel;
import net.miginfocom.swing.MigLayout;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Optional;

@ServiceProvider(service = EntitySettingsPanel.class, position = 10)
public class RectangleSettingsPanel extends JPanel implements EntitySettingsPanel {

    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36, wrap, spanx";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final RectangleSettingsManager settingsManager = new RectangleSettingsManager();

    private TextFieldWithUnit cornerRadiusTextField;
    private boolean updating = false;

    public RectangleSettingsPanel() {
        super(new MigLayout("insets 0, gap 10, fillx", "[sg label,right] 10 [grow]"));
        initializeComponents();
        buildLayout();
        setupListeners();
    }

    private void initializeComponents() {
        cornerRadiusTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
    }

    private void buildLayout() {
        add(new SeparatorLabel(Localization.getString("designer.panel.shape-settings.rectangle.title"), SwingConstants.RIGHT), "spanx, growx");

        add(new JLabel("Corner radius", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(cornerRadiusTextField, FIELD_CONSTRAINTS);
    }

    private void setupListeners() {
        cornerRadiusTextField.addPropertyChangeListener("value", evt -> firePropertyChange(CORNER_RADIUS.getPropertyName(), evt.getNewValue()));
    }

    public String getTitle() {
        return "Text Properties";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        cornerRadiusTextField.setEnabled(enabled);
    }

    @Override
    public boolean isApplicable(Group selectionGroup) {
        return selectionGroup.getChildren().stream()
                .allMatch(Rectangle.class::isInstance);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setFromSelection(Group selectionGroup) {
        Optional<Rectangle> rectangle = selectionGroup.getAllChildren().stream()
                .filter(c -> c instanceof Rectangle)
                .map(c -> (Rectangle) c).findFirst();

        rectangle.ifPresent(r -> {
            updating = true;
            try {
                cornerRadiusTextField.setValue(r.getCornerRadius());
            } finally {
                updating = false;
            }
        });
    }

    @Override
    public void applyChangeToSelection(String setting, Object newValue, Group selectionGroup) {
        if (EntitySetting.fromPropertyName(setting).equals(CORNER_RADIUS)) {
            selectionGroup.getAllChildren().stream()
                    .filter(c -> c instanceof Rectangle)
                    .map(c -> (Rectangle) c)
                    .forEach(rectangle -> rectangle.setCornerRadius((Double) newValue));
        }
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
        action.redo();
        controller.getUndoManager().addAction(action);
    }

    private UndoableAction createAction(String propertyName, Object newValue, List<Entity> entities) {
        if (CORNER_RADIUS.getPropertyName().equals(propertyName)) {
            return new ChangeEntitySettingsAction(entities, CORNER_RADIUS, newValue, settingsManager);
        } else {
            throw new IllegalArgumentException("Unsupported property: " + propertyName + " (valid properties are: " + CORNER_RADIUS.getPropertyName() + ")");
        }
    }

    private void firePropertyChange(String propertyName, Object newValue) {
        if (!updating) {
            pcs.firePropertyChange(propertyName, null, newValue);
        }
    }
}
