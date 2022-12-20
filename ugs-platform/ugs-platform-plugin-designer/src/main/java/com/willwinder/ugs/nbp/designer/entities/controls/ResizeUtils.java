/*
    Copyright 2022 Will Winder

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
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.geom.Point2D;

public class ResizeUtils {

    private ResizeUtils() {
    }

    public static Point2D getDeltaMovement(Location location, Size size, Size newSize) {
        Size deltaSize = new Size(size.getWidth() - newSize.getWidth(), size.getHeight() - newSize.getHeight());
        Point2D movement = new Point2D.Double(0, 0);
        if (location == Location.BOTTOM_LEFT) {
            movement.setLocation(deltaSize.getWidth(), deltaSize.getHeight());
        } else if (location == Location.BOTTOM_RIGHT) {
            movement.setLocation(0, deltaSize.getHeight());
        } else if (location == Location.TOP_LEFT) {
            movement.setLocation(deltaSize.getWidth(), 0);
        } else if (location == Location.LEFT) {
            movement.setLocation(deltaSize.getWidth(), 0);
        } else if (location == Location.BOTTOM) {
            movement.setLocation(0, deltaSize.getHeight());
        }
        return movement;
    }

    public static void performScaling(Entity target, Location location, Size originalSize, Size newSize) {
        // Do not scale if the entity will become too small after operation
        if (newSize.getWidth() < 1 || newSize.getHeight() < 1) {
            return;
        }

        target.move(getDeltaMovement(location, originalSize, newSize));
        target.setSize(newSize);
    }
}
