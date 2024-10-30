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
package com.willwinder.ugs.nbm.visualizer.utils;

import com.jogamp.opengl.GL2ES2;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.glsl.ShaderUtil;

/**
 * Utility to load shaders from files, URLs, and strings.
 *
 * <p>
 * {@code ShaderLoader} is a simple utility for loading shaders.  It takes shaders directly as
 * strings.  It will create and compile the shaders, and link them together into a program.  Both
 * compiling and linking are verified.  If a problem occurs a {@link GLException} is thrown with
 * the appropriate log attached.
 *
 * <p>
 * Note it is highly recommended that if the developer passes the strings directly to {@code
 * ShaderLoader} that they contain newlines.  That way if any errors do occur their line numbers
 * will be reported correctly.  This means that if the shader is to be embedded in Java code, a
 * "\n" should be appended to every line.
 */
public final class ShaderLoader {

    /**
     * Prevents instantiation.
     */
    private ShaderLoader() {
        // empty
    }

    /**
     * Checks that a shader was compiled correctly.
     *
     * @param gl     OpenGL context, assumed not null
     * @param shader OpenGL handle to a shader
     * @return true if shader was compiled without errors
     */
    public static boolean isShaderCompiled(final GL2ES2 gl, final int shader) {
        return ShaderUtil.isShaderStatusValid(gl, shader, GL2ES2.GL_COMPILE_STATUS, null);
    }

    /**
     * Checks that a shader program was linked successfully.
     *
     * @param gl      OpenGL context, assumed not null
     * @param program OpenGL handle to a shader program
     * @return true if program was linked successfully
     */
    public static boolean isProgramLinked(final GL2ES2 gl, final int program) {
        return ShaderUtil.isProgramStatusValid(gl, program, GL2ES2.GL_LINK_STATUS);
    }

    /**
     * Checks that a shader program was validated successfully.
     *
     * @param gl      OpenGL context, assumed not null
     * @param program OpenGL handle to a shader program
     * @return true if program was validated successfully
     */
    public static boolean isProgramValidated(final GL2ES2 gl, final int program) {
        return ShaderUtil.isProgramStatusValid(gl, program, GL2ES2.GL_VALIDATE_STATUS);
    }

    /**
     * Loads a shader program from a pair of strings.
     *
     * @param gl                   Current OpenGL context
     * @param vertexShaderSource   Vertex shader source
     * @param fragmentShaderSource Fragment shader source
     * @return OpenGL handle to the shader program, not negative
     * @throws NullPointerException     if context or either source is null
     * @throws IllegalArgumentException if either source is empty
     * @throws GLException              if program did not compile, link, or validate successfully
     */
    /*@Nonnegative*/
    public static int loadProgram(final GL2ES2 gl, final String vertexShaderSource, final String fragmentShaderSource) {
        // Create the shaders
        final int vertexShaderId = loadShader(gl, vertexShaderSource, GL2ES2.GL_VERTEX_SHADER);
        final int fragmentShaderId = loadShader(gl, fragmentShaderSource, GL2ES2.GL_FRAGMENT_SHADER);

        // Create a program and attach the shaders
        final int program = gl.glCreateProgram();
        gl.glAttachShader(program, vertexShaderId);
        gl.glAttachShader(program, fragmentShaderId);

        // Link the program
        gl.glLinkProgram(program);
        if (!isProgramLinked(gl, program)) {
            final String log = ShaderUtil.getProgramInfoLog(gl, program);
            throw new GLException(log);
        }

        // Clean up the shaders
        gl.glDeleteShader(vertexShaderId);
        gl.glDeleteShader(fragmentShaderId);
        return program;
    }

    /**
     * Loads a shader from a string.
     *
     * @param gl     Current OpenGL context, assumed not null
     * @param source Source code of the shader as one long string, assumed not null or empty
     * @param type   Type of shader, assumed valid
     * @return OpenGL handle to the shader, not negative
     * @throws GLException if a GLSL-capable context is not active or could not compile shader
     */
    private static int loadShader(final GL2ES2 gl, final String source, final int type) {
        final int shader = gl.glCreateShader(type);
        gl.glShaderSource(shader,
                1,
                new String[]{source},
                null);

        gl.glCompileShader(shader);
        if (!isShaderCompiled(gl, shader)) {
            final String log = ShaderUtil.getShaderInfoLog(gl, shader);
            throw new GLException(log);
        }

        return shader;
    }
}