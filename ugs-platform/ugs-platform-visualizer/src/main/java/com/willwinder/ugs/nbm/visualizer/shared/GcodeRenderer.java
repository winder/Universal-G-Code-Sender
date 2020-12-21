/*
    Copyright 2013-2018 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.shared;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.renderables.Grid;
import com.willwinder.ugs.nbm.visualizer.renderables.MachineBoundries;
import com.willwinder.ugs.nbm.visualizer.renderables.MouseOver;
import com.willwinder.ugs.nbm.visualizer.renderables.OrientationCube;
import com.willwinder.ugs.nbm.visualizer.renderables.Tool;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.helpers.FPSCounter;
import com.willwinder.universalgcodesender.uielements.helpers.Overlay;
import com.willwinder.universalgcodesender.visualizer.MouseProjectionUtils;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_BG;

/**
 * 3D Canvas for GCode Visualizer
 *
 * @author wwinder
 */
@SuppressWarnings("serial")
@ServiceProviders(value = {
        @ServiceProvider(service = IRenderableRegistrationService.class),
        @ServiceProvider(service = GcodeRenderer.class)})
public class GcodeRenderer implements GLEventListener, IRenderableRegistrationService {
    private static final Logger logger = Logger.getLogger(GcodeRenderer.class.getName());
    
    private static boolean ortho = true;
    private static double orthoRotation = -45;
    private static boolean forceOldStyle = false;
    private static boolean debugCoordinates = false; // turn on coordinate debug output

    // Machine data
    private final Position machineCoord;
    private final Position workCoord;
    
    // GL Utility
    private GLU glu;
    private GLAutoDrawable drawable = null;
    
    // Projection variables
    private Position center, eye;
    private Position objectMin, objectMax;
    private double maxSide;
    private int xSize, ySize;
    private double minArcLength;
    private double arcLength;

    // Scaling
    private double scaleFactor = 1;
    private double scaleFactorBase = 1;
    private double zoomMultiplier = 1;
    private final boolean invertZoom = false; // TODO: Make configurable
    // const values until added to settings
    private final double minZoomMultiplier = .1;
    private final double maxZoomMultiplier = 50;
    private final double zoomIncrement = 0.2;

    // Movement
    private final int panMouseButton = InputEvent.BUTTON2_MASK; // TODO: Make configurable
    private double panMultiplierX = 1;
    private double panMultiplierY = 1;
    private Position translationVectorH;
    private Position translationVectorV;

    // Mouse rotation data
    private Point mouseLastWindow;
    private Point mouseCurrentWindow;
    private Position mouseWorldXY;
    private Position rotation;
    
    private FPSCounter fpsCounter;
    private Overlay overlay;
    private final String dimensionsLabel = "";

    private final ArrayList<Renderable> objects;
    private boolean idle = true;

    // Preferences
    private java.awt.Color clearColor;
    
    /**
     * Constructor.
     */
    public GcodeRenderer() {
        eye = new Position(0, 0, 1.5);
        center = new Position(0, 0, 0);
        objectMin = new Position(-10,-10,-10);
        objectMax = new Position( 10, 10, 10);
       
        workCoord = new Position(0, 0, 0);
        machineCoord = new Position(0, 0, 0);
       
        rotation = new Position(0.0, -30.0, 0.0);
        setVerticalTranslationVector();
        setHorizontalTranslationVector();

        objects = new ArrayList<>();
        objects.add(new MachineBoundries(Localization.getString("platform.visualizer.renderable.machine-boundries")));
        objects.add(new Tool(Localization.getString("platform.visualizer.renderable.tool-location")));
        objects.add(new MouseOver(Localization.getString("platform.visualizer.renderable.mouse-indicator")));
        objects.add(new OrientationCube(0.5f, Localization.getString("platform.visualizer.renderable.orientation-cube")));
        objects.add(new Grid(Localization.getString("platform.visualizer.renderable.grid")));
        Collections.sort(objects);

        reloadPreferences();
    }

    @Override
    public final Collection<Renderable> getRenderables() {
        return objects;
    }

    @Override
    public void registerRenderable(Renderable r) {
        if (r == null) return;
        if( !objects.contains(r) ) {
            objects.add(r);
            Collections.sort(objects);
        }
    }

    @Override
    public void removeRenderable(Renderable r) {
        if (r == null) return;
        if( objects.contains(r) ) {
            objects.remove(r);
            Collections.sort(objects);
        }
    }
    
    /**
     * Get the location on the XY plane of the mouse.
     */
    public Position getMouseWorldLocation() {
        return this.mouseWorldXY;
    }

    public void setWorkCoordinate(Position p) {
        this.workCoord.set(p.getPositionIn(UnitUtils.Units.MM));
    }
    
    public void setMachineCoordinate(Position p) {
        this.machineCoord.set(p.getPositionIn(UnitUtils.Units.MM));
    }

    final public void reloadPreferences() {
        VisualizerOptions vo = new VisualizerOptions();

        clearColor = vo.getOptionForKey(VISUALIZER_OPTION_BG).value;

        for (Renderable r : objects) {
            r.reloadPreferences(vo);
        }
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

        this.fpsCounter = new FPSCounter(drawable, new Font("SansSerif", Font.BOLD, 12));
        this.overlay = new Overlay(drawable, new Font("SansSerif", Font.BOLD, 12));
        this.overlay.setColor(127, 127, 127, 100);
        this.overlay.setTextLocation(Overlay.LOWER_LEFT);

        // Parse random gcode file and generate something to draw.
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
        glu = new GLU();                         // get GL Utilities
        gl.glShadeModel(GL2.GL_SMOOTH); // blends colors nicely, and smoothes out lighting
        gl.glClearColor(clearColor.getRed()/255f, clearColor.getGreen()/255f, clearColor.getBlue()/255f, clearColor.getAlpha()/255f);
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL2.GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST); // best perspective correction

        /*
        gl.glLoadIdentity();
        float[] lmodel_ambient = { 0.5f, 0.5f, 0.5f, 1.0f };
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, lmodel_ambient, 0);
        */

        // init lighting
        float ambient[] = { .6f, .6f, .6f, 1.f };
        float diffuse[] = { .6f, .6f, .6f, 1.0f };
        float position[] = { 0f, 0f, 20f, 1.0f };

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glEnable(GL2.GL_LIGHT0);  
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, position, 0);

        // Allow glColor to set colors
        gl.glEnable (GL2.GL_COLOR_MATERIAL) ;
        gl.glColorMaterial(GL.GL_FRONT, GL2.GL_DIFFUSE);
        gl.glColorMaterial(GL.GL_FRONT, GL2.GL_AMBIENT);
        //gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
        //gl.glColorMaterial(GL.GL_FRONT, GL2.GL_SPECULAR);


        float mat_specular[] =
            { 1.0f, 1.0f, 1.0f, 1.0f };
        float diffuseMaterial[] =
            { 0.5f, 0.5f, 0.5f, 1.0f };
     
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, diffuseMaterial, 0);
        //gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, mat_specular, 0);
        //gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 25.0f);

        //gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);



        gl.glEnable(GL2.GL_LIGHTING); 
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

        resizeForCamera(objectMin, objectMax, 0.9);
    }

    public void setObjectSize(Position min, Position max) {
        if (min == null || max == null) {
            this.objectMin = new Position(-10,-10,-10);
            this.objectMax = new Position( 10, 10, 10);
            idle = true;
        } else {
            this.objectMin = min;
            this.objectMax = max;
            idle = false;
        }
        resizeForCamera(objectMin, objectMax, 0.9);
    }

    /**
     * Zoom the visualizer to the given region.
     */
    public void zoomToRegion(Position min, Position max, double bufferFactor) {
        if (min == null || max == null) return;

        if (this.ySize == 0){ this.ySize = 1; }  // prevent divide by zero

        // Figure out offset compared to the current center.
        Position regionCenter = VisualizerUtils.findCenter(min, max);
        this.eye.x = regionCenter.x - this.center.x;
        this.eye.y = regionCenter.y - this.center.y;

        // Figure out what the scale factors would be if we reset this object.
        double _scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, min, max, bufferFactor);
        double _scaleFactor = _scaleFactorBase * this.zoomMultiplier;

        // Calculate the zoomMultiplier needed to get to that scale, and set it.
        this.zoomMultiplier = _scaleFactor/this.scaleFactorBase;
        this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
    }

    /**
     * Zoom to display the given region leaving the suggested buffer.
     */
    private void resizeForCamera(Position min, Position max, double bufferFactor) {
        if (min == null || max == null) return;

        if (this.ySize == 0){ this.ySize = 1; }  // prevent divide by zero

        this.center = VisualizerUtils.findCenter(min, max);
        this.scaleFactorBase = VisualizerUtils.findScaleFactor(this.xSize, this.ySize, min, max, bufferFactor);
        this.scaleFactor = this.scaleFactorBase * this.zoomMultiplier;
        this.panMultiplierX = VisualizerUtils.getRelativeMovementMultiplier(min.x, max.x, this.xSize);
        this.panMultiplierY = VisualizerUtils.getRelativeMovementMultiplier(min.y, max.y, this.ySize);
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

        // Update normals when an object is scaled
        gl.glEnable(GL2.GL_NORMALIZE); 

        // Setup the current matrix so that the projection can be done.
        if (mouseLastWindow != null) {
            gl.glPushMatrix();
                gl.glRotated(this.rotation.x, 0.0, 1.0, 0.0);
                gl.glRotated(this.rotation.y, 1.0, 0.0, 0.0);
                gl.glTranslated(-this.eye.x - this.center.x, -this.eye.y - this.center.y, -this.eye.z - this.center.z);
                this.mouseWorldXY = MouseProjectionUtils.intersectPointWithXYPlane(
                        drawable, mouseLastWindow.x, mouseLastWindow.y);
            gl.glPopMatrix();
        } else {
            this.mouseWorldXY = new Position(0, 0, 0);
        }

        // Render the different parts of the scene.
        for (Renderable r : objects) {
            // Don't draw disabled renderables.
            if (!r.isEnabled()) continue;

            gl.glPushMatrix();
                // in case a renderable sets the color, set it back to gray and opaque.
                gl.glColor4f(0.5f, 0.5f, 0.5f, 1f);

                if (r.rotate()) {
                    gl.glRotated(this.rotation.x, 0.0, 1.0, 0.0);
                    gl.glRotated(this.rotation.y, 1.0, 0.0, 0.0);
                }
                if (r.center()) {
                    gl.glTranslated(-this.eye.x - this.center.x, -this.eye.y - this.center.y, -this.eye.z - this.center.z);
                }

                if (!r.enableLighting()) {
                    gl.glDisable(GL2.GL_LIGHTING);
                }
                try {
                    r.draw(drawable, idle, machineCoord, workCoord, objectMin, objectMax, scaleFactor, mouseWorldXY, rotation);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "An exception occurred while drawing " + r.getClass().getSimpleName(), e);
                }
                if (!r.enableLighting()) {
                    gl.glEnable(GL2.GL_LIGHTING);
                    gl.glEnable(GL2.GL_LIGHT0);
                }
            gl.glPopMatrix();
        }
        
        this.fpsCounter.draw();
        this.overlay.draw(this.dimensionsLabel);
    
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
     * Setup the perspective matrix.
     */
    private void setupPerpective(int x, int y, GLAutoDrawable drawable, boolean ortho) {
        final GL2 gl = drawable.getGL().getGL2();
        float aspectRatio = (float)x / y;

        if (ortho) {
            gl.glMatrixMode(GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glOrtho(-0.60*aspectRatio/scaleFactor,0.60*aspectRatio/scaleFactor,-0.60/scaleFactor,0.60/scaleFactor,
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
    
    /**
     * Called after each render.
     */
    private void update() {
        if (debugCoordinates) {
            System.out.println("Machine coordinates: " + this.machineCoord.toString());
            System.out.println("Work coordinates: " + this.workCoord.toString());
            System.out.println("-----------------");
        }
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

        translationVectorH = new Position(x, y, yz);
        translationVectorH.normalizeXYZ();
    }

    private void setVerticalTranslationVector(){
        double y = Math.cos(Math.toRadians(this.rotation.y));
        double yz = Math.sin(Math.toRadians(this.rotation.y));

        translationVectorV = new Position(0, y, yz);
        translationVectorV.normalizeXYZ();
    }

    public void mouseMoved(Point lastPoint) {
        mouseLastWindow = lastPoint;
    }
    
    public void mouseRotate(Point point) {
        this.mouseCurrentWindow = point;
        if (this.mouseLastWindow != null) {
            int dx = this.mouseCurrentWindow.x - this.mouseLastWindow.x;
            int dy = this.mouseCurrentWindow.y - this.mouseLastWindow.y;

            rotation.x += dx / 2.0;
            rotation.y = Math.min(0, Math.max(-180, this.rotation.y += dy / 2.0));

            if (ortho) {
                setHorizontalTranslationVector();
                setVerticalTranslationVector();
            }
        }
        
        // Now that the motion has been accumulated, reset last.
        this.mouseLastWindow = this.mouseCurrentWindow;
    }
    
    public void mousePan(Point point) {
        this.mouseCurrentWindow = point;
        int dx = this.mouseCurrentWindow.x - this.mouseLastWindow.x;
        int dy = this.mouseCurrentWindow.y - this.mouseLastWindow.y;
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
        this.mouseLastWindow = this.mouseCurrentWindow;
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
        moveCamera(new Position(0, 0, 1.5), new Position(0, -30, 0), 1);
    }

    /**
     * Moves the camera to a position and rotation
     *
     * @param position to the given position
     * @param rotation directs the camera given this rotation
     * @param zoom the zoom level
     */
    public void moveCamera(Position position, Position rotation, double zoom) {
        this.zoomMultiplier = Math.min(Math.max(zoom, minZoomMultiplier), maxZoomMultiplier);
        this.scaleFactor = this.scaleFactorBase;
        this.eye = new Position(position);
        this.rotation = new Position(rotation);
    }
}
