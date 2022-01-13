package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.KeyboardEntityEvent;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import org.openide.util.ImageUtilities;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Optional;

public class ZoomControl extends AbstractControl {

    private static final double ZOOM_FACTOR = 0.5;
    private final Controller controller;
    private final Cursor zoomInCursor;
    private final Cursor zoomOutCursor;
    private boolean isShiftPressed;

    public ZoomControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
        zoomInCursor = Toolkit.getDefaultToolkit().createCustomCursor(ImageUtilities.loadImage("img/zoom-in.svg", false), new Point(8, 8), "zoom-in");
        zoomOutCursor = Toolkit.getDefaultToolkit().createCustomCursor(ImageUtilities.loadImage("img/zoom-out.svg", false), new Point(8, 8), "zoom-in");

    }

    @Override
    public boolean isWithin(Point2D point) {
        return controller.getTool() == Tool.ZOOM;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {

    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent) {
            MouseEntityEvent mouseEntityEvent = (MouseEntityEvent) entityEvent;
            isShiftPressed = mouseEntityEvent.isShiftPressed();

            if(mouseEntityEvent.getType() == EventType.MOUSE_PRESSED) {
                double zoomFactor = ZOOM_FACTOR;
                if(isShiftPressed) {
                    zoomFactor = -zoomFactor;
                }
                mouseEntityEvent.getCurrentMousePosition();
                controller.getDrawing().setScale(controller.getDrawing().getScale() + zoomFactor);
            }
        } else if (entityEvent instanceof KeyboardEntityEvent) {
            KeyboardEntityEvent keyboardEntityEvent = (KeyboardEntityEvent) entityEvent;
            if(keyboardEntityEvent.getKeyCode() == KeyEvent.VK_SHIFT){
                isShiftPressed = keyboardEntityEvent.getType() == EventType.KEY_PRESSED;
                controller.getDrawing().repaint();
            }
        }
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        if (isShiftPressed) {
            return Optional.ofNullable(zoomOutCursor);
        } else {
            return Optional.ofNullable(zoomInCursor);
        }
    }
}
