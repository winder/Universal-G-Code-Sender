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
public class KeyEntityEvent extends EntityEvent {

    private final char key;
    private boolean shiftPressed;
    private boolean altPressed;
    private boolean ctrlPressed;

    public KeyEntityEvent(Entity entity, EventType type, char key) {
        super(entity, type);
        this.key = key;
    }

    public char getKey() {
        return key;
    }
}
