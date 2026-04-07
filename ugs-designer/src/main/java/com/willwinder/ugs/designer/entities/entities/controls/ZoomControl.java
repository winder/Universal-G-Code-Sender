package com.willwinder.ugs.designer.entities.entities.controls;

import com.willwinder.ugs.designer.entities.entities.EntityEvent;
import com.willwinder.ugs.designer.entities.entities.EventType;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.gui.KeyboardEntityEvent;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.ImageIcon;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
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

        zoomInCursor = Toolkit.getDefaultToolkit().createCustomCursor(SvgIconLoader.loadImageIcon("img/zoom-in.svg", SvgIconLoader.SIZE_SMALL).map(ImageIcon::getImage).orElse(null), new Point(8, 8), "zoom-in");
        zoomOutCursor = Toolkit.getDefaultToolkit().createCustomCursor(SvgIconLoader.loadImageIcon("img/zoom-out.svg", SvgIconLoader.SIZE_SMALL).map(ImageIcon::getImage).orElse(null), new Point(8, 8), "zoom-out");
    }

    @Override
    public boolean isWithin(Point2D point) {
        return controller.getTool() == Tool.ZOOM;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        // Not applicable
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent mouseEntityEvent) {
            isShiftPressed = mouseEntityEvent.isShiftPressed();

            if (mouseEntityEvent.getType() == EventType.MOUSE_PRESSED) {
                double zoomFactor = ZOOM_FACTOR;
                if (isShiftPressed) {
                    zoomFactor = -zoomFactor;
                }
                controller.getDrawing().setScale(controller.getDrawing().getScale() + zoomFactor);
            }
        } else if (entityEvent instanceof KeyboardEntityEvent keyboardEntityEvent) {
            if (keyboardEntityEvent.getKeyCode() == KeyEvent.VK_SHIFT) {
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
