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

package com.willwinder.universalgcodesender.visualizer;

import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Window manager for visualizer. Creates 3D canvas and manages data.
 *
 * @author wwinder
 */
public class VisualizerPanel extends JPanel implements ControllerListener, UGSEventListener {

    private static final int FPS = 20; // animator's target frames per second

    private final VisualizerCanvas canvas;

    public VisualizerPanel() {
        this(null);
    }

    public VisualizerPanel(BackendAPI backend) {
        super(new BorderLayout());
        if (backend != null) {
            backend.addControllerListener(this);
            backend.addUGSEventListener(this);
        }

        // Create the OpenGL rendering canvas
        this.canvas = new VisualizerCanvas();

        // Create a animator that drives canvas' display() at the specified FPS.
        FPSAnimator animator = new FPSAnimator(canvas, FPS, true);

        animator.start(); // start the animation loop

        initComponents();
    }

    private void initComponents() {
        add(canvas);
    }

    public void setGcodeFile(String file) {
        canvas.setGcodeFile(file);
    }

    public void setProcessedGcodeFile(String file) {
        canvas.setProcessedGcodeFile(file);
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        // Give coordinates to canvas.
        this.canvas.setMachineCoordinate(status.getMachineCoord());
        this.canvas.setWorkCoordinate(status.getWorkCoord());
    }
    
    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void receivedAlarm(Alarm alarm) {

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
        canvas.setCurrentCommandNumber(command.getCommandNumber());
    }

    @Override
    public void commandComment(String comment) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void probeCoordinates(Position p) {
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