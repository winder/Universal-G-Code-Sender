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



import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.jogamp.opengl.GL.GL_FRONT_AND_BACK;
import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLEventListener;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_AMBIENT_AND_DIFFUSE;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import com.jogamp.opengl.glu.GLU;
import com.willwinder.ugs.nbm.visualizer.util.OrientationCube;
import com.willwinder.ugs.nbm.visualizer.util.Renderable;
import com.willwinder.ugs.nbm.visualizer.util.Tool;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.uielements.FPSCounter;
import com.willwinder.universalgcodesender.uielements.Overlay;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    /*
    // Gcode file data
    private String gcodeFile = null;
    private boolean processedFile = false; // True if the file should be opened with GcodeStreamReader.
    private boolean isDrawable = false; //True if a file is loaded; false if not
    private List<LineSegment> gcodeLineList; //An ArrayList of linesegments composing the model
    private int currentCommandNumber = 0;
    private int lastCommandNumber = 0;
    */

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
    private double scaleFactor = 1;
    private double scaleFactorBase = 1;
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

    /*
    // OpenGL Object Buffer Variables
    private int numberOfVertices = -1;
    private float[] lineVertexData = null;
    private byte[] lineColorData = null;
    private FloatBuffer lineVertexBuffer = null;
    private ByteBuffer lineColorBuffer = null;
    */

    Collection<Integer> highlightedLines = null;
    
    // Track when arrays need to be updated due to changing data.
    private boolean colorArrayDirty = false;
    private boolean vertexArrayDirty = false;
    
    private FPSCounter fpsCounter;
    private Overlay overlay;
    private String dimensionsLabel = "";

    private ArrayList<Renderable> objects;
    
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

        objects = new ArrayList<>();
        objects.add(new Tool());
        objects.add(new OrientationCube(0.5f));
        Collections.sort(objects);
    }

    public void addRenderable(Renderable r) {
        objects.add(r);
        Collections.sort(objects);
    }
    
    public void forceRedraw() {
        if (drawable != null) {
            drawable.display();
        }
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

        // TODO: Figure out scale factor / dimensions label based on GcodeRenderer
        /*
            this.scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, this.objectMin, this.objectMax);
            this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;

            double objectWidth = this.objectMax.x-this.objectMin.x;
            double objectHeight = this.objectMax.y-this.objectMin.y;
            this.dimensionsLabel = Localization.getString("VisualizerCanvas.dimensions") + ": " 
                    + Localization.getString("VisualizerCanvas.width") + "=" + format.format(objectWidth) + " " 
                    + Localization.getString("VisualizerCanvas.height") + "=" + format.format(objectHeight);

        */

        this.fpsCounter = new FPSCounter((GLDrawable) drawable, new Font("SansSerif", Font.BOLD, 12));
        this.overlay = new Overlay((GLDrawable) drawable, new Font("SansSerif", Font.BOLD, 12));
        this.overlay.setColor(127, 127, 127, 100);
        this.overlay.setTextLocation(Overlay.LOWER_LEFT);

        // Parse random gcode file and generate something to draw.
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
        glu = new GLU();                         // get GL Utilities
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glDepthFunc(GL2.GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction
        gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        gl.glLoadIdentity();

        for (Renderable r : objects) {
            r.init(drawable);
        }
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

        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, xSize, ySize);

        initObjectVariables();
    }

    public void setObjectSize(Point3d min, Point3d max) {
        this.objectMin = min;
        this.objectMax = max;
        initObjectVariables();
    }

    private void initObjectVariables() {
        if (this.objectMin == null || this.objectMax == null) return;

        if (this.ySize == 0){ this.ySize = 1; }  // prevent divide by zero

        this.center = VisualizerUtils.findCenter(objectMin, objectMax);
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
        this.setupPerpective(this.xSize, this.ySize, drawable, ortho);

        final GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        //gl.glEnable(GL2.GL_LIGHTING); 
        gl.glEnable(GL2.GL_LIGHT0);  
        gl.glEnable(GL2.GL_NORMALIZE); 
        gl.glEnable (GL2.GL_COLOR_MATERIAL ) ;

        float ambient[] = { 0.4f, 0.4f, 0.4f, 1.0f };
        float diffuse[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float specular[] = { 1.0f, 1.0f, 1.0f, 1.0f };
        float position[] = { 10f, 10f, 0f, 0.0f };
        float lmodel_ambient[] = { 0.9f, 0.6f, 0.6f, 1.0f };

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, specular, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);

        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
        // Draw model
        /*
        for (Renderable r : objects) {
            gl.glPushMatrix();

            // Rotate first to center object.
            gl.glRotated(this.rotation.x, 0.0, 1.0, 0.0);
            gl.glRotated(this.rotation.y, 1.0, 0.0, 0.0);

            // Scale the model so that it will fit on the window.
            //gl.glScaled(this.scaleFactor, this.scaleFactor, this.scaleFactor);
            gl.glTranslated(-this.eye.x - this.center.x, -this.eye.y - this.center.y, -this.eye.z - this.center.z);
        
            //renderModel(drawable);
            //renderGrid(drawable);

            gl.glPopMatrix();
        }
        */
        
        //renderCornerAxes(drawable);

        for (Renderable r : objects) {
            gl.glPushMatrix();
                r.setWorkCoord(workCoord);
                r.setScaleFactor(scaleFactor);
                if (r.rotate()) {
                    gl.glRotated(this.rotation.x, 0.0, 1.0, 0.0);
                    gl.glRotated(this.rotation.y, 1.0, 0.0, 0.0);
                }
                if (r.center()) {
                    gl.glTranslated(-this.eye.x - this.center.x, -this.eye.y - this.center.y, -this.eye.z - this.center.z);
                }
                r.draw(drawable);
            gl.glPopMatrix();
        }
        
        this.fpsCounter.draw();
        this.overlay.draw(this.dimensionsLabel);
        //this(drawable, new Font("SansSerif", Font.BOLD, 12));
    
        gl.glLoadIdentity();
        update();
    }

    private void renderCornerAxes(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glLineWidth(4);

        float ar = (float)xSize/ySize;

        float size = 0.1f;
        float size2 = size/2;
        float fromEdge = 0.8f;

        gl.glPushMatrix();
            gl.glTranslated(-0.51*ar*fromEdge, 0.51*fromEdge, -0.5f);
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
     * Draws a grid along the XY axis.
     */
    private void renderGrid(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            gl.glRotated(90, 1.0, 0.0, 0.0);
            gl.glScaled(1./this.scaleFactor, 1./this.scaleFactor, 1./this.scaleFactor);
            gl.glColor4f(.3f,.3f,.3f, .5f);
            // floor
            double side = 1;
            gl.glBegin(gl.GL_QUADS);
            gl.glVertex3d(-side, 0,-side);
            gl.glVertex3d(-side, 0, side);
            gl.glVertex3d( side, 0, side);
            gl.glVertex3d( side, 0,-side);
            gl.glEnd();

            // grid
            gl.glBegin(GL_LINES);
            for(double i=-side;i<=side;i++) {
                if (i==0) { gl.glColor3d(.6f,.3f,.3f); } else { gl.glColor3d(.25,.25,.25); };
                gl.glVertex3d(i,0.001,-side);
                gl.glVertex3d(i,0.001,side);
                gl.glVertex3d(i,-0.001,-side);
                gl.glVertex3d(i,-0.001,side);
                if (i==0) { gl.glColor3d(.3,.3,.6); } else { gl.glColor3d(.25,.25,.25); };
                gl.glVertex3d(-side,0.001,i);
                gl.glVertex3d(side,0.001,i);
                gl.glVertex3d(-side,-0.001,i);
                gl.glVertex3d(side,-0.001,i);
            };
            gl.glEnd();

        gl.glPopMatrix();

    }
    
    /**
     * Setup the perspective matrix.
     */
    private void setupPerpective(int x, int y, GLAutoDrawable drawable, boolean ortho) {
        final GL2 gl = drawable.getGL().getGL2();
        float aspectRatio = (float)x / y;

        if (ortho) {
            gl.glMatrixMode(GL_PROJECTION);
            gl.glLoadIdentity();
            // Object's longest dimension is 1, make window slightly larger.
            gl.glOrtho(-0.51*aspectRatio/scaleFactor,0.51*aspectRatio/scaleFactor,-0.51/scaleFactor,0.51/scaleFactor,
                    -10/scaleFactor,10/scaleFactor);
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity();
        } else {
            // Setup perspective projection, with aspect ratio matches viewport
            gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
            gl.glLoadIdentity();             // reset projection matrix

            glu.gluPerspective(45.0, aspectRatio, 0.1, 20000.0); // fovy, aspect, zNear, zFar
            // Move camera out and point it at the origin
            glu.gluLookAt(this.eye.x,  this.eye.y,  this.eye.z,
                          0, 0, 0,
                          0, 1, 0);
            
            // Enable the model-view transform
            gl.glMatrixMode(GL_MODELVIEW);
            gl.glLoadIdentity(); // reset
        }
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
    synchronized public void dispose(GLAutoDrawable drawable) { 
        logger.log(Level.INFO, "Disposing OpenGL context.");
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
        if (this.last != null) {
            int dx = this.current.x - this.last.x;
            int dy = this.current.y - this.last.y;

            rotation.x = this.rotation.x += dx / 2.0;
            rotation.y = Math.min(0, Math.max(-180, this.rotation.y += dy / 2.0));

            if (ortho) {
                setHorizontalTranslationVector();
                setVerticalTranslationVector();
            }
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
}