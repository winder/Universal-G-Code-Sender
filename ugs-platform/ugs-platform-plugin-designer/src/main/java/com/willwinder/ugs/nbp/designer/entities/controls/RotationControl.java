package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.actions.RotateAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class RotationControl extends AbstractControl {
    public static final int SIZE = 8;
    private static final Logger LOGGER = Logger.getLogger(RotationControl.class.getSimpleName());
    private final Rectangle2D shape;
    private Point2D startPosition = new Point2D.Double();
    private double startRotation = 0d;
    private Point2D center;

    public RotationControl(SelectionManager selectionManager) {
        super(selectionManager);
        shape = new Rectangle2D.Double(0, 0, SIZE, SIZE);
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
     *
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

        Rectangle bounds = getSelectionManager().getRelativeShape().getBounds();
        transform.translate(bounds.getX(), bounds.getY() + bounds.getHeight());
        transform.translate(bounds.getWidth() / 2 - (SIZE / 2d), SIZE);

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
    public void setSize(Dimension s) {

    }

    @Override
    public void render(Graphics2D graphics) {
        updatePosition();
        graphics.setStroke(new BasicStroke(0));
        graphics.setColor(Color.GRAY);
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
                double deltaAngle = calcRotationAngleInDegrees(target.getCenter(), mousePosition) - calcRotationAngleInDegrees(target.getCenter(), startPosition);
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
