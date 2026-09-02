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
 * What a {@link Renderable} draws through. This is the whole surface between the scene and the
 * graphics API, so nothing above the {@code render} package needs to know what is drawing.
 *
 * <p>Colours are {@code float[4]} RGBA in 0..1. Model matrices are column major {@code float[16]}
 * as built by {@link Mat4}; the context multiplies them with the camera's view projection.
 * Widths are in logical pixels, the context scales them for high DPI displays.
 *
 * <p>Meshes may be uploaded and released at any time on the JavaFX application thread, inside
 * or outside a frame.
 */
public interface RenderContext {

    Camera camera();

    Viewport viewport();

    MeshHandle upload(float[] vertices, VertexLayout layout);

    void release(MeshHandle mesh);

    /**
     * Draws a {@link VertexLayout#LINE} mesh in a single colour.
     */
    void drawLines(MeshHandle mesh, float[] model, float[] rgba, float widthPx);

    /**
     * Draws a {@link VertexLayout#LINE} mesh with the colour stored on each vertex.
     */
    void drawColoredLines(MeshHandle mesh, float[] model, float widthPx);

    /**
     * Draws a {@link VertexLayout#LINE} mesh with per vertex colours, including their alpha,
     * recolouring every segment whose command number is at or below {@code completedCommand}
     * with {@code completedRgba}. Pass a negative number when nothing has run.
     */
    void drawToolpath(MeshHandle mesh, float[] model, float widthPx, int completedCommand, float[] completedRgba);

    /**
     * Uploads an image for {@link #drawTextured}. The pixels are packed ARGB, row by row from
     * the top, as {@code BufferedImage.getRGB} returns them.
     */
    TextureHandle uploadTexture(int width, int height, int[] argb);

    void release(TextureHandle texture);

    /**
     * Draws a {@link VertexLayout#TEXTURED} mesh with the texture, faded to {@code opacity}.
     */
    void drawTextured(MeshHandle mesh, float[] model, TextureHandle texture, float opacity);

    /**
     * Draws a {@link VertexLayout#MESH} mesh, diffuse shaded from a fixed light when {@code lit}
     * and flat otherwise.
     */
    void drawTriangles(MeshHandle mesh, float[] model, float[] rgba, boolean lit);

    /**
     * Whether the following draws are depth tested against what has already been drawn. The
     * scene sets this per layer; a renderable may override it for its own draws.
     */
    void setDepthTest(boolean enabled);

    /**
     * Directs the following draws into a rectangle of the frame with its own view projection
     * and a cleared depth buffer, for insets such as the orientation cube. Must be paired with
     * {@link #endSubViewport()}.
     *
     * @param x              left edge in physical pixels of the frame
     * @param y              top edge in physical pixels of the frame
     * @param width          width in physical pixels
     * @param height         height in physical pixels
     * @param viewProjection the matrix that replaces the camera's while the sub viewport is active
     */
    void beginSubViewport(int x, int y, int width, int height, float[] viewProjection);

    /**
     * Returns to drawing the whole frame with the camera's view projection.
     */
    void endSubViewport();
}
