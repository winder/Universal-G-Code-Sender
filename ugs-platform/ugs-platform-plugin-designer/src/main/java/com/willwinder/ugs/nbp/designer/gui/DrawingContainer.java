/*
    Copyright 2021 Will Winder

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;

/**
 * A simple container that contains a Drawing instance and keeps it
 * centered.
 *
 * @author Alex Lagerstedt
 */
public class DrawingContainer extends JPanel implements ComponentListener {

    private static final long serialVersionUID = 0;
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
        controller.getDrawing().addMouseWheelListener(e -> controller.getDrawing().setScale(controller.getDrawing().getScale() + (e.getPreciseWheelRotation() * 0.1)));

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
        scrollPane = new JScrollPane(d, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        scrollPane.setPreferredSize(getSize());
        scrollPane.setBounds(0, 0, getWidth(), getHeight());


        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        PanelButton toolButton = new PanelButton("Tool", controller.getSettings().getToolDescription());
        toolButton.setMinimumSize(new Dimension(60, 48));
        toolButton.setPreferredSize(new Dimension(70, 48));
        controller.getSettings().addListener(() -> toolButton.setText(controller.getSettings().getToolDescription()));
        toolButton.addActionListener(new OpenToolSettingsAction(controller));
        buttonPanel.add(toolButton);

        add(Box.createHorizontalStrut(6));
        PanelButton stockButton = new PanelButton("Stock", controller.getSettings().getStockSizeDescription());
        stockButton.setMinimumSize(new Dimension(60, 48));
        stockButton.setPreferredSize(new Dimension(70, 48));
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
            buttonPanel.setBounds(0, bounds.height - 80,  bounds.width - 20, 80);
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
}
