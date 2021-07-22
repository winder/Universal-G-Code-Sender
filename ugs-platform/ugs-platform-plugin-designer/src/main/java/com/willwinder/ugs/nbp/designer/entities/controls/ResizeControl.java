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
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

/**
 * @author Joacim Breiler
 */
public class ResizeControl extends AbstractControl {
    public static final int SIZE = 6;
    public static final int MARGIN = 6;
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
        AffineTransform t = getSelectionManager().getTransform();
        Rectangle2D bounds = getSelectionManager().getRelativeShape().getBounds2D();
        t.translate(bounds.getX(), bounds.getY());

        double halfSize = SIZE / 2d;
        if (location == Location.BOTTOM_RIGHT) {
            t.translate(bounds.getWidth() + MARGIN, -MARGIN);
        } else if (location == Location.TOP_LEFT) {
            t.translate(-MARGIN, bounds.getHeight() + MARGIN);
        } else if (location == Location.TOP_RIGHT) {
            t.translate(bounds.getWidth() + MARGIN, bounds.getHeight() + MARGIN);
        } else if (location == Location.BOTTOM_LEFT) {
            t.translate(- MARGIN, - MARGIN);
        } else if (location == Location.TOP) {
            t.translate(bounds.getWidth() / 2d, bounds.getHeight() + MARGIN);
        } else if (location == Location.BOTTOM) {
            t.translate(bounds.getWidth() / 2d,  - MARGIN);
        } else if (location == Location.LEFT) {
            t.translate(-MARGIN, bounds.getHeight() / 2d);
        } else if (location == Location.RIGHT) {
            t.translate(bounds.getWidth() + MARGIN, bounds.getHeight() / 2d);
        }

        // Transform the position from relative space to real space
        Point2D center = new Point2D.Double();
        t.transform(new Point2D.Double(0, 0), center);

        this.transform = new AffineTransform();
        this.transform.translate(center.getX() - halfSize, center.getY() - halfSize);
    }

    @Override
    public void setSize(Size size) {

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
            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startOffset = new Point2D.Double(mousePosition.getX() - getPosition().getX(), mousePosition.getY() - getPosition().getY());
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - getPosition().getX() - startOffset.getX(), mousePosition.getY() - getPosition().getY() - startOffset.getY());

                Size size = getSelectionManager().getSize();
                double deltaX = deltaMovement.getX() / size.getWidth();
                double deltaY = deltaMovement.getY() / size.getHeight();

                double scale = deltaX;
                double scaleSizeX = scale * size.getWidth();
                double scaleSizeY = scale * size.getHeight();

                if (size.getWidth() - Math.abs(scaleSizeX) < 1 || size.getHeight() - Math.abs(scaleSizeY) < 1) {
                    return;
                }

                if (location == Location.BOTTOM_LEFT) {
                    target.move(new Point2D.Double(scaleSizeX, scaleSizeY));
                    target.scale(1d - scale, 1d - scale);
                } else if (location == Location.TOP_RIGHT) {
                    target.scale(1d + scale, 1d + scale);
                } else if (location == Location.BOTTOM_RIGHT) {
                    target.move(new Point2D.Double(0, -scaleSizeY));
                    target.scale(1d + scale, 1d + scale);
                } else if (location == Location.TOP_LEFT) {
                    target.move(new Point2D.Double(scaleSizeX, 0));
                    target.scale(1d - scale, 1d - scale);
                } else if (location == Location.LEFT) {
                    target.move(new Point2D.Double(deltaX * size.getWidth(), 0));
                    target.scale(1d - deltaX, 1d);
                } else if (location == Location.BOTTOM) {
                    target.move(new Point2D.Double(0, deltaY * size.getHeight()));
                    target.scale(1d, 1d - deltaY);
                } else if (location == Location.TOP) {
                    target.scale(1d, 1d + deltaY);
                } else if (location == Location.RIGHT) {
                    target.scale(1d + deltaX, 1d);
                }
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                LOGGER.info("Stopped moving " + target.getPosition());
            }
        }
    }
}
