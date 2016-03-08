/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import java.util.ArrayList;
import java.util.Collection;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class Highlight extends Renderable {

    GcodeModel model;

    private Collection<Integer> highlightedLines = null;

    private int numberOfVertices = -1;
    private float[] lineVertexData = null;

    public Highlight(GcodeModel model) {
        super(9);
        this.model = model;
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
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor) {
        if (lineVertexData == null || highlightedLines == null || highlightedLines.size() == 0) {
            return;
        }

        GL2 gl = drawable.getGL().getGL2();

        gl.glDisable(GL2.GL_LIGHTING);

        //gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glBegin(GL_LINES);
        gl.glLineWidth(2.0f);

        byte[] c = GcodeModel.Color.YELLOW.getBytes();
        for (int verts = 0; verts < (this.numberOfVertices * 3); ) {
            gl.glColor3ub(c[0],c[1],c[2]);
            gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
            gl.glColor3ub(c[0],c[1],c[2]);
            gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
        }

        gl.glEnd();
    }

    public void setHighlightedLines(Collection<Integer> lines) {
        this.highlightedLines = lines;

        if (lines.size() == 0) {
            this.numberOfVertices = -1;
            this.lineVertexData = null;
            return;
        }

        ArrayList<LineSegment> highlights = new ArrayList<>();
        int vertIndex = 0;
        for(LineSegment ls : model.getLineList()) {
            if (lines.contains(ls.getLineNumber())) {
                highlights.add(ls);
            }
        }

        this.numberOfVertices = highlights.size() * 2;
        this.lineVertexData = new float[numberOfVertices * 3];

        for (LineSegment ls : highlights) {
            System.out.println("Line number: " + ls.getLineNumber());
            Point3d p1 = ls.getStart();
            Point3d p2 = ls.getEnd();

            // p1 location
            lineVertexData[vertIndex++] = (float)p1.x;
            lineVertexData[vertIndex++] = (float)p1.y;
            lineVertexData[vertIndex++] = (float)p1.z;
            //p2
            lineVertexData[vertIndex++] = (float)p2.x;
            lineVertexData[vertIndex++] = (float)p2.y;
            lineVertexData[vertIndex++] = (float)p2.z;
        }
    }
}
