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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

/**
 * How the floats of an uploaded vertex array are interpreted.
 */
public enum VertexLayout {
    /**
     * Two vertices per line segment, each {@code x, y, z, r, g, b, a, command}. The command number
     * lets the toolpath shader recolour everything the controller has finished; other lines
     * write {@code -1}. Built with {@link LineMeshBuilder}.
     */
    LINE(8),

    /**
     * Three vertices per triangle, each {@code x, y, z, nx, ny, nz}. Built by
     * {@link SceneMeshes} and {@link MeshConverter}.
     */
    MESH(6),
    /**
     * Three vertices per triangle, each {@code x, y, z, u, v}, sampling the texture bound by
     * {@link RenderContext#drawTextured}.
     */
    TEXTURED(5);

    private final int floatsPerVertex;

    VertexLayout(int floatsPerVertex) {
        this.floatsPerVertex = floatsPerVertex;
    }

    public int floatsPerVertex() {
        return floatsPerVertex;
    }

    public int bytesPerVertex() {
        return floatsPerVertex * Float.BYTES;
    }
}
