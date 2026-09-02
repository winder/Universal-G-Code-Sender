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

import com.willwinder.ugs.designer.actions.UndoManager;
import com.willwinder.ugs.designer.entities.cuttable.Text;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import javafx.geometry.Point3D;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

/**
 * What every editor tool works with: the designer controller, the camera for pixel to
 * millimeter conversions, picking, snapping, the shared {@link EditorState} and the callbacks
 * into the visualizer.
 *
 * @param requestRender asks for a new frame after the tool changed something
 * @param textEditor    opens the inline editor for a text entity
 */
public record ToolContext(Controller controller, Camera camera, HitTester hitTester, Snapper snapper,
                          EditorState state, Runnable requestRender, Consumer<Text> textEditor) {
    /** How close to an outline a click may land, in logical pixels. */
    public static final double PICK_TOLERANCE_PX = 4;

    public SelectionManager selection() {
        return controller.getSelectionManager();
    }

    public UndoManager undoManager() {
        return controller.getUndoManager();
    }

    public double worldUnitsPerPixel() {
        return camera.worldUnitsPerPixel();
    }

    /**
     * The world units one pixel covers at a design position, which in a perspective view
     * depends on how far that position is from the camera.
     */
    public double worldUnitsPerPixelAt(Point2D design) {
        return camera.worldUnitsPerPixelAt(new Point3D(design.getX(), design.getY(), 0));
    }

    public double pickTolerance(Point2D design) {
        return PICK_TOLERANCE_PX * worldUnitsPerPixelAt(design);
    }

    public void selectTool(Tool tool) {
        controller.setTool(tool);
    }

    public void render() {
        requestRender.run();
    }
}
