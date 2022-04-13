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
public class C2dFile {
    @SerializedName("RECT_OBJECTS")
    private List<C2dRectangleObject> rectangleObjects;

    @SerializedName("CURVE_OBJECTS")
    private List<C2dCurveObject> curveObjects;

    @SerializedName("CIRCLE_OBJECTS")
    private List<C2dCircleObject> circleObjects;

    public List<C2dCircleObject> getCircleObjects() {
        if (circleObjects != null) {
            return circleObjects;
        }
        return Collections.emptyList();
    }

    public List<C2dRectangleObject> getRectangleObjects() {
        if (rectangleObjects != null) {
            return rectangleObjects;
        }
        return Collections.emptyList();
    }

    public List<C2dCurveObject> getCurveObjects() {
        if (curveObjects != null) {
            return curveObjects;
        }
        return Collections.emptyList();
    }
}
