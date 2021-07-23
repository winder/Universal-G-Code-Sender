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

import com.willwinder.ugs.nbp.designer.actions.RotateAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Joacim Breiler
 */
public class RotationControl extends AbstractControl {
    public static final int SIZE = 6;
    public static final int MARGIN = 12;
    private static final Logger LOGGER = Logger.getLogger(RotationControl.class.getSimpleName());
    private final Shape shape;
    private Point2D startPosition = new Point2D.Double();
    private double startRotation = 0d;
    private Point2D center;

    public RotationControl(SelectionManager selectionManager) {
        super(selectionManager);
        shape = new Ellipse2D.Double(0, 0, SIZE, SIZE);
    }

    /**
     * Calculates the angle from centerPt to targetPt in degrees.
     * The return should range from [0,360), rotating CLOCKWISE,
     * 0 and 360 degrees represents NORTH,
     * 90 degrees represents EAST, etc...
     * <p>
     * Assumes all points are in the same coordinate space.  If they are not,
     * you will need to call SwingUtilities.convertPointToScreen or equivalent
     * on all arguments before passing them  to this function.
     * <p>
     * Source: https://stackoverflow.com/a/16340752
     *
     * @param centerPt Point we are rotating around.
     * @param targetPt Point we want to calculate the angle to.
     * @return angle in degrees.  This is the angle from centerPt to targetPt.
     */
    public static double calcRotationAngleInDegrees(Point2D centerPt, Point2D targetPt) {
        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.
        // NOTE: By preserving Y and X param order to atan2,  we are expecting
        // a CLOCKWISE angle direction.
        double theta = Math.atan2(targetPt.getY() - centerPt.getY(), targetPt.getX() - centerPt.getX());

        // rotate the theta angle clockwise by 90 degrees
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.
        // subtracting would rotate it counter-clockwise
        theta += Math.PI / 2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if (angle < 0) {
            angle += 360;
        }

        if (angle > 360) {
            angle -= 360;
        }

        return angle;
    }

    private void updatePosition() {
        // Create transformation for where to position the controller in relative space
        AffineTransform transform = getSelectionManager().getTransform();

        Rectangle2D bounds = getSelectionManager().getRelativeShape().getBounds2D();
        transform.translate(bounds.getX(), bounds.getY() + bounds.getHeight());
        transform.translate(bounds.getWidth() / 2 - (SIZE / 2d), MARGIN);

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
    public void setSize(Size size) {

    }

    @Override
    public void render(Graphics2D graphics) {
        updatePosition();
        graphics.setStroke(new BasicStroke(0));
        graphics.setColor(Colors.CONTROL_HANDLE);
        Shape shape = getShape();
        graphics.fill(shape);

        double centerX = getSelectionManager().getCenter().getX();
        double centerY = getSelectionManager().getCenter().getY();
        graphics.setStroke(new BasicStroke(0.8f));

        graphics.draw(new Line2D.Double(centerX - (SIZE / 2d), centerY, centerX + (SIZE / 2d), centerY));
        graphics.draw(new Line2D.Double(centerX, centerY - (SIZE / 2d), centerX, centerY + (SIZE / 2d)));
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
                double deltaAngle = Math.round((calcRotationAngleInDegrees(target.getCenter(), startPosition) - calcRotationAngleInDegrees(target.getCenter(), mousePosition)) * 10d) / 10d;
                target.rotate(center, deltaAngle);
                startPosition = mousePosition;
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                double totalRotation = (startRotation + target.getRotation());
                LOGGER.info("Stopped rotating " + totalRotation);
                addUndoAction(center, totalRotation, target);
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
