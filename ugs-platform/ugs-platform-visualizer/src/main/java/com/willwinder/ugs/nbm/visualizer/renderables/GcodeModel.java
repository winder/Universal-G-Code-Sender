/*
    Copyright 2016-2024 Will Winder

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
import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_MODEL;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wwinder
 */
public class GcodeModel extends Renderable implements UGSEventListener {
    public static final double ARC_SEGMENT_LENGTH = 0.8;
    private static final Logger logger = Logger.getLogger(GcodeModel.class.getName());
    private final GcodeLineColorizer colorizer = new GcodeLineColorizer();
    private final BackendAPI backend;
    private boolean colorArrayDirty;
    private boolean vertexArrayDirty;
    private boolean vertexBufferDirty;
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

    public GcodeModel(String title, BackendAPI backend) {
        super(10, title, VISUALIZER_OPTION_MODEL);
        objectSize = new Position(0, 0, 0);
        reloadPreferences(new VisualizerOptions());
        this.backend = backend;
        backend.addUGSEventListener(this);
    }

    @Override
    public final void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);
        colorizer.reloadPreferences(vo);
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
        if (gl.isFunctionAvailable("glGenBuffers")
                && gl.isFunctionAvailable("glBindBuffer")
                && gl.isFunctionAvailable("glBufferData")
                && gl.isFunctionAvailable("glDeleteBuffers")) {
            gl.glEnableClientState(GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL_COLOR_ARRAY);

            // Initialize OpenGL arrays if required.
            if (this.vertexBufferDirty && !vertexArrayDirty && !colorArrayDirty) {
                updateVertexBuffers();
                this.vertexBufferDirty = false;
            }
            if (this.colorArrayDirty) {
                this.updateGLColorArray();
                this.colorArrayDirty = false;
            }
            if (this.vertexArrayDirty) {
                this.updateGLGeometryArray();
                this.vertexArrayDirty = false;
            }
            gl.glLineWidth(1.0f);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, lineVertexBuffer);
            gl.glColorPointer(4, GL.GL_UNSIGNED_BYTE, 0, lineColorBuffer);
            gl.glDrawArrays(GL.GL_LINES, 0, numberOfVertices);
            gl.glDisableClientState(GL_COLOR_ARRAY);
            gl.glDisableClientState(GL_VERTEX_ARRAY);
        }
        // Traditional OpenGL
        else {
            gl.glBegin(GL_LINES);
            gl.glLineWidth(1.0f);

            int verts = 0;
            int colors = 0;
            for (int i = 0; i < pointList.size(); i++) {
                gl.glColor4ub(lineColorData[colors++], lineColorData[colors++], lineColorData[colors++], lineColorData[colors++]);
                gl.glVertex3d(lineVertexData[verts++], lineVertexData[verts++], lineVertexData[verts++]);
            }
            gl.glEnd();
        }
    }

    public Position getMin() {
        return this.objectMin;
    }

    public Position getMax() {
        return this.objectMax;
    }

    /**
     * Parse the gcodeFile and store the resulting geometry and data about it.
     */
    private boolean generateObject() {
        isDrawable = false;
        if (this.gcodeFile == null) {
            return false;
        }

        try {
            logger.log(Level.INFO, "About to process {}", gcodeFile);
            GcodeViewParse gcvp = new GcodeViewParse();
            gcodeLineList = loadModel(gcvp);

            // Convert LineSegments to points.
            this.pointList = new ArrayList<>(gcodeLineList.size());

            for (LineSegment ls : gcodeLineList) {
                this.pointList.add(VisualizerUtils.toCartesian(ls));
            }
            gcodeLineList = pointList;

            this.objectMin = gcvp.getMinimumExtremes();
            this.objectMax = gcvp.getMaximumExtremes();
            this.colorizer.setMaxSpindleSpeed(gcvp.getMaxSpindleSpeed());
            this.colorizer.setMaxFeedRate(gcvp.getMaxFeedRate());

            if (gcodeLineList.isEmpty()) {
                return false;
            }

            logger.info("Object bounds: X (" + objectMin.x + ", " + objectMax.x + ")");
            logger.info("               Y (" + objectMin.y + ", " + objectMax.y + ")");
            logger.info("               Z (" + objectMin.z + ", " + objectMax.z + ")");

            Position center = VisualizerUtils.findCenter(objectMin, objectMax);
            logger.info("Center = " + center);
            logger.info("Num Line Segments :" + gcodeLineList.size());

            objectSize.x = this.objectMax.x - this.objectMin.x;
            objectSize.y = this.objectMax.y - this.objectMin.y;
            objectSize.z = this.objectMax.z - this.objectMin.z;

            // Now that the object is known, fill the buffers.
            this.isDrawable = true;

            this.numberOfVertices = gcodeLineList.size() * 2;
            this.lineVertexData = new float[numberOfVertices * 4];
            this.lineColorData = new byte[numberOfVertices * 4];

            this.updateVertexBuffers();
        } catch (GcodeParserException | IOException e) {
            String error = Localization.getString("mainWindow.error.openingFile") + " : " + e.getLocalizedMessage();
            logger.log(Level.SEVERE, error, e);
            GUIHelpers.displayErrorDialog(error);
            return false;
        }

        return true;
    }

    private List<LineSegment> loadModel(GcodeViewParse gcvp) throws IOException, GcodeParserException {
        try (IGcodeStreamReader gsr = new GcodeStreamReader(new File(gcodeFile), new DefaultCommandCreator())) {
            return gcvp.toObjFromReader(gsr, ARC_SEGMENT_LENGTH);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> linesInFile;
            linesInFile = VisualizerUtils.readFiletoArrayList(this.gcodeFile);
            return gcvp.toObjRedux(linesInFile, ARC_SEGMENT_LENGTH);
        }
    }

    /**
     * Convert the gcodeLineList into vertex and color arrays.
     */
    private void updateVertexBuffers() {
        if (this.isDrawable) {
            int vertIndex = 0;
            int colorIndex = 0;
            byte[] c = new byte[4];
            Position workPosition = backend.getWorkPosition();
            for (LineSegment ls : gcodeLineList) {
                Color color = colorizer.getColor(ls, this.currentCommandNumber);

                Position p1 = addMissingCoordinateFromWorkPosition(ls.getStart(), workPosition);
                Position p2 = addMissingCoordinateFromWorkPosition(ls.getEnd(), workPosition);

                c[0] = (byte) color.getRed();
                c[1] = (byte) color.getGreen();
                c[2] = (byte) color.getBlue();
                c[3] = (byte) color.getAlpha();

                // colors
                //p1
                lineColorData[colorIndex++] = c[0];
                lineColorData[colorIndex++] = c[1];
                lineColorData[colorIndex++] = c[2];
                lineColorData[colorIndex++] = c[3];

                //p2
                lineColorData[colorIndex++] = c[0];
                lineColorData[colorIndex++] = c[1];
                lineColorData[colorIndex++] = c[2];
                lineColorData[colorIndex++] = c[3];

                // p1 location
                lineVertexData[vertIndex++] = (float) p1.x;
                lineVertexData[vertIndex++] = (float) p1.y;
                lineVertexData[vertIndex++] = (float) p1.z;
                //p2
                lineVertexData[vertIndex++] = (float) p2.x;
                lineVertexData[vertIndex++] = (float) p2.y;
                lineVertexData[vertIndex++] = (float) p2.z;
            }

            this.colorArrayDirty = true;
            this.vertexArrayDirty = true;
        }
    }

    private Position addMissingCoordinateFromWorkPosition(Position position, Position workPosition) {
        if (!Double.isNaN(position.getX()) && Double.isNaN(position.getY())&& Double.isNaN(position.getZ())) {
            return position;
        }

        Position result = new Position(position);
        if (Double.isNaN(result.getX())) {
            result.setX(workPosition.getX());
        }
        if (Double.isNaN(result.getY())) {
            result.setY(workPosition.getY());
        }
        if (Double.isNaN(result.getZ())) {
            result.setZ(workPosition.getZ());
        }
        return result;
    }

    /**
     * Initialize or update open gl geometry array in native buffer objects.
     */
    private void updateGLGeometryArray() {
        // Reset buffer and set to null of new geometry doesn't fit.
        if (lineVertexBuffer != null) {
            ((Buffer) lineVertexBuffer).clear();
            if (lineVertexBuffer.remaining() < lineVertexData.length) {
                lineVertexBuffer = null;
            }
        }

        if (lineVertexBuffer == null) {
            lineVertexBuffer = Buffers.newDirectFloatBuffer(lineVertexData.length);
        }

        lineVertexBuffer.put(lineVertexData);
        ((Buffer) lineVertexBuffer).flip();
    }

    /**
     * Initialize or update open gl color array in native buffer objects.
     */
    private void updateGLColorArray() {
        // Reset buffer and set to null of new colors don't fit.
        if (lineColorBuffer != null) {
            ((Buffer) lineColorBuffer).clear();

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
        ((Buffer) lineColorBuffer).flip();

    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent stateEvent) {
            if (stateEvent.getPreviousState() != ControllerState.RUN && stateEvent.getPreviousState() != ControllerState.JOG) {
                return;
            }
            if (stateEvent.getState() != ControllerState.IDLE) {
                return;
            }
            vertexBufferDirty = true;
        }
    }
}
