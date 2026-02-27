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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class VisualizerUtils {
    public static MeshView createCone(float radius, float height, int divisions) {
        TriangleMesh mesh = new TriangleMesh();

        // 1. Add the tip point (apex of the cone)
        mesh.getPoints().addAll(0f, -height / 2f, 0f); // tip

        // 2. Add base circle points
        for (int i = 0; i < divisions; i++) {
            double angle = 2 * Math.PI * i / divisions;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            mesh.getPoints().addAll(x, height / 2f, z);
        }

        // 3. Texture coordinates (needed, but not used here)
        mesh.getTexCoords().addAll(0, 0);

        // 4. Create triangle faces from tip to each base segment
        for (int i = 0; i < divisions; i++) {
            int p0 = 0; // tip
            int p1 = i + 1;
            int p2 = (i + 1) % divisions + 1;

            mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0); // side face
        }

        // 5. Optionally add base (disk) using triangles to center
        int baseCenterIndex = mesh.getPoints().size() / 3;
        mesh.getPoints().addAll(0f, height / 2f, 0f); // center of base

        for (int i = 0; i < divisions; i++) {
            int p0 = baseCenterIndex;
            int p1 = (i + 1) % divisions + 1;
            int p2 = i + 1;

            mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0); // base face
        }

        // Create MeshView and material
        MeshView cone = new MeshView(mesh);
        cone.setDrawMode(DrawMode.FILL);
        cone.setCullFace(CullFace.BACK);
        return cone;
    }
}
