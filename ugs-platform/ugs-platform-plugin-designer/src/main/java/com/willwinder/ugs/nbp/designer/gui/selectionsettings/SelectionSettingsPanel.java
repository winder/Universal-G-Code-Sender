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
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.gui.CutTypeCombo;
import com.willwinder.ugs.nbp.designer.gui.anchor.AnchorSelectorPanel;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings.EntitySettingsComponent;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings.TextSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import com.willwinder.universalgcodesender.uielements.components.PercentSpinner;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.JComponent;
import java.awt.geom.Point2D;
import java.awt.Component;
import java.awt.Container;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

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

    // Section titles and separators
    // Default section is always visible and doesn't need references
    private JLabel entitySectionTitleLabel;
    private JSeparator entitySectionSeparator;
    private JLabel cuttingSectionTitleLabel;
    private JSeparator cuttingSectionSeparator;

    // Default section components
    private TextFieldWithUnit widthTextField;
    private TextFieldWithUnit heightTextField;
    private TextFieldWithUnit rotation;
    private TextFieldWithUnit posXTextField;
    private TextFieldWithUnit posYTextField;
    private AnchorSelectorPanel anchorSelector;
    private JLabel widthLabel;
    private JLabel heightLabel;
    private JToggleButton lockRatioButton;

    // Cutting section components
    private PercentSpinner spindleSpeedSpinner;
    private JLabel spindleSpeedLabel;
    private JLabel laserPassesLabel;
    private JSlider passesSlider;
    private JLabel feedRateLabel;
    private UnitSpinner feedRateSpinner;
    private JLabel cutTypeLabel;
    private CutTypeCombo cutTypeComboBox;
    private JLabel leadInPercentSliderLabel;
    private JSlider leadInPercentSlider;
    private JLabel leadOutPercentSliderLabel;
    private JSlider leadOutPercentSlider;
    private JLabel startDepthLabel;
    private UnitSpinner startDepthSpinner;
    private JLabel targetDepthLabel;
    private UnitSpinner targetDepthSpinner;
    private JLabel includeInExportLabel;
    private JCheckBox includeInExport;

    // Custom entity section components
    // Replaced inline text controls with a dynamic content panel
    private JPanel entityContentPanel;
    // Registry of custom entity settings components
    private final List<EntitySettingsComponent> entityComponents = new ArrayList<>();
    private EntitySettingsComponent activeEntityComponent;
    private PropertyChangeListener activeEntityComponentListener;

    public SelectionSettingsPanel(Controller controller) {
        fieldEventDispatcher = new FieldEventDispatcher();

        // Single grid layout as originally, with labels/fields columns
        setLayout(new MigLayout("hidemode 3, insets 10, gap 10", "[sg label] 10 [grow] 10 [60px]"));

        // Build sections into the same panel
        buildTransformSection();
        // Add custom entity settings section placeholder (initially hidden)
        buildEntitySection();
        buildCuttingSection();

        // Wire controller and listeners
        setController(controller);
        model.addListener(this);
        FieldActionDispatcher fieldActionDispatcher = new FieldActionDispatcher(model, controller);
        fieldEventDispatcher.addListener(fieldActionDispatcher);

        // Discover and register all EntitySettingsComponent implementations via Lookup
        registerEntityComponent(new TextSettingsPanel());


        // Start disabled until a selection is available
        setEnabled(false);

        // Optional: components can also be registered programmatically by callers via registerEntityComponent().
    }

    private void buildEntitySection() {
        addSectionTitleAndSeparator("Custom entity settings", false);
        entityContentPanel = new JPanel(new MigLayout("insets 0, gap 10, fillx", "[grow]"));
        add(entityContentPanel, "spanx, growx, wrap");
        entityContentPanel.setVisible(false);
    }

    // Add a common helper for section headers and separators.
    private void addSectionTitleAndSeparator(String title, boolean visible) {
        JLabel titleLabel = new JLabel(title);
        JSeparator separator = new JSeparator();
        add(titleLabel, "spanx, gaptop 5, gapbottom 0, alignx left, wrap");
        add(separator, "spanx, growx, gaptop 0, gapbottom 5, wrap");
        titleLabel.setVisible(visible);
        separator.setVisible(visible);

        // Keep references for sections that we toggle dynamically
        if ("Custom entity settings".equals(title)) {
            this.entitySectionTitleLabel = titleLabel;
            this.entitySectionSeparator = separator;
        } else if ("Cutting options".equals(title)) {
            this.cuttingSectionTitleLabel = titleLabel;
            this.cuttingSectionSeparator = separator;
        }
    }

    private boolean selectionHasSetting(Group selectionGroup, EntitySetting entitySetting) {
        return selectionGroup.getSettings().contains(entitySetting);
    }

    private void buildTransformSection() {
        addSectionTitleAndSeparator("Transform", true);

        // Position X / Anchor
        createAndAddLabel(EntitySetting.POSITION_X);
        posXTextField = createAndAddField(EntitySetting.POSITION_X, TextFieldUnit.MM, false);
        anchorSelector = new AnchorSelectorPanel();
        anchorSelector.setAnchor(model.getAnchor());
        anchorSelector.addListener((model::setAnchor));
        add(anchorSelector, "span 1 2, grow, wrap");

        // Position Y
        createAndAddLabel(EntitySetting.POSITION_Y);
        posYTextField = createAndAddField(EntitySetting.POSITION_Y, TextFieldUnit.MM, true);

        // Width / lock ratio
        widthLabel = createAndAddLabel(EntitySetting.WIDTH);
        widthTextField = createAndAddField(EntitySetting.WIDTH, TextFieldUnit.MM, false);
        lockRatioButton = new JToggleButton(ImageUtilities.loadImageIcon("img/link.svg", false));
        lockRatioButton.setSelectedIcon(ImageUtilities.loadImageIcon("img/link-off.svg", false));
        lockRatioButton.addActionListener(l -> model.setLockRatio(!lockRatioButton.isSelected()));
        add(lockRatioButton, "span 1 2, growy, wrap");

        // Height
        heightLabel = createAndAddLabel(EntitySetting.HEIGHT);
        heightTextField = createAndAddField(EntitySetting.HEIGHT, TextFieldUnit.MM, true);

        // Rotation
        createAndAddLabel(EntitySetting.ROTATION);
        rotation = createAndAddField(EntitySetting.ROTATION, TextFieldUnit.DEGREE, true);
    }


    /**
     * Allow external registration of custom entity settings components.
     */
    public void registerEntityComponent(EntitySettingsComponent component) {
        if (component != null) {
            entityComponents.add(component);
        }
    }

    private void buildCuttingSection() {
        addSectionTitleAndSeparator("Cutting options", true);

        // Cut type
        cutTypeComboBox = new CutTypeCombo();
        fieldEventDispatcher.registerListener(EntitySetting.CUT_TYPE, cutTypeComboBox);
        cutTypeLabel = createAndAddLabel(EntitySetting.CUT_TYPE);
        add(cutTypeComboBox, FIELD_CONSTRAINTS + ", spanx");

        // Feed rate
        feedRateLabel = createAndAddLabel(EntitySetting.FEED_RATE);
        feedRateSpinner = new UnitSpinner(50, TextFieldUnit.MM_PER_MINUTE, 50d, 10000d, 10d);
        add(feedRateSpinner, FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.FEED_RATE, feedRateSpinner);

        // Lead in
        leadInPercentSliderLabel = createAndAddLabel(EntitySetting.LEAD_IN_PERCENT);
        leadInPercentSlider = new JSlider(0, 300, 0);
        leadInPercentSlider.setPaintLabels(true);
        leadInPercentSlider.setPaintTicks(true);
        leadInPercentSlider.setSnapToTicks(true);
        leadInPercentSlider.setMinorTickSpacing(50);
        leadInPercentSlider.setMajorTickSpacing(100);
        add(leadInPercentSlider, SLIDER_FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.LEAD_IN_PERCENT, leadInPercentSlider);

        // Lead out
        leadOutPercentSliderLabel = createAndAddLabel(EntitySetting.LEAD_OUT_PERCENT);
        leadOutPercentSlider = new JSlider(0, 300, 0);
        leadOutPercentSlider.setPaintLabels(true);
        leadOutPercentSlider.setPaintTicks(true);
        leadOutPercentSlider.setSnapToTicks(true);
        leadOutPercentSlider.setMinorTickSpacing(50);
        leadOutPercentSlider.setMajorTickSpacing(100);
        add(leadOutPercentSlider, SLIDER_FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.LEAD_OUT_PERCENT, leadOutPercentSlider);

        // Spindle speed / laser power
        spindleSpeedLabel = createAndAddLabel(EntitySetting.SPINDLE_SPEED);
        spindleSpeedSpinner = new PercentSpinner(0.5d, 0d);
        add(spindleSpeedSpinner, FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.SPINDLE_SPEED, spindleSpeedSpinner);

        // Passes
        laserPassesLabel = createAndAddLabel(EntitySetting.PASSES);
        passesSlider = new JSlider(0, 10, 1);
        passesSlider.setPaintLabels(true);
        passesSlider.setPaintTicks(true);
        passesSlider.setMinorTickSpacing(1);
        passesSlider.setMajorTickSpacing(5);
        add(passesSlider, SLIDER_FIELD_CONSTRAINTS + ", spanx");
        fieldEventDispatcher.registerListener(EntitySetting.PASSES, passesSlider);

        // Start depth
        startDepthLabel = createAndAddLabel(EntitySetting.START_DEPTH);
        startDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, -10000d, 10000d, 0.1d);
        startDepthSpinner.setPreferredSize(startDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.START_DEPTH, startDepthSpinner);
        add(startDepthSpinner, FIELD_CONSTRAINTS + ", spanx");

        // Target depth
        targetDepthLabel = createAndAddLabel(EntitySetting.TARGET_DEPTH);
        targetDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, -10000d, 10000d, 0.1d);
        targetDepthSpinner.setPreferredSize(targetDepthSpinner.getPreferredSize());
        fieldEventDispatcher.registerListener(EntitySetting.TARGET_DEPTH, targetDepthSpinner);
        add(targetDepthSpinner, FIELD_CONSTRAINTS + ", spanx");

        // Include in export
        includeInExport = new JCheckBox();
        includeInExport.setSelected(true);
        fieldEventDispatcher.registerListener(EntitySetting.INCLUDE_IN_EXPORT, includeInExport);
        includeInExportLabel = createAndAddLabel(EntitySetting.INCLUDE_IN_EXPORT);
        add(includeInExport, FIELD_CONSTRAINTS + ", spanx");
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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        boolean hasCutTypeSelection = enabled && model.getCutType() != null && model.getCutType() != CutType.NONE;
        setEnabledRecursive(this, enabled, hasCutTypeSelection);
    }

    private void setEnabledRecursive(Container container, boolean enabled, boolean hasCutTypeSelection) {
        for (Component c : container.getComponents()) {
            if (c == startDepthSpinner || c == startDepthLabel || c == targetDepthSpinner || c == targetDepthLabel) {
                c.setEnabled(hasCutTypeSelection);
            } else {
                c.setEnabled(enabled);
            }
            if (c instanceof Container child) {
                setEnabledRecursive(child, enabled, hasCutTypeSelection);
            }
        }
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

    private void setController(Controller controller) {
        this.controller = controller;
        this.controller.getSelectionManager().addSelectionListener(this);
        this.controller.getSelectionManager().addListener(this);
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
        if (startDepthSpinner != null) startDepthSpinner.setEnabled(hasCutTypeSelection);
        if (startDepthLabel != null) startDepthLabel.setEnabled(hasCutTypeSelection);
        if (targetDepthSpinner != null) targetDepthSpinner.setEnabled(hasCutTypeSelection);
        if (targetDepthLabel != null) targetDepthLabel.setEnabled(hasCutTypeSelection);
        if (includeInExport != null) includeInExport.setVisible(hasCutType);
        if (includeInExportLabel != null) includeInExportLabel.setVisible(hasCutType);

        // Determine active custom entity component by applicability
        EntitySettingsComponent newActive = entityComponents.stream()
                .filter(c -> c.isApplicable(selectionGroup))
                .findFirst()
                .orElse(null);

        if (newActive != activeEntityComponent) {
            // Unwire old listener
            if (activeEntityComponent != null && activeEntityComponentListener != null) {
                activeEntityComponent.removeChangeListener(activeEntityComponentListener);
            }
            // Clear UI
            clearEntityPanel();
            activeEntityComponent = newActive;
            activeEntityComponentListener = null;

            if (activeEntityComponent != null) {
                // Mount new component
                ensureEntityPanelMounted(activeEntityComponent.getComponent());
                // Wire generic change propagation
                activeEntityComponentListener = evt -> {
                    Group currentGroup = controller.getSelectionManager().getSelectionGroup();
                    activeEntityComponent.applyChangeToSelection(evt.getPropertyName(), evt.getNewValue(), currentGroup);
                };
                activeEntityComponent.addChangeListener(activeEntityComponentListener);
            }
        }

        // Sync UI of active component from current selection
        if (activeEntityComponent != null) {
            activeEntityComponent.setFromSelection(selectionGroup);
            if (entitySectionTitleLabel != null) entitySectionTitleLabel.setVisible(true);
            if (entitySectionSeparator != null) entitySectionSeparator.setVisible(true);
            entityContentPanel.setVisible(true);
        } else {
            if (entitySectionTitleLabel != null) entitySectionTitleLabel.setVisible(false);
            if (entitySectionSeparator != null) entitySectionSeparator.setVisible(false);
            if (entityContentPanel != null) entityContentPanel.setVisible(false);
        }

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

        boolean showCutting = hasCutType || hasStartDepth || hasTargetDepth || hasLaserPower || hasLaserPasses || hasFeedRate || hasLeadIn || hasLeadOut;
        if (cuttingSectionTitleLabel != null) cuttingSectionTitleLabel.setVisible(showCutting);
        if (cuttingSectionSeparator != null) cuttingSectionSeparator.setVisible(showCutting);

        lockRatioButton.setVisible(hasWidth && hasHeight);
    }

    private void ensureEntityPanelMounted(JComponent panel) {
        if (entityContentPanel == null) return;
        if (panel.getParent() != entityContentPanel) {
            entityContentPanel.removeAll();
            entityContentPanel.add(panel, "growx, spanx, wrap");
            entityContentPanel.revalidate();
            entityContentPanel.repaint();
        }
    }

    private void clearEntityPanel() {
        if (entityContentPanel == null) return;
        if (entityContentPanel.getComponentCount() > 0) {
            entityContentPanel.removeAll();
            entityContentPanel.revalidate();
            entityContentPanel.repaint();
        }
    }
}
