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
package com.willwinder.ugs.nbp.designer.io.c2d.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class C2dCurveObject {
    private List<Double[]> cp1;

    @SerializedName("control_point_1")
    private List<Double[]> controlPoints1;

    private List<Double[]> cp2;

    @SerializedName("control_point_2")
    private List<Double[]> controlPoints2;

    @SerializedName("point_type")
    private Integer[] pointType;
    private List<Double[]> points;
    private Double[] position;

    public Integer[] getPointType() {
        return pointType;
    }

    public List<Double[]> getPoints() {
        return points;
    }

    public Double[] getPosition() {
        if (position == null) {
            return new Double[]{0d, 0d};
        }
        return position;
    }

    public List<Double[]> getControlPoints1() {
        if (cp1 != null) {
            return cp1;
        } else if (controlPoints1 != null) {
            return controlPoints1;
        }

        return Collections.emptyList();
    }

    public List<Double[]> getControlPoints2() {
        if (cp2 != null) {
            return cp2;
        } else if (controlPoints2 != null) {
            return controlPoints2;
        }

        return Collections.emptyList();
    }
}
