/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.actions;

import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.universalgcodesender.model.Position;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action for moving the camera in the visualizer to the specified location
 *
 * @author Joacim Breiler
 */
public class MoveCameraAction extends AbstractAction {

    public static final Position CAMERA_POSITION = new Position(0, 0, 1.5);
    public static final Position ROTATION_TOP = new Position(0, 0, 0);
    public static final Position ROTATION_LEFT = new Position(90, -90, 0);
    public static final Position ROTATION_FRONT = new Position(0, -90, 0);
    public static final Position ROTATION_ISOMETRIC = new Position(30, -30, 0);

    private final GcodeRenderer gcodeRenderer;
    private Position position;
    private Position rotation;
    private double zoom;

    /**
     * A constructor for creating the action
     *
     * @param gcodeRenderer the renderer to be used
     * @param position      the position of the camera
     * @param rotation      the rotation in which direction the camera should be rotate against
     * @param zoom          the zoom level
     */
    public MoveCameraAction(GcodeRenderer gcodeRenderer, Position position, Position rotation, double zoom) {
        this.gcodeRenderer = gcodeRenderer;
        this.position = position;
        this.rotation = rotation;
        this.zoom = zoom;
    }

    /**
     * A constructor for creating the action with some defaults
     *
     * @param gcodeRenderer the renderer to be used
     * @param rotation      the rotation in which direction the camera should be rotate against
     */
    public MoveCameraAction(GcodeRenderer gcodeRenderer, Position rotation) {
        this(gcodeRenderer, CAMERA_POSITION, rotation, 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gcodeRenderer.moveCamera(position, rotation, zoom);
    }
}