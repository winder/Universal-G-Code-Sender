/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.awt.Shape;
import java.awt.geom.PathIterator;

public class TabShapesTest {

    @Test
    public void create_shouldReturnOneStretchPerTab() {
        Rectangle rectangle = tabbedRectangle(CutType.ON_PATH, 4);
        Settings settings = new Settings();
        settings.setTabLength(4);

        Shape tabs = TabShapes.create(rectangle, settings);

        Assertions.assertThat(countStretches(tabs)).isEqualTo(4);
        Assertions.assertThat(totalLength(tabs)).isEqualTo(4 * 4, Assertions.within(0.1));
    }

    @Test
    public void create_shouldShortenTabsThatWouldNotFitAroundTheShape() {
        Rectangle rectangle = tabbedRectangle(CutType.OUTSIDE_PATH, 4);
        Settings settings = new Settings();
        settings.setTabLength(20);

        Shape tabs = TabShapes.create(rectangle, settings);

        // Half of the 40mm perimeter is left to be cut, leaving 5mm for each of the four tabs
        Assertions.assertThat(countStretches(tabs)).isEqualTo(4);
        Assertions.assertThat(totalLength(tabs)).isEqualTo(20, Assertions.within(0.1));
    }

    @Test
    public void create_shouldReturnNothingWhenTabsAreTurnedOff() {
        Rectangle rectangle = tabbedRectangle(CutType.ON_PATH, 4);
        rectangle.setTabs(false);

        Shape tabs = TabShapes.create(rectangle, new Settings());

        Assertions.assertThat(tabs.getPathIterator(null).isDone()).isTrue();
    }

    @Test
    public void create_shouldReturnNothingWhenTheCutTypeCanNotLeaveTabs() {
        Rectangle rectangle = tabbedRectangle(CutType.POCKET, 4);

        Shape tabs = TabShapes.create(rectangle, new Settings());

        Assertions.assertThat(tabs.getPathIterator(null).isDone()).isTrue();
    }

    private static Rectangle tabbedRectangle(CutType cutType, int tabCount) {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setCutType(cutType);
        rectangle.setTabs(true);
        rectangle.setTabCount(tabCount);
        return rectangle;
    }

    private static int countStretches(Shape shape) {
        int stretches = 0;
        PathIterator iterator = shape.getPathIterator(null);
        double[] coordinates = new double[6];
        while (!iterator.isDone()) {
            if (iterator.currentSegment(coordinates) == PathIterator.SEG_MOVETO) {
                stretches++;
            }
            iterator.next();
        }
        return stretches;
    }

    private static double totalLength(Shape shape) {
        double length = 0;
        double previousX = 0;
        double previousY = 0;
        PathIterator iterator = shape.getPathIterator(null);
        double[] coordinates = new double[6];
        while (!iterator.isDone()) {
            if (iterator.currentSegment(coordinates) == PathIterator.SEG_LINETO) {
                length += Math.hypot(coordinates[0] - previousX, coordinates[1] - previousY);
            }
            previousX = coordinates[0];
            previousY = coordinates[1];
            iterator.next();
        }
        return length;
    }
}
