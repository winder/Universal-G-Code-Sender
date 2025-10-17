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
import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.gui.anchor.AnchorSelectorPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.Component;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;


/*
 * @Author giro-dev
 */
@ServiceProvider(service = EntitySettingsComponent.class, position = 9)
public class TransformationSettingsComponent extends JPanel implements EntitySettingsComponent {

    public static final String PROP_POSITION_X = "positionX";
    public static final String PROP_POSITION_Y = "positionY";
    public static final String PROP_WIDTH = "width";
    public static final String PROP_HEIGHT = "height";
    public static final String PROP_ROTATION = "rotation";
    public static final String PROP_ANCHOR = "anchor";
    public static final String PROP_LOCK_RATIO = "lockRatio";

    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private TextFieldWithUnit posXTextField;
    private TextFieldWithUnit posYTextField;
    private TextFieldWithUnit widthTextField;
    private TextFieldWithUnit heightTextField;
    private TextFieldWithUnit rotationTextField;
    private AnchorSelectorPanel anchorSelector;
    private JToggleButton lockRatioButton;

    private boolean updating = false;
    private boolean lockRatio = false;
    private Anchor currentAnchor = Anchor.CENTER; // Add field to store current anchor

    public TransformationSettingsComponent() {
        super(new MigLayout("insets 0, gap 10, fillx", "[sg label,right] 10 [grow] 10 [60px]"));
        initializeComponents();
        buildLayout();
        setupListeners();
    }

    private void initializeComponents() {
        posXTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        posYTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        widthTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        heightTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        rotationTextField = new TextFieldWithUnit(TextFieldUnit.DEGREE, 4, 0);
        anchorSelector = new AnchorSelectorPanel();

        lockRatioButton = new JToggleButton(ImageUtilities.loadImageIcon("img/link.svg", false));
        lockRatioButton.setSelectedIcon(ImageUtilities.loadImageIcon("img/link-off.svg", false));
    }

    private void buildLayout() {
        add(new JLabel("Transform", SwingConstants.LEFT), "spanx, gaptop 5, gapbottom 0, wrap");
        add(new JSeparator(), "spanx, growx, gaptop 0, gapbottom 5, wrap");

        addLabeledField("X", posXTextField, anchorSelector, "span 1 2");
        addLabeledField("Y", posYTextField, null, null);
        addLabeledField("Width", widthTextField, lockRatioButton, "span 1 2, growy");
        addLabeledField("Height", heightTextField, null, null);
        addLabeledField("Rotation", rotationTextField, null, "spanx");
    }

    private void addLabeledField(String labelText, JComponent field, JComponent extraComponent, String extraConstraints) {
        add(new JLabel(labelText, SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(field, FIELD_CONSTRAINTS + (extraComponent == null ? ", wrap" : ""));
        if (extraComponent != null) {
            add(extraComponent, (extraConstraints != null ? extraConstraints : "grow") + ", wrap");
        }
    }

    private void setupListeners() {
        posXTextField.addPropertyChangeListener("value", evt -> firePropertyChange(PROP_POSITION_X, evt.getNewValue()));
        posYTextField.addPropertyChangeListener("value", evt -> firePropertyChange(PROP_POSITION_Y, evt.getNewValue()));

        widthTextField.addPropertyChangeListener("value", evt -> {
            if (updating) return;
            double newWidth = (Double) evt.getNewValue();
            handleRatioChange(newWidth, heightTextField, widthTextField, (Double) evt.getOldValue(), PROP_HEIGHT);
            firePropertyChange(PROP_WIDTH, evt.getNewValue());
        });

        heightTextField.addPropertyChangeListener("value", evt -> {
            if (updating) return;
            double newHeight = (Double) evt.getNewValue();
            handleRatioChange(newHeight, widthTextField, heightTextField, (Double) evt.getOldValue(), PROP_WIDTH);
            firePropertyChange(PROP_HEIGHT, evt.getNewValue());
        });

        rotationTextField.addPropertyChangeListener("value", evt -> firePropertyChange(PROP_ROTATION, evt.getNewValue()));

        anchorSelector.addListener(anchor -> {
            currentAnchor = anchor;
            firePropertyChange(PROP_ANCHOR, anchor);
        });

        lockRatioButton.addActionListener(e -> {
            lockRatio = lockRatioButton.isSelected();
            firePropertyChange(PROP_LOCK_RATIO, lockRatio);
        });
    }

    private void handleRatioChange(double newValue, TextFieldWithUnit otherField,
                                  TextFieldWithUnit currentField, Double oldValue, String otherProp) {
        if ( otherField.getDoubleValue() > 0 && oldValue != null && oldValue > 0) {
            double ratio = otherField.getDoubleValue() / oldValue;
            double newOtherValue = newValue * ratio;
            updating = true;
            otherField.setDoubleValue(newOtherValue);
            updating = false;
            firePropertyChange(otherProp, newOtherValue);
        }
    }

    private void firePropertyChange(String propertyName, Object newValue) {
        if (!updating) {
            pcs.firePropertyChange(propertyName, null, newValue);
        }
    }

    public String getTitle() {
        return "Transform";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Component[] components = {posXTextField, posYTextField, widthTextField, heightTextField,
                                rotationTextField, anchorSelector, lockRatioButton};
        for (Component component : components) {
            if (component != null) component.setEnabled(enabled);
        }
    }

    @Override
    public boolean isApplicable(Group selectionGroup) {
        return !selectionGroup.getChildren().isEmpty();
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setFromSelection(Group selectionGroup) {
        if (selectionGroup.getChildren().isEmpty()) return;

        Entity firstEntity = selectionGroup.getChildren().get(0);
        updating = true;
        try {
            Point2D position = firstEntity.getPosition(currentAnchor);
            posXTextField.setValue(position.getX());
            posYTextField.setValue(position.getY());
            widthTextField.setDoubleValue(firstEntity.getSize().getWidth());
            heightTextField.setDoubleValue(firstEntity.getSize().getHeight());
            rotationTextField.setValue(firstEntity.getRotation());
            anchorSelector.setAnchor(currentAnchor);
            lockRatioButton.setSelected(lockRatio);
        } finally {
            updating = false;
        }
    }

    @Override
    public void applyChangeToSelection(String propertyName, Object newValue, Group selectionGroup) {
        selectionGroup.getChildren().forEach(entity -> {
            switch (propertyName) {
                case PROP_POSITION_X -> {
                    Point2D currentPos = entity.getPosition(currentAnchor);
                    entity.setPosition(currentAnchor, new Point2D.Double((Double) newValue, currentPos.getY()));
                }
                case PROP_POSITION_Y -> {
                    Point2D currentPos = entity.getPosition(currentAnchor);
                    entity.setPosition(currentAnchor, new Point2D.Double(currentPos.getX(), (Double) newValue));
                }
                case PROP_WIDTH -> {
                    Size currentSize = entity.getSize();
                    double newWidth = (Double) newValue;
                    if (lockRatio && currentSize.getHeight() > 0) {
                        double ratio = currentSize.getHeight() / currentSize.getWidth();
                        double newHeight = newWidth * ratio;
                        entity.setSize(currentAnchor, new Size(newWidth, newHeight));
                    } else {
                        entity.setSize(currentAnchor, new Size(newWidth, currentSize.getHeight()));
                    }
                }
                case PROP_HEIGHT -> {
                    Size currentSize = entity.getSize();
                    double newHeight = (Double) newValue;
                    if (lockRatio && currentSize.getHeight() > 0) {
                        double ratio = currentSize.getWidth() / currentSize.getHeight();
                        double newWidth = newHeight * ratio;
                        entity.setSize(currentAnchor, new Size(newWidth, newHeight));
                    } else {
                        entity.setSize(currentAnchor, new Size(currentSize.getWidth(), newHeight));
                    }
                }
                case PROP_ANCHOR -> {
                    currentAnchor = (Anchor) newValue;
                    updatePositionFieldsForAnchor(selectionGroup);
                }
                case PROP_LOCK_RATIO -> this.lockRatio = (Boolean) newValue;
            }
        });
    }

    private void updatePositionFieldsForAnchor(Group selectionGroup) {
        if (selectionGroup.getChildren().isEmpty()) return;

        Entity firstEntity = selectionGroup.getChildren().get(0);
        Point2D position = firstEntity.getPosition(currentAnchor);

        updating = true;
        try {
            posXTextField.setValue(position.getX());
            posYTextField.setValue(position.getY());
        } finally {
            updating = false;
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
        if (action != null) {
            action.redo();
            controller.getUndoManager().addAction(action);
        }
    }

    private UndoableAction createAction(String propertyName, Object newValue, List<Entity> entities) {
        EntitySetting setting = mapPropertyToEntitySetting(propertyName);
        return setting != null ? new ChangeEntitySettingsAction(entities, setting, newValue) : null;
    }

    private EntitySetting mapPropertyToEntitySetting(String propertyName) {
        return switch (propertyName) {
            case PROP_POSITION_X -> EntitySetting.POSITION_X;
            case PROP_POSITION_Y -> EntitySetting.POSITION_Y;
            case PROP_WIDTH -> EntitySetting.WIDTH;
            case PROP_HEIGHT -> EntitySetting.HEIGHT;
            case PROP_ROTATION -> EntitySetting.ROTATION;
            case PROP_ANCHOR -> EntitySetting.ANCHOR;
            case PROP_LOCK_RATIO -> null; // This is UI state, not an entity setting
            default -> null;
        };
    }
}
