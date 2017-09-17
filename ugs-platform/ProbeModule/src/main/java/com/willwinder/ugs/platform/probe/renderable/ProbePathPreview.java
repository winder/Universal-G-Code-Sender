/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.platform.probe.renderable;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class ProbePathPreview extends Renderable {
    private final GLUT glut;
    private GLU glu;
    GLUquadric gq;

    public ProbePathPreview(String title) {
        super(10, title);
        glut = new GLUT();
    }

    @Override
    public boolean rotate() {
        return true;
    }

    @Override
    public boolean center() {
        return true;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        glu = new GLU();
        gq = glu.gluNewQuadric();
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
        // Setup lighting
            //gl.glEnable(GL2.GL_LIGHTING); 
            float red[] = { 0.8f, 0.1f, 0.0f, 0.7f };
            gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, red, 0);

            gl.glShadeModel(GL2.GL_FLAT);

            gl.glNormal3f(0.0f, 0.0f, 1.0f);

            //glu.gluQuadricNormals(gq, GLU.GLU_SMOOTH);
            glut.glutSolidTeapot(5);
            //glut.glutSolidCube(5);
            //glu.
            //gl.glDisable(GL2.GL_LIGHTING); 
        gl.glPopMatrix();
    }
    
}
