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

import com.willwinder.ugs.nbp.designer.actions.OpenStockSettingsAction;
import com.willwinder.ugs.nbp.designer.actions.OpenToolSettingsAction;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * A container that will add button overlays
 *
 * @author Joacim Breiler
 */
public class DrawingOverlayContainer extends JPanel implements ComponentListener {
    private final JPanel buttonPanel;
    private final JComponent component;

    public DrawingOverlayContainer(Controller controller, JComponent component) {
        setLayout(new BorderLayout());

        this.component = component;
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

        layeredPane.add(component, JLayeredPane.DEFAULT_LAYER, 0);
        layeredPane.add(buttonPanel, JLayeredPane.PALETTE_LAYER, 0);
        add(layeredPane, BorderLayout.CENTER);
        revalidate();

        // This is a workaround to be able to resize the layered pane
        addComponentListener(this);
    }

    @Override
    public void componentResized(ComponentEvent e) {
        // Needed to properly resize the layered pane
        SwingUtilities.invokeLater(() -> {
            Rectangle bounds = getBounds();
            component.setBounds(0, 0, bounds.width, bounds.height);
            buttonPanel.setBounds(0, bounds.height - 80, bounds.width - 30, 80);
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
