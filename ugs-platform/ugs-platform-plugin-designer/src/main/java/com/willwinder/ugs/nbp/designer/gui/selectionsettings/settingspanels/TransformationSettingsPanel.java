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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.nbp.designer.actions.SettingsActionFactory;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.settings.TransformationEntitySettingsManager;
import com.willwinder.ugs.nbp.designer.gui.anchor.AnchorSelectorPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;


/*
 * @Author giro-dev
 */
@ServiceProvider(service = EntitySettingsPanel.class, position = 9)
public class TransformationSettingsPanel extends JPanel implements EntitySettingsPanel {

    // Use EntitySetting property names instead of string constants
    private static final String PROP_POSITION_X = EntitySetting.POSITION_X.getPropertyName();
    private static final String PROP_POSITION_Y = EntitySetting.POSITION_Y.getPropertyName();
    private static final String PROP_WIDTH = EntitySetting.WIDTH.getPropertyName();
    private static final String PROP_HEIGHT = EntitySetting.HEIGHT.getPropertyName();
    private static final String PROP_ROTATION = EntitySetting.ROTATION.getPropertyName();
    private static final String PROP_ANCHOR = EntitySetting.ANCHOR.getPropertyName();
    private static final String PROP_LOCK_RATIO = EntitySetting.LOCK_RATIO.getPropertyName();

    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final TransformationEntitySettingsManager settingsManager = new TransformationEntitySettingsManager();

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


    public TransformationSettingsPanel() {
        super(new MigLayout("insets 0, gap 10, fillx", "[sg label,right] 10 [grow] 10 [60px]"));
        initializeComponents();
        buildLayout();
        setupListeners();
        lockRatioButton.setSelected(true);
    }

    private void initializeComponents() {
        posXTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        posYTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        widthTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        heightTextField = new TextFieldWithUnit(TextFieldUnit.MM, 4, 0);
        rotationTextField = new TextFieldWithUnit(TextFieldUnit.DEGREE, 4, 0);
        anchorSelector = new AnchorSelectorPanel();

        lockRatioButton = new JToggleButton(ImageUtilities.loadImageIcon("img/link-off.svg", false));
        lockRatioButton.setSelectedIcon(ImageUtilities.loadImageIcon("img/link.svg", false));

    }

    private void buildLayout() {
        add(new JLabel(Localization.getString("designer.panel.shape-settings.transform.title"), SwingConstants.LEFT), "spanx, gaptop 5, gapbottom 0, wrap");
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
            firePropertyChange(PROP_WIDTH, evt.getNewValue());

            // Update height field when lockRatio is enabled
            if (lockRatio && evt.getNewValue() instanceof Double newWidth) {
                updating = true;
                try {
                    double currentWidth = widthTextField.getDoubleValue();
                    double currentHeight = heightTextField.getDoubleValue();
                    if (currentWidth > 0) {
                        double ratio = currentHeight / currentWidth;
                        heightTextField.setDoubleValue(newWidth * ratio);
                    }
                } finally {
                    updating = false;
                }
            }
        });

        heightTextField.addPropertyChangeListener("value", evt -> {
            if (updating) return;
            firePropertyChange(PROP_HEIGHT, evt.getNewValue());

            // Update width field when lockRatio is enabled
            if (lockRatio && evt.getNewValue() instanceof Double newHeight) {
                updating = true;
                try {
                    double currentWidth = widthTextField.getDoubleValue();
                    double currentHeight = heightTextField.getDoubleValue();
                    if (currentHeight > 0) {
                        double ratio = currentWidth / currentHeight;
                        widthTextField.setDoubleValue(newHeight * ratio);
                    }
                } finally {
                    updating = false;
                }
            }
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

        updating = true;
        try {
            Point2D position = selectionGroup.getPosition(currentAnchor);
            posXTextField.setValue(position.getX());
            posYTextField.setValue(position.getY());
            widthTextField.setDoubleValue(selectionGroup.getSize().getWidth());
            heightTextField.setDoubleValue(selectionGroup.getSize().getHeight());
            rotationTextField.setValue(selectionGroup.getRotation());
            anchorSelector.setAnchor(currentAnchor);
            lockRatioButton.setSelected(lockRatio);
        } finally {
            updating = false;
        }
    }

    @Override
    public void applyChangeToSelection(String setting, Object newValue, Group selectionGroup) {
        switch (Objects.requireNonNull(EntitySetting.fromPropertyName(setting))) {
            case POSITION_X -> {
                    Point2D currentPos = selectionGroup.getPosition(currentAnchor);
                    selectionGroup.setPosition(currentAnchor, new Point2D.Double((Double) newValue, currentPos.getY()));
                }
            case POSITION_Y -> {
                    Point2D currentPos = selectionGroup.getPosition(currentAnchor);
                    selectionGroup.setPosition(currentAnchor, new Point2D.Double(currentPos.getX(), (Double) newValue));
                }
            case WIDTH -> {
                    Size currentSize = selectionGroup.getSize();
                    double newWidth = (Double) newValue;
                    double newHeight = lockRatio ? currentSize.getHeight() * (newWidth / currentSize.getWidth()) : currentSize.getHeight();
                    selectionGroup.setSize(currentAnchor, new Size(newWidth, newHeight));
                }
            case HEIGHT -> {
                    Size currentSize = selectionGroup.getSize();
                    double newHeight = (Double) newValue;
                    double newWidth = lockRatio ? currentSize.getWidth() * (newHeight / currentSize.getHeight()) : currentSize.getWidth();
                    selectionGroup.setSize(currentAnchor, new Size(newWidth, newHeight));
                }
            case ANCHOR -> {
                    currentAnchor = (Anchor) newValue;
                    updatePositionFieldsForAnchor(selectionGroup);
                }
            case LOCK_RATIO -> this.lockRatio = !(Boolean) newValue;
            }
    }

    private void updatePositionFieldsForAnchor(Group selectionGroup) {
        if (selectionGroup.getChildren().isEmpty()) return;

        Point2D position = selectionGroup.getPosition(currentAnchor);

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
    public void createAndExecuteUndoableAction(String setting, Object newValue, Group selectionGroup, Controller controller) {
        if (selectionGroup.getChildren().isEmpty()) return;

        // For transformations, we need to work with the group as a whole to maintain relationships
        UndoableAction action = createGroupAction(EntitySetting.fromPropertyName(setting), newValue, selectionGroup);
        if (action != null) {
            action.redo();
            controller.getUndoManager().addAction(action);
        }
    }

    private UndoableAction createGroupAction(EntitySetting propertyName, Object newValue, Group selectionGroup) {
        // Use the centralized factory to create actions
        return SettingsActionFactory.createAction(
                propertyName,
                newValue,
                selectionGroup,
                currentAnchor,
                lockRatio,
                settingsManager
        );
    }

    private EntitySetting mapPropertyToEntitySetting(String propertyName) {
        return SettingsActionFactory.mapPropertyToEntitySetting(propertyName);
    }
}
