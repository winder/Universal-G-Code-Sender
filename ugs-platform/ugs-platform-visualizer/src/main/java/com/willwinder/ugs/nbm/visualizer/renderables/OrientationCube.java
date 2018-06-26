/**
 * Draw a cube with the orientation labeled on the sides.
 */

/*
    Copyright 2016-2017 Will Winder

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

package com.willwinder.ugs.nbm.visualizer.renderables;

import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.Position;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

public class OrientationCube extends Renderable {
  private final float size;
  private final float[] color = {0.8f, 0.8f, 0.8f};
  private final float[] border = {0.2f, 0.2f, 0.2f};

  private TextRenderer renderer;
  private float textScaleFactor;

  public OrientationCube(float s, String title) {
    super(Integer.MIN_VALUE, title);  
    size = s;
  }

  @Override
  public void reloadPreferences(VisualizerOptions vo) {

  }

  @Override
  public boolean rotate() {
      return true;
  }

  @Override
  public boolean center() {
      return false;
  }

  @Override
  public void init(GLAutoDrawable drawable) {
    renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
    renderer.setColor(0.2f, 0.2f, 0.2f, 1f);

    // Compute the scale factor of the largest string which will make
    // them all fit on the faces of the cube
    Rectangle2D bounds = renderer.getBounds("Z+");
    float w = (float) bounds.getWidth();

    textScaleFactor = size / (w * 1.7f);
  }

  @Override
  public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
    GL2 gl = drawable.getGL().getGL2();

    int ySize = drawable.getDelegatedDrawable().getSurfaceHeight();
    int xSize = drawable.getDelegatedDrawable().getSurfaceWidth();

    // Set viewport to the corner.
    float fromEdge = 0.8f;
    int squareSize = ySize-(int)(ySize*fromEdge);
    gl.glViewport(0, (int)(ySize*fromEdge), squareSize, squareSize);

    gl.glPushMatrix();
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-0.5, 0.5, -0.5, 0.5, -0.5, 0.5); //, maxSide, maxSide, maxSide, maxSide, maxSide);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        
        drawCube(gl);
    gl.glPopMatrix();

    gl.glViewport(0, 0, xSize, ySize);
  }

  private void drawCube(GL2 gl) {
    // Six faces of cube
    // Top face
    gl.glPushMatrix();
    gl.glRotatef(-90, 1, 0, 0);
    gl.glRotatef(180, 0, 0, 1);
    drawFace(gl, size, color, border, "Y+");
    gl.glPopMatrix();

    // Front face
    drawFace(gl, size, color, border, "Z+");

    // Right face
    gl.glPushMatrix();
    gl.glRotatef(90, 0, 1, 0);

    gl.glPushMatrix();
    gl.glRotatef(90, 0, 0, 1);
    drawFace(gl, size, color, border, "X+");
    gl.glPopMatrix();

    // Back face    
    gl.glRotatef(90, 0, 1, 0);
    gl.glPushMatrix();
    gl.glRotatef(180, 0, 0, 1);
    drawFace(gl, size, color, border, "Z-");
    gl.glPopMatrix();

    // Left face    
    gl.glRotatef(90, 0, 1, 0);
    gl.glRotatef(-90, 0, 0, 1);
    drawFace(gl, size, color, border, "X-");
    gl.glPopMatrix();
    // Bottom face
    gl.glPushMatrix();
    gl.glRotatef(90, 1, 0, 0);
    drawFace(gl, size, color, border, "Y-");
    gl.glPopMatrix();
  }

  private void drawFace(GL2 gl,
                        float faceSize,
                        float[] color,
                        float[] border,
                        String text) {
    float halfFaceSize = faceSize / 2;
    float borderSize = halfFaceSize * 0.8f;
    float layer2 = halfFaceSize + 0.001f;
    // Face is centered around the local coordinate system's z axis,
    // at a z depth of faceSize / 2
    gl.glColor3f(border[0], border[1], border[2]);
    gl.glBegin(GL_QUADS);
    gl.glVertex3f(-halfFaceSize, -halfFaceSize, halfFaceSize);
    gl.glVertex3f( halfFaceSize, -halfFaceSize, halfFaceSize);
    gl.glVertex3f( halfFaceSize,  halfFaceSize, halfFaceSize);
    gl.glVertex3f(-halfFaceSize,  halfFaceSize, halfFaceSize);
    gl.glEnd();

    gl.glColor3f(color[0], color[1], color[2]);
    gl.glBegin(GL_QUADS);
    gl.glVertex3f(-borderSize, -borderSize, layer2);
    gl.glVertex3f( borderSize, -borderSize, layer2);
    gl.glVertex3f( borderSize,  borderSize, layer2);
    gl.glVertex3f(-borderSize,  borderSize, layer2);
    gl.glEnd();

    // Now draw the overlaid text. In this setting, we don't want the
    // text on the backward-facing faces to be visible, so we enable
    // back-face culling; and since we're drawing the text over other
    // geometry, to avoid z-fighting we disable the depth test. We
    // could plausibly also use glPolygonOffset but this is simpler.
    // Note that because the TextRenderer pushes the enable state
    // internally we don't have to reset the depth test or cull face
    // bits after we're done.
    renderer.begin3DRendering();
    gl.glDisable(GL_DEPTH_TEST);
    gl.glEnable(GL_CULL_FACE);
    // Note that the defaults for glCullFace and glFrontFace are
    // GL_BACK and GL_CCW, which match the TextRenderer's definition
    // of front-facing text.
    Rectangle2D bounds = renderer.getBounds(text);
    float w = (float) bounds.getWidth();
    float h = (float) bounds.getHeight();
    renderer.draw3D(text,
                    w / -2.0f * textScaleFactor,
                    h / -2.0f * textScaleFactor,
                    layer2,
                    textScaleFactor);
    renderer.end3DRendering();
    gl.glDisable(GL_CULL_FACE);
    gl.glEnable(GL_DEPTH_TEST);
  }
}
