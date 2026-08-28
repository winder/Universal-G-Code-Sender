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

import com.willwinder.ugs.designer.entities.EntitySetting;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.io.gcode.toolpaths.Tabs;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the stretches of a shape that are left as tabs, so that they can be drawn on top of the
 * shape and show where it will stay attached to the stock.
 * <p>
 * The tabs are placed along the outline of the shape as it was drawn. A cut that follows the inside
 * or the outside of the outline is offset by the radius of the tool, which moves its tabs by the
 * same amount without changing how many there are or where along the shape they sit.
 *
 * @author Joacim Breiler
 */
public final class TabShapes {

    private TabShapes() {
    }

    /**
     * Returns the stretches of the given shape that are left as tabs, as a path of separate
     * sub-paths. The path is empty when the shape is not cut with tabs.
     *
     * @param cuttable the shape to find the tabs of
     * @param settings the settings deciding how long a tab may be
     * @return the tabs of the shape
     */
    public static Shape create(Cuttable cuttable, Settings settings) {
        Path2D.Double tabs = new Path2D.Double();
        if (!cuttable.hasTabs() || !cuttable.getCutType().getSettings().contains(EntitySetting.TABS)) {
            return tabs;
        }

        for (List<PartialPosition> contour : toContours(cuttable.getShape(), settings.getFlatnessPrecision())) {
            Tabs.split(contour, cuttable.getTabCount(), settings.getTabLength()).stream()
                    .filter(Tabs.Section::tab)
                    .forEach(section -> append(tabs, section.coordinates()));
        }
        return tabs;
    }

    /**
     * Splits the shape into its separate contours, each of which is given its own set of tabs the
     * same way each part of the generated tool path is.
     */
    private static List<List<PartialPosition>> toContours(Shape shape, double flatness) {
        List<List<PartialPosition>> contours = new ArrayList<>();
        List<PartialPosition> current = new ArrayList<>();
        double startX = 0;
        double startY = 0;

        PathIterator iterator = shape.getPathIterator(null, flatness);
        double[] coordinates = new double[6];
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coordinates)) {
                case PathIterator.SEG_MOVETO -> {
                    addContour(contours, current);
                    current = new ArrayList<>();
                    startX = coordinates[0];
                    startY = coordinates[1];
                    current.add(position(startX, startY));
                }
                case PathIterator.SEG_LINETO -> current.add(position(coordinates[0], coordinates[1]));
                case PathIterator.SEG_CLOSE -> current.add(position(startX, startY));
                default -> {
                    // The path is flattened, so there are no curves left to handle
                }
            }
            iterator.next();
        }

        addContour(contours, current);
        return contours;
    }

    private static void addContour(List<List<PartialPosition>> contours, List<PartialPosition> contour) {
        if (contour.size() > 1) {
            contours.add(contour);
        }
    }

    private static PartialPosition position(double x, double y) {
        return new PartialPosition(x, y, UnitUtils.Units.MM);
    }

    private static void append(Path2D path, List<PartialPosition> coordinates) {
        path.moveTo(coordinates.get(0).getX(), coordinates.get(0).getY());
        coordinates.stream()
                .skip(1)
                .forEach(coordinate -> path.lineTo(coordinate.getX(), coordinate.getY()));
    }
}
