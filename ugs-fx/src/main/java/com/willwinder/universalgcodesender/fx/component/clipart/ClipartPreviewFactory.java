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
package com.willwinder.universalgcodesender.fx.component.clipart;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;

/**
 * Converts the AWT geometry of a designer entity into a JavaFX {@link Path} that is scaled to fit,
 * and centered in, a square preview box. The designer uses a Y-up coordinate system so the shape is
 * flipped vertically to render upright on screen.
 */
public final class ClipartPreviewFactory {
    private ClipartPreviewFactory() {
    }

    public static Path createPreview(Shape shape, double size) {
        Path path = new Path();
        path.setStroke(null);
        path.setFill(Colors.BLACKISH);
        path.setSmooth(true);
        path.setMouseTransparent(true);

        PathIterator iterator = shape.getPathIterator(createFitTransform(shape.getBounds2D(), size));
        path.setFillRule(iterator.getWindingRule() == PathIterator.WIND_EVEN_ODD ? FillRule.EVEN_ODD : FillRule.NON_ZERO);

        double[] coords = new double[6];
        while (!iterator.isDone()) {
            switch (iterator.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO -> path.getElements().add(new MoveTo(coords[0], coords[1]));
                case PathIterator.SEG_LINETO -> path.getElements().add(new LineTo(coords[0], coords[1]));
                case PathIterator.SEG_QUADTO -> path.getElements().add(new QuadCurveTo(coords[0], coords[1], coords[2], coords[3]));
                case PathIterator.SEG_CUBICTO -> path.getElements().add(new CubicCurveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]));
                case PathIterator.SEG_CLOSE -> path.getElements().add(new ClosePath());
                default -> throw new IllegalStateException("Unknown path segment type");
            }
            iterator.next();
        }
        return path;
    }

    private static AffineTransform createFitTransform(Rectangle2D bounds, double size) {
        double largestSide = Math.max(bounds.getWidth(), bounds.getHeight());
        double scale = largestSide > 0 ? size / largestSide : 1;

        AffineTransform transform = new AffineTransform();
        transform.translate(size / 2, size / 2);
        transform.scale(scale, -scale);
        transform.translate(-bounds.getCenterX(), -bounds.getCenterY());
        return transform;
    }
}
