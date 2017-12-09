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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

public class RoundedPanel extends JPanel implements MouseListener {

    private int radius;
    private Color hoverBackground;
    private boolean mouseOver = false;
    private List<RoundedPanelClickListener> listeners = new ArrayList<>();

    public RoundedPanel(int radius) {
        super();
        setOpaque(false);
        this.radius = radius;
        addMouseListener(this);
    }

    public Color getHoverBackground() {
        return hoverBackground;
    }

    public void setHoverBackground(Color hoverBackground) {
        this.hoverBackground = hoverBackground;
    }

    public void addClickListener(RoundedPanelClickListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void paintComponent(Graphics gfx) {
        super.paintComponent(gfx);
        Dimension arcs = new Dimension(radius, radius);
        Graphics2D gfx2d = (Graphics2D) gfx;
        gfx2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gfx2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // background
        Color backGround;
        if (mouseOver && hoverBackground != null && isEnabled()) backGround = hoverBackground;
        else backGround = getBackground();

        gfx2d.setColor(backGround);
        gfx2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);

        // border
        gfx2d.setColor(getForeground());
        gfx2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcs.width, arcs.height);
    }

    @Override
    public void mouseClicked(MouseEvent exc) {
        if (! isEnabled()) return;
        listeners.forEach(RoundedPanelClickListener::onClick);
    }

    @Override
    public void mousePressed(MouseEvent exc) {}

    @Override
    public void mouseReleased(MouseEvent exc) {}

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

    public interface RoundedPanelClickListener {
        void onClick();
    }
}
