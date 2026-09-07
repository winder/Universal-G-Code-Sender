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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.actions.ChangeTextAction;
import com.willwinder.ugs.designer.entities.cuttable.Text;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import javafx.geometry.Point3D;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.awt.geom.Rectangle2D;

/**
 * A text field placed over a text entity in the visualizer. Enter commits the new text as one
 * undoable change, Escape or clicking elsewhere cancels.
 */
public final class TextEditOverlay extends TextField {
    private static final double MIN_WIDTH = 120;
    private final Camera camera;
    private Text entity;

    public TextEditOverlay(Camera camera) {
        this.camera = camera;
        setVisible(false);
        setManaged(false);
        setOnAction(event -> commit());
        setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                hide();
                event.consume();
            }
        });
        focusedProperty().addListener((observable, wasFocused, isFocused) -> {
            if (!isFocused && isVisible()) {
                hide();
            }
        });
    }

    /**
     * Opens the editor over the entity, positioned at the entity's top-left corner on screen.
     */
    public void edit(Text text) {
        entity = text;
        setText(text.getText());
        Rectangle2D bounds = text.getBounds();
        camera.project(new Point3D(bounds.getMinX(), bounds.getMaxY(), 0)).ifPresent(pixel -> {
            relocate(pixel.getX(), pixel.getY() - 30);
        });
        double width = camera.project(new Point3D(bounds.getMaxX(), bounds.getMaxY(), 0))
                .flatMap(right -> camera.project(new Point3D(bounds.getMinX(), bounds.getMaxY(), 0))
                        .map(left -> right.getX() - left.getX()))
                .orElse(MIN_WIDTH);
        setPrefWidth(Math.max(width, MIN_WIDTH));
        resize(getPrefWidth(), 26);
        setVisible(true);
        requestFocus();
        selectAll();
    }

    public void hide() {
        setVisible(false);
        entity = null;
    }

    private void commit() {
        Text target = entity;
        String text = getText();
        hide();
        if (target != null && text != null && !text.equals(target.getText())) {
            ChangeTextAction action = new ChangeTextAction(target, text);
            action.redo();
            ControllerFactory.getController().getUndoManager().addAction(action);
        }
    }
}
