/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.geom.Point2D;

/**
 * An undoable action for resizing a group while maintaining aspect ratio optionally.
 * This action handles both width and height changes and preserves the anchor position.
 *
 * @author giro-dev
 */
public class ResizeGroupAction implements UndoableAction {

    private final Group selectionGroup;
    private final Anchor anchor;
    private final Size originalSize;
    private final Point2D originalPosition;
    private final Size newSize;
    private final DimensionType dimensionType;

    public enum DimensionType {
        WIDTH, HEIGHT
    }

    /**
     * Creates a resize action for a group.
     *
     * @param selectionGroup the group to resize
     * @param anchor         the anchor point for the resize operation
     * @param dimensionType  which dimension is being changed (width or height)
     * @param newValue       the new value for the dimension
     * @param lockRatio      whether to maintain aspect ratio
     */
    public ResizeGroupAction(Group selectionGroup, Anchor anchor, DimensionType dimensionType,
                             double newValue, boolean lockRatio) {
        this.selectionGroup = selectionGroup;
        this.anchor = anchor;
        this.dimensionType = dimensionType;
        this.originalSize = selectionGroup.getSize();
        this.originalPosition = selectionGroup.getPosition(anchor);

        // Calculate the new size based on dimension type and lock ratio
        double newWidth, newHeight;
        if (dimensionType == DimensionType.WIDTH) {
            newWidth = newValue;
            newHeight = lockRatio && originalSize.getWidth() > 0
                    ? originalSize.getHeight() * (newWidth / originalSize.getWidth())
                    : originalSize.getHeight();
        } else {
            newHeight = newValue;
            newWidth = lockRatio && originalSize.getHeight() > 0
                    ? originalSize.getWidth() * (newHeight / originalSize.getHeight())
                    : originalSize.getWidth();
        }

        this.newSize = new Size(newWidth, newHeight);
    }

    @Override
    public void redo() {
        selectionGroup.setSize(anchor, newSize);
    }

    @Override
    public void undo() {
        selectionGroup.setSize(anchor, originalSize);
        selectionGroup.setPosition(anchor, originalPosition);
    }

    @Override
    public String toString() {
        return "Change group " + (dimensionType == DimensionType.WIDTH ? "width" : "height");
    }
}

