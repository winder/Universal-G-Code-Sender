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

package com.willwinder.universalgcodesender;

import com.jogamp.common.nio.Buffers;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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

    private String gcodeFile = null;
    private boolean isDrawable = false; //True if a file is loaded; false if not
    private ArrayList<LineSegment> gcodeLineList; //An ArrayList of linesegments composing the model
    private int currentCommandNumber = 0;
    private int lastCommandNumber = 0;

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
    
    private enum Color {
        RED, 
        BLUE, 
        PURPLE, 
        YELLOW, 
        OTHER_YELLOW, 
        GREEN, 
        WHITE,
        GRAY,
    }
    
    public VisualizerCanvas() {
       this.addGLEventListener(this);
       this.addKeyListener(this);

       this.eye = new Point3d(0, 0, 1.5);
       this.center = new Point3d(0, 0, 0);
    }
    
    public void setCurrentCommandNumber(int num) {
        this.currentCommandNumber = num;
        this.createVertexBuffers();
        this.colorArrayDirty = true;
    }
    
    public int getLastCommandNumber() {
        return this.lastCommandNumber;
    }
    public void setGcodeFile(String file) {
        this.gcodeFile = file;
        generateObject();
    }
    
    /** Constructor to setup the GUI for this Component */
    public ArrayList<String> readFiletoArrayList(String gCode) {
        ArrayList<String> vect = null;
        
        try {
            File gCodeFile = new File(gCode);

            vect = new ArrayList<String>();

            FileInputStream fstream = new FileInputStream(gCodeFile);
            DataInputStream dis = new DataInputStream(fstream);
            BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));

            String line;
            while ((line = fileStream.readLine()) != null) {
                vect.add(line);
            }
        } catch (Exception e) {
            System.out.println("Crapped out while reading file in visualizer canvas.");
        }
        
        return vect;
    }
    
    static private Point3d findCenter(Point3d min, Point3d max)
    {
        Point3d center = new Point3d();
        center.x = (min.x + max.x) / 2.0;
        center.y = (min.y + max.y) / 2.0;
        center.z = (min.z + max.z) / 2.0;
        
        return center;
    }
    
    static private double findMaxSide(Point3d min, Point3d max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        double z = Math.abs(min.z) + Math.abs(max.z);
        return Math.max(x, Math.max(y, z));
    }
    
    static private double findAspectRatio(Point3d min, Point3d max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        
        return x / y;
    }
    
    /**
     * Find a factor to scale the object model by so that it fits in the window.
     */
    static private double findScaleFactor(double x, double y, Point3d min, Point3d max) {
        if (y == 0 || x == 0 || min == null || max == null) {
            return 1;
        }
        
        double xObj = Math.abs(min.x) + Math.abs(max.x);
        double yObj = Math.abs(min.y) + Math.abs(max.y);
        
        double windowRatio = x/y;
        double objRatio = xObj/yObj;

        // This works for narrow tall objects.
        if (windowRatio < objRatio) {
            return (1.0/xObj) * windowRatio;
        } else {
            return 1.0/yObj;
        }
    }
    
    public void generateObject()
    {
        if (this.gcodeFile == null){ return; }
        
        GcodeViewParse gcvp = new GcodeViewParse();
        gcodeLineList = (gcvp.toObj(readFiletoArrayList(this.gcodeFile)));
        
        objectMin = gcvp.getMinimumExtremes();
        objectMax = gcvp.getMaximumExtremes();

        // Grab the line number off the last line.
        this.lastCommandNumber = gcodeLineList.get(gcodeLineList.size() - 1).getLineNumber();
        
        System.out.println("Object bounds: X ("+objectMin.x+", "+objectMax.x+")");
        System.out.println("               Y ("+objectMin.y+", "+objectMax.y+")");
        System.out.println("               Z ("+objectMin.z+", "+objectMax.z+")");
        
        this.center = findCenter(objectMin, objectMax);
        System.out.println("Center = " + center.toString());
        System.out.println("Num Line Segments :" + gcodeLineList.size());

        this.maxSide = findMaxSide(objectMin, objectMax);
        
        this.scaleFactor = 1.0/this.maxSide;
        this.scaleFactor = findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);        

        isDrawable = true;
        
        // Now that the object is known, fill the buffers.
        this.createVertexBuffers();
        this.colorArrayDirty = true;
        this.vertexArrayDirty = true;
    }

    // ------ Implement methods declared in GLEventListener ------

    /**
     * Called back immediately after the OpenGL context is initialized. Can be used
     * to perform one-time initialization. Run only once.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
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
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        this.xSize = width;
        this.ySize = height;

        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        if (height == 0){ height = 1; }  // prevent divide by zero
        this.aspectRatio = (float)width / height;

        this.scaleFactor = findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
        
        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);
    }

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
     * Called back by the animator to perform rendering.
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
            render(drawable);
        }
        
        gl.glDisable(GL.GL_DEPTH_TEST);

        gl.glPopMatrix();
        
        update();
    }
    
    /**
     * Render the GCode object.
     */
    private void render(GLAutoDrawable drawable) {
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
            
            gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
            gl.glDrawArrays( GL.GL_LINES, 0, numberOfVertices);
            gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
            gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        }
        // Traditional OpenGL
        else {
            this.oldStyle(drawable);
        }

        // makes the gui stay on top of elements
        // drawn before.
    }

    /**
     * Initialize open gl geometry array in native buffer objects.
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
     * Initialize open gl color array in native buffer objects.
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
    
    /**
     * Traditional OpenGL writing each point and color.
     */
    private void oldStyle(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        Point3d p1, p2;

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

        //gl.glDisable(GL2.GL_LINE_SMOOTH);
    }
    
    /**
     * Convert the gcodeLineList into vertex and color arrays.
     */
    private void createVertexBuffers() {
        if (this.isDrawable) {
            this.numberOfVertices = gcodeLineList.size() * 2;
            this.lineVertexData = new float[numberOfVertices * 3];
            this.lineColorData = new byte[numberOfVertices * 3];
            
            Color color;
            int vertIndex = 0;
            int colorIndex = 0;
            for(LineSegment ls : gcodeLineList) {
                // Find the lines color.
                if (ls.isArc()) {
                    color = Color.RED;
                } else if (ls.isFastTraverse()) {
                    color = Color.BLUE;
                } else if (ls.isZMovement()) {
                    color = Color.GREEN;
                } else {
                    color = Color.WHITE;
                }

                // Override color if it is cutoff
                if (ls.getLineNumber() < this.currentCommandNumber) {
                    color = Color.GRAY;
                }

                // Draw it.
                {
                    Point3d p1 = ls.getStart();
                    Point3d p2 = ls.getEnd();
                    byte[] c = getVertexColor(color);

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
    
    // For seeing the tool path.
    //private int count = 0;
    //private boolean increasing = true;
    
    private void update() {
        
        /*
        // Increases the cutoff number each frame to show the tool path.
        count++;
        
        if (increasing) currentCommandNumber+=10;
        else            currentCommandNumber-=10;

        if (this.currentCommandNumber > this.lastCommandNumber) increasing = false;
        else if (this.currentCommandNumber <= 0)             increasing = true;
        */ 
    }
    
    private byte[] getVertexColor(VisualizerCanvas.Color color) {
        byte[] ret;
        switch (color) {
            case RED:
                ret = new byte[]{(byte)255, (byte)100, (byte)100};
                break;
            case BLUE:
                ret = new byte[]{(byte)0, (byte)255, (byte)255};
                break;
            case PURPLE: 
                ret = new byte[]{(byte)242, (byte)0, (byte)255};
                break;
            case YELLOW: 
                ret = new byte[]{(byte)237, (byte)255, (byte)0};
                break;
            case OTHER_YELLOW: 
                ret = new byte[]{(byte)234, (byte)212, (byte)7};
                break;
            case GREEN: 
                ret = new byte[]{(byte)33, (byte)255, (byte)0};
                break;
            case WHITE:
                ret = new byte[]{(byte)255, (byte)255, (byte)255};
                break;
            case GRAY:
                ret = new byte[]{(byte)80, (byte)80, (byte)80};
                break;
            default:
                // white
                ret = new byte[]{(byte)255, (byte)255, (byte)255};
        }
        return ret;
    }
    
    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { }

    @Override
    public void keyTyped(KeyEvent ke) {
        //System.out.println ("key typed");
    }

    static double DELTA_SIZE = 0.1;
    @Override
    public void keyPressed(KeyEvent ke) {
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

    @Override
    public void keyReleased(KeyEvent ke) {
        //System.out.println ("key released");
    }
}