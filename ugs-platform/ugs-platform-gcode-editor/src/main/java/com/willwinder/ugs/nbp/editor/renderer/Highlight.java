/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.ugs.nbp.editor.renderer;

import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_HIGHLIGHT;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wwinder
 */
public class Highlight extends Renderable {

    private GcodeModel model;

    private Collection<Integer> highlightedLines = null;

    private int numberOfVertices = -1;
    private float[] lineVertexData = null;

    // Preferences
    private Color highlightColor;

    public Highlight(GcodeModel model, String title) {
        super(9, title);
        this.model = model;
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    final public void reloadPreferences(VisualizerOptions vo) {
        highlightColor = vo.getOptionForKey(VISUALIZER_OPTION_HIGHLIGHT).value;

    }

    @Override
    public boolean enableLighting() {
        return false;
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
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
        if (lineVertexData == null || highlightedLines == null || highlightedLines.isEmpty()) {
            return;
        }

        GL2 gl = drawable.getGL().getGL2();

        //gl.glEnable(GL2.GL_LINE_SMOOTH);
        gl.glBegin(GL_LINES);
        gl.glLineWidth(2.0f);

        float[] c = VisualizerOptions.colorToFloatArray(Color.YELLOW);
        for (int verts = 0; verts < (this.numberOfVertices * 3); ) {
            gl.glColor4fv(c, 0);
            gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
            gl.glColor4fv(c, 0);
            gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
        }

        gl.glEnd();
    }

    public void setHighlightedLines(Collection<Integer> lines) {
        this.highlightedLines = lines;

        if (lines.isEmpty()) {
            this.numberOfVertices = -1;
            this.lineVertexData = null;
            return;
        }

        ArrayList<LineSegment> highlights = new ArrayList<>();
        int vertIndex = 0;
        for (LineSegment ls : model.getLineList()) {
            if (lines.contains(ls.getLineNumber() -1)) {
                highlights.add(ls);
            }
        }

        this.numberOfVertices = highlights.size() * 2;
        this.lineVertexData = new float[numberOfVertices * 3];

        for (LineSegment ls : highlights) {
            //System.out.println("Line number: " + ls.getLineNumber());
            Position p1 = ls.getStart();
            Position p2 = ls.getEnd();

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
