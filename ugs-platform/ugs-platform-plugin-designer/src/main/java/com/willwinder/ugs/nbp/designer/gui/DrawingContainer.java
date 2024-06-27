/*
    Copyright 2021-2024 Will Winder

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

import com.willwinder.ugs.nbp.designer.actions.OpenStockSettingsAction;
import com.willwinder.ugs.nbp.designer.actions.OpenToolSettingsAction;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;

import javax.swing.Box;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/**
 * A simple container that contains a Drawing instance and keeps it
 * centered.
 *
 * @author Alex Lagerstedt
 * @author Joacim Breiler
 */
public class DrawingContainer extends JPanel implements ComponentListener, MouseWheelListener {

    public static final double CENTER_ZOOM_SCALE_FACTOR = 0.8;
    private final transient Controller controller;
    private JScrollPane scrollPane;
    private JPanel buttonPanel;

    public DrawingContainer(Controller controller) {
        super();
        this.controller = controller;
        setLayout(new BorderLayout());
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

        addComponentListener(this);
    }

    public void setDrawing(Drawing d) {
        removeAll();
        scrollPane = new JScrollPane(d, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
            @Override
            protected void processMouseWheelEvent(MouseWheelEvent e) {
                if (getParent() != null) {
                    // Prevent mouse scroll events to on scrollbars as it is used for zooming.
                    getParent().dispatchEvent(SwingUtilities.convertMouseEvent(this, e, getParent()));
                }
            }
        };
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        scrollPane.setPreferredSize(getSize());
        scrollPane.setBounds(0, 0, getWidth(), getHeight());
        scrollPane.setWheelScrollingEnabled(false);

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        ToolButton toolButton = new ToolButton(controller);
        toolButton.setMinimumSize(new Dimension(60, 40));
        toolButton.setMaximumSize(new Dimension(100, 40));
        toolButton.addActionListener(new OpenToolSettingsAction(controller));
        buttonPanel.add(toolButton);

        add(Box.createHorizontalStrut(6));
        PanelButton stockButton = new PanelButton("Stock", controller.getSettings().getStockSizeDescription());
        stockButton.setMinimumSize(new Dimension(60, 40));
        stockButton.setMaximumSize(new Dimension(100, 40));
        controller.getSettings().addListener(() -> stockButton.setText(controller.getSettings().getStockSizeDescription()));
        stockButton.addActionListener(new OpenStockSettingsAction(controller));
        buttonPanel.add(stockButton);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER, 0);
        layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER, 0);
        add(layeredPane, BorderLayout.CENTER);
        revalidate();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        SwingUtilities.invokeLater(() -> {
            Rectangle bounds = getBounds();
            scrollPane.setBounds(0, 0, bounds.width, bounds.height);
            buttonPanel.setBounds(0, bounds.height - 80, bounds.width - 20, 80);
            revalidate();
        });
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        // Not used
    }

    @Override
    public void componentShown(ComponentEvent e) {
        // Not used
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        // Not used
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        Rectangle viewRect = scrollPane.getViewport().getViewRect();
        Dimension size = scrollPane.getViewport().getView().getSize();

        // Get the mouse position relative to the center
        double mouseX = (e.getPoint().getX() - viewRect.getCenterX()) * CENTER_ZOOM_SCALE_FACTOR;
        double mouseY = (e.getPoint().getY() - viewRect.getCenterY()) * CENTER_ZOOM_SCALE_FACTOR;

        // Get the current view position in percent
        double previousXScrollbarPercent = (viewRect.getX() + mouseX) / size.getWidth();
        double previousYScrollbarPercent = (viewRect.getY() + mouseY) / size.getHeight();

        // Apply the scaling
        double scaleFactor = (e.getPreciseWheelRotation() * controller.getDrawing().getScale() * 0.1) * (backend.getSettings().isInvertMouseZoom() ? -1d : 1d);
        controller.getDrawing().setScale(controller.getDrawing().getScale() + scaleFactor);

        scrollPane.getHorizontalScrollBar().setValue((int) Math.round((controller.getDrawing().getMinimumSize().getWidth() * previousXScrollbarPercent)));
        scrollPane.getVerticalScrollBar().setValue((int) Math.round((controller.getDrawing().getMinimumSize().getHeight() * previousYScrollbarPercent)));
    }
}
