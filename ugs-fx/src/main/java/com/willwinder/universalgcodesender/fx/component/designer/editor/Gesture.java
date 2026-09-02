/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;

import java.awt.geom.Point2D;

/**
 * A press, drag, release sequence on the design, in design coordinates. Gestures change the
 * model live while dragging and commit one undoable action on release.
 */
interface Gesture {

    void begin(PointerEvent event, Point2D design);

    void drag(PointerEvent event, Point2D design);

    void end(PointerEvent event, Point2D design);
}
