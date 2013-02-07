/*
 * Window manager for visualizer. Creates 3D canvas and manages data.
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

import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.universalgcodesender.CommUtils.Capabilities;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

/**
 *
 * @author wwinder
 * @template http://www3.ntu.edu.sg/home/ehchua/programming/opengl/JOGL2.0.html
 */
public class VisualizerWindow extends javax.swing.JFrame implements SerialCommunicatorListener {

    private static String TITLE = "G-Code Visualizer";  // window's title
    private static final int CANVAS_WIDTH = 640;  // width of the drawable
    private static final int CANVAS_HEIGHT = 480; // height of the drawable
    private static final int FPS = 60; // animator's target frames per second

    // Interactive members.
    private boolean realTime = false;
    private boolean position = false;
    private Capabilities positionVersion = null;
    private Coordinate machineCoordinate;
    private Coordinate workCoordinate;

    private String gcodeFile = null;
    private VisualizerCanvas canvas = null;
    
    /**
     * Creates new form Visualizer
     */
    public VisualizerWindow() {

        // Create the OpenGL rendering canvas
        this.canvas = new VisualizerCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        // Create a animator that drives canvas' display() at the specified FPS.
        final FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

        // Create the top-level container
        final JFrame frame = this; // Swing's JFrame or AWT's Frame
        frame.getContentPane().add(canvas);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread() {
                    @Override
                    public void run() {
                        if (animator.isStarted()) animator.stop();
                        System.exit(0);
                    }
                }.start();
            }
        });
        frame.setTitle(TITLE);
        frame.pack();
        frame.setVisible(true);
        animator.start(); // start the animation loop
    }                                

    public void setGcodeFile(String file) {
        this.gcodeFile = file;
        canvas.setGcodeFile(file);
    }
    
    @Override
    public void capabilitiesListener(Capabilities capability) {
        switch (capability) {
            case REAL_TIME:
                this.realTime = true;
                break;
            case POSITION_C:
                this.position = true;
                this.positionVersion = Capabilities.POSITION_C;
                break;
        }
    }

    @Override
    public void positionStringListener(String position) {
        machineCoordinate = GrblUtils.getMachinePositionFromPositionString(position, this.positionVersion);        
        workCoordinate = GrblUtils.getWorkPositionFromPositionString(position, this.positionVersion);
    }
    
    
    @Override
    public void fileStreamComplete(String filename, boolean success) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandQueued(GcodeCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandSent(GcodeCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandComment(String comment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageForConsole(String msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void verboseMessageForConsole(String msg) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String preprocessCommand(String command) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
