/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;

import java.awt.geom.Point2D;

/**
 * @author Joacim Breiler
 */
public class MouseEntityEvent extends EntityEvent {

    private final Point2D currentMousePosition;
    private final Point2D startMousePosition;
    private boolean shiftPressed;
    private boolean altPressed;
    private boolean ctrlPressed;

    public MouseEntityEvent(Entity entity, EventType type, Point2D startMousePosition, Point2D currentMousePosition) {
        super(entity, type);
        this.currentMousePosition = currentMousePosition;
        this.startMousePosition = startMousePosition;
    }

    public MouseEntityEvent(Control entity, EventType type, Point2D startMousePosition, Point2D currentMousePosition, boolean shiftPressed, boolean ctrlPressed, boolean altPressed) {
        this(entity, type, startMousePosition, currentMousePosition);
        this.shiftPressed = shiftPressed;
        this.altPressed = altPressed;
        this.ctrlPressed = ctrlPressed;
    }

    public Point2D getCurrentMousePosition() {
        return currentMousePosition;
    }

    public Point2D getStartMousePosition() {
        return startMousePosition;
    }

    public Point2D getMovementDelta() {
        return new Point2D.Double(startMousePosition.getX() - currentMousePosition.getX(), startMousePosition.getY() - currentMousePosition.getY());
    }

    public boolean isShiftPressed() {
        return shiftPressed;
    }

    public boolean isAltPressed() {
        return altPressed;
    }

    public boolean isCtrlPressed() {
        return ctrlPressed;
    }
}
