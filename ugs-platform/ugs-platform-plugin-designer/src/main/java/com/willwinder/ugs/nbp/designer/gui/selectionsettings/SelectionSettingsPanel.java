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
import com.willwinder.universalgcodesender.uielements.components.PercentSpinner;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import java.awt.geom.Point2D;
import java.util.Arrays;

/**
 * @author Joacim Breiler
 */
public class SelectionSettingsPanel extends JPanel implements SelectionListener, EntityListener, SelectionSettingsModelListener {
    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS_NO_WRAP = "grow, w 60:60:300, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = FIELD_CONSTRAINTS_NO_WRAP + ", wrap";
    private static final String SLIDER_FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 44";
    private final SelectionSettingsModel model = new SelectionSettingsModel();
    private final transient FieldEventDispatcher fieldEventDispatcher;
    private transient Controller controller;
    private TextFieldWithUnit widthTextField;
    private TextFieldWithUnit rotation;
    private TextFieldWithUnit posXTextField;
    private TextFieldWithUnit posYTextField;
    private PercentSpinner spindleSpeedSpinner;
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
    private JLabel spindleSpeedLabel;
    private JLabel laserPassesLabel;
    private JSlider passesSlider;
    private JLabel feedRateLabel;
    private UnitSpinner feedRateSpinner;
    private JLabel cutTypeLabel;
    private JLabel leadInPercentSliderLabel;
    private JSlider leadInPercentSlider;
    private JLabel leadOutPercentSliderLabel;
    private JSlider leadOutPercentSlider;


    public SelectionSettingsPanel(Controller controller) {
        fieldEventDispatcher = new FieldEventDispatcher();
        setLayout(new MigLayout("hidemode 3, insets 10, gap 10", "[sg label] 10 [grow] 10 [60px]"));
        addTextSettingFields();
        addPositionFields();
        addCutFields();
        setController(controller);
        model.addListener(this);
        FieldActionDispatcher fieldActionDispatcher = new FieldActionDispatcher(model, controller);
        fieldEventDispatcher.addListener(fieldActionDispatcher);
    }

    private static Boolean selectionHasSetting(Group selectionGroup, EntitySetting entitySetting) {
        return selectionGroup.getSettings().contains(entitySetting);
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
        add(new JSeparator(), "hmin 2, grow, spanx, wrap");
    }

    private JLabel createAndAddLabel(EntitySetting entitySetting) {
        JLabel label = new JLabel(entitySetting.getLabel(), SwingConstants.RIGHT);
        add(label, LABEL_CONSTRAINTS);
        return label;
    }

    private TextFieldWithUnit createAndAddField(EntitySetting setting, TextFieldUnit units, boolean wrap) {
        TextFieldWithUnit field = new TextFieldWithUnit(units, 4, 0);
        fieldEventDispatcher.registerListener(setting, field);
        add(field, wrap ? FIELD_CONSTRAINTS : FIELD_CONSTRAINTS_NO_WRAP);
        return field;
    }

    private void addCutFields() {
        cutTypeComboBox = new CutTypeCombo();
        fieldEventDispatcher.registerListener(EntitySetting.CUT_TYPE, cutTypeComboBox);

        cutTypeLabel = createAndAddLabel(EntitySetting.CUT_TYPE);
        add(cutTypeComboBox, FIELD_CONSTRAINTS + ", spanx");

        feedRateLabel = createAndAddLabel(EntitySetting.FEED_RATE);
        feedRateSpinner = new UnitSpinner(50, TextFieldUnit.MM_PER_MINUTE, 50d, 10000d, 10d);
        add(feedRateSpinner, FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.FEED_RATE, feedRateSpinner);

        leadInPercentSliderLabel = createAndAddLabel(EntitySetting.LEAD_IN_PERCENT);
        leadInPercentSlider = new JSlider(0, 300, 0);
        leadInPercentSlider.setPaintLabels(true);
        leadInPercentSlider.setPaintTicks(true);
        leadInPercentSlider.setSnapToTicks(true);
        leadInPercentSlider.setMinorTickSpacing(50);
        leadInPercentSlider.setMajorTickSpacing(100);

        add(leadInPercentSlider, SLIDER_FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.LEAD_IN_PERCENT, leadInPercentSlider);

        leadOutPercentSliderLabel = createAndAddLabel(EntitySetting.LEAD_OUT_PERCENT);
        leadOutPercentSlider = new JSlider(0, 300, 0);
        leadOutPercentSlider.setPaintLabels(true);
        leadOutPercentSlider.setPaintTicks(true);
        leadOutPercentSlider.setSnapToTicks(true);
        leadOutPercentSlider.setMinorTickSpacing(50);
        leadOutPercentSlider.setMajorTickSpacing(100);

        add(leadOutPercentSlider, SLIDER_FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.LEAD_OUT_PERCENT, leadOutPercentSlider);

        spindleSpeedLabel = createAndAddLabel(EntitySetting.SPINDLE_SPEED);
        spindleSpeedSpinner = new PercentSpinner(0.5d, 0d);
        add(spindleSpeedSpinner, FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.SPINDLE_SPEED, spindleSpeedSpinner);

        laserPassesLabel = createAndAddLabel(EntitySetting.PASSES);
        passesSlider = new JSlider(0, 10, 1);
        passesSlider.setPaintLabels(true);
        passesSlider.setPaintTicks(true);
        passesSlider.setMinorTickSpacing(1);
        passesSlider.setMajorTickSpacing(5);

        add(passesSlider, SLIDER_FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.PASSES, passesSlider);

        startDepthLabel = createAndAddLabel(EntitySetting.START_DEPTH);
        startDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, -10000d, 10000d, 0.1d);
        startDepthSpinner.setPreferredSize(startDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.START_DEPTH, startDepthSpinner);
        add(startDepthSpinner, FIELD_CONSTRAINTS + ", spanx");

        targetDepthLabel = createAndAddLabel(EntitySetting.TARGET_DEPTH);
        targetDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, -10000d, 10000d, 0.1d);

        targetDepthSpinner.setPreferredSize(targetDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.TARGET_DEPTH, targetDepthSpinner);
        add(targetDepthSpinner, FIELD_CONSTRAINTS + ", spanx");
        setEnabled(false);
        
        targetDepthSpinner.setPreferredSize(targetDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.TARGET_DEPTH, targetDepthSpinner);
        add(targetDepthSpinner, FIELD_CONSTRAINTS + ", spanx");
        setEnabled(false);
        
        includeInExport = new JCheckBox();
        includeInExport.setSelected(true);
        fieldEventDispatcher.registerListener(EntitySetting.INCLUDE_IN_EXPORT, includeInExport);
        includeInExportLabel = createAndAddLabel(EntitySetting.INCLUDE_IN_EXPORT);
        add(includeInExport, FIELD_CONSTRAINTS + ", spanx");
        
    }
    private JLabel includeInExportLabel;
    
    private JCheckBox includeInExport;
    
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
        add(textTextField, FIELD_CONSTRAINTS + ", spanx");

        fontLabel = new JLabel("Font", SwingConstants.RIGHT);
        fontLabel.setVisible(false);
        add(fontLabel, "grow");

        fontDropDown = new FontCombo();
        fieldEventDispatcher.registerListener(EntitySetting.FONT_FAMILY, fontDropDown);
        fontDropDown.setVisible(false);
        add(fontDropDown, FIELD_CONSTRAINTS + ", spanx");

        fontSeparator = new JSeparator(SwingConstants.HORIZONTAL);
        fontSeparator.setVisible(false);
        add(fontSeparator, "hmin 2, grow, spanx, wrap");
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
        // Temporarily disable the field event dispatcher so that it won't trigger updates on select
        fieldEventDispatcher.setEnabled(false);
        onEvent(new EntityEvent(controller.getSelectionManager(), EventType.SELECTED));
        fieldEventDispatcher.setEnabled(true);
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
        model.updateFromEntity(selectionGroup);
        controller.getDrawing().invalidate();
    }

    public void release() {
        this.controller.getSelectionManager().removeSelectionListener(this);
        this.controller.getSelectionManager().removeListener(this);
    }

    @Override
    public void onModelUpdate(EntitySetting entitySetting) {
        Group selectionGroup = controller.getSelectionManager().getSelectionGroup();

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
            // Work around for when the text is being edited this would overwrite it
            if (!textTextField.hasFocus()) {
                textTextField.setText(model.getText());
            }

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
        } else if (entitySetting == EntitySetting.SPINDLE_SPEED) {
            spindleSpeedSpinner.setValue(model.getSpindleSpeed() / 100d);
            selectionGroup.setSpindleSpeed(model.getSpindleSpeed());
        } else if (entitySetting == EntitySetting.PASSES) {
            passesSlider.setValue(model.getPasses());
            selectionGroup.setPasses(model.getPasses());
        } else if (entitySetting == EntitySetting.FEED_RATE) {
            feedRateSpinner.setValue(model.getFeedRate());
            selectionGroup.setFeedRate(model.getFeedRate());
        } else if (entitySetting == EntitySetting.LEAD_IN_PERCENT) {
            leadInPercentSlider.setValue(model.getLeadInPercent());
            selectionGroup.setLeadInPercent(model.getLeadInPercent());
        } else if (entitySetting == EntitySetting.LEAD_OUT_PERCENT) {
            leadOutPercentSlider.setValue(model.getLeadOutPercent());
            selectionGroup.setLeadOutPercent(model.getLeadOutPercent());
        } else if (entitySetting == EntitySetting.INCLUDE_IN_EXPORT) {
            includeInExport.setSelected(model.getIncludeInExport());
            selectionGroup.setIncludeInExport(model.getIncludeInExport());
        }

        handleComponentVisibility(selectionGroup);
    }

    private void handleComponentVisibility(Group selectionGroup) {
        CutType cutType = selectionGroup.getCutType();

        boolean hasCutType = selectionHasSetting(selectionGroup, EntitySetting.CUT_TYPE);
        cutTypeComboBox.setVisible(hasCutType);
        cutTypeLabel.setVisible(hasCutType);

        final boolean hasCutTypeSelection = cutType != CutType.NONE;
        startDepthSpinner.setEnabled(hasCutTypeSelection);
        startDepthLabel.setEnabled(hasCutTypeSelection);
        targetDepthSpinner.setEnabled(hasCutTypeSelection);
        targetDepthLabel.setEnabled(hasCutTypeSelection);
        includeInExport.setVisible(hasCutType);
        includeInExportLabel.setVisible(hasCutType);
        
        boolean isTextCuttable = selectionHasSetting(selectionGroup, EntitySetting.TEXT);
        textTextField.setVisible(isTextCuttable);
        textLabel.setVisible(isTextCuttable);
        fontLabel.setVisible(isTextCuttable);
        fontDropDown.setVisible(isTextCuttable);
        fontSeparator.setVisible(isTextCuttable);

        boolean hasWidth = selectionHasSetting(selectionGroup, EntitySetting.WIDTH);
        widthLabel.setVisible(hasWidth);
        widthTextField.setVisible(hasWidth);

        boolean hasHeight = selectionHasSetting(selectionGroup, EntitySetting.HEIGHT);
        heightLabel.setVisible(hasHeight);
        heightTextField.setVisible(hasHeight);

        boolean hasAnchor = selectionHasSetting(selectionGroup, EntitySetting.ANCHOR);
        anchorSelector.setVisible(hasAnchor);

        boolean hasStartDepth = selectionHasSetting(selectionGroup, EntitySetting.START_DEPTH) &&
                cutType.getSettings().contains(EntitySetting.START_DEPTH);
        startDepthSpinner.setVisible(hasStartDepth);
        startDepthLabel.setVisible(hasStartDepth);

        boolean hasTargetDepth = selectionHasSetting(selectionGroup, EntitySetting.TARGET_DEPTH) &&
                cutType.getSettings().contains(EntitySetting.TARGET_DEPTH);
        targetDepthSpinner.setVisible(hasTargetDepth);
        targetDepthLabel.setVisible(hasTargetDepth);

        boolean hasLaserPower = selectionHasSetting(selectionGroup, EntitySetting.SPINDLE_SPEED) &&
                cutType.getSettings().contains(EntitySetting.SPINDLE_SPEED);
        spindleSpeedLabel.setText(cutType == CutType.LASER_FILL || cutType == CutType.LASER_ON_PATH ? "Power" : EntitySetting.SPINDLE_SPEED.getLabel());
        spindleSpeedLabel.setVisible(hasLaserPower);
        spindleSpeedSpinner.setVisible(hasLaserPower);

        boolean hasLaserPasses = selectionHasSetting(selectionGroup, EntitySetting.PASSES) &&
                cutType.getSettings().contains(EntitySetting.PASSES);
        laserPassesLabel.setVisible(hasLaserPasses);
        passesSlider.setVisible(hasLaserPasses);

        boolean hasFeedRate = selectionHasSetting(selectionGroup, EntitySetting.FEED_RATE) &&
                cutType.getSettings().contains(EntitySetting.FEED_RATE);
        feedRateLabel.setVisible(hasFeedRate);
        feedRateSpinner.setVisible(hasFeedRate);

        boolean hasLeadIn = selectionHasSetting(selectionGroup, EntitySetting.LEAD_IN_PERCENT) &&
                cutType.getSettings().contains(EntitySetting.LEAD_IN_PERCENT);
        leadInPercentSlider.setVisible(hasLeadIn);
        leadInPercentSliderLabel.setVisible(hasLeadIn);

        boolean hasLeadOut = selectionHasSetting(selectionGroup, EntitySetting.LEAD_OUT_PERCENT) &&
                cutType.getSettings().contains(EntitySetting.LEAD_OUT_PERCENT);
        leadOutPercentSlider.setVisible(hasLeadOut);
        leadOutPercentSliderLabel.setVisible(hasLeadOut);

        lockRatioButton.setVisible(hasWidth && hasHeight);
    }
}
