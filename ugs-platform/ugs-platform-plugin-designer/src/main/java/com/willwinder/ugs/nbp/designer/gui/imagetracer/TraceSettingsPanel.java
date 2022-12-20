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

import com.jidesoft.swing.RangeSlider;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class TraceSettingsPanel extends JPanel {
    private final JSlider colors = new JSlider(2, 10, 5);
    private final JSlider lineThreshold = new JSlider(0, 100, 0);
    private final JSlider curveThreshold = new JSlider(0, 100, 0);
    private final JSlider colorsQuantization = new JSlider(1, 100, 1);
    private final JSlider pathOmit = new JSlider(0, 100, 0);
    private final JSlider blurRadius = new JSlider(0, 5, 0);
    private final JSlider blurDelta = new JSlider(0, 1014, 0);
    private final RangeSlider colorRange = new RangeSlider(0, 255, 0, 255);
    private final List<ChangeListener> changeListeners = new ArrayList<>();

    public TraceSettingsPanel() {
        setLayout(new MigLayout("fill, insets 0, wrap 1"));

        colors.addChangeListener(this::updateValues);
        colors.setSnapToTicks(true);
        add(new JLabel("Number of layers"));
        add(colors);

        colorRange.addChangeListener(this::updateValues);
        colorRange.setMinorTickSpacing(10);
        colorRange.setSnapToTicks(true);
        add(new JLabel("Color range"));
        add(colorRange);

        colorsQuantization.addChangeListener(this::updateValues);
        colorsQuantization.setSnapToTicks(true);
        add(new JLabel("Color quantization"));
        add(colorsQuantization);

        add(new JSeparator(SwingConstants.HORIZONTAL), "grow");

        lineThreshold.addChangeListener(this::updateValues);
        lineThreshold.setMinorTickSpacing(5);
        lineThreshold.setSnapToTicks(true);
        add(new JLabel("Smooth lines"));
        add(lineThreshold);

        curveThreshold.addChangeListener(this::updateValues);
        curveThreshold.setMinorTickSpacing(5);
        curveThreshold.setSnapToTicks(true);
        add(new JLabel("Smooth curves"));
        add(curveThreshold);

        pathOmit.addChangeListener(this::updateValues);
        pathOmit.setSnapToTicks(true);
        add(new JLabel("Filter noise"));
        add(pathOmit);

        add(new JSeparator(SwingConstants.HORIZONTAL), "grow");


        blurRadius.addChangeListener(this::updateValues);
        blurRadius.setSnapToTicks(true);
        add(new JLabel("Blur radius"));
        add(blurRadius);

        blurDelta.addChangeListener(this::updateValues);
        blurDelta.setSnapToTicks(true);
        add(new JLabel("Blur delta"));
        add(blurDelta);
    }

    private void updateValues(ChangeEvent e) {
            if(colorRange.getHighValue() == colorRange.getMinimum()) {
                colorRange.setHighValue(colorRange.getMinimum() + 20);
            } else if(colorRange.getLowValue() == colorRange.getMaximum()) {
                colorRange.setLowValue(colorRange.getMaximum() - 20);
            }

        if (!((JSlider) e.getSource()).getValueIsAdjusting()) {
            changeListeners.forEach(l -> l.stateChanged(e));
        }
    }

    public TraceSettings getSettings() {
        TraceSettings settings =  new TraceSettings();
        settings.setLineThreshold(Integer.valueOf(lineThreshold.getValue()).floatValue() / 10f);
        settings.setQuadThreshold(Integer.valueOf(curveThreshold.getValue()).floatValue() / 10f);
        settings.setPathOmit(pathOmit.getValue());
        settings.setNumberOfColors(colors.getValue());
        settings.setColorQuantize(colorsQuantization.getValue());
        settings.setBlurRadius(blurRadius.getValue());
        settings.setBlurDelta(blurDelta.getValue());
        settings.setStartColor(colorRange.getLowValue());
        settings.setEndColor(colorRange.getHighValue());
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
        colorRange.setEnabled(enabled);
    }

    public void addListener(ChangeListener listener) {
        changeListeners.add(listener);
    }
}
