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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.Entity;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * What the tools want drawn besides the design itself: the rubber band, a creation preview,
 * the hovered handle, the vertex handles and a text readout near the cursor. Tools write it,
 * the handles renderable and the overlay painter read it.
 */
public final class EditorState {
    private Rectangle2D rubberBand;
    private Shape preview;
    private HandleSet.Handle hoveredHandle;
    private Entity hoveredEntity;
    private List<Point2D> vertices = List.of();
    private int hoveredVertex = -1;
    private String readout;
    private Point2D readoutPosition;

    public Rectangle2D rubberBand() {
        return rubberBand;
    }

    public void setRubberBand(Rectangle2D rubberBand) {
        this.rubberBand = rubberBand;
    }

    public Shape preview() {
        return preview;
    }

    public void setPreview(Shape preview) {
        this.preview = preview;
    }

    public HandleSet.Handle hoveredHandle() {
        return hoveredHandle;
    }

    public void setHoveredHandle(HandleSet.Handle hoveredHandle) {
        this.hoveredHandle = hoveredHandle;
    }

    public Entity hoveredEntity() {
        return hoveredEntity;
    }

    public void setHoveredEntity(Entity hoveredEntity) {
        this.hoveredEntity = hoveredEntity;
    }

    public List<Point2D> vertices() {
        return vertices;
    }

    public int hoveredVertex() {
        return hoveredVertex;
    }

    public void setVertices(List<Point2D> vertices, int hoveredVertex) {
        this.vertices = List.copyOf(vertices);
        this.hoveredVertex = hoveredVertex;
    }

    public String readout() {
        return readout;
    }

    public Point2D readoutPosition() {
        return readoutPosition;
    }

    /**
     * Shows a short text next to a design position, for the size while resizing or the angle
     * while rotating. Pass null to hide it.
     */
    public void setReadout(String readout, Point2D position) {
        this.readout = readout;
        this.readoutPosition = position;
    }

    /**
     * Drops everything a gesture may have left behind.
     */
    public void clearTransient() {
        rubberBand = null;
        preview = null;
        readout = null;
        readoutPosition = null;
    }
}
