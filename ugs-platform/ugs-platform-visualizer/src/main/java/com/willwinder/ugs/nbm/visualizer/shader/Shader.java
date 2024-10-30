/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.shader;

import com.jogamp.opengl.GL2;

/**
 * An interface for describing a shader program
 *
 * @author Joacim Breiler
 */
public interface Shader {
    /**
     * Initializes the shader program
     *
     * @param gl the current OpenGL context
     */
    void init(GL2 gl);

    /**
     * Disposes the shader program
     *
     * @param gl the current OpenGL context
     */
    void dispose(GL2 gl);

    /**
     * Returns the ID of the loaded shader program to be referenced in OpenGL
     * @return the id of the shader program
     */
    int getProgramId();
}
