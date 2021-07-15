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
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class EntityPathSegmentV1 implements Serializable {

    @Expose
    private EntityPathTypeV1 type;
    private List<Double[]> coordinates;

    public EntityPathTypeV1 getType() {
        return type;
    }

    public void setType(EntityPathTypeV1 type) {
        this.type = type;
    }

    public void setCoordinates(List<Double[]> coordinates) {
        this.coordinates = coordinates;
    }

    public List<Double[]> getCoordinates() {
        return coordinates;
    }
}
