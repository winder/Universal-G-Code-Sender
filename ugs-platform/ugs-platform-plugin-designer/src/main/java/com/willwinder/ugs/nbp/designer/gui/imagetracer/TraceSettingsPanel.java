/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.imagetracer;

import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import java.awt.Dimension;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Joacim Breiler
 */
public class TraceSettingsPanel extends JPanel {

    private final JSlider colors = new JSlider(2, 100, 25);
    private final JSlider lineThreshold = new JSlider(0, 100, 0);
    private final JSlider curveThreshold = new JSlider(0, 100, 0);
    private final JSlider colorsQuantization = new JSlider(1, 100, 1);
    private final JSlider pathOmit = new JSlider(0, 100, 0);
    private final JSlider blurRadius = new JSlider(0, 5, 0);
    private final JSlider blurDelta = new JSlider(0, 1014, 0);
    private final JSlider startColor = new JSlider(0, 255,0);
    private final JSlider endColor = new JSlider(0, 255,255);
    private final JCheckBox enableAdvanced = new JCheckBox("Advanced Depth Map", true);
    
    private final JCheckBox invertDepthmap = new JCheckBox("Invert Z-Axis", false);
    private final JCheckBox cutLayerContents = new JCheckBox("Cut Layer Contents", false);
    private final TextFieldWithUnit minimumDetailSize = new TextFieldWithUnit(TextFieldUnit.MM, 2, 1);
    
    private final List<ChangeListener> changeListeners = new ArrayList<>();
    private final TextFieldWithUnit startDepth = new TextFieldWithUnit(TextFieldUnit.MM, 2, 0 /* Default Value Here*/);
    private final TextFieldWithUnit targetDepth = new TextFieldWithUnit(TextFieldUnit.MM, 2, 10 /* Default Value Here*/);

    private final JLabel lblColors = new JLabel("Layer Count");
    private final JLabel lblColorsStart = new JLabel("Color range start");
    private final JLabel lblColorsEnd = new JLabel("Color range start");
    private final JLabel lblColorsQuantization = new JLabel("Color quantization");
    private final JLabel lblThreshold = new JLabel("Smooth lines");

    private final JLabel lblCurveThreshold = new JLabel("Smooth curves");
    private final JLabel lblPathOmit = new JLabel("Filter noise");
    private final JLabel lblBlurRadius = new JLabel("Blur radius");
    private final JLabel lblBlurDelta = new JLabel("Blur delta");
    private final JLabel lblStartDepth = new JLabel("Start Depth");
    private final JLabel lblTargetDepth = new JLabel("Target Depth");
    private final JLabel lblMinDetailSize = new JLabel("Min. Detail Size");

        
    public TraceSettingsPanel() {
        setLayout(new MigLayout("insets 0, wrap 1"));
        
        add(lblColors);
        add(setupSlider(colors), "grow, wrap");
//        lblColors.setText("Layer Count (" + colors.getValue() + ")");
        
        add(lblColorsStart);
        add(setupSlider(startColor,10), "grow, wrap");

        add(lblColorsEnd);
        add(setupSlider(endColor,10), "grow, wrap");

        add(lblColorsQuantization);
        add(setupSlider(colorsQuantization),  "grow, wrap");

        add(new JSeparator(SwingConstants.HORIZONTAL), "grow, wrap");

        add(lblThreshold);
        add(setupSlider(lineThreshold,5), "grow, wrap");

        add(lblCurveThreshold);
        add(setupSlider(curveThreshold,5),  "grow, wrap");

        add(lblPathOmit);
        add(setupSlider(pathOmit),  "grow, wrap");

        add(new JSeparator(SwingConstants.HORIZONTAL), "grow, wrap");

        add(lblBlurRadius);
        add(setupSlider(blurRadius), "grow, wrap");

        add(lblBlurDelta);
        add(setupSlider(blurDelta), "grow, wrap");
        
        add(new JSeparator(SwingConstants.HORIZONTAL), "grow, wrap");
        
        enableAdvanced.addChangeListener(this::updateValues);        
        add(enableAdvanced, "grow, wrap");
        
        invertDepthmap.addChangeListener(this::updateValues);        
        add(invertDepthmap, "grow, wrap");
        
//        cutLayerContents.addChangeListener(this::updateValues);        
//        add(cutLayerContents, "grow, wrap");
                
        add(lblStartDepth, "split 2");
        add(startDepth, "grow, wrap");

        add(lblTargetDepth, "split 2");
        add(targetDepth, "grow, wrap");
        
        minimumDetailSize.getDocument().addDocumentListener(this.updateValuesDocListener());   
        add(lblMinDetailSize, "split 2");
        add(minimumDetailSize, "grow, wrap");
        updateValues(null);
    }
    private JSlider setupSlider(JSlider slider) {
        return setupSlider(slider,-1);
    }
    private JSlider setupSlider(JSlider slider,int value) {
        if (value != -1) {
            curveThreshold.setMinorTickSpacing(value);
        }
        slider.addChangeListener(this::updateValues);
        slider.setSnapToTicks(true);            
        return slider;
    } 
    private DocumentListener updateValuesDocListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    changedUpdate(e);
                });
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    changedUpdate(e);
                });
            }

            @Override
            public void changedUpdate(DocumentEvent e) {         
                ChangeEvent ce = new ChangeEvent(this);
                updateValues(ce);
            }
        };

    }
    private void updateValues(ChangeEvent e) {
        if (e != null) {
            if (e.getSource() == endColor && endColor.getValue() <= startColor.getValue()) {
                startColor.setValue(endColor.getValue() - 10);
            }

            if (e.getSource() == startColor && startColor.getValue() >= endColor.getValue()) {
                endColor.setValue(startColor.getValue() + 10);
            }
            if (e.getSource() instanceof JSlider jSlider) {
                if (!jSlider.getValueIsAdjusting()) {
                    changeListeners.forEach(l -> l.stateChanged(e));
                }
            } else {
                changeListeners.forEach(l -> l.stateChanged(e));
            }
        }
        lblColors.setText("Layer Count" + getFormattedString(colors));
        lblColorsStart.setText("Color range start" + getFormattedString(startColor));
        lblColorsEnd.setText("Color range end" + getFormattedString(endColor));
        lblColorsQuantization.setText("Color quantization" + getFormattedString(colorsQuantization));
        lblThreshold.setText("Smooth lines " + getFormattedString(lineThreshold));
        lblCurveThreshold.setText("Smooth curves" + getFormattedString(curveThreshold));
        lblPathOmit.setText("Filter noise" + getFormattedString(pathOmit));
        lblBlurRadius.setText("Blur radius" + getFormattedString(blurRadius));
        lblBlurDelta.setText("Blur delta" + getFormattedString(blurDelta));
                
        // Make sure new controls toggle appropriately
        setEnabled(isEnabled());
        if (minimumDetailSize.getDoubleValue() < 1) {
            minimumDetailSize.setDoubleValue(1.0);            
        }
    }

    public String getFormattedString(JSlider ctrl) {
        return switch (("" + ctrl.getValue()).length()) {
            default ->
                " (" + ctrl.getValue() + ")";
        };
    }

    public TraceSettings getSettings() {
        TraceSettings settings = new TraceSettings();
        settings.setLineThreshold(Integer.valueOf(lineThreshold.getValue()).floatValue() / 10f);
        settings.setQuadThreshold(Integer.valueOf(curveThreshold.getValue()).floatValue() / 10f);
        settings.setPathOmit(pathOmit.getValue());
        settings.setNumberOfColors(colors.getValue());
        settings.setColorQuantize(colorsQuantization.getValue());
        settings.setBlurRadius(blurRadius.getValue());
        settings.setBlurDelta(blurDelta.getValue());
        settings.setStartColor(startColor.getValue());
        settings.setEndColor(endColor.getValue());
        
        settings.setStartDepth(startDepth.getDoubleValue());
        settings.setTargetDepth(targetDepth.getDoubleValue());
        settings.setEnableAdvancedMode(enableAdvanced.isSelected());
        settings.setInvertedColorMap(invertDepthmap.isSelected());
        
        settings.setCutLayerContents(cutLayerContents.isSelected()); // TODO REMOVE. 
                
        settings.setMinimumDetailSize(minimumDetailSize.getDoubleValue());
        return settings;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        lineThreshold.setEnabled(enabled);
        curveThreshold.setEnabled(enabled);
        pathOmit.setEnabled(enabled);
        colors.setEnabled(enabled);
        colorsQuantization.setEnabled(enabled);
        blurRadius.setEnabled(enabled);
        blurDelta.setEnabled(enabled);
        startColor.setEnabled(enabled);
        endColor.setEnabled(enabled);
        enableAdvanced.setEnabled(enabled);
        startDepth.setEnabled(enabled && enableAdvanced.isSelected());
        targetDepth.setEnabled(enabled && enableAdvanced.isSelected());
        invertDepthmap.setEnabled(enabled && enableAdvanced.isSelected());
        cutLayerContents.setEnabled(enabled && enableAdvanced.isSelected());
        minimumDetailSize.setEnabled(enabled && enableAdvanced.isSelected());
        lblStartDepth.setEnabled(enabled && enableAdvanced.isSelected());
        lblTargetDepth.setEnabled(enabled && enableAdvanced.isSelected());
        lblMinDetailSize.setEnabled(enabled && enableAdvanced.isSelected());
    }

    public void addListener(ChangeListener listener) {
        changeListeners.add(listener);
    }
}
