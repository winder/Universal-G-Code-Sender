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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import java.awt.Adjustable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * A container that will add scroll bars and handle zooming
 *
 * @author Joacim Breiler
 */
public class DrawingScrollContainer extends JPanel implements MouseWheelListener {
    public static final int MARGIN = 1000;
    private final transient Controller controller;
    private JScrollBar horizontalScrollBar;
    private JScrollBar verticalScrollBar;

    public DrawingScrollContainer(Controller controller) {
        super();
        this.controller = controller;
        setLayout(new GridBagLayout());
        setDrawing(controller.getDrawing());

        MouseListener mouseListener = new MouseListener(controller);
        controller.getDrawing().addMouseListener(mouseListener);
        controller.getDrawing().addMouseMotionListener(mouseListener);
        controller.getDrawing().addMouseWheelListener(this);

        KeyboardListener keyboardListener = new KeyboardListener(controller, mouseListener);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                keyboardListener.keyPressed(e);
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                keyboardListener.keyReleased(e);
            } else if (e.getID() == KeyEvent.KEY_TYPED) {
                keyboardListener.keyTyped(e);
            }
            return false;
        });
    }

    public void setDrawing(Drawing drawing) {
        removeAll();
        createComponents(drawing);
        drawing.addPropertyChangeListener("minimumSize", e -> updateScrollBarsSizes());
        resetView();
    }

    public void resetView() {
        // Reset the drawing view
        controller.getDrawing().refresh();
        updateScrollBarsSizes();
        setScrollbarPercent(horizontalScrollBar, -0.02);
        setScrollbarPercent(verticalScrollBar, 0.02);
    }

    private void createComponents(Drawing drawing) {
        horizontalScrollBar = new JScrollBar(Adjustable.HORIZONTAL);
        horizontalScrollBar.setValue((int) drawing.getPosition().x);
        horizontalScrollBar.addAdjustmentListener(l ->
                updateDrawingPositionFromScrollBars()
        );

        GridBagConstraints drawingConstraints = new GridBagConstraints();
        drawingConstraints.gridx = 0;
        drawingConstraints.gridy = 0;
        drawingConstraints.weighty = 1;
        drawingConstraints.weightx = 1;
        drawingConstraints.fill = 1;
        add(drawing, drawingConstraints);

        GridBagConstraints hScrollConstraints = new GridBagConstraints();
        hScrollConstraints.gridx = 0;
        hScrollConstraints.gridy = 1;
        hScrollConstraints.weighty = 0;
        hScrollConstraints.weightx = 1;
        hScrollConstraints.fill = 1;
        add(horizontalScrollBar, hScrollConstraints);

        verticalScrollBar = new JScrollBar(Adjustable.VERTICAL);
        verticalScrollBar.setValue((int) drawing.getPosition().y);
        verticalScrollBar.addAdjustmentListener(l -> updateDrawingPositionFromScrollBars());

        GridBagConstraints vScrollConstraints = new GridBagConstraints();
        vScrollConstraints.gridx = 2;
        vScrollConstraints.gridy = 0;
        vScrollConstraints.weighty = 1;
        vScrollConstraints.weightx = 0;
        vScrollConstraints.fill = 1;
        add(verticalScrollBar, vScrollConstraints);
    }

    private void updateScrollBarsSizes() {
        Rectangle2D bounds = controller.getDrawing().getBounds();
        int margin = (int) (MARGIN * controller.getDrawing().getScale());
        horizontalScrollBar.setMinimum((int) bounds.getMinX() - margin);
        horizontalScrollBar.setMaximum((int) bounds.getMaxX() + margin);
        verticalScrollBar.setMinimum((int) bounds.getMinY() - margin);
        verticalScrollBar.setMaximum((int) bounds.getMaxY() + margin);
    }

    private void updateDrawingPositionFromScrollBars() {
        controller.getDrawing().setPosition(horizontalScrollBar.getValue(), -verticalScrollBar.getValue());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        Drawing drawing = controller.getDrawing();

        // Mouse point in Drawing component coordinates
        Point2D mouseScreen = new Point2D.Double(e.getPoint().getX(), e.getPoint().getY());

        // World point currently under the mouse
        Point2D mouseWorld;
        try {
            AffineTransform inv = drawing.getTransform().createInverse();
            mouseWorld = inv.transform(mouseScreen, null);
        } catch (Exception ex) {
            // If transform is temporarily non-invertible, fall back to old behavior (no anchor).
            double scaleFactor = (e.getPreciseWheelRotation() * drawing.getScale() * 0.1)
                    * (backend.getSettings().isInvertMouseZoom() ? -1d : 1d);
            drawing.setScale(drawing.getScale() + scaleFactor);
            updateScrollBarsSizes();
            return;
        }

        double scaleFactor = (e.getPreciseWheelRotation() * drawing.getScale() * 0.1)
                * (backend.getSettings().isInvertMouseZoom() ? -1d : 1d);

        double newScale = Math.max(Math.abs(drawing.getScale() + scaleFactor), Drawing.MIN_SCALE);
        drawing.setScale(newScale);

        // Scrollbar ranges depend on scale, so update them after scaling
        updateScrollBarsSizes();

        // Get that same world point ends up after scaling
        Point2D mouseScreenAfter = drawing.getTransform().transform(mouseWorld, null);

        // Adjust position so the world point stays under the mouse delta in screen coords
        double dx = mouseScreen.getX() - mouseScreenAfter.getX();
        double dy = mouseScreen.getY() - mouseScreenAfter.getY();

        Point2D.Double posPx = drawing.getPosition();
        double newPosPxX = posPx.getX() - dx;
        double newPosPxY = posPx.getY() + dy;

        horizontalScrollBar.setValue((int) Math.round(newPosPxX));
        verticalScrollBar.setValue((int) Math.round(-newPosPxY));
    }

    private void setScrollbarPercent(JScrollBar scrollBar, double percent) {
        double width = ((double) scrollBar.getMaximum() - (double) scrollBar.getMinimum());
        scrollBar.setValue((int) Math.round(width * percent));
    }
}
