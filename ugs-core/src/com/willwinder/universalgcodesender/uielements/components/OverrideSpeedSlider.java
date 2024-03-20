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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.firmware.IOverrideManager;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeListener;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class OverrideSpeedSlider extends JPanel {
    private final JSlider slider;

    public OverrideSpeedSlider(IOverrideManager overrideManager, OverrideType type) {
        List<Integer> steps = overrideManager.getSliderSteps(type);
        int minValue = steps.stream().min(Integer::compare).orElse(0);
        int maxValue = steps.stream().max(Integer::compare).orElse(0);

        slider = new JSlider(minValue, maxValue, overrideManager.getSliderDefault(type));
        slider.setMinorTickSpacing(0);
        slider.setMajorTickSpacing(10);

        Dictionary<Integer, JComponent> dict = new Hashtable<>();
        steps.forEach(step -> dict.put(step, new JLabel(step + "%")));
        slider.setLabelTable(dict);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setPaintTrack(true);

        setLayout(new MigLayout("fill, inset 0"));
        add(slider, "grow, w 100::");
    }

    public void addChangeListener(ChangeListener l) {
        slider.addChangeListener(l);
    }

    public int getValue() {
        return slider.getValue();
    }

    public void setValue(int value) {
        slider.setValue(value);
    }
}
