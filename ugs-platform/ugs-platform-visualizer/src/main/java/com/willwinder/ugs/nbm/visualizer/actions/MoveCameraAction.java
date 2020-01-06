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

import javax.swing.*;
import javax.vecmath.Point3d;
import java.awt.event.ActionEvent;

/**
 * An action for moving the camera in the vizualiser to the specified location
 *
 * @author Joacim Breiler
 */
public class MoveCameraAction extends AbstractAction {

    public static final Point3d CAMERA_POSITION = new Point3d(0, 0, 1.5);
    public static final Point3d ROTATION_TOP = new Point3d(0, 0, 0);
    public static final Point3d ROTATION_LEFT = new Point3d(90, -90, 0);
    public static final Point3d ROTATION_FRONT = new Point3d(0, -90, 0);
    public static final Point3d ROTATION_ISOMETRIC = new Point3d(30, -30, 0);

    private final GcodeRenderer gcodeRenderer;
    private Point3d position;
    private Point3d rotation;
    private double zoom;

    /**
     * A constructor for creating the action
     *
     * @param gcodeRenderer the renderer to be used
     * @param position      the position of the camera
     * @param rotation      the rotation in which direction the camera should be rotate against
     * @param zoom          the zoom level
     */
    public MoveCameraAction(GcodeRenderer gcodeRenderer, Point3d position, Point3d rotation, double zoom) {
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
    public MoveCameraAction(GcodeRenderer gcodeRenderer, Point3d rotation) {
        this(gcodeRenderer, CAMERA_POSITION, rotation, 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        gcodeRenderer.moveCamera(position, rotation, zoom);
    }
}