/*
 * 3D Canvas for GCode Visualizer.
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013-2016 Will Winder

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
package com.willwinder.ugs.nbm.visualizer;



import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_LEQUAL;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_NICEST;
import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLEventListener;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_COLOR_ARRAY;
import static com.jogamp.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;
import com.jogamp.opengl.glu.GLU;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.uielements.FPSCounter;
import com.willwinder.universalgcodesender.uielements.Overlay;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author wwinder
 * 
 */
@SuppressWarnings("serial")
public class GcodeRenderer implements GLEventListener {
    private static final Logger logger = Logger.getLogger(GcodeRenderer.class.getName());
    
    static boolean ortho = true;
    static double orthoRotation = -45;
    static boolean forceOldStyle = false;
    static boolean debugCoordinates = false; // turn on coordinate debug output
    
    final static private DecimalFormat format = new DecimalFormat("####.00");

    // Machine data
    private Point3d machineCoord;
    private Point3d workCoord;
    
    // Gcode file data
    private String gcodeFile = null;
    private boolean processedFile = false; // True if the file should be opened with GcodeStreamReader.
    private boolean isDrawable = false; //True if a file is loaded; false if not
    private List<LineSegment> gcodeLineList; //An ArrayList of linesegments composing the model
    private int currentCommandNumber = 0;
    private int lastCommandNumber = 0;

    // GL Utility
    private GLU glu;
    GLAutoDrawable drawable = null;
    
    // Projection variables
    private Point3d center, eye;
    private Point3d objectMin, objectMax;
    private double maxSide;
    private int xSize, ySize;
    private double minArcLength;
    private double arcLength;

    // Scaling
    private double scaleFactor;
    private double scaleFactorBase;
    private double zoomMultiplier = 1;
    private boolean invertZoom = false; // TODO: Make configurable
    // const values until added to settings
    private final double minZoomMultiplier = 1;
    private final double maxZoomMultiplier = 30;
    private final double zoomIncrement = 0.2;

    // Movement
    private int panMouseButton = InputEvent.BUTTON2_MASK; // TODO: Make configurable
    private double panMultiplierX = 1;
    private double panMultiplierY = 1;
    private Vector3d translationVectorH;
    private Vector3d translationVectorV;

    // Mouse rotation data
    Point last;
    Point current;
    private Point3d rotation;

    // OpenGL Object Buffer Variables
    private int numberOfVertices = -1;
    private float[] lineVertexData = null;
    private byte[] lineColorData = null;
    private FloatBuffer lineVertexBuffer = null;
    private ByteBuffer lineColorBuffer = null;

    Collection<Integer> highlightedLines = null;
    
    // Track when arrays need to be updated due to changing data.
    private boolean colorArrayDirty = false;
    private boolean vertexArrayDirty = false;
    
    private FPSCounter fpsCounter;
    private Overlay overlay;
    private String dimensionsLabel = "";
    
    /**
     * Constructor.
     */
    public GcodeRenderer() {
       this.eye = new Point3d(0, 0, 1.5);
       this.center = new Point3d(0, 0, 0);
       
       this.workCoord = new Point3d(0, 0, 0);
       this.machineCoord = new Point3d(0, 0, 0);
       
       this.rotation = new Point3d(0.0, -30.0, 0.0);
       setVerticalTranslationVector();
       setHorizontalTranslationVector();
    }
    
    /**
     * This is used to gray out completed commands.
     */
    public void setCurrentCommandNumber(int num) {
        this.currentCommandNumber = num;
        this.createVertexBuffers();
        this.colorArrayDirty = true;
    }
    
    /**
     * Returns the last command number used for generating the gcode object.
     */
    public int getLastCommandNumber() {
        return this.lastCommandNumber;
    }
    
    public void setProcessedGcodeFile(String file) {
        this.processedFile = true;
        setFile(file);
    }
    public void setGcodeFile(String file) {
        this.processedFile = false;
        setFile(file);
    }

    public void forceRedraw() {
        this.createVertexBuffers();
        if (drawable != null) {
            drawable.display();
        }
    }

    /**
     * Assign a gcode file to drawing.
     */
    public void setFile(String file) {
        this.gcodeFile = file;
        this.isDrawable = false;
        this.currentCommandNumber = 0;
        this.lastCommandNumber = 0;
        
        generateObject();
        
        // Force a display in case an animator isn't running.
        forceRedraw();

        logger.log(Level.INFO, "Done setting gcode file.");
    }
    
    public void setWorkCoordinate(Position p) {
        this.workCoord.set(p.getPositionIn(Utils.Units.MM));
    }
    
    public void setMachineCoordinate(Position p) {
        this.machineCoord.set(p.getPositionIn(Utils.Units.MM));
    }

    // ------ Implement methods declared in GLEventListener ------

    /**
     * Called back immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     * GLEventListener method.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        logger.log(Level.INFO, "Initializing OpenGL context.");

        this.drawable = drawable;

        generateObject();

        this.fpsCounter = new FPSCounter((GLDrawable) drawable, new Font("SansSerif", Font.BOLD, 12));
        this.overlay = new Overlay((GLDrawable) drawable, new Font("SansSerif", Font.BOLD, 12));
        this.overlay.setColor(127, 127, 127, 100);
        this.overlay.setTextLocation(Overlay.LOWER_LEFT);

        // Parse random gcode file and generate something to draw.
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
        glu = new GLU();                         // get GL Utilities
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
        gl.glLoadIdentity();
    }

    /**
     * Call-back handler for window re-size event. Also called when the drawable is
     * first set to visible.
     * GLEventListener method.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        //logger.log(Level.INFO, "Reshaping OpenGL context.");
        this.xSize = width;
        this.ySize = height;

        if (!isDrawable) return;

        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        initObjectVariables();
        // Set the view port (display area) to cover the entire window
        //gl.glViewport(0, 0, width, height);
    }

    void initObjectVariables() {
        if (this.ySize == 0){ this.ySize = 1; }  // prevent divide by zero

        this.scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
        this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        this.panMultiplierX = VisualizerUtils.getRelativeMovementMultiplier(this.objectMin.x, this.objectMax.x, this.xSize);
        this.panMultiplierY = VisualizerUtils.getRelativeMovementMultiplier(this.objectMin.y, this.objectMax.y, this.ySize);
    }

    /**
     * Called back by the animator to perform rendering.
     * GLEventListener method.
     */
    @Override
    public void display(GLAutoDrawable drawable) {

        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        
        //renderAxes(drawable);

        this.setupPerpective(this.xSize, this.ySize, drawable, ortho);
        // Rotate prior to translating so that rotation happens from middle of
        // object.
        // Manual rotation
        gl.glRotated(this.rotation.x, 0.0, 1.0, 0.0);
        gl.glRotated(this.rotation.y, 1.0, 0.0, 0.0);

        // Draw model
        if (isDrawable) {
            // Scale the model so that it will fit on the window.
            gl.glScaled(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            gl.glTranslated(-this.eye.x - this.center.x, -this.eye.y - this.center.y, -this.eye.z - this.center.z);
        
            renderModel(drawable);
            renderTool(drawable);
        }
        
        gl.glDisable(GL.GL_DEPTH_TEST);

        gl.glPopMatrix();
        
        this.fpsCounter.draw();
        this.overlay.draw(this.dimensionsLabel);
        //this(drawable, new Font("SansSerif", Font.BOLD, 12));
    
        gl.glLoadIdentity();
        update();
    }
    
    private void renderAxes(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();

        gl.glPushMatrix();
            gl.glTranslated(-0.5f, -0.5f, -0.5f);
            gl.glRotated(this.rotation.x, 0.0, 1.0, 0.0);
            gl.glRotated(this.rotation.y, 1.0, 0.0, 0.0);
            
            gl.glBegin(GL_LINES);
            
            // X-Axis
            gl.glColor3f( 1, 0, 0 );
            gl.glVertex3f( 0, 0f, 0f );
            gl.glVertex3f( 1, 0f, 0f );

            // Y-Axis
            gl.glColor3f( 0, 1, 0 );
            gl.glVertex3f( 0, 0f, 0f );
            gl.glVertex3f( 0, 1f, 0f );
            
            // Z-Axis
            gl.glColor3f( 0, 1, 1 );
            gl.glVertex3f( 0, 0f, 0f );
            gl.glVertex3f( 0, 0f, 1f );
            
            gl.glEnd();
        
        gl.glPopMatrix();
        //# Draw number 50 on x/y-axis line.
        //glRasterPos2f(50,-5)
        //glutInit()
        //A = 53
        //glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, A)
        //glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, 48)

        //glRasterPos2f(-5,50)
        //glutInit()
        //A = 53
        //glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, A)
        //glutBitmapCharacter(GLUT_BITMAP_HELVETICA_18, 48)
    }
    
    /**
     * Draws a tool at the current work coordinates.
     */
    private void renderTool(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        
        gl.glLineWidth(8.0f);
        byte []color;
        color = VisualizerUtils.Color.YELLOW.getBytes();
        int verts = 0;
        int colors = 0;
        
        gl.glBegin(GL_LINES);
        
            gl.glColor3ub(color[0], color[1], color[2]);
            gl.glVertex3d(this.workCoord.x, this.workCoord.y, this.workCoord.z);
            gl.glColor3ub(color[0], color[1], color[2]);
            gl.glVertex3d(this.workCoord.x, this.workCoord.y, this.workCoord.z+(1.0/this.scaleFactor));
            
        gl.glEnd();
    }
    
    /**
     * Render the GCode object.
     */
    private void renderModel(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        // Batch mode if available 
        if(!forceOldStyle
                && gl.isFunctionAvailable( "glGenBuffers" )
                && gl.isFunctionAvailable( "glBindBuffer" )
                && gl.isFunctionAvailable( "glBufferData" )
                && gl.isFunctionAvailable( "glDeleteBuffers" ) ) {
            
            // Initialize OpenGL arrays if required.
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
            for(LineSegment ls : gcodeLineList)
            {
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
    
    /**
     * Setup the perspective matrix.
     */
    private void setupPerpective(int x, int y, GLAutoDrawable drawable, boolean ortho) {
        final GL2 gl = drawable.getGL().getGL2();
        float aspectRatio = (float)x / y;

        if (ortho) {
            gl.glDisable(GL_DEPTH_TEST);
            //gl.glDisable(GL_LIGHTING);
            gl.glMatrixMode(GL_PROJECTION);
            gl.glLoadIdentity();
            // Object's longest dimension is 1, make window slightly larger.
            gl.glOrtho(-0.51*aspectRatio,0.51*aspectRatio,-0.51,0.51,-10,10);
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity();
        } else {
            gl.glEnable(GL.GL_DEPTH_TEST);

            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
            gl.glLoadIdentity();             // reset projection matrix

            glu.gluPerspective(45.0, aspectRatio, 0.1, 100.0); // fovy, aspect, zNear, zFar
            // Move camera out and point it at the origin
            glu.gluLookAt(this.eye.x,  this.eye.y,  this.eye.z,
                          0, 0, 0,
                          0, 1, 0);
            
            // Enable the model-view transform
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity(); // reset

        }
    }
    
    /**
     * Parse the gcodeFile and store the resulting geometry and data about it.
     */
    private void generateObject()
    {
        isDrawable = false;
        if (this.gcodeFile == null){ return; }
        
        try {
            GcodeViewParse gcvp = new GcodeViewParse();
            if (this.processedFile) {
                GcodeStreamReader gsr = new GcodeStreamReader(new File(gcodeFile));
                gcodeLineList = gcvp.toObjFromReader(gsr, 0.3);
            }
            else {
                List<String> linesInFile;
                linesInFile = VisualizerUtils.readFiletoArrayList(this.gcodeFile);
                gcodeLineList = gcvp.toObjRedux(linesInFile, 0.3);
            }

            this.objectMin = gcvp.getMinimumExtremes();
            this.objectMax = gcvp.getMaximumExtremes();

            if (gcodeLineList.size() == 0) {
                return;
            }

            // Grab the line number off the last line.
            this.lastCommandNumber = gcodeLineList.get(gcodeLineList.size() - 1).getLineNumber();

            System.out.println("Object bounds: X ("+objectMin.x+", "+objectMax.x+")");
            System.out.println("               Y ("+objectMin.y+", "+objectMax.y+")");
            System.out.println("               Z ("+objectMin.z+", "+objectMax.z+")");

            this.center = VisualizerUtils.findCenter(objectMin, objectMax);
            System.out.println("Center = " + center.toString());
            System.out.println("Num Line Segments :" + gcodeLineList.size());

            this.maxSide = VisualizerUtils.findMaxSide(objectMin, objectMax);

            this.scaleFactorBase = 1.0/this.maxSide;
            this.scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;

            double objectWidth = this.objectMax.x-this.objectMin.x;
            double objectHeight = this.objectMax.y-this.objectMin.y;
            this.dimensionsLabel = Localization.getString("VisualizerCanvas.dimensions") + ": " 
                    + Localization.getString("VisualizerCanvas.width") + "=" + format.format(objectWidth) + " " 
                    + Localization.getString("VisualizerCanvas.height") + "=" + format.format(objectHeight);

            // Now that the object is known, fill the buffers.
            this.isDrawable = true;
            this.initObjectVariables();

            this.createVertexBuffers();
        } catch (IOException e) {
            System.out.println("Error opening file: " + e.getLocalizedMessage());
        }

    }

    /**
     * Convert the gcodeLineList into vertex and color arrays.
     */
    private void createVertexBuffers() {
        if (this.isDrawable) {
            this.numberOfVertices = gcodeLineList.size() * 2;
            this.lineVertexData = new float[numberOfVertices * 3];
            this.lineColorData = new byte[numberOfVertices * 3];
            
            VisualizerUtils.Color color;
            int vertIndex = 0;
            int colorIndex = 0;
            for(LineSegment ls : gcodeLineList) {
                // Find the lines color.
                if (ls.isArc()) {
                    color = VisualizerUtils.Color.RED;
                } else if (ls.isFastTraverse()) {
                    color = VisualizerUtils.Color.BLUE;
                } else if (ls.isZMovement()) {
                    color = VisualizerUtils.Color.GREEN;
                } else {
                    color = VisualizerUtils.Color.WHITE;
                }

                if (highlightedLines != null && highlightedLines.contains(ls.getLineNumber())) {
                    color = VisualizerUtils.Color.YELLOW;
                }

                // Override color if it is cutoff
                if (ls.getLineNumber() < this.currentCommandNumber) {
                    color = VisualizerUtils.Color.GRAY;
                }

                // Draw it.
                {
                    Point3d p1 = ls.getStart();
                    Point3d p2 = ls.getEnd();
                    byte[] c = color.getBytes();

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
                    lineVertexData[vertIndex++] = (float)p1.x;
                    lineVertexData[vertIndex++] = (float)p1.y;
                    lineVertexData[vertIndex++] = (float)p1.z;
                    //p2
                    lineVertexData[vertIndex++] = (float)p2.x;
                    lineVertexData[vertIndex++] = (float)p2.y;
                    lineVertexData[vertIndex++] = (float)p2.z;
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
            lineVertexBuffer.clear();
            if (lineVertexBuffer.remaining() < lineVertexData.length) {
                lineVertexBuffer = null;
            }
        }
        
        if (lineVertexBuffer == null) {
            lineVertexBuffer = Buffers.newDirectFloatBuffer(lineVertexData.length);
        }
        
        lineVertexBuffer.put(lineVertexData);
        lineVertexBuffer.flip();
        gl.glVertexPointer( 3, GL.GL_FLOAT, 0, lineVertexBuffer );
    }
    
    /**
     * Initialize or update open gl color array in native buffer objects.
     */
    private void updateGLColorArray(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        // Reset buffer and set to null of new colors don't fit.
        if (lineColorBuffer != null) {
            lineColorBuffer.clear();

            if (lineColorBuffer.remaining() < lineColorData.length) {
                lineColorBuffer = null;
            }
        }
        
        if (lineColorBuffer == null) {
            if (this.lineColorData == null) {
                this.createVertexBuffers();
            }
            lineColorBuffer = Buffers.newDirectByteBuffer(this.lineColorData.length);
        }
        
        lineColorBuffer.put(lineColorData);
        lineColorBuffer.flip();
        gl.glColorPointer( 3, GL.GL_UNSIGNED_BYTE, 0, lineColorBuffer );
    }
    
    // For seeing the tool path.
    //private int count = 0;
    //private boolean increasing = true;
    /**
     * Called after each render.
     */
    private void update() {
        if (debugCoordinates) {
            System.out.println("Machine coordinates: " + this.machineCoord.toString());
            System.out.println("Work coordinates: " + this.workCoord.toString());
            System.out.println("-----------------");
        }
        
        /*
        // Increases the cutoff number each frame to show the tool path.
        count++;
        
        if (increasing) currentCommandNumber+=10;
        else            currentCommandNumber-=10;

        if (this.currentCommandNumber > this.lastCommandNumber) increasing = false;
        else if (this.currentCommandNumber <= 0)             increasing = true;
        */ 
    }

    public void setHighlightedLines(Collection<Integer> lines) {
        highlightedLines = lines;
        this.colorArrayDirty = true;
    }
    
    /**
     * Called back before the OpenGL context is destroyed. 
     * Release resource such as buffers.
     * GLEventListener method.
     */
    @Override
    synchronized public void dispose(GLAutoDrawable drawable) { 
        logger.log(Level.INFO, "Disposing OpenGL context.");

        this.isDrawable = false;
        this.lineColorBuffer = null;
        this.lineVertexBuffer = null;
        this.gcodeLineList = null;
        this.isDrawable = false;
        this.numberOfVertices = 0;
    }
   
    private void setHorizontalTranslationVector() {
        double x = Math.cos(Math.toRadians(this.rotation.x));
        double xz = Math.sin(Math.toRadians(this.rotation.x));

        double y = xz * Math.sin(Math.toRadians(this.rotation.y));
        double yz = xz * Math.cos(Math.toRadians(this.rotation.y));

        translationVectorH = new Vector3d(x, y, yz);
        translationVectorH.normalize();
    }

    private void setVerticalTranslationVector(){
        double y = Math.cos(Math.toRadians(this.rotation.y));
        double yz = Math.sin(Math.toRadians(this.rotation.y));

        translationVectorV = new Vector3d(0, y, yz);
        translationVectorV.normalize();
    }

    public void mouseMoved(Point lastPoint) {
        last = lastPoint;
    }
    
    public void mouseRotate(Point point) {
        this.current = point;
        int dx = this.current.x - this.last.x;
        int dy = this.current.y - this.last.y;

        this.rotation.x += dx / 2.0;
        this.rotation.y -= dy / 2.0;
        if (ortho) {
            setHorizontalTranslationVector();
            setVerticalTranslationVector();
        }
        
        // Now that the motion has been accumulated, reset last.
        this.last = this.current;
    }
    
    public void mousePan(Point point) {
        this.current = point;
        int dx = this.current.x - this.last.x;
        int dy = this.current.y - this.last.y;
        pan(dx, dy);
    }

    public void pan(int dx, int dy) {
        if (ortho) {
            // Treat dx and dy as vectors relative to the rotation angle.
            this.eye.x -= ((dx * this.translationVectorH.x * this.panMultiplierX) + (dy * this.translationVectorV.x * panMultiplierY));
            this.eye.y += ((dy * this.translationVectorV.y * panMultiplierY) - (dx * this.translationVectorH.y * this.panMultiplierX));
            this.eye.z -= ((dx * this.translationVectorH.z * this.panMultiplierX) + (dy * this.translationVectorV.z * panMultiplierY));
        } else {
            this.eye.x += dx;
            this.eye.y += dy;
        }
        
        // Now that the motion has been accumulated, reset last.
        this.last = this.current;
    }
    
    public void zoom(int delta) {
        if (delta == 0)
            return;

        if (delta > 0) {
            if (this.invertZoom)
                zoomOut(delta);
            else
                zoomIn(delta);
        } else if (delta < 0) {
            if (this.invertZoom)
                zoomIn(delta * -1);
            else
                zoomOut(delta * -1);
        }
    }

    private void zoomOut(int increments) {
        if (ortho) {
            if (this.zoomMultiplier <= this.minZoomMultiplier)
                return;

            this.zoomMultiplier -= increments * zoomIncrement;
            if (this.zoomMultiplier < this.minZoomMultiplier)
                this.zoomMultiplier = this.minZoomMultiplier;

            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        } else {
            this.eye.z += increments;
        }
    }

    private void zoomIn(int increments) {
        if (ortho) {
            if (this.zoomMultiplier >= this.maxZoomMultiplier)
                return;

            this.zoomMultiplier += increments * zoomIncrement;
            if (this.zoomMultiplier > this.maxZoomMultiplier)
                this.zoomMultiplier = this.maxZoomMultiplier;

            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        } else {
            this.eye.z -= increments;
        }
    }

    /**
     * Reset the view angle and zoom.
     */
    public void resetView() {
        this.zoomMultiplier = 1;
        this.scaleFactor = this.scaleFactorBase;
        this.eye.x = 0;
        this.eye.y = 0;
        this.eye.z = 1.5;
        this.rotation.x = 0;
        this.rotation.y = -30;
        this.rotation.z = 0;
    }

    public double getMinArcLength() {
        return minArcLength;
    }

    public void setMinArcLength(double minArcLength) {
        if (this.minArcLength != minArcLength) {
            this.minArcLength = minArcLength;
            if (this.gcodeFile != null) {
                this.setGcodeFile(this.gcodeFile);
            }
        }
    }
}