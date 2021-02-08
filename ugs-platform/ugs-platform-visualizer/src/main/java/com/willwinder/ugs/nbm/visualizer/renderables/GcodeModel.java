/*
    Copyright 2016-2020 Will Winder

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

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.*;
import com.willwinder.ugs.nbm.visualizer.shared.RotationService;

import java.util.ArrayList;

/**
 *
 * @author wwinder
 */
public class GcodeModel extends Renderable {
    private static final Logger logger = Logger.getLogger(GcodeModel.class.getName());

    private final RotationService rs;

    private boolean forceOldStyle = false;
    private boolean colorArrayDirty, vertexArrayDirty, vertexBufferDirty;

    // Gcode file data
    private String gcodeFile = null;
    private boolean isDrawable = false; //True if a file is loaded; false if not

    // TODO: don't save the line list.
    private List<LineSegment> gcodeLineList; //An ArrayList of linesegments composing the model
    private List<LineSegment> pointList; //An ArrayList of linesegments composing the model
    private int currentCommandNumber = 0;

    // OpenGL Object Buffer Variables
    private int numberOfVertices = -1;
    private float[] lineVertexData = null;
    private byte[] lineColorData = null;
    private FloatBuffer lineVertexBuffer = null;
    private ByteBuffer lineColorBuffer = null;

    private Position objectMin;
    private Position objectMax;
    private Position objectSize;

    // Preferences
    private Color linearColor;
    private Color rapidColor;
    private Color arcColor;
    private Color plungeColor;
    private Color completedColor;

    public GcodeModel(String title, RotationService rs) {
        super(10, title);
        objectSize = new Position(0, 0, 0);
        this.rs = rs;
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    final public void reloadPreferences(VisualizerOptions vo) {
        linearColor = vo.getOptionForKey(VISUALIZER_OPTION_LINEAR).value;
        rapidColor = vo.getOptionForKey(VISUALIZER_OPTION_RAPID).value;
        arcColor = vo.getOptionForKey(VISUALIZER_OPTION_ARC).value;
        plungeColor = vo.getOptionForKey(VISUALIZER_OPTION_PLUNGE).value;
        completedColor = vo.getOptionForKey(VISUALIZER_OPTION_COMPLETE).value;
        vertexBufferDirty = true;
    }

    /**
     * Assign a gcode file to drawing.
     */
    public boolean setGcodeFile(String file) {
        this.gcodeFile = file;
        this.isDrawable = false;
        this.currentCommandNumber = 0;

        boolean result = generateObject();

        // Force a display in case an animator isn't running.
        //forceRedraw();

        logger.log(Level.INFO, "Done setting gcode file.");
        return result;
    }

    /**
     * This is used to gray out completed commands.
     */
    public void setCurrentCommandNumber(int num) {
        currentCommandNumber = num;
        vertexBufferDirty = true;
    }

    public List<LineSegment> getLineList() {
        return this.pointList != null ? this.pointList : Collections.emptyList();
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
        generateObject();
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
        if (!isDrawable) return;

        GL2 gl = drawable.getGL().getGL2();

        // Batch mode if available
        boolean forceOldStyle = false;
        if(!forceOldStyle
                && gl.isFunctionAvailable( "glGenBuffers" )
                && gl.isFunctionAvailable( "glBindBuffer" )
                && gl.isFunctionAvailable( "glBufferData" )
                && gl.isFunctionAvailable( "glDeleteBuffers" ) ) {

            // Initialize OpenGL arrays if required.
            if (this.vertexBufferDirty && !vertexArrayDirty && !colorArrayDirty) {
                updateVertexBuffers();
                this.vertexBufferDirty = false;
            }
            if (this.colorArrayDirty) {
                this.updateGLColorArray(drawable);
                this.colorArrayDirty = false;
            }
            if (this.vertexArrayDirty) {
                this.updateGLGeometryArray(drawable);
                this.vertexArrayDirty = false;
            }
            gl.glLineWidth(1.0f);
            gl.glEnableClientState(GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL_COLOR_ARRAY);
            gl.glDrawArrays( GL.GL_LINES, 0, numberOfVertices);
            gl.glDisableClientState(GL_COLOR_ARRAY);
            gl.glDisableClientState(GL_VERTEX_ARRAY);
        }
        // Traditional OpenGL
        else {

            // TODO: By using a GL_LINE_STRIP I can easily use half the number of
            //       verticies. May lose some control over line colors though.
            //gl.glEnable(GL2.GL_LINE_SMOOTH);
            gl.glBegin(GL_LINES);
            gl.glLineWidth(1.0f);

            int verts = 0;
            int colors = 0;
            for (int i = 0; i < pointList.size(); i++) {
                gl.glColor3ub(lineColorData[colors++],lineColorData[colors++],lineColorData[colors++]);
                gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
                gl.glColor3ub(lineColorData[colors++],lineColorData[colors++],lineColorData[colors++]);
                gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
            }

            gl.glEnd();
        }

        // makes the gui stay on top of elements
        // drawn before.
    }
    
    public Position getMin() {
        return this.objectMin;
    }

    public Position getMax() {
        return this.objectMax;
    }

    private static double sinIfNotZero(double angle) {
      return angle == 0 ? 0.0 : Math.sin(Math.toRadians(angle));
    }

    private static double cosIfNotZero(double angle) {
      return angle == 0 ? 0.0 : Math.cos(Math.toRadians(angle));
    }

    public static LineSegment toCartesian(LineSegment p) {
        Position start = new Position(p.getStart().x, p.getStart().y, p.getStart().z);
        Position end = new Position(p.getEnd().x, p.getEnd().y, p.getEnd().z);

        if (hasRotation(p.getStart()) || hasRotation(p.getEnd())) {
          double sx = p.getStart().x;
          double sy = p.getStart().y;
          double sz = p.getStart().z;
          double sa = p.getStart().a;
          double sb = p.getStart().b;
          double sc = p.getStart().c;
          double sSinA = sinIfNotZero(sa);
          double sCosA = cosIfNotZero(sa);
          double sSinB = sinIfNotZero(sb);
          double sCosB = cosIfNotZero(sb);
          double sSinC = sinIfNotZero(sc);
          double sCosC = cosIfNotZero(sc);

          double ex = p.getEnd().x;
          double ey = p.getEnd().y;
          double ez = p.getEnd().z;
          double ea = p.getEnd().a;
          double eb = p.getEnd().b;
          double ec = p.getEnd().c;
          double eSinA = sinIfNotZero(ea);
          double eCosA = cosIfNotZero(ea);
          double eSinB = sinIfNotZero(eb);
          double eCosB = cosIfNotZero(eb);
          double eSinC = sinIfNotZero(ec);
          double eCosC = cosIfNotZero(ec);

          // X-Axis rotation
          // x1 = x0
          // y1 = y0cos(u) − z0sin(u)
          // z1 = y0sin(u) + z0cos(u)	
          if (sa != 0) {
            start.y = sy * sCosA - sz * sSinA;
            start.z = sy * sSinA + sz * sCosA;
          }
          if (ea != 0) {
            end.y = ey * eCosA - ez * eSinA;
            end.z = ey * eSinA + ez * eCosA;
            
          }

          // Y-Axis rotation
          // x2 = x1cos(v) + z1sin(v)
          // y2 = y1
          // z2 = − x1sin(v) + z1cos(v)	
          if (sb != 0) {
            start.x = sx * sCosB + sz * sSinB;
            start.z = -1 * sx * sSinB + sz * sCosB;
          }
          if (eb != 0) {
            end.x = ex * eCosB + ez * eSinB;
            end.z = -1 * ex * eSinB + ez * eCosB;
          }
          
          // Z-Axis rotation
          // x3 = x2cos(w) − y2sin(w)
          // y3 = x2sin(w) + y2cos(w)
          // z3 = z2	
          if (sc != 0) {
            start.x = sx * sCosC - sy * sSinC;
            start.y = sx * sSinC + sy * sCosC;
          }
          if (ec != 0) {
            end.x = ex * eCosC - ey * eSinC;
            end.y = ex * eSinC + ey * eCosC;
          }
        }

        // TODO: Somehow figure out how to optimize the way Position, Point3d, PointSegment and LineSegment are used.
        LineSegment next = new LineSegment(start, end, p.getLineNumber());
        next.setIsArc(p.isArc());
        next.setIsFastTraverse(p.isFastTraverse());
        next.setIsRotation(p.isFastTraverse());
        next.setIsZMovement(p.isZMovement());
        next.setSpeed(p.getSpeed());

        return next;
    }

    /**
     * Returns true if the position has any rotation
     *
     * @param position
     * @return true if the position contains rotations
     */
    private static boolean hasRotation(Position position) {
        return position.a != 0 || position.b != 0 || position.c != 0;
    }

    /**
     * Parse the gcodeFile and store the resulting geometry and data about it.
     */
    private boolean generateObject()
    {
        isDrawable = false;
        if (this.gcodeFile == null){ return false; }

        try {
            GcodeViewParse gcvp = new GcodeViewParse();
            logger.log(Level.INFO, "About to process {}", gcodeFile);
            try {
                IGcodeStreamReader gsr = new GcodeStreamReader(new File(gcodeFile));
                gcodeLineList = gcvp.toObjFromReader(gsr, 0.3);
            } catch (GcodeStreamReader.NotGcodeStreamFile e) {
                List<String> linesInFile;
                linesInFile = VisualizerUtils.readFiletoArrayList(this.gcodeFile);
                gcodeLineList = gcvp.toObjRedux(linesInFile, 0.3);
            }

            // Convert LineSegments to points.
            this.pointList = new ArrayList<>(gcodeLineList.size());

            for (LineSegment ls : gcodeLineList) {
              this.pointList.add(GcodeModel.toCartesian(ls));
            }
            gcodeLineList = pointList;

            this.objectMin = gcvp.getMinimumExtremes();
            this.objectMax = gcvp.getMaximumExtremes();

            if (gcodeLineList.isEmpty()) {
                return false;
            }

            // Grab the line number off the last line.

            System.out.println("Object bounds: X ("+objectMin.x+", "+objectMax.x+")");
            System.out.println("               Y ("+objectMin.y+", "+objectMax.y+")");
            System.out.println("               Z ("+objectMin.z+", "+objectMax.z+")");

            Position center = VisualizerUtils.findCenter(objectMin, objectMax);
            System.out.println("Center = " + center.toString());
            System.out.println("Num Line Segments :" + gcodeLineList.size());

            objectSize.x = this.objectMax.x-this.objectMin.x;
            objectSize.y = this.objectMax.y-this.objectMin.y;
            objectSize.z = this.objectMax.z-this.objectMin.z;

            /*
            this.scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;

            this.dimensionsLabel = Localization.getString("VisualizerCanvas.dimensions") + ": "
                    + Localization.getString("VisualizerCanvas.width") + "=" + format.format(objectWidth) + " "
                    + Localization.getString("VisualizerCanvas.height") + "=" + format.format(objectHeight);
            */

            // Now that the object is known, fill the buffers.
            this.isDrawable = true;

            this.numberOfVertices = gcodeLineList.size() * 2;
            this.lineVertexData = new float[numberOfVertices * 3];
            this.lineColorData = new byte[numberOfVertices * 3];

            this.updateVertexBuffers();
        } catch (GcodeParserException | IOException e) {
            String error = Localization.getString("mainWindow.error.openingFile") + " : " + e.getLocalizedMessage();
            System.out.println(error);
            GUIHelpers.displayErrorDialog(error);
            return false;
        }

        return true;
    }

    /**
     * Convert the gcodeLineList into vertex and color arrays.
     */
    private void updateVertexBuffers() {
        if (this.isDrawable) {
            Color color;
            int vertIndex = 0;
            int colorIndex = 0;
            byte[] c = new byte[3];
            for (LineSegment ls : gcodeLineList) {
                // Find the lines color.
                if (ls.isArc()) {
                    color = arcColor;
                } else if (ls.isFastTraverse()) {
                    color = rapidColor;
                } else if (ls.isZMovement()) {
                    color = plungeColor;
                } else {
                    color = linearColor;
                }

                // Override color if it is cutoff
                if (ls.getLineNumber() < this.currentCommandNumber) {
                    color = completedColor;
                }

                // Draw it.
                {
                    Position p1 = ls.getStart();
                    Position p2 = ls.getEnd();

                    c[0] = (byte) color.getRed();
                    c[1] = (byte) color.getGreen();
                    c[2] = (byte) color.getBlue();

                    // colors
                    //p1
                    lineColorData[colorIndex++] = c[0];
                    lineColorData[colorIndex++] = c[1];
                    lineColorData[colorIndex++] = c[2];

                    //p2
                    lineColorData[colorIndex++] = c[0];
                    lineColorData[colorIndex++] = c[1];
                    lineColorData[colorIndex++] = c[2];

                    // p1 location
                    lineVertexData[vertIndex++] = (float) p1.x;
                    lineVertexData[vertIndex++] = (float) p1.y;
                    lineVertexData[vertIndex++] = (float) p1.z;
                    //p2
                    lineVertexData[vertIndex++] = (float) p2.x;
                    lineVertexData[vertIndex++] = (float) p2.y;
                    lineVertexData[vertIndex++] = (float) p2.z;
                }
            }

            this.colorArrayDirty = true;
            this.vertexArrayDirty = true;
        }
    }

    /**
     * Initialize or update open gl geometry array in native buffer objects.
     */
    private void updateGLGeometryArray(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Reset buffer and set to null of new geometry doesn't fit.
        if (lineVertexBuffer != null) {
            ((Buffer)lineVertexBuffer).clear();
            if (lineVertexBuffer.remaining() < lineVertexData.length) {
                lineVertexBuffer = null;
            }
        }

        if (lineVertexBuffer == null) {
            lineVertexBuffer = Buffers.newDirectFloatBuffer(lineVertexData.length);
        }

        lineVertexBuffer.put(lineVertexData);
        ((Buffer)lineVertexBuffer).flip();
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, lineVertexBuffer );
    }

    /**
     * Initialize or update open gl color array in native buffer objects.
     */
    private void updateGLColorArray(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        // Reset buffer and set to null of new colors don't fit.
        if (lineColorBuffer != null) {
            ((Buffer)lineColorBuffer).clear();

            if (lineColorBuffer.remaining() < lineColorData.length) {
                lineColorBuffer = null;
            }
        }

        if (lineColorBuffer == null) {
            if (lineColorData == null) {
                updateVertexBuffers();
            }
            lineColorBuffer = Buffers.newDirectByteBuffer(this.lineColorData.length);
        }

        lineColorBuffer.put(lineColorData);
        ((Buffer)lineColorBuffer).flip();
        gl.glColorPointer( 3, GL.GL_UNSIGNED_BYTE, 0, lineColorBuffer );
    }
}
