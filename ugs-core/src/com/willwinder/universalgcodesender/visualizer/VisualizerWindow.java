/*
 * Window manager for visualizer. Creates 3D canvas and manages data.
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

package com.willwinder.universalgcodesender.visualizer;

import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.WindowSettings;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class VisualizerWindow extends javax.swing.JFrame 
implements ControllerListener, WindowListener {

    private static String TITLE = Localization.getString("visualizer.title");  // window's title
    private static final int FPS = 20; // animator's target frames per second
    
    // OpenGL Control
    FPSAnimator animator;
    
    private String gcodeFile = null;
    private VisualizerCanvas canvas = null;
    
    /**
     * Creates new form Visualizer
     */
    public VisualizerWindow() {
        this(new WindowSettings(0,0,640,480));
    }
    
    public VisualizerWindow(WindowSettings ws) {

        this.setPreferredSize(new Dimension(ws.width, ws.height));
        this.setLocation(ws.xLocation, ws.yLocation);
        // Create the OpenGL rendering canvas
        this.canvas = new VisualizerCanvas();
        canvas.setPreferredSize(new Dimension(ws.width, ws.height));
        canvas.setLocation(ws.xLocation, ws.yLocation);

        // Create a animator that drives canvas' display() at the specified FPS.
        this.animator = new FPSAnimator(canvas, FPS, true);

        // Create the top-level container
        final JFrame frame = this; // Swing's JFrame or AWT's Frame
        frame.getContentPane().add(canvas);
        
        // Manage pausing and resuming the animator when it doesn't need to run.
        frame.addWindowListener(this);
        
        frame.setTitle(TITLE);
        frame.pack();
        frame.setVisible(true);
        animator.start(); // start the animation loop
    }                                

    public void setGcodeFile(String file) {
        this.gcodeFile = file;
        canvas.setGcodeFile(this.gcodeFile);
    }

    public void setProcessedGcodeFile(String file) {
        this.gcodeFile = file;
        canvas.setProcessedGcodeFile(this.gcodeFile);
    }
    
    public void setCompletedCommandNumber(int num) {
        this.canvas.setCurrentCommandNumber(num);
    }

    public double getMinArcLength() {
        return this.canvas.getMinArcLength();
    }

    public void setMinArcLength(double minArcLength) {
        this.canvas.setMinArcLength(minArcLength);
    }

    public double getArcLength() {
        return  this.canvas.getArcLength();
    }

    public void setArcLength(double arcLength) {
        this.canvas.setArcLength(arcLength);
    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
        // Give coordinates to canvas.
        this.canvas.setMachineCoordinate(machineCoord);
        this.canvas.setWorkCoordinate(workCoord);
    }
    
    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        // TODO: When canned cycles are handled in the controller I'll need to
        //       update the visualizer to use commands sniffed from this queue.
    }

    @Override
    public void commandSent(GcodeCommand command) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandComment(String comment) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void postProcessData(int numRows) {
        // Visualizer doesn't care.
    }
    
    // Window Listener Events.

    @Override
    public void windowClosing(WindowEvent e) {
        // Use a dedicate thread to run the stop() to ensure that the
        // animator stops before program exits.
        new Thread() {
            @Override
            public void run() {
                if (animator.isStarted()){ animator.pause(); }
            }
        }.start();
    }

    @Override
    public void windowActivated(WindowEvent e) {
        if (animator.isPaused()) { animator.resume(); }
    }
        

    @Override
    public void windowOpened(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowClosed(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowIconified(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}