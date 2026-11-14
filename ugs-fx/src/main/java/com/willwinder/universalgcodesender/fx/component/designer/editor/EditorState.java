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

import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.ConstraintPlacement;
import com.willwinder.ugs.designer.constraint.DimensionLine;
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
    /**
     * A point of a path being edited, as the vertex tool wants it drawn.
     *
     * @param position where the point is, in design coordinates
     * @param node     the node a curve handle belongs to, drawn as a line to it, or null when
     *                 this point is a node itself
     * @param endpoint whether this node is an end of an open path, connected on one side only
     * @param selected whether this is the selected node
     * @param hovered  whether the cursor is over this point
     */
    public record VertexHandle(Point2D position, Point2D node, boolean endpoint, boolean selected, boolean hovered) {
        public boolean isCurveHandle() {
            return node != null;
        }
    }

    private Rectangle2D rubberBand;
    private Shape preview;
    private HandleSet.Handle hoveredHandle;
    private Entity hoveredEntity;
    private List<VertexHandle> vertices = List.of();
    private List<ConstraintPlacement.Marker> snapMarkers = List.of();
    private Constraint hoveredConstraint;
    private DimensionLine previewDimension;
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

    public List<VertexHandle> vertices() {
        return vertices;
    }

    public void setVertices(List<VertexHandle> vertices) {
        this.vertices = List.copyOf(vertices);
    }

    public List<ConstraintPlacement.Marker> snapMarkers() {
        return snapMarkers;
    }

    public void setSnapMarkers(List<ConstraintPlacement.Marker> snapMarkers) {
        this.snapMarkers = List.copyOf(snapMarkers);
    }

    public Constraint hoveredConstraint() {
        return hoveredConstraint;
    }

    /**
     * Whether the constraint's dimension is drawn highlighted, as it is when selected or hovered.
     */
    public boolean isHighlighted(Constraint constraint, ConstraintManager constraintManager) {
        return constraint == hoveredConstraint || constraintManager.getSelectedConstraint().orElse(null) == constraint;
    }

    public void setHoveredConstraint(Constraint hoveredConstraint) {
        this.hoveredConstraint = hoveredConstraint;
    }

    public DimensionLine previewDimension() {
        return previewDimension;
    }

    /**
     * Shows the dimension of a constraint being placed, drawn with its arrows. Pass null to hide it.
     */
    public void setPreviewDimension(DimensionLine previewDimension) {
        this.previewDimension = previewDimension;
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
        previewDimension = null;
        readout = null;
        readoutPosition = null;
    }
}
