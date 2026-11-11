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

import com.willwinder.ugs.designer.constraint.DimensionLine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Paints dimensions, snap markers and readouts on a drawing, which is in design units with the
 * y axis pointing up. Sizes are given in pixels and scaled with the drawing's zoom.
 */
public final class DimensionPainter {
    private static final double SNAP_MARKER_PX = 7;
    private static final double READOUT_OFFSET_PX = 14;
    private static final Color LABEL_BACKGROUND = new Color(255, 255, 255, 217);
    private static final Color READOUT_COLOR = new Color(48, 48, 48);
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    private DimensionPainter() {
    }

    /**
     * Paints the extension lines, the dimension line with its arrowheads and the label, or the
     * line and marks of an alignment.
     */
    public static void paintDimension(Graphics2D graphics, Drawing drawing, DimensionLine dimension, boolean highlighted) {
        double worldPerPixel = 1 / drawing.getScale();
        graphics.setColor(highlighted ? Colors.DIMENSION_HIGHLIGHT : Colors.DIMENSION);
        graphics.setStroke(new BasicStroke((float) worldPerPixel));
        dimension.lines(worldPerPixel).forEach(line -> graphics.draw(new Line2D.Double(line[0], line[1])));
        dimension.arrowheads(worldPerPixel).forEach(triangle -> graphics.fill(polygon(triangle)));

        if (!dimension.hasLabel()) {
            return;
        }
        Color labelColor = highlighted ? Colors.DIMENSION_HIGHLIGHT : Colors.DIMENSION_LABEL;
        dimension.labelPositions(worldPerPixel).forEach(center -> paintLabel(graphics, drawing, dimension.label(), center, 0, 0, labelColor));
    }

    /**
     * Paints a point that can be picked as a small square, filled when it is hovered or picked.
     */
    public static void paintSnapMarker(Graphics2D graphics, Drawing drawing, Point2D position, boolean highlighted) {
        double size = SNAP_MARKER_PX / drawing.getScale();
        Rectangle2D square = new Rectangle2D.Double(position.getX() - size / 2, position.getY() - size / 2, size, size);
        graphics.setColor(highlighted ? Colors.CONTROL_BORDER : Color.WHITE);
        graphics.fill(square);
        graphics.setColor(Colors.CONTROL_BORDER);
        graphics.setStroke(new BasicStroke((float) (1 / drawing.getScale())));
        graphics.draw(square);
    }

    /**
     * Paints a text beside the cursor, such as the distance of a dimension being placed.
     */
    public static void paintReadout(Graphics2D graphics, Drawing drawing, String text, Point2D cursor) {
        double centerX = READOUT_OFFSET_PX + DimensionLine.labelWidthPx(text) / 2;
        double centerY = -READOUT_OFFSET_PX - DimensionLine.LABEL_HEIGHT_PX / 2;
        paintLabel(graphics, drawing, text, cursor, centerX, centerY, READOUT_COLOR);
    }

    // The label is centered at centerX pixels to the right of and centerY pixels below the position
    private static void paintLabel(Graphics2D graphics, Drawing drawing, String text, Point2D position,
                                   double centerX, double centerY, Color color) {
        double worldPerPixel = 1 / drawing.getScale();
        Graphics2D labelGraphics = (Graphics2D) graphics.create();
        try {
            // Draws in pixels with the y axis pointing down, as text is drawn
            labelGraphics.translate(position.getX(), position.getY());
            labelGraphics.scale(worldPerPixel, -worldPerPixel);
            double width = DimensionLine.labelWidthPx(text);
            double height = DimensionLine.LABEL_HEIGHT_PX;
            labelGraphics.setColor(LABEL_BACKGROUND);
            labelGraphics.fill(new RoundRectangle2D.Double(centerX - width / 2, centerY - height / 2, width, height, 4, 4));
            labelGraphics.setFont(FONT);
            labelGraphics.setColor(color);
            FontMetrics metrics = labelGraphics.getFontMetrics();
            float textX = (float) (centerX - metrics.stringWidth(text) / 2.0);
            float textY = (float) (centerY + (metrics.getAscent() - metrics.getDescent()) / 2.0);
            labelGraphics.drawString(text, textX, textY);
        } finally {
            labelGraphics.dispose();
        }
    }

    private static Path2D polygon(Point2D[] points) {
        Path2D.Double polygon = new Path2D.Double();
        polygon.moveTo(points[0].getX(), points[0].getY());
        for (int i = 1; i < points.length; i++) {
            polygon.lineTo(points[i].getX(), points[i].getY());
        }
        polygon.closePath();
        return polygon;
    }
}
