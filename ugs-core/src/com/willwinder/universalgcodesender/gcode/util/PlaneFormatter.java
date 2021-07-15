/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.gcode.util;

import com.willwinder.universalgcodesender.model.Position;

/**
 *
 * @author wwinder
 */
public class PlaneFormatter {
    private final Plane plane;

    public PlaneFormatter(Plane plane) {
        if (plane == null) {
            this.plane = Plane.XY;
        } else {
            this.plane = plane;
        }
    }

    public double axis0(Position point) {
        switch(plane) {
            case XY: return point.x;
            case ZX: return point.z;
            case YZ: return point.y;
            default: throw new IllegalArgumentException("Plane not supported: " + plane);
        }
    }

    public double axis1(Position point) {
        switch(plane) {
            case XY: return point.y;
            case ZX: return point.x;
            case YZ: return point.z;
            default: throw new IllegalArgumentException("Plane not supported: " + plane);
        }
    }

    public double linear(Position point) {
        switch(plane) {
            case XY: return point.z;
            case ZX: return point.y;
            case YZ: return point.x;
            default: throw new IllegalArgumentException("Plane not supported: " + plane);
        }
    }
    
    public void setAxis0(Position point, double value) {
        switch(plane) {
            case XY: point.x = value; return;
            case ZX: point.z = value; return;
            case YZ: point.y = value; return;
            default: throw new IllegalArgumentException("Plane not supported: " + plane);
        }
    }

    public void setAxis1(Position point, double value) {
        switch(plane) {
            case XY: point.y = value; return;
            case ZX: point.x = value; return;
            case YZ: point.z = value; return;
            default: throw new IllegalArgumentException("Plane not supported: " + plane);
        }
    }

    public void setLinear(Position point, double value) {
        switch(plane) {
            case XY: point.z = value; return;
            case ZX: point.y = value; return;
            case YZ: point.x = value; return;
            default: throw new IllegalArgumentException("Plane not supported: " + plane);
        }
    }
}
