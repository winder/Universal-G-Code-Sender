/*
    Copyright 2016-2022 Will Winder

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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.CNCPoint;
import com.willwinder.universalgcodesender.model.Position;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_HIGHLIGHT;
import static com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils.getAngle;

/**
 * Highlights the selected lines in the editor. It will attempt to buffer the lines with quads to make them more visible
 * because GL's line width is platform dependant and is also deprecated.
 *
 * @author wwinder
 */
public class Highlight extends Renderable {

    public static final double LINE_WIDTH = 0.004d;

    private final GcodeModel model;
    private final List<CNCPoint> points = Collections.synchronizedList(new ArrayList<>());

    // Preferences
    private Color highlightColor = Color.YELLOW;
    private double scaleFactor = 0.1;
    private int startLine = 0;
    private int endLine = 0;

    public Highlight(GcodeModel model, String title) {
        super(9, title);
        this.model = model;
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    public final void reloadPreferences(VisualizerOptions vo) {
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
        // Not used
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
        if (points.isEmpty() && startLine > 0 && endLine > 0) {
            return;
        }

        // Scale was changed since last render, regenerate points
        if (this.scaleFactor != scaleFactor) {
            this.scaleFactor = scaleFactor;
            generateBufferedLines();
        }

        float[] c = VisualizerOptions.colorToFloatArray(highlightColor);
        GL2 gl = drawable.getGL().getGL2();
        gl.glBegin(GL2ES3.GL_QUADS);
            gl.glColor4fv(c, 0);
            points.forEach(point -> gl.glVertex3d(point.x, point.y, point.z));
        gl.glEnd();
    }


    public void setHighlightedLines(int startLine, int endLine) {
        if( this.startLine == startLine && this.endLine == endLine) {
            return;
        }

        this.startLine = startLine;
        this.endLine = endLine;
        generateBufferedLines();
    }

    private void generateBufferedLines() {
        points.clear();
        double offset = LINE_WIDTH / scaleFactor / 2d;
        double halfPI = Math.PI / 2d;
        List<CNCPoint> newPoints = model.getLineList().stream()
                .filter(ls -> ls.getLineNumber() > startLine && ls.getLineNumber() - 1 <= endLine)
                .flatMap(lineSegment -> {
                    double angle = getAngle(lineSegment.getStart(), lineSegment.getEnd(), new PlaneFormatter(Plane.XY));
                    Position xyOffset = new Position(offset * Math.cos(angle - halfPI), offset * Math.sin(angle - halfPI), 0.0);
                    Position zOffset = new Position(0, 0, 0.01);

                    CNCPoint aPoint = new Position(lineSegment.getStart()).sub(xyOffset).add(zOffset);
                    CNCPoint bPoint = new Position(lineSegment.getEnd()).sub(xyOffset).add(zOffset);
                    CNCPoint cPoint = new Position(lineSegment.getEnd()).add(xyOffset).add(zOffset);
                    CNCPoint dPoint = new Position(lineSegment.getStart()).add(xyOffset).add(zOffset);
                    return Stream.of(aPoint, bPoint, cPoint, dPoint);
                })
                .collect(Collectors.toList());

        points.addAll(newPoints);
    }
}
