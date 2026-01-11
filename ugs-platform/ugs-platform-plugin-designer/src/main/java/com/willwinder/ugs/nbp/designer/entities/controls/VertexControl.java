/*
    Copyright 2026 Joacim Breiler

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

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.path.EditablePath;
import com.willwinder.ugs.nbp.designer.model.path.PointType;
import com.willwinder.ugs.nbp.designer.model.path.Segment;

import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Optional;

/**
 * Displays a vertex control handle for manipulating it
 *
 * @author Joacim Breiler
 */
public class VertexControl extends SnapToGridControl {

    private static final int SIZE = 6;
    private final Controller controller;

    private final EditablePath editablePath;
    private final int pointIndex;
    private final Path target;

    private final RectangularShape handle;
    private final Segment segment;
    private AffineTransform transform;
    private boolean isHover = false;

    public VertexControl(Controller controller, Path target, EditablePath editablePath, Segment segment, int pointIndex) {
        super(controller.getSelectionManager());
        this.controller = controller;
        this.target = target;
        this.segment = segment;
        this.editablePath = editablePath;
        this.pointIndex = pointIndex;

        PointType pointType = segment.getType().getPointTypes().get(pointIndex);
        handle = switch (pointType) {
            case COORDINATE -> new Rectangle2D.Double();
            case CONTROL_POINT -> new Ellipse2D.Double();
        };

        updatePosition();
    }

    private void updatePosition() {
        Point2D p = segment.getPoint(pointIndex);
        transform = new AffineTransform();
        transform.translate(p.getX(), p.getY());
    }

    @Override
    public Shape getRelativeShape() {
        return handle;
    }

    @Override
    public Shape getShape() {
        return transform.createTransformedShape(handle);
    }

    @Override
    public boolean isWithin(Point2D point) {
        double size = SIZE / controller.getDrawing().getScale();
        double hitBuffer = (size / 2d) * 1.2;

        return controller.getTool() == Tool.VERTEX && (getShape().contains(point) || getShape().intersects(point.getX() - hitBuffer, point.getY() - hitBuffer, hitBuffer * 2, hitBuffer * 2));
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.of(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void render(Graphics2D g, Drawing drawing) {
        if (controller.getTool() != Tool.VERTEX) {
            return;
        }

        double size = SIZE / drawing.getScale();
        handle.setFrame(-size / 2, -size / 2, size, size);
        updatePosition();
        g.setColor(isHover ? Colors.CONTROL_BORDER : Colors.CONTROL_HANDLE);
        g.fill(getShape());
    }

    @Override
    public void onEvent(EntityEvent event) {
        if (event instanceof MouseEntityEvent mouseEvent) {
            Point2D mousePosition = mouseEvent.getCurrentMousePosition();
            isHover = isWithin(mousePosition);

            if (mouseEvent.getType() == EventType.MOUSE_DRAGGED) {
                segment.getPoint(pointIndex).setLocation(mousePosition);
                target.setShape(EditablePath.toPath2D(editablePath));
            }
        }
    }
}