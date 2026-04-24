/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

/**
 * Renders a simple icon showing the silhouette of an endmill for the given shape. Drawn with
 * Java2D so we don't need to ship new SVG assets.
 */
public class ToolShapeIcon implements Icon {
    private final EndmillShape shape;
    private final int size;

    public ToolShapeIcon(EndmillShape shape, int size) {
        this.shape = shape;
        this.size = size;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(1.2f));
            g2.setColor(c != null ? c.getForeground() : Color.DARK_GRAY);
            g2.translate(x, y);
            drawShape(g2);
        } finally {
            g2.dispose();
        }
    }

    private void drawShape(Graphics2D g2) {
        int pad = Math.max(1, size / 8);
        int width = size - pad * 2;
        int shankHeight = size / 3;
        int cutterHeight = size - shankHeight - pad * 2;
        int centerX = size / 2;

        // Shank (rectangle at top)
        int shankLeft = centerX - width / 4;
        int shankRight = centerX + width / 4;
        g2.drawRect(shankLeft, pad, shankRight - shankLeft, shankHeight);

        // Cutter silhouette depends on shape
        int cutterTop = pad + shankHeight;
        int cutterBottom = cutterTop + cutterHeight;
        int cutterLeft = centerX - width / 2;
        int cutterRight = centerX + width / 2;

        Shape cutter = switch (shape) {
            case V_BIT -> vBitShape(cutterLeft, cutterRight, cutterTop, cutterBottom, centerX);
            case BALL -> ballShape(cutterLeft, cutterRight, cutterTop, cutterBottom);
            case COMPRESSION -> compressionShape(cutterLeft, cutterRight, cutterTop, cutterBottom);
            default -> rectCutter(cutterLeft, cutterRight, cutterTop, cutterBottom);
        };
        g2.draw(cutter);

        // Flute direction hint
        switch (shape) {
            case UPCUT -> drawFluteArrow(g2, centerX, cutterTop, cutterBottom, true);
            case DOWNCUT -> drawFluteArrow(g2, centerX, cutterTop, cutterBottom, false);
            case STRAIGHT -> g2.drawLine(centerX, cutterTop, centerX, cutterBottom);
            default -> { /* no extra hint */ }
        }
    }

    private Shape rectCutter(int left, int right, int top, int bottom) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(left, top);
        p.lineTo(right, top);
        p.lineTo(right, bottom);
        p.lineTo(left, bottom);
        p.closePath();
        return p;
    }

    private Shape vBitShape(int left, int right, int top, int bottom, int centerX) {
        Path2D.Float p = new Path2D.Float();
        p.moveTo(left, top);
        p.lineTo(right, top);
        p.lineTo(centerX, bottom);
        p.closePath();
        return p;
    }

    private Shape ballShape(int left, int right, int top, int bottom) {
        int diameter = right - left;
        int radius = diameter / 2;
        GeneralPath p = new GeneralPath();
        p.moveTo(left, top);
        p.lineTo(right, top);
        p.lineTo(right, bottom - radius);
        p.curveTo(right, bottom, left, bottom, left, bottom - radius);
        p.closePath();
        return p;
    }

    private Shape compressionShape(int left, int right, int top, int bottom) {
        int mid = (top + bottom) / 2;
        Path2D.Float p = new Path2D.Float();
        p.moveTo(left + 1, top);
        p.lineTo(right - 1, top);
        p.lineTo(right, mid);
        p.lineTo(right - 1, bottom);
        p.lineTo(left + 1, bottom);
        p.lineTo(left, mid);
        p.closePath();
        return p;
    }

    private void drawFluteArrow(Graphics2D g2, int centerX, int top, int bottom, boolean up) {
        int mid = (top + bottom) / 2;
        int arrowSize = Math.max(2, size / 8);
        if (up) {
            g2.drawLine(centerX, top + 1, centerX, bottom - 1);
            g2.drawLine(centerX, top + 1, centerX - arrowSize, top + 1 + arrowSize);
            g2.drawLine(centerX, top + 1, centerX + arrowSize, top + 1 + arrowSize);
        } else {
            g2.drawLine(centerX, top + 1, centerX, bottom - 1);
            g2.drawLine(centerX, bottom - 1, centerX - arrowSize, bottom - 1 - arrowSize);
            g2.drawLine(centerX, bottom - 1, centerX + arrowSize, bottom - 1 - arrowSize);
        }
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
