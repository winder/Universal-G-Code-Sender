/*
 * 3D Canvas for GCode Visualizer.
 *
 * Created on Jan 29, 2013
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

package com.willwinder.universalgcodesender.visualizer;

import com.jogamp.common.nio.Buffers;
import com.willwinder.universalgcodesender.uielements.FPSCounter;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.*;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point3d;
 
/**
 *
 * @author wwinder
 * 
 */
@SuppressWarnings("serial")
public class VisualizerCanvas extends GLCanvas implements GLEventListener, KeyListener {
    static boolean ortho = true;
    static double orthoRotation = -35;
    static boolean forceOldStyle = false;
    static boolean debugCoordinates = false; // turn on coordinate debug output

    private String gcodeFile = null;
    private boolean isDrawable = false; //True if a file is loaded; false if not
    private ArrayList<LineSegment> gcodeLineList; //An ArrayList of linesegments composing the model
    private int currentCommandNumber = 0;
    private int lastCommandNumber = 0;
    private Point3d machineCoord;
    private Point3d workCoord;

    // GL Utility
    private GLU glu;
    
    // Projection variables
    private Point3d center, eye;
    private Point3d objectMin, objectMax;
    private double maxSide;
    private double scaleFactor;
    private double aspectRatio;
    private int xSize, ySize;

    // OpenGL Object Buffer Variables
    private int numberOfVertices = -1;
    private float[] lineVertexData = null;
    private byte[] lineColorData = null;
    private FloatBuffer lineVertexBuffer = null;
    private ByteBuffer lineColorBuffer = null;
    
    // Track when arrays need to be updated due to changing data.
    private boolean colorArrayDirty = false;
    private boolean vertexArrayDirty = false;
    
    private FPSCounter fpsCounter;
    
    /**
     * Constructor.
     */
    public VisualizerCanvas() {
       this.addGLEventListener(this);
       this.addKeyListener(this);

       this.eye = new Point3d(0, 0, 1.5);
       this.center = new Point3d(0, 0, 0);
       
       this.workCoord = new Point3d(0, 0, 0);
       this.machineCoord = new Point3d(0, 0, 0);
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
    
    /**
     * Assign a gcode file to drawing.
     */
    public void setGcodeFile(String file) {
        this.gcodeFile = file;
        generateObject();
    }
    
    public void setWorkCoordinate(Point3d p) {
        this.workCoord.set(p);
    }
    
    public void setMachineCoordinate(Point3d p) {
        this.machineCoord.set(p);
    }

    // ------ Implement methods declared in GLEventListener ------

    /**
     * Called back immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     * GLEventListener method.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        this.fpsCounter = new FPSCounter(drawable, new Font("SansSerif", Font.BOLD, 12));

        // Parse random gcode file and generate something to draw.
        generateObject();
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
        glu = new GLU();                         // get GL Utilities
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
    }

    /**
     * Call-back handler for window re-size event. Also called when the drawable is
     * first set to visible.
     * GLEventListener method.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.xSize = width;
        this.ySize = height;

        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        if (height == 0){ height = 1; }  // prevent divide by zero
        this.aspectRatio = (float)width / height;

        this.scaleFactor = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
        
        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);
    }

    /**
     * Called back by the animator to perform rendering.
     * GLEventListener method.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        this.setupPerpective(this.xSize, this.ySize, drawable, ortho);

        final GL2 gl = drawable.getGL().getGL2();
        
        // Scale the model so that it will fit on the window.
        gl.glScaled(this.scaleFactor, this.scaleFactor, this.scaleFactor);

        // Shift model to center of window.
        gl.glTranslated(-this.center.x, -this.center.y, 0);
        
        if (ortho) {
            gl.glRotated(orthoRotation, 1.0, 0.0, 0.0);
        }
 
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        // Draw model
        if (isDrawable) {
            renderAxes(drawable);
            renderModel(drawable);
            renderTool(drawable);
        }
        
        gl.glDisable(GL.GL_DEPTH_TEST);

        gl.glPopMatrix();
        
        this.fpsCounter.draw();
        //this(drawable, new Font("SansSerif", Font.BOLD, 12));

        
        update();
    }
    
    private void renderAxes(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        
        gl.glBegin(GL_LINES);
        
        // X-Axis
        gl.glColor3f( 1, 0, 0 );
        gl.glVertex3f( 0, 0, 0 );
        gl.glVertex3f( 50, 0, 0 );

        // Y-Axis
        gl.glColor3f( 0, 1, 0 );
        gl.glVertex3f( 0, 0, 0 );
        gl.glVertex3f( 0, 50, 0 );
        
        // Z-Axis
        gl.glColor3f( 0, 0, 1 );
        gl.glVertex3f( 0, 0, 0 );
        gl.glVertex3f( 0, 0, 50 );
        
        gl.glEnd();
        
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
        color = VisualizerUtils.getVertexColor(VisualizerUtils.Color.YELLOW);
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

            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glDrawArrays( GL.GL_LINES, 0, numberOfVertices);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
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
        
        if (ortho) {
            gl.glDisable(GL_DEPTH_TEST);
            //gl.glDisable(GL_LIGHTING);
            gl.glMatrixMode(GL_PROJECTION);
            gl.glPushMatrix();
            gl.glLoadIdentity();
            // Object's longest dimension is 1, make window slightly larger.
            gl.glOrtho(-0.51*this.aspectRatio,0.51*this.aspectRatio,-0.51,0.51,-10,10);
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glPushMatrix();
            gl.glLoadIdentity();
        } else {
            gl.glEnable(GL.GL_DEPTH_TEST);

            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
            gl.glLoadIdentity();             // reset projection matrix

            glu.gluPerspective(45.0, this.aspectRatio, 0.1, 100.0); // fovy, aspect, zNear, zFar
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
        if (this.gcodeFile == null){ return; }
        
        try {

        GcodeViewParse gcvp = new GcodeViewParse();
        ArrayList<String> linesInFile;
        linesInFile = VisualizerUtils.readFiletoArrayList(this.gcodeFile);
        gcodeLineList = gcvp.toObj(linesInFile);
        
        this.objectMin = gcvp.getMinimumExtremes();
        this.objectMax = gcvp.getMaximumExtremes();

        // Grab the line number off the last line.
        this.lastCommandNumber = gcodeLineList.get(gcodeLineList.size() - 1).getLineNumber();
        
        System.out.println("Object bounds: X ("+objectMin.x+", "+objectMax.x+")");
        System.out.println("               Y ("+objectMin.y+", "+objectMax.y+")");
        System.out.println("               Z ("+objectMin.z+", "+objectMax.z+")");
        
        this.center = VisualizerUtils.findCenter(objectMin, objectMax);
        System.out.println("Center = " + center.toString());
        System.out.println("Num Line Segments :" + gcodeLineList.size());

        this.maxSide = VisualizerUtils.findMaxSide(objectMin, objectMax);
        
        this.scaleFactor = 1.0/this.maxSide;
        this.scaleFactor = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);        

        this.isDrawable = true;
        
        // Now that the object is known, fill the buffers.
        this.createVertexBuffers();
        this.colorArrayDirty = true;
        this.vertexArrayDirty = true;
        
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

                // Override color if it is cutoff
                if (ls.getLineNumber() < this.currentCommandNumber) {
                    color = VisualizerUtils.Color.GRAY;
                }

                // Draw it.
                {
                    Point3d p1 = ls.getStart();
                    Point3d p2 = ls.getEnd();
                    byte[] c = VisualizerUtils.getVertexColor(color);

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
    
    /**
     * Called back before the OpenGL context is destroyed. 
     * Release resource such as buffers.
     * GLEventListener method.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { 
        this.lineColorBuffer = null;
        this.lineVertexBuffer = null;
    }

    /**
     * KeyListener method.
     */
    @Override
    public void keyTyped(KeyEvent ke) {
        //System.out.println ("key typed");
    }

    /**
     * KeyListener method.
     */
    @Override
    public void keyPressed(KeyEvent ke) {
        double DELTA_SIZE = 0.1;
            
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                this.eye.y+=DELTA_SIZE;
                break;
            case KeyEvent.VK_DOWN:
                this.eye.y-=DELTA_SIZE;
                break;
            case KeyEvent.VK_LEFT:
                this.eye.x-=DELTA_SIZE;
                break;
            case KeyEvent.VK_RIGHT:
                this.eye.x+=DELTA_SIZE;
                break;
        }
        
        switch(ke.getKeyChar()) {
            case 'p':
                this.eye.z+=DELTA_SIZE;
                break;
            case ';':
                this.eye.z-=DELTA_SIZE;
                break;
                
            case 'w':
                this.center.y+=DELTA_SIZE;
                break;
            case 's':
                this.center.y-=DELTA_SIZE;
                break;
            case 'a':
                this.center.x-=DELTA_SIZE;
                break;
            case 'd':
                this.center.x+=DELTA_SIZE;
                break;
            case 'r':
                this.center.z+=DELTA_SIZE;
                break;
            case 'f':
                this.center.z-=DELTA_SIZE;
                break;
        }
        
        //System.out.println("Eye: " + eye.toString()+"\nCent: "+cent.toString());
    }
    
    /**
     * KeyListener method.
     */
    @Override
    public void keyReleased(KeyEvent ke) {
        //System.out.println ("key released");
    }
}