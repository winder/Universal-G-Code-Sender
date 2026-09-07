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
package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.ViewOrientation;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.OrientationCubeRenderable;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Highlights the orientation cube's faces on hover and snaps the view when one is clicked.
 * Everything over the cube's inset is claimed so that no gesture below starts there.
 */
public final class OrientationCubeHandler implements InputHandler {
    private final OrientationCubeRenderable cube;
    private final Consumer<ViewOrientation> onFaceClicked;
    private ViewOrientation pressedFace;

    public OrientationCubeHandler(OrientationCubeRenderable cube, Consumer<ViewOrientation> onFaceClicked) {
        this.cube = cube;
        this.onFaceClicked = onFaceClicked;
    }

    @Override
    public boolean onPressed(PointerEvent event) {
        if (!cube.contains(event.x(), event.y())) {
            return false;
        }
        pressedFace = event.button() == MouseButton.PRIMARY
                ? cube.faceAt(event.x(), event.y()).orElse(null)
                : null;
        return true;
    }

    @Override
    public void onReleased(PointerEvent event, PointerEvent pressed) {
        ViewOrientation face = pressedFace;
        pressedFace = null;
        if (face != null && cube.faceAt(event.x(), event.y()).filter(face::equals).isPresent()) {
            onFaceClicked.accept(face);
        }
    }

    @Override
    public boolean onMoved(PointerEvent event) {
        cube.setHoveredFace(cube.faceAt(event.x(), event.y()).orElse(null));
        return cube.contains(event.x(), event.y());
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event) {
        return cube.faceAt(event.x(), event.y()).map(face -> Cursor.HAND);
    }
}
