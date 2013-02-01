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

import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.media.opengl.GL;
import static javax.media.opengl.GL.*; // GL2 constants
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static javax.media.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import javax.media.opengl.glu.GLU;

 
/**
 *
 * @author wwinder
 * @template http://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html
 * 
 * JOGL 2.0 Program Template (GLCanvas)
 * This is a "Component" which can be added into a top-level "Container".
 * It also handles the OpenGL events to render graphics.
 */
@SuppressWarnings("serial")
public class VisualizerCanvas extends GLCanvas implements GLEventListener {
    //private String gcodeFile = "/home/wwinder/Desktop/programs/GRBL/Universal-G-Code-Sender/test_files/shapeoko.txt";
    private String gcodeFile = "/home/wwinder/Desktop/programs/GRBL/Universal-G-Code-Sender/test_files/bigarc.gcode";
    //private String gcodeFile = "/home/wwinder/Desktop/programs/GRBL/Universal-G-Code-Sender/test_files/face.gcode";
    //private String gcodeFile = "/home/wwinder/Desktop/programs/GRBL/Universal-G-Code-Sender/test_files/buffer_stress_test.gcode";
    //private String gcodeFile = "/home/wwinder/Desktop/programs/GRBL/Universal-G-Code-Sender/test_files/line_skip_test.gcode";
    //private String gcodeFile = "/home/wwinder/Desktop/programs/GRBL/Universal-G-Code-Sender/test_files/electric_turtle.nc";
    
    private GLU glu;  // for the GL Utility
    private float angle = 0.0f;  // rotation angle of the triangle
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
    
    private float[] findCenter(float[] bounds)
    {
        float cent[] = {(bounds[0] + bounds[3]),(bounds[1] + bounds[4]),(bounds[2] + bounds[5])};
        return cent;
    }
    
    public void generateObject()
    {
        //scale(1, -1, 1); //orient cooridnate plane right-handed props to whosawwhatsis for discovering this

        GcodeViewParse gcvp = new GcodeViewParse();
        objCommands = (gcvp.toObj(readFiletoArrayList(this.gcodeFile)));
        float bounds[] = gcvp.getExtremes();
        //extremes[] = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; // -x, -y, -z, x, y, z
        
        
        System.out.println("Object bounds");
        System.out.println("           "+bounds[4]);
        System.out.println("             / \\"+bounds[2]);
        System.out.println("              | /            ");
        System.out.println("              |/             "+bounds[3]);
        System.out.println(bounds[0]+"<---------------------->");
        System.out.println("             /|              ");
        System.out.println("            / |              ");
        System.out.println("           / \\ /          ");
        System.out.println("          /"+bounds[1]);
        System.out.println("      "+bounds[5]);
        float cent[] = findCenter(bounds);
        System.out.println("Center = " + cent);
        //cam.lookAt(-1*bounds[0], -1*bounds[1], 0, camOffset);
        System.out.println("objComBumands :" + objCommands.size());
        maxSlider = objCommands.get(objCommands.size() - 1).getLayer() - 1; // Maximum slider value is highest layer
        defaultValue = maxSlider;
        //controlP5.remove("Layer Slider");
        //controlP5.addSlider("Layer Slider",minSlider,maxSlider,defaultValue,20,100,10,300).setNumberOfTickMarks(maxSlider);
        //controlP5.addControlWindow("ControlWindow", 50, 50, 20, 20);

        //curLayer = (int)Math.round(controlP5.controller("Layer Slider").value());
        isDrawable = true;
    }
    
    public VisualizerCanvas() {
       this.addGLEventListener(this);
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

       // ----- Your OpenGL initialization code here -----
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

        if (height == 0) height = 1;   // prevent divide by zero
        float aspect = (float)width / height;

        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
        gl.glLoadIdentity();             // reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.1, 100.0); // fovy, aspect, zNear, zFar

        // Move camera out and point it at the origin
        glu.gluLookAt(0, 0, -10, 0, 0, 0, 0, -1, 0);

        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset
    }

    /**
     * Called back by the animator to perform rendering.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        final GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL.GL_DEPTH_TEST);



        gl.glMatrixMode(GL_MODELVIEW);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        gl.glPushMatrix();
        //noSmooth();

        if(isDrawable) {
            gl.glScaled(1, -1, 1);
            float[] points = new float[6];

            int maxLayer = 100; //(int)Math.round(controlP5.controller("Layer Slider").value());

            int curTransparency = 0;
            int curColor = 0;
            gl.glBegin(GL_LINES);
            //beginShape(LINES);
            gl.glLineWidth(2.0f);

            for(LineSegment ls : objCommands)
            {
                if(ls.getLayer() < maxLayer) {
                        curTransparency = SOLID;
                }
                if(ls.getLayer() == maxLayer) {
                        curTransparency = SUPERSOLID;
                }
                if(ls.getLayer() > maxLayer) {
                        curTransparency = TRANSPARENT;
                }
                if(!ls.getExtruding()) {
                        //stroke(WHITE,TRANSPARENT);
                }
                if(!dualExtrusionColoring) {
                    if(ls.getExtruding())
                    {
                        if(isSpeedColored)
                        {
                            if(ls.getSpeed() > LOW_SPEED && ls.getSpeed() < MEDIUM_SPEED) {
                                    //stroke(PURPLE, curTransparency);
                            }
                            if(ls.getSpeed() > MEDIUM_SPEED && ls.getSpeed() < HIGH_SPEED) {
                                    //stroke(BLUE, curTransparency);
                            }
                            else if(ls.getSpeed() >= HIGH_SPEED) {
                                    //stroke(OTHER_YELLOW, curTransparency);
                            }
                            else { //Very low speed....
                                    //stroke(GREEN, curTransparency);
                            }
                        }
                        if(!isSpeedColored)
                        {
                            if(curColor == 0) {
                             //stroke(GREEN, SUPERSOLID);
                            } 
                            if(curColor == 1) {
                            //stroke(RED, SUPERSOLID); 
                            } 
                            if(curColor == 2) {
                             //stroke(BLUE, SUPERSOLID);
                            } 
                            if(curColor == 3) {
                             //stroke(YELLOW, SUPERSOLID);
                            }
                             curColor++; 
                            if(curColor == 4) {
                                curColor = 0; 
                            }
                        }
                    }
                }
                if(dualExtrusionColoring)
                {
                    if(ls.getExtruding())
                    {
                        if(ls.getToolhead() == 0)
                        {
                                //stroke(BLUE, curTransparency);
                        }
                        if(ls.getToolhead() == 1)
                        {
                                //stroke(GREEN, curTransparency);
                        }
                    }
                }

                // Actually draw it.
                if(!is2D || (ls.getLayer() == maxLayer)) {
                    points = ls.getPoints();

                    gl.glVertex3d(points[0], points[1], points[2]);
                    gl.glVertex3d(points[3], points[4], points[5]);
                }
            }

            gl.glEnd();
            //endShape();
            if((curLayer != maxLayer) && is2D)
            {
              //cam.setDistance(cam.getDistance() + (maxLayer - curLayer)*.3,0);
            }
             curLayer = maxLayer;
        }

        gl.glPopMatrix();
        // makes the gui stay on top of elements
        // drawn before.
        gl.glDisable(GL.GL_DEPTH_TEST);


            //render(drawable);
            //update();
    }

    // Render a triangle
    private void render(GLAutoDrawable drawable) {
        // Get the OpenGL graphics context
        GL2 gl = drawable.getGL().getGL2();
        // Clear the color and the depth buffers
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        // Reset the view (x, y, z axes back to normal)
        gl.glLoadIdentity();   

        // Draw a triangle
        float sin = (float)Math.sin(angle);
        float cos = (float)Math.cos(angle);
        gl.glTranslatef(0.0f, 0.0f, -6.0f); // translate into the screen
        gl.glBegin(GL_TRIANGLES);
            gl.glColor3f(1.0f, 0.0f, 0.0f);   // Red
            gl.glVertex2d(-cos, -cos);
            gl.glColor3f(0.0f, 1.0f, 0.0f);   // Green
            gl.glVertex2d(0.0f, cos);
            gl.glColor3f(0.0f, 0.0f, 1.0f);   // Blue
            gl.glVertex2d(sin, -sin);
        gl.glEnd();
    }

    // Update the angle of the triangle after each frame
    private void update() {
        angle += 0.01f;
    }

    /**
     * Called back before the OpenGL context is destroyed. Release resource such as buffers.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) { }



private boolean dualExtrusionColoring = false ;

    //PeasyCam cam; //The camera object, PeasyCam extends the default processing camera and enables user interaction
    //ControlP5 controlP5; //ControlP5 object, ControlP5 is a library used for drawing GUI items
    //PMatrix3D currCameraMatrix; //By having 2 camera matrix I'm able to switch a 3D pannable area and a fixed gui in relation to the user
    //PGraphicsOpenGL g3; //The graphics object, necessary for openGL manipulations
    //ControlGroup panButts; //The group of controlP5 buttons related to panning
    private boolean is2D = false;
    private boolean isDrawable = false; //True if a file is loaded; false if not
    private boolean isPlatformed = false;
    private boolean isSpeedColored = true;
    private String gCode; //The path of the gcode File
    private ArrayList<LineSegment> objCommands; //An ArrayList of linesegments composing the model
    private int curScale = 20; 
    private int curLayer = 0;


    ////////////ALPHA VALUES//////////////

    private final int TRANSPARENT = 20;
    private final int SOLID = 100;
    private final int SUPERSOLID = 255;

    //////////////////////////////////////

    ////////////COLOR VALUES/////////////

/*
    private final int RED = color(255,200,200);
    private final int BLUE = color(0, 255, 255);
    private final int PURPLE = color(242, 0, 255);
    private final int YELLOW = color(237, 255, 0);
    private final int OTHER_YELLOW = color(234, 212, 7);
    private final int GREEN = color(33, 255, 0);
    private final int WHITE = color(255, 255, 255);
*/
    //////////////////////////////////////

    ///////////SPEED VALUES///////////////

    private float LOW_SPEED = 700;
    private float MEDIUM_SPEED = 1400;
    private float HIGH_SPEED = 1900;

    //////////////////////////////////////

    //////////SLIDER VALUES/////////////

    private int minSlider = 1;
    private int maxSlider;
    private int defaultValue;

    ////////////////////////////////////

    /////////Canvas Size///////////////

    private int xSize;
    private int ySize;

    ////////////////////////////////////
    private int camOffset = 70;
    private int textBoxOffset = 200;

}