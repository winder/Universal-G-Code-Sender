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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EntityListener;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.gui.CutTypeCombo;
import com.willwinder.ugs.nbp.designer.gui.FontCombo;
import com.willwinder.ugs.nbp.designer.gui.anchor.AnchorSelectorPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * @author Joacim Breiler
 */
public class SelectionSettingsPanel extends JPanel implements SelectionListener, EntityListener, SelectionSettingsModelListener {
    private final SelectionSettingsModel model = new SelectionSettingsModel();
    private final transient FieldEventDispatcher fieldEventDispatcher;
    private transient Controller controller;
    private TextFieldWithUnit widthTextField;
    private TextFieldWithUnit rotation;
    private TextFieldWithUnit posXTextField;
    private TextFieldWithUnit posYTextField;
    private JLabel startDepthLabel;
    private JLabel targetDepthLabel;
    private CutTypeCombo cutTypeComboBox;
    private UnitSpinner startDepthSpinner;
    private UnitSpinner targetDepthSpinner;
    private TextFieldWithUnit heightTextField;
    private JLabel textLabel;
    private FontCombo fontDropDown;
    private JLabel fontLabel;
    private JSeparator fontSeparator;
    private JTextField textTextField;
    private AnchorSelectorPanel anchorSelector;
    private JLabel widthLabel;
    private JLabel heightLabel;
    private JToggleButton lockRatioButton;

    public SelectionSettingsPanel(Controller controller) {
        fieldEventDispatcher = new FieldEventDispatcher();
        setLayout(new MigLayout("fill, hidemode 3, insets 5", "[sg label] 5 [grow] 5 [60px]"));
        addTextSettingFields();
        addPositionFields();
        addCutFields();
        setController(controller);
        model.addListener(this);
        FieldActionDispatcher fieldActionDispatcher = new FieldActionDispatcher(model, controller);
        fieldEventDispatcher.addListener(fieldActionDispatcher);
    }

    private void addPositionFields() {
        createAndAddLabel(EntitySetting.POSITION_X);
        posXTextField = createAndAddField(EntitySetting.POSITION_X, TextFieldUnit.MM, false);
        anchorSelector = new AnchorSelectorPanel();
        anchorSelector.setAnchor(model.getAnchor());
        anchorSelector.addListener((model::setAnchor));
        add(anchorSelector, "span 1 2, grow, wrap");

        createAndAddLabel(EntitySetting.POSITION_Y);
        posYTextField = createAndAddField(EntitySetting.POSITION_Y, TextFieldUnit.MM, true);

        widthLabel = createAndAddLabel(EntitySetting.WIDTH);
        widthTextField = createAndAddField(EntitySetting.WIDTH, TextFieldUnit.MM, false);
        lockRatioButton = new JToggleButton(ImageUtilities.loadImageIcon("img/link.svg", false));
        lockRatioButton.setSelectedIcon(ImageUtilities.loadImageIcon("img/link-off.svg", false));
        lockRatioButton.addActionListener(l -> model.setLockRatio(!lockRatioButton.isSelected()));
        add(lockRatioButton, "span 1 2, growy, wrap");

        heightLabel = createAndAddLabel(EntitySetting.HEIGHT);
        heightTextField = createAndAddField(EntitySetting.HEIGHT, TextFieldUnit.MM, true);

        createAndAddLabel(EntitySetting.ROTATION);
        rotation = createAndAddField(EntitySetting.ROTATION, TextFieldUnit.DEGREE, true);
        add(new JSeparator(), "grow, spanx, wrap");
    }

    private JLabel createAndAddLabel(EntitySetting entitySetting) {
        JLabel label = new JLabel(entitySetting.getLabel(), SwingConstants.RIGHT);
        add(label, "grow");
        return label;
    }

    private TextFieldWithUnit createAndAddField(EntitySetting setting, TextFieldUnit units, boolean wrap) {
        TextFieldWithUnit field = new TextFieldWithUnit(units, 4, 0);
        fieldEventDispatcher.registerListener(setting, field);
        add(field, wrap ? "grow, wrap" : "grow");
        return field;
    }

    private void addCutFields() {
        cutTypeComboBox = new CutTypeCombo();
        fieldEventDispatcher.registerListener(EntitySetting.CUT_TYPE, cutTypeComboBox);

        JLabel cutTypeLabel = new JLabel("Cut type", SwingConstants.RIGHT);
        add(cutTypeLabel, "grow");
        add(cutTypeComboBox, "grow, wrap");

        startDepthLabel = new JLabel("Start depth", SwingConstants.RIGHT);
        add(startDepthLabel, "grow");

        startDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, null, null, 0.1d);
        startDepthSpinner.setPreferredSize(startDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.START_DEPTH, startDepthSpinner);
        add(startDepthSpinner, "grow, wrap");

        targetDepthLabel = new JLabel("Target depth", SwingConstants.RIGHT);
        add(targetDepthLabel, "grow");

        targetDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, 0d, null, 0.1d);

        targetDepthSpinner.setPreferredSize(targetDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.TARGET_DEPTH, targetDepthSpinner);
        add(targetDepthSpinner, "grow, wrap");
        setEnabled(false);
    }

    private void setController(Controller controller) {
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
        this.controller.getSelectionManager().addListener(this);
    }


    private void addTextSettingFields() {
        textLabel = new JLabel("Text", SwingConstants.RIGHT);
        textLabel.setVisible(false);
        add(textLabel, "grow");

        textTextField = new JTextField();
        textTextField.setVisible(false);
        fieldEventDispatcher.registerListener(EntitySetting.TEXT, textTextField);
        add(textTextField, "grow, wrap");

        fontLabel = new JLabel("Font", SwingConstants.RIGHT);
        fontLabel.setVisible(false);
        add(fontLabel, "grow");

        fontDropDown = new FontCombo();
        fieldEventDispatcher.registerListener(EntitySetting.FONT_FAMILY, fontDropDown);
        fontDropDown.setVisible(false);
        add(fontDropDown, "grow, wrap");

        fontSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        fontSeparator.setVisible(false);
        add(fontSeparator, "grow, spanx, wrap");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(component -> {
            if (component == startDepthSpinner || component == startDepthLabel || component == targetDepthSpinner || component == targetDepthLabel) {
                boolean hasCutType = enabled && model.getCutType() != CutType.NONE;
                component.setEnabled(hasCutType);
            } else {
                component.setEnabled(enabled);
            }
        });
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        onEvent(new EntityEvent(controller.getSelectionManager(), EventType.SELECTED));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        Group selectionGroup = controller.getSelectionManager().getSelectionGroup();
        if (selectionGroup.getChildren().isEmpty()) {
            model.reset();
            setEnabled(false);
            return;
        }

        setEnabled(true);
        boolean isTextCuttable = selectionGroup.getChildren().get(0) instanceof Text;
        if (isTextCuttable) {
            Text textEntity = (Text) selectionGroup.getChildren().get(0);
            model.setText(textEntity.getText());
            model.setFontFamily(textEntity.getFontFamily());
        }

        model.setPositionX(selectionGroup.getPosition(model.getAnchor()).getX());
        model.setPositionY(selectionGroup.getPosition(model.getAnchor()).getY());
        model.setWidth(selectionGroup.getSize().getWidth());
        model.setHeight(selectionGroup.getSize().getHeight());
        model.setRotation(selectionGroup.getRotation());
        model.setStartDepth(selectionGroup.getStartDepth());
        model.setTargetDepth(selectionGroup.getTargetDepth());
        model.setCutType(selectionGroup.getCutType());
        controller.getDrawing().invalidate();
    }

    public void release() {
        this.controller.getSelectionManager().removeSelectionListener(this);
        this.controller.getSelectionManager().removeListener(this);
    }

    @Override
    public void onModelUpdate(EntitySetting entitySetting) {
        Group selectionGroup = controller.getSelectionManager().getSelectionGroup();
        Entity firstChild = selectionGroup.getChildren().isEmpty() ? selectionGroup : selectionGroup.getChildren().get(0);

        if (entitySetting == EntitySetting.WIDTH) {
            widthTextField.setDoubleValue(model.getWidth());
            selectionGroup.setSize(new Size(model.getWidth(), selectionGroup.getSize().getHeight()));
        } else if (entitySetting == EntitySetting.HEIGHT) {
            heightTextField.setDoubleValue(model.getHeight());
            selectionGroup.setSize(new Size(selectionGroup.getSize().getWidth(), model.getHeight()));
        } else if (entitySetting == EntitySetting.POSITION_X) {
            selectionGroup.setPosition(model.getAnchor(), new Point2D.Double(model.getPositionX(), selectionGroup.getPosition(model.getAnchor()).getY()));
            posXTextField.setValue(model.getPositionX());
        } else if (entitySetting == EntitySetting.POSITION_Y) {
            selectionGroup.setPosition(model.getAnchor(), new Point2D.Double(selectionGroup.getPosition(model.getAnchor()).getX(), model.getPositionY()));
            posYTextField.setValue(model.getPositionY());
        } else if (entitySetting == EntitySetting.ROTATION) {
            rotation.setValue(model.getRotation());
            selectionGroup.setRotation(model.getRotation());
        } else if (entitySetting == EntitySetting.ANCHOR) {
            anchorSelector.setAnchor(model.getAnchor());
            selectionGroup.setPosition(model.getAnchor(), selectionGroup.getPosition(model.getAnchor()));
        } else if (entitySetting == EntitySetting.CUT_TYPE) {
            cutTypeComboBox.setSelectedItem(model.getCutType());
            selectionGroup.setCutType(model.getCutType());
        } else if (entitySetting == EntitySetting.START_DEPTH) {
            startDepthSpinner.setValue(model.getStartDepth());
            selectionGroup.setStartDepth(model.getStartDepth());
        } else if (entitySetting == EntitySetting.TARGET_DEPTH) {
            targetDepthSpinner.setValue(model.getTargetDepth());
            selectionGroup.setTargetDepth(model.getTargetDepth());
        } else if (entitySetting == EntitySetting.TEXT) {
            textTextField.setText(model.getText());
            if (!selectionGroup.getChildren().isEmpty() && selectionGroup.getChildren().get(0) instanceof Text textEntity) {
                textEntity.setText(model.getText());
            }
        } else if (entitySetting == EntitySetting.FONT_FAMILY) {
            fontDropDown.setSelectedItem(model.getFontFamily());
            if (!selectionGroup.getChildren().isEmpty() && selectionGroup.getChildren().get(0) instanceof Text textEntity) {
                textEntity.setFontFamily(model.getFontFamily());
            }
        } else if (entitySetting == EntitySetting.LOCK_RATIO) {
            lockRatioButton.setSelected(!model.getLockRatio());
        }

        final boolean hasCutTypeSelection = selectionGroup.getCutType() != CutType.NONE;
        startDepthSpinner.setEnabled(hasCutTypeSelection);
        startDepthLabel.setEnabled(hasCutTypeSelection);
        targetDepthSpinner.setEnabled(hasCutTypeSelection);
        targetDepthLabel.setEnabled(hasCutTypeSelection);

        boolean isTextCuttable = firstChild.getSettings().contains(EntitySetting.TEXT);
        textTextField.setVisible(isTextCuttable);
        textLabel.setVisible(isTextCuttable);
        fontLabel.setVisible(isTextCuttable);
        fontDropDown.setVisible(isTextCuttable);
        fontSeparator.setVisible(isTextCuttable);

        boolean hasWidth = firstChild.getSettings().contains(EntitySetting.WIDTH);
        widthLabel.setVisible(hasWidth);
        widthTextField.setVisible(hasWidth);

        boolean hasHeight = firstChild.getSettings().contains(EntitySetting.HEIGHT);
        heightLabel.setVisible(hasHeight);
        heightTextField.setVisible(hasHeight);

        boolean hasAnchor = firstChild.getSettings().contains(EntitySetting.ANCHOR);
        anchorSelector.setVisible(hasAnchor);

        lockRatioButton.setVisible(hasWidth && hasHeight);
    }
}
