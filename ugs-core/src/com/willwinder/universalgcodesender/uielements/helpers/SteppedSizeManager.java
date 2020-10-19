/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import com.willwinder.universalgcodesender.utils.ThreadHelper;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Track size of a component and fire event when nominal size changes.
 */
public class SteppedSizeManager {

    private int currentSize = -1;
    private JComponent component;
    private List<Dimension> dimensions;
    private List<SteppedSizeChangeListener> listeners = new ArrayList<>();

    /**
     * Pass in list of "ascending" Dimensions representing thresholds to larger nominal sizes.
     */
    public SteppedSizeManager(JComponent component, Dimension... dimensions) {
        this.component = component;
        this.dimensions = new ArrayList<>(Arrays.asList(dimensions));
        component.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent event) {
                determineNominalSize();
            }
        });
    }

    /**
     * Compare current dimensions with those representing nominal thresholds.
     */
    private void determineNominalSize() {
        int previousSize = currentSize;
        currentSize = 0;
        int width = component.getWidth();
        int height = component.getHeight();
        // increment size for each threshold exceeded
        dimensions.forEach(dimension -> {
            if (width > dimension.width && height > dimension.height) currentSize++;
        });
        if (previousSize != currentSize) fireSizeChanged(currentSize);
    }

    private void fireSizeChanged(int size) {
        listeners.forEach(l ->
                ThreadHelper.invokeLater(() ->
                        l.onSizeChange(size)));
    }

    public void addListener(SteppedSizeChangeListener listener) {
        listeners.add(listener);
    }

    public interface SteppedSizeChangeListener {
        void onSizeChange(int size);
    }
}
