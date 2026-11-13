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
package com.willwinder.ugs.designer.io.ugsd.v1;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.CenterPoint;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstraintV1Test {

    @Test
    public void fromInternal_shouldWriteTheKindOfADistance() {
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(20, 0, 10, 10);
        DistanceConstraint constraint = DistanceConstraint.measured(reference, CenterPoint.INSTANCE, target, CenterPoint.INSTANCE, Axis.HORIZONTAL, 0);

        JsonObject result = new Gson().toJsonTree(ConstraintV1.fromInternal(constraint, "a", "b")).getAsJsonObject();

        assertThat(result.get("kind").getAsString()).isEqualTo("DISTANCE");
    }

    @Test
    public void toInternal_shouldNotReadAConstraintWithoutAKind() {
        Map<String, Entity> entitiesById = Map.of("a", new Rectangle(0, 0, 10, 10), "b", new Rectangle(20, 0, 10, 10));
        ConstraintV1 constraint = new Gson().fromJson("""
                {"referenceId": "a", "referencePoint": {"type": "CENTER"}, "targetId": "b", "targetPoint": {"type": "CENTER"},
                 "axis": "HORIZONTAL", "distance": 20}""", ConstraintV1.class);

        Optional<Constraint> result = constraint.toInternal(entitiesById);

        assertThat(result).isEmpty();
    }
}
