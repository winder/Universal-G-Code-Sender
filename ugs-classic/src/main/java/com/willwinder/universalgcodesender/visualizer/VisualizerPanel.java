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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Window manager for visualizer. Creates 3D canvas and manages data.
 *
 * @author wwinder
 */
public class VisualizerPanel extends JPanel implements UGSEventListener {

    private static final int FPS = 20; // animator's target frames per second

    private final VisualizerCanvas canvas;

    public VisualizerPanel() {
        this(null);
    }

    public VisualizerPanel(BackendAPI backend) {
        super(new BorderLayout());
        if (backend != null) {
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
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof FileStateEvent) {
            FileStateEvent fileStateEvent = (FileStateEvent) evt;
            switch (fileStateEvent.getFileState()) {
                case FILE_LOADING:
                    setGcodeFile(fileStateEvent.getFile());
                    break;

                case FILE_LOADED:
                    setProcessedGcodeFile(fileStateEvent.getFile());
                    break;

                default:
                    break;
            }
        } else if (evt instanceof ControllerStatusEvent) {
            ControllerStatusEvent controllerStatusEvent = (ControllerStatusEvent) evt;
            this.canvas.setMachineCoordinate(controllerStatusEvent.getStatus().getMachineCoord());
            this.canvas.setWorkCoordinate(controllerStatusEvent.getStatus().getWorkCoord());
        } else if (evt instanceof CommandEvent) {
            CommandEvent commandEvent = (CommandEvent) evt;
            if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_COMPLETE && !commandEvent.getCommand().isGenerated()) {
                canvas.setCurrentCommandNumber(commandEvent.getCommand().getCommandNumber());
            }
        }
    }
}