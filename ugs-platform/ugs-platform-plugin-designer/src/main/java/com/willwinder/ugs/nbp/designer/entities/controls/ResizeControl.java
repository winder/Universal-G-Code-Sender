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
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.actions.ResizeAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class ResizeControl extends AbstractControl {
    public static final int SIZE = 8;
    public static final int MARGIN = 6;
    public static final double ARC_SIZE = 1d;
    private final Location location;
    private final RoundRectangle2D.Double shape;
    private AffineTransform transform = new AffineTransform();
    private Point2D.Double startOffset = new Point2D.Double();
    private boolean isHovered;
    private Size originalSize;
    private Point2D originalPosition;

    public ResizeControl(Controller controller, Location location) {
        super(controller.getSelectionManager());
        this.location = location;
        this.shape = new RoundRectangle2D.Double(0, 0, SIZE, SIZE, ARC_SIZE, ARC_SIZE);
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        Cursor cursor = null;
        if (location == Location.TOP_LEFT) {
            cursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
        } else if (location == Location.TOP_RIGHT) {
            cursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
        } else if (location == Location.BOTTOM_LEFT) {
            cursor = new Cursor(Cursor.SW_RESIZE_CURSOR);
        } else if (location == Location.BOTTOM_RIGHT) {
            cursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
        } else if (location == Location.BOTTOM) {
            cursor = new Cursor(Cursor.S_RESIZE_CURSOR);
        } else if (location == Location.TOP) {
            cursor = new Cursor(Cursor.N_RESIZE_CURSOR);
        } else if (location == Location.LEFT) {
            cursor = new Cursor(Cursor.W_RESIZE_CURSOR);
        } else if (location == Location.RIGHT) {
            cursor = new Cursor(Cursor.E_RESIZE_CURSOR);
        }
        return Optional.ofNullable(cursor);
    }

    @Override
    public Shape getShape() {
        return transform.createTransformedShape(getRelativeShape());
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (getSelectionManager().getSelection().isEmpty()) {
            return;
        }

        if (getSelectionManager().getSelection().size() == 1 &&
                getSelectionManager().getSelection().get(0) instanceof Point) {
            return;
        }

        updatePosition(drawing);
        if (isHovered) {
            graphics.setColor(Colors.CONTROL_BORDER);
        } else {
            graphics.setColor(Colors.CONTROL_HANDLE);
        }
        graphics.fill(getShape());
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startOffset = new Point2D.Double(mousePosition.getX() - getPosition().getX(), mousePosition.getY() - getPosition().getY());
                originalSize = getSelectionManager().getSize();
                originalPosition = getSelectionManager().getPosition();
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                Size size = getSelectionManager().getSize();
                Size newSize = calculateNewSize(size, mousePosition);
                ResizeUtils.performScaling(getSelectionManager(), location, size, newSize);
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                getSelectionManager().setPosition(originalPosition);
                Size newSize = calculateNewSize(originalSize, mousePosition);
                addUndoAction(getSelectionManager(), location, originalSize, newSize);
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_IN) {
                isHovered = true;
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_OUT) {
                isHovered = false;
            }
        }
    }

    private void addUndoAction(Entity target, Location location, Size originalSize, Size newSize) {
        UndoManager undoManager = ControllerFactory.getUndoManager();
        if (undoManager != null) {
            List<Entity> entityList = new ArrayList<>();
            if (target instanceof SelectionManager) {
                entityList.addAll(((SelectionManager) target).getSelection());
            } else {
                entityList.add(target);
            }
            ResizeAction resizeAction = new ResizeAction(entityList, location, originalSize, newSize);
            resizeAction.redo();
            undoManager.addAction(resizeAction);
        }
    }

    private void updatePosition(Drawing drawing) {
        double size = SIZE / drawing.getScale();
        double halfSize = size / 2d;
        double arcSize = ARC_SIZE / drawing.getScale();
        double margin = MARGIN / drawing.getScale();

        this.shape.setRoundRect(0, 0, size, size, arcSize, arcSize);

        // Create transformation for where to position the controller in relative space
        AffineTransform t = getSelectionManager().getTransform();
        Rectangle2D bounds = getSelectionManager().getRelativeShape().getBounds2D();
        t.translate(bounds.getX(), bounds.getY());

        if (location == Location.BOTTOM_RIGHT) {
            t.translate(bounds.getWidth() + margin, -margin);
        } else if (location == Location.TOP_LEFT) {
            t.translate(-margin, bounds.getHeight() + margin);
        } else if (location == Location.TOP_RIGHT) {
            t.translate(bounds.getWidth() + margin, bounds.getHeight() + margin);
        } else if (location == Location.BOTTOM_LEFT) {
            t.translate(-margin, -margin);
        } else if (location == Location.TOP) {
            t.translate(bounds.getWidth() / 2d, bounds.getHeight() + margin);
        } else if (location == Location.BOTTOM) {
            t.translate(bounds.getWidth() / 2d, -margin);
        } else if (location == Location.LEFT) {
            t.translate(-margin, bounds.getHeight() / 2d);
        } else if (location == Location.RIGHT) {
            t.translate(bounds.getWidth() + margin, bounds.getHeight() / 2d);
        }

        // Transform the position from relative space to real space
        Point2D center = new Point2D.Double();
        t.transform(new Point2D.Double(0, 0), center);

        this.transform = new AffineTransform();
        this.transform.translate(center.getX() - halfSize, center.getY() - halfSize);
    }

    private Size calculateNewSize(Size size, Point2D mousePosition) {
        int decimals = 1;

        Entity target = getSelectionManager();

        Point2D deltaMovement = new Point2D.Double(Utils.roundToDecimals(mousePosition.getX() - getPosition().getX() - startOffset.getX(), decimals), Utils.roundToDecimals(mousePosition.getY() - getPosition().getY() - startOffset.getY(), decimals));
        Point2D scaleFactor = getScaleFactor(deltaMovement.getX() / size.getWidth(), deltaMovement.getY() / size.getHeight());
        return new Size(Utils.roundToDecimals(target.getSize().getWidth() * scaleFactor.getX(), decimals), Utils.roundToDecimals(target.getSize().getHeight() * scaleFactor.getY(), decimals));
    }


    private Point2D getScaleFactor(double deltaX, double deltaY) {
        Point2D scaleFactor = new Point2D.Double(0, 0);
        if (location == Location.BOTTOM_LEFT) {
            scaleFactor.setLocation(1d - deltaX, 1d - deltaX);
        } else if (location == Location.TOP_RIGHT) {
            scaleFactor.setLocation(1d + deltaX, 1d + deltaX);
        } else if (location == Location.BOTTOM_RIGHT) {
            scaleFactor.setLocation(1d + deltaX, 1d + deltaX);
        } else if (location == Location.TOP_LEFT) {
            scaleFactor.setLocation(1d - deltaX, 1d - deltaX);
        } else if (location == Location.LEFT) {
            scaleFactor.setLocation(1d - deltaX, 1d);
        } else if (location == Location.BOTTOM) {
            scaleFactor.setLocation(1d, 1d - deltaY);
        } else if (location == Location.TOP) {
            scaleFactor.setLocation(1d, 1d + deltaY);
        } else if (location == Location.RIGHT) {
            scaleFactor.setLocation(1d + deltaX, 1d);
        }
        return scaleFactor;
    }

    @Override
    public String toString() {
        return "ResizeControl";
    }
}
