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
import com.willwinder.ugs.designer.constraint.ShapePoint;
import com.willwinder.ugs.designer.constraint.VertexPoint;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ShapePointV1Test {

    @Test
    public void toInternal_shouldReadAVertex() {
        ShapePointV1 point = new Gson().fromJson("{\"type\": \"VERTEX\", \"segment\": 2, \"end\": true}", ShapePointV1.class);

        Optional<ShapePoint> result = point.toInternal();

        assertThat(result).contains(new VertexPoint(2, true));
    }
}
