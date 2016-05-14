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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.WindowSettings;
import com.willwinder.universalgcodesender.uielements.LengthLimitedDocument;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *
 * @author wwinder
 */
public class VisualizerPanel extends JPanel implements ControllerListener, UGSEventListener {

    private static String TITLE = Localization.getString("visualizer.title");  // window's title
    private static final int FPS = 20; // animator's target frames per second

    // OpenGL Control
    private final FPSAnimator animator;

    private final VisualizerCanvas canvas;

    private final BackendAPI backend;

    public VisualizerPanel() {
        this(null);
    }

    public VisualizerPanel(BackendAPI backend) {
        super(new BorderLayout());
        this.backend = backend;
        if (backend != null) {
            backend.addControllerListener(this);
            backend.addUGSEventListener(this);
        }

        // Create the OpenGL rendering canvas
        this.canvas = new VisualizerCanvas();

        // Create a animator that drives canvas' display() at the specified FPS.
        this.animator = new FPSAnimator(canvas, FPS, true);

        animator.start(); // start the animation loop

        initComponents();
    }

    private void initComponents() {
        add(canvas);
    }

    public void loadSettings() {
        setMinArcLength(backend.getSettings().getSmallArcThreshold());
        setArcLength(backend.getSettings().getSmallArcSegmentLength());
    }

    public void setGcodeFile(String file) {
        canvas.setGcodeFile(file);
    }

    public void setProcessedGcodeFile(String file) {
        canvas.setProcessedGcodeFile(file);
    }
    
    private void setMinArcLength(double minArcLength) {
        this.canvas.setMinArcLength(minArcLength);
    }

    private void setArcLength(double arcLength) {
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

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isFileChangeEvent()) {
            switch(evt.getFileState()) {
                case FILE_LOADING:
                    setGcodeFile(evt.getFile());
                    break;

                case FILE_LOADED:
                    setProcessedGcodeFile(evt.getFile());
                    break;

                default:
                    break;
            }
        }
    }
}