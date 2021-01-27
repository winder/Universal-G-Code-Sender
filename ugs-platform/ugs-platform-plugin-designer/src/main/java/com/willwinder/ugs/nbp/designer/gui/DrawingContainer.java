package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.events.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.events.ControllerListener;

import javax.swing.*;
import java.awt.*;

/**
 * A simple container that contains a Drawing instance and keeps it
 * centered.
 *
 * @author Alex Lagerstedt
 */
public class DrawingContainer extends JPanel implements ControllerListener {

    private static final long serialVersionUID = 0;

    private final JScrollPane scrollpane;
    private final Controller controller;
    private final MouseListener mouseListener;


    public DrawingContainer(Controller controller) {
        super();
        setLayout(new GridLayout(0, 1));
        this.scrollpane = new JScrollPane();
        this.controller = controller;
        this.mouseListener = new MouseListener(controller);

        setDrawing(this.controller.getDrawing());
    }

    public void setDrawing(Drawing d) {
        removeAll();
        add(d);
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
