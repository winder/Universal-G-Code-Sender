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

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;

import javax.swing.*;
import java.awt.GridLayout;

/**
 * A simple container that contains a Drawing instance and keeps it
 * centered.
 *
 * @author Alex Lagerstedt
 */
public class DrawingContainer extends JPanel implements ControllerListener {

    private static final long serialVersionUID = 0;

    private final Controller controller;
    private final MouseListener mouseListener;


    public DrawingContainer(Controller controller) {
        super();
        setLayout(new GridLayout(0, 1));
        this.controller = controller;
        this.mouseListener = new MouseListener(controller);
        setDrawing(this.controller.getDrawing());
    }

    public void setDrawing(Drawing d) {
        removeAll();
        JScrollPane scrollPane = new JScrollPane(d, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(5);
        add(scrollPane);
        revalidate();

        d.addMouseListener(mouseListener);
        d.addMouseMotionListener(mouseListener);
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.NEW_DRAWING) {
            setDrawing(controller.getDrawing());
        }
    }
}
