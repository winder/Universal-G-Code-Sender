/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

/**
 *
 * @author wwinder
 */
public class Tool extends Renderable {
    private GLU glu;
    GLUquadric gq;

    public Tool() {
        super(9);
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
    public void draw(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(gl.GL_LIGHTING); 
        byte color[] = VisualizerUtils.Color.YELLOW.getBytes();
        
        gl.glPushMatrix();
            gl.glScaled(1./scaleFactor, 1./scaleFactor, 1./scaleFactor);
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);

            gl.glColor3f(1f, 1f, 0f);
            glu.gluQuadricNormals(gq, glu.GLU_SMOOTH);
            glu.gluCylinder(gq, 0f, .1f, .25, 16, 1);
        gl.glPopMatrix();
        gl.glDisable(gl.GL_LIGHTING); 

        /*
        // The ugly yellow line. RIP.
        gl.glBegin(GL_LINES);
        
            gl.glLineWidth(8.0f);
            gl.glColor3ub(color[0], color[1], color[2]);
            gl.glVertex3d(this.workCoord.x, this.workCoord.y, this.workCoord.z);
            gl.glColor3ub(color[0], color[1], color[2]);
            gl.glVertex3d(this.workCoord.x, this.workCoord.y, this.workCoord.z+(1.0/this.scaleFactor));
            
        gl.glEnd();
        */
    }
}
