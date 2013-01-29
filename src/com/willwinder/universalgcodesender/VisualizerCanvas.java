/*
 * 3D Canvas for GCode Visualizer.
 *
 * Created on Jan 29, 2013, 3:04:38 PM
 */

/*
    Copywrite 2013 Will Winder

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

package com.willwinder.universalgcodesender;

import static javax.media.opengl.GL.*; // GL2 constants
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import javax.media.opengl.glu.GLU;
 
/**
 *
 * @author wwinder
 * @template http://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html
 * 
 * JOGL 2.0 Program Template (GLCanvas)
 * This is a "Component" which can be added into a top-level "Container".
 * It also handles the OpenGL events to render graphics.
 */
@SuppressWarnings("serial")
public class VisualizerCanvas extends GLCanvas implements GLEventListener {
    private GLU glu;  // for the GL Utility
    private float angle = 0.0f;  // rotation angle of the triangle
    /** Constructor to setup the GUI for this Component */
    public VisualizerCanvas() {
       this.addGLEventListener(this);
    }

    // ------ Implement methods declared in GLEventListener ------

    /**
     * Called back immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
       GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
       glu = new GLU();                         // get GL Utilities
       gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
       gl.glClearDepth(1.0f);      // set clear depth value to farthest
       gl.glEnable(GL_DEPTH_TEST); // enables depth testing
       gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
       gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
       gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting

       // ----- Your OpenGL initialization code here -----
    }

    /**
     * Call-back handler for window re-size event. Also called when the drawable is
     * first set to visible.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
       GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

       if (height == 0) height = 1;   // prevent divide by zero
       float aspect = (float)width / height;

       // Set the view port (display area) to cover the entire window
       gl.glViewport(0, 0, width, height);

       // Setup perspective projection, with aspect ratio matches viewport
       gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
       gl.glLoadIdentity();             // reset projection matrix
       glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

       // Enable the model-view transform
       gl.glMatrixMode(GL_MODELVIEW);
       gl.glLoadIdentity(); // reset
    }

    /**
     * Called back by the animator to perform rendering.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        render(drawable);
        update();
    }

    // Render a triangle
    private void render(GLAutoDrawable drawable) {
        // Get the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();
        // Clear the color and the depth buffers
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Reset the view (x, y, z axes back to normal)
        gl.glLoadIdentity();   

        // Draw a triangle
        float sin = (float)Math.sin(angle);
        float cos = (float)Math.cos(angle);
        gl.glTranslatef(0.0f, 0.0f, -6.0f); // translate into the screen
        gl.glBegin(GL_TRIANGLES);
            gl.glColor3f(1.0f, 0.0f, 0.0f);   // Red
            gl.glVertex2d(-cos, -cos);
            gl.glColor3f(0.0f, 1.0f, 0.0f);   // Green
            gl.glVertex2d(0.0f, cos);
            gl.glColor3f(0.0f, 0.0f, 1.0f);   // Blue
            gl.glVertex2d(sin, -sin);
        gl.glEnd();
    }

    // Update the angle of the triangle after each frame
    private void update() {
        angle += 0.01f;
    }

    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { }
}