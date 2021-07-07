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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

/**
 * @author Joacim Breiler
 */
public class ResizeControl extends AbstractControl {
    public static final int SIZE = 8;
    private static final Logger LOGGER = Logger.getLogger(ResizeControl.class.getSimpleName());
    private final Location location;
    private final Rectangle2D shape;
    private AffineTransform transform = new AffineTransform();
    private Point2D.Double startOffset = new Point2D.Double();

    public ResizeControl(SelectionManager selectionManager, Location location) {
        super(selectionManager);
        this.location = location;
        this.shape = new Rectangle2D.Double(0, 0, SIZE, SIZE);
    }

    @Override
    public Shape getShape() {
        return transform.createTransformedShape(getRelativeShape());
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    private void updatePosition() {
        // Create transformation for where to position the controller in relative space
        AffineTransform transform = getSelectionManager().getTransform();
        Rectangle bounds = getSelectionManager().getRelativeShape().getBounds();
        transform.translate(bounds.getX(), bounds.getY());

        double halfSize = SIZE / 2d;
        if (location == Location.TOP_RIGHT) {
            transform.translate(bounds.getWidth(), 0);
        } else if (location == Location.BOTTOM_LEFT) {
            transform.translate(0, bounds.getHeight());
        } else if (location == Location.BOTTOM_RIGHT) {
            transform.translate(bounds.getWidth(), bounds.getHeight());
        }

        // Transform the position from relative space to real space
        Point2D center = new Point2D.Double();
        transform.transform(new Point2D.Double(0, 0), center);

        this.transform = new AffineTransform();
        this.transform.translate(center.getX() - halfSize, center.getY() - halfSize);
    }

    @Override
    public void setSize(Dimension s) {

    }

    @Override
    public void render(Graphics2D graphics) {
        updatePosition();
        graphics.setStroke(new BasicStroke(1));
        graphics.setColor(Colors.CONTROL_HANDLE);
        graphics.fill(getShape());
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Entity target = getSelectionManager();
            Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - target.getPosition().getX() - startOffset.getX(), mousePosition.getY() - target.getPosition().getY() - startOffset.getY());
            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startOffset = new Point2D.Double(mousePosition.getX() - target.getPosition().getX(), mousePosition.getY() - target.getPosition().getY());
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                if (location == Location.TOP_LEFT) {
                    Dimension size = getSelectionManager().getSize();
                    double sx = (double) size.width / ((double) size.width - deltaMovement.getX());
                    double sy = (double) size.height/ ((double) size.height - deltaMovement.getY());

                    target.scale(sx, sy);
                    target.move(deltaMovement);
                }
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                LOGGER.info("Stopped moving " + target.getPosition());
            }
        }
    }
}
