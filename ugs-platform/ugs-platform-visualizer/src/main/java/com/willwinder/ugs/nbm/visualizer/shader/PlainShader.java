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
import com.jogamp.opengl.GLException;
import com.willwinder.ugs.nbm.visualizer.utils.ShaderLoader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * A simple shader program that will render each vertex with the given color
 *
 * @author Joacim Breiler
 */
public class PlainShader implements Shader {
    private final String vertexShader;
    private final String fragmentShader;
    private int shaderProgramId;

    public PlainShader() {
        this("/shaders/plain.vert.glsl", "/shaders/plain.frag.glsl");
    }

    public PlainShader(String vertexShader, String fragmentShader) {
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public void init(GL2 gl) {
        try {
            InputStream vertexShaderInputStream = getClass().getResourceAsStream(vertexShader);
            if (vertexShaderInputStream == null) {
                throw new IOException("Could not find vertex shader file");
            }

            InputStream fragmentShaderInputStream = getClass().getResourceAsStream(fragmentShader);
            if (fragmentShaderInputStream == null) {
                throw new IOException("Could not find fragment shader file");
            }

            shaderProgramId = ShaderLoader.loadProgram(gl, IOUtils.toString(vertexShaderInputStream, StandardCharsets.UTF_8), IOUtils.toString(fragmentShaderInputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new GLException(e);
        }
    }

    public void dispose(GL2 gl) {
        if (shaderProgramId <= 0) {
            return;
        }

        gl.glDeleteShader(shaderProgramId);
    }

    public int getProgramId() {
        return shaderProgramId;
    }
}
