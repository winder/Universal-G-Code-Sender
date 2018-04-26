/*
    Copyright 2018 Will Winder

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

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

/**
 * A rounded border
 *
 * @author Joacim Breiler
 */
public class RoundedBorder extends AbstractBorder {

    private final Color color;
    private final int radius;
    private final BasicStroke stroke;

    public RoundedBorder(Color color, int radius) {
        this.color = color;
        this.radius = radius;
        this.stroke = new BasicStroke(1);

    }

    public RoundedBorder(int radius) {
        this(null, radius);
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D.Double rectangle = new RoundRectangle2D.Double(x, y, width - 1, height - 1, radius, radius);
        Area rectangleArea = new Area(rectangle);
        Area area = new Area(new Rectangle(0, 0, width, height));
        area.subtract(rectangleArea);
        g2d.setClip(area);
        g2d.setColor(c.getParent().getBackground());
        g2d.fillRect(0, 0, width, height);
        g2d.setClip(null);

        if (color == null) {
            g2d.setColor(c.getBackground());
        } else {
            g2d.setColor(color);
        }

        g2d.setStroke(stroke);
        g2d.draw(rectangleArea);
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return getBorderInsets(c, new Insets(radius, radius, radius, radius));
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = radius;
        return insets;
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}