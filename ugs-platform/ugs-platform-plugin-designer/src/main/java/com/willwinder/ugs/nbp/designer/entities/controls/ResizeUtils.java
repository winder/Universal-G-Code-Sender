/*
    Copyright 2022-2024 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.geom.Point2D;

public class ResizeUtils {

    private ResizeUtils() {
    }

    public static Point2D getDeltaMovement(Anchor anchor, Size size, Size newSize) {
        Size deltaSize = new Size(size.getWidth() - newSize.getWidth(), size.getHeight() - newSize.getHeight());
        Point2D movement = new Point2D.Double(0, 0);
        if (anchor == Anchor.TOP_RIGHT) {
            movement.setLocation(deltaSize.getWidth(), deltaSize.getHeight());
        } else if (anchor == Anchor.TOP_LEFT) {
            movement.setLocation(0, deltaSize.getHeight());
        } else if (anchor == Anchor.BOTTOM_RIGHT) {
            movement.setLocation(deltaSize.getWidth(), 0);
        } else if (anchor == Anchor.RIGHT_CENTER) {
            movement.setLocation(deltaSize.getWidth(), deltaSize.getHeight() / 2);
        } else if (anchor == Anchor.TOP_CENTER) {
            movement.setLocation(deltaSize.getWidth() / 2, deltaSize.getHeight());
        } else if (anchor == Anchor.CENTER) {
            movement.setLocation(deltaSize.getWidth() / 2, deltaSize.getHeight() / 2);
        } else if (anchor == Anchor.BOTTOM_CENTER) {
            movement.setLocation(deltaSize.getWidth() / 2, 0);
        } else if (anchor == Anchor.LEFT_CENTER) {
            movement.setLocation(0, deltaSize.getHeight() / 2);
        }
        return movement;
    }

    public static void performScaling(Entity target, Anchor anchor, Size originalSize, Size newSize) {
        // Do not scale if the entity will become too small after operation
        if (newSize.getWidth() <= 0 || newSize.getHeight() <= 0) {
            return;
        }
        target.move(getDeltaMovement(anchor, originalSize, newSize));
        target.setSize(newSize);
    }
}
