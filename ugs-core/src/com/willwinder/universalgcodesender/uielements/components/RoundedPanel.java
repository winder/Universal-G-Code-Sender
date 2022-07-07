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

package com.willwinder.universalgcodesender.uielements.components;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RoundedPanel extends JPanel implements MouseListener {

    private final int bottomLeft;
    private final int bottomRight;
    private final int topLeftRadius;
    private final int topRightRadius;

    private Color hoverBackground;
    private Color pressedBackground;
    private Color backgroundDisabled;
    private Color foregroundDisabled;
    private boolean mouseOver = false;
    private boolean mousePressed = false;
    private final List<RoundedPanelClickListener> listeners = new ArrayList<>();
    private Component pressedComponent;

    public RoundedPanel(int radius) {
        this(radius, radius, radius,radius);
    }

    public RoundedPanel(int bottomLeftRadius, int bottomRightRadius, int topLeftRadius, int topRightRadius) {
        super();
        this.bottomLeft = bottomLeftRadius;
        this.bottomRight = bottomRightRadius;
        this.topLeftRadius = topLeftRadius;
        this.topRightRadius = topRightRadius;
        setOpaque(false);
        addMouseListener(this);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Arrays.stream(getComponents()).forEach(c -> c.setEnabled(enabled));
    }

    public Color getHoverBackground() {
        return hoverBackground;
    }

    public void setHoverBackground(Color hoverBackground) {
        this.hoverBackground = hoverBackground;
    }

    public Color getBackgroundDisabled() {
        return backgroundDisabled;
    }

    public void setBackgroundDisabled(Color backgroundDisabled) {
        this.backgroundDisabled = backgroundDisabled;
    }

    public void addClickListener(RoundedPanelClickListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Graphics2D gfx2d = (Graphics2D) gfx;
        gfx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // background
        Color background = getBackground();
        if (!isEnabled() && backgroundDisabled != null) {
            background = backgroundDisabled;
        } else if (mousePressed && pressedBackground != null && isEnabled()) {
            background = pressedBackground;
        } else if (mouseOver && hoverBackground != null && isEnabled()) {
            background = hoverBackground;
        }

        Color foreground = getForeground();
        if (!isEnabled() && foregroundDisabled != null) {
            foreground = foregroundDisabled;
        }

        Shape roundedRectangle = createRoundedRectangle();
        gfx2d.setColor(background);
        gfx2d.fill(roundedRectangle);

        // border
        gfx2d.setColor(foreground);
        gfx2d.draw(roundedRectangle);
    }

    private Shape createRoundedRectangle() {
        int width = getWidth() - 1;
        int height = getHeight() - 1;

        Area area = new Area();
        Path2D path2D = new Path2D.Double();
        path2D.moveTo(0, height - bottomLeft);
        path2D.curveTo(0, height, 0, height, bottomLeft, height);
        path2D.lineTo(width - bottomRight, height);
        path2D.curveTo(width, height, width, height, width, height - bottomRight);
        path2D.lineTo(width, topRightRadius);
        path2D.curveTo(width, 0, width, 0, width - topRightRadius, 0);
        path2D.lineTo(topLeftRadius, 0);
        path2D.curveTo(0, 0, 0, 0, 0, topLeftRadius);
        area.add(new Area(path2D));
        return area;
    }

    @Override
    public void mouseClicked(MouseEvent exc) {
        // Ignore these events as they behave unpredictable
    }

    @Override
    public void mousePressed(MouseEvent exc) {
        this.mousePressed = true;
        this.repaint();

        pressedComponent = exc.getComponent();

        if (!isEnabled()) return;
        listeners.forEach(RoundedPanelClickListener::onPressed);
    }

    @Override
    public void mouseReleased(MouseEvent exc) {
        this.mousePressed = false;
        this.repaint();

        if (!isEnabled()) return;
        listeners.forEach(RoundedPanelClickListener::onReleased);

        if (pressedComponent.contains(exc.getPoint())) {
            listeners.forEach(RoundedPanelClickListener::onClick);
        }
    }

    @Override
    public void mouseEntered(MouseEvent exc) {
        this.mouseOver = true;
        this.repaint();
    }

    @Override
    public void mouseExited(MouseEvent exc) {
        this.mouseOver = false;
        this.repaint();
    }

    public void setPressedBackground(Color pressedBackground) {
        this.pressedBackground = pressedBackground;
    }

    public Color getForegroundDisabled() {
        return foregroundDisabled;
    }

    public void setForegroundDisabled(Color foregroundDisabled) {
        this.foregroundDisabled = foregroundDisabled;
    }

    public interface RoundedPanelClickListener {
        void onClick();
        default void onPressed() {}
        default void onReleased() {}
    }
}
