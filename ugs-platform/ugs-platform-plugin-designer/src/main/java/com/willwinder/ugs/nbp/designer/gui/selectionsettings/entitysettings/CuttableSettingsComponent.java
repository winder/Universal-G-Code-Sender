/*
    Copyright 2024 Albert Giro Quer

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

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.gui.CutTypeCombo;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.components.PercentSpinner;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;
import net.miginfocom.swing.MigLayout;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

@ServiceProvider(service = EntitySettingsComponent.class, position = 10)
public class CuttableSettingsComponent extends JPanel implements EntitySettingsComponent {
    
    public static final String PROP_CUT_TYPE = "cutType";
    public static final String PROP_START_DEPTH = "startDepth";
    public static final String PROP_TARGET_DEPTH = "targetDepth";
    public static final String PROP_SPINDLE_SPEED = "spindleSpeed";
    public static final String PROP_PASSES = "passes";
    public static final String PROP_FEED_RATE = "feedRate";
    public static final String PROP_LEAD_IN_PERCENT = "leadInPercent";
    public static final String PROP_LEAD_OUT_PERCENT = "leadOutPercent";
    public static final String PROP_INCLUDE_IN_EXPORT = "includeInExport";

    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 36, wrap, spanx";
    private static final String SLIDER_CONSTRAINTS = "grow, w 60:60:300, hmin 32, hmax 44, wrap, spanx";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private CutTypeCombo cutTypeComboBox;
    private UnitSpinner startDepthSpinner;
    private UnitSpinner targetDepthSpinner;
    private PercentSpinner spindleSpeedSpinner;
    private JSlider passesSlider;
    private UnitSpinner feedRateSpinner;
    private JSlider leadInPercentSlider;
    private JSlider leadOutPercentSlider;
    private JCheckBox includeInExport;

    private boolean updating = false;

    public CuttableSettingsComponent() {
        super(new MigLayout("insets 0, gap 10, fillx", "[sg label,right] 10 [grow]"));
        initializeComponents();
        buildLayout();
        setupListeners();
    }

    private void initializeComponents() {
        cutTypeComboBox = new CutTypeCombo();
        startDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, -10000d, 10000d, 0.1d);
        targetDepthSpinner = new UnitSpinner(0, TextFieldUnit.MM, -10000d, 10000d, 0.1d);
        spindleSpeedSpinner = new PercentSpinner(0.5d, 0d);
        feedRateSpinner = new UnitSpinner(50, TextFieldUnit.MM_PER_MINUTE, 50d, 10000d, 10d);
        
        passesSlider = createSlider(0, 10, 1, 1, 5);
        leadInPercentSlider = createSlider(0, 300, 0, 50, 100);
        leadOutPercentSlider = createSlider(0, 300, 0, 50, 100);

        includeInExport = new JCheckBox();
        includeInExport.setSelected(true);
    }

    private JSlider createSlider(int min, int max, int value, int minorTick, int majorTick) {
        JSlider slider = new JSlider(min, max, value);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setSnapToTicks(true);
        slider.setMinorTickSpacing(minorTick);
        slider.setMajorTickSpacing(majorTick);
        return slider;
    }

    private void buildLayout() {
        add(new JLabel("Cutting Options", SwingConstants.LEFT), "spanx, gaptop 5, gapbottom 0, wrap");
        add(new JSeparator(), "spanx, growx, gaptop 0, gapbottom 5, wrap");

        addLabeledComponent("Cut Type", cutTypeComboBox);
        addLabeledComponent("Start Depth", startDepthSpinner);
        addLabeledComponent("Target Depth", targetDepthSpinner);
        addLabeledComponent("Spindle Speed", spindleSpeedSpinner);
        addLabeledComponent("Feed Rate", feedRateSpinner);
        addLabeledSlider("Passes", passesSlider);
        addLabeledSlider("Lead In %", leadInPercentSlider);
        addLabeledSlider("Lead Out %", leadOutPercentSlider);
        addLabeledComponent("Include in Export", includeInExport);
    }

    private void addLabeledComponent(String labelText, JComponent component) {
        add(new JLabel(labelText, SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(component, FIELD_CONSTRAINTS);
    }

    private void addLabeledSlider(String labelText, JSlider slider) {
        add(new JLabel(labelText, SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(slider, SLIDER_CONSTRAINTS);
    }

    private void setupListeners() {
        cutTypeComboBox.addActionListener(e -> firePropertyChange(PROP_CUT_TYPE, cutTypeComboBox.getSelectedItem()));
        startDepthSpinner.addPropertyChangeListener("value", evt -> firePropertyChange(PROP_START_DEPTH, evt.getNewValue()));
        targetDepthSpinner.addPropertyChangeListener("value", evt -> firePropertyChange(PROP_TARGET_DEPTH, evt.getNewValue()));
        spindleSpeedSpinner.addPropertyChangeListener("value", evt -> {
            Double value = (Double) evt.getNewValue();
            firePropertyChange(PROP_SPINDLE_SPEED, (int)(value * 100));
        });
        feedRateSpinner.addPropertyChangeListener("value", evt -> firePropertyChange(PROP_FEED_RATE, evt.getNewValue()));
        passesSlider.addChangeListener(e -> firePropertyChange(PROP_PASSES, passesSlider.getValue()));
        leadInPercentSlider.addChangeListener(e -> firePropertyChange(PROP_LEAD_IN_PERCENT, leadInPercentSlider.getValue()));
        leadOutPercentSlider.addChangeListener(e -> firePropertyChange(PROP_LEAD_OUT_PERCENT, leadOutPercentSlider.getValue()));
        includeInExport.addActionListener(e -> firePropertyChange(PROP_INCLUDE_IN_EXPORT, includeInExport.isSelected()));
    }

    private void firePropertyChange(String propertyName, Object newValue) {
        if (!updating) {
            pcs.firePropertyChange(propertyName, null, newValue);
        }
    }

    public String getTitle() {
        return "Cutting Options";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Component[] components = {cutTypeComboBox, startDepthSpinner, targetDepthSpinner,
                                spindleSpeedSpinner, feedRateSpinner, passesSlider,
                                leadInPercentSlider, leadOutPercentSlider, includeInExport};
        for (Component component : components) {
            if (component != null) component.setEnabled(enabled);
        }
    }

    @Override
    public boolean isApplicable(Group selectionGroup) {
        return selectionGroup.getChildren().stream()
                .allMatch(Cuttable.class::isInstance);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setFromSelection(Group selectionGroup) {
        Cuttable firstCuttable = selectionGroup.getChildren().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .findFirst()
                .orElse(null);
                
        if (firstCuttable != null) {
            updating = true;
            try {
                cutTypeComboBox.setSelectedItem(firstCuttable.getCutType());
                startDepthSpinner.setValue(firstCuttable.getStartDepth());
                targetDepthSpinner.setValue(firstCuttable.getTargetDepth());
                spindleSpeedSpinner.setValue(firstCuttable.getSpindleSpeed() / 100.0);
                feedRateSpinner.setValue(firstCuttable.getFeedRate());
                passesSlider.setValue(firstCuttable.getPasses());
                leadInPercentSlider.setValue(firstCuttable.getLeadInPercent());
                leadOutPercentSlider.setValue(firstCuttable.getLeadOutPercent());
                updateLabelsForCutType(firstCuttable.getCutType());
            } finally {
                updating = false;
            }
        }
    }

    private void updateLabelsForCutType(CutType cutType) {
        for (Component comp : getComponents()) {
            if (comp instanceof JLabel label && ("Spindle Speed".equals(label.getText()) || "Power".equals(label.getText()))) {
                label.setText((cutType == CutType.LASER_FILL || cutType == CutType.LASER_ON_PATH) ? "Power" : "Spindle Speed");
                break;
            }
        }
    }

    @Override
    public void applyChangeToSelection(String propertyName, Object newValue, Group selectionGroup) {
        selectionGroup.getChildren().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .forEach(cuttable -> applyCuttableProperty(cuttable, propertyName, newValue));
    }

    private void applyCuttableProperty(Cuttable cuttable, String propertyName, Object newValue) {
        switch (propertyName) {
            case PROP_CUT_TYPE -> cuttable.setCutType((CutType) newValue);
            case PROP_START_DEPTH -> cuttable.setStartDepth((Double) newValue);
            case PROP_TARGET_DEPTH -> cuttable.setTargetDepth((Double) newValue);
            case PROP_SPINDLE_SPEED -> cuttable.setSpindleSpeed((Integer) newValue);
            case PROP_FEED_RATE -> cuttable.setFeedRate((Integer) newValue);
            case PROP_PASSES -> cuttable.setPasses((Integer) newValue);
            case PROP_LEAD_IN_PERCENT -> cuttable.setLeadInPercent((Integer) newValue);
            case PROP_LEAD_OUT_PERCENT -> cuttable.setLeadOutPercent((Integer) newValue);
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
}
