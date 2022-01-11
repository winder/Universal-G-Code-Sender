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
import com.willwinder.ugs.nbp.designer.actions.RotateAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class RotationControl extends AbstractControl {
    public static final int SIZE = 8;
    public static final int MARGIN = 16;

    private final Ellipse2D.Double shape;
    private Cursor cursor;
    private Point2D startPosition = new Point2D.Double();
    private double startRotation = 0d;
    private Point2D center;
    private boolean isHovered;

    public RotationControl(SelectionManager selectionManager) {
        super(selectionManager);
        shape = new Ellipse2D.Double(0, 0, SIZE, SIZE);

        try {
            cursor = Toolkit.getDefaultToolkit().createCustomCursor(ImageUtilities.loadImage("img/cursors/rotate.svg", false), new Point(8, 8), "rotater");
        } catch (HeadlessException e) {
            cursor = null;
        }
    }

    private void updatePosition(Drawing drawing) {
        double size = SIZE / drawing.getScale();
        double margin = MARGIN / drawing.getScale();
        shape.setFrame(0, 0, size, size);

        // Create transformation for where to position the controller in relative space
        AffineTransform transform = getSelectionManager().getTransform();

        Rectangle2D bounds = getSelectionManager().getRelativeShape().getBounds2D();
        transform.translate(bounds.getX(), bounds.getY() + bounds.getHeight());
        transform.translate(bounds.getWidth() / 2 - (size / 2d), margin);

        // Transform the position from relative space to real space
        Point2D result = new Point2D.Double();
        transform.transform(new Point2D.Double(0, 0), result);

        // Create a new transform for the control
        transform = new AffineTransform();
        transform.translate(result.getX(), result.getY());
        setTransform(transform);
    }

    @Override
    public Shape getShape() {
        return getTransform().createTransformedShape(shape);
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.ofNullable(cursor);
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (getSelectionManager().getSelection().isEmpty()) {
            return;
        }

        updatePosition(drawing);
        graphics.setColor(Colors.CONTROL_HANDLE);

        if (isHovered) {
            graphics.setColor(Colors.CONTROL_BORDER);
            graphics.draw(getShape());

            // Draw cross
            double halfSize = SIZE / drawing.getScale();
            double centerX = getSelectionManager().getCenter().getX();
            double centerY = getSelectionManager().getCenter().getY();
            graphics.draw(new Line2D.Double(centerX - halfSize, centerY, centerX + halfSize, centerY));
            graphics.draw(new Line2D.Double(centerX, centerY - halfSize, centerX, centerY + halfSize));
        }

        graphics.fill(getShape());
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Entity target = getSelectionManager();
            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startPosition = mousePosition;
                startRotation = target.getRotation();
                center = target.getCenter();
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                int decimals = 0;
                if (mouseShapeEvent.isAltPressed()) {
                    decimals = 1;
                }

                double deltaAngle = Utils.calcRotationAngleInDegrees(target.getCenter(), startPosition) - Utils.calcRotationAngleInDegrees(target.getCenter(), mousePosition);

                // Adjust the delta angle to achieve some rounding
                double fractionToRound = deltaAngle + target.getRotation() - Utils.roundToDecimals(deltaAngle + target.getRotation(), decimals);
                deltaAngle = deltaAngle - fractionToRound;

                target.rotate(center, deltaAngle);
                startPosition = mousePosition;
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                double totalRotation = (startRotation + target.getRotation());
                addUndoAction(center, totalRotation, target);
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_IN) {
                isHovered = true;
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_OUT) {
                isHovered = false;
            }
        }
    }

    private void addUndoAction(Point2D center, double rotation, Entity target) {
        UndoManager undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        if (undoManager != null) {
            List<Entity> entityList = new ArrayList<>();
            if (target instanceof SelectionManager) {
                entityList.addAll(((SelectionManager) target).getSelection());
            } else {
                entityList.add(target);
            }
            undoManager.addAction(new RotateAction(entityList, center, rotation));
        }
    }
}
