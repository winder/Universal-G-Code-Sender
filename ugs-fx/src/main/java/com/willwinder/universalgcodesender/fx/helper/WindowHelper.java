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
package com.willwinder.universalgcodesender.fx.helper;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.util.Optional;

/**
 * Helpers for finding the window an action was triggered from and for placing dialogs over it.
 */
public final class WindowHelper {

    private WindowHelper() {
    }

    /**
     * The window that the event originated from, falling back to the focused window when the
     * event did not come from a node in a scene (for example a keyboard shortcut).
     *
     * @return the window, or null when none could be resolved
     */
    public static Window resolveWindow(Event event) {
        if (event != null && event.getSource() instanceof Node node && node.getScene() != null) {
            return node.getScene().getWindow();
        }
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }

    /**
     * Centers the stage over its owner window, or on the screen when it has no owner.
     * <p>
     * A stage that has not been shown and has no explicit size does not know its size yet, since
     * JavaFX sizes it to its scene when it is shown. Such a stage is placed using the preferred
     * size of its scene so it appears close to the center right away, and centered exactly once
     * it has been shown and its real size is known.
     */
    public static void centerOnOwner(Stage stage) {
        if (Double.isNaN(stage.getWidth()) || Double.isNaN(stage.getHeight())) {
            estimateSize(stage).ifPresent(size -> center(stage, size.getWidth(), size.getHeight()));
            stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<>() {
                @Override
                public void handle(WindowEvent event) {
                    stage.removeEventHandler(WindowEvent.WINDOW_SHOWN, this);
                    centerOnOwner(stage);
                }
            });
            return;
        }
        center(stage, stage.getWidth(), stage.getHeight());
    }

    private static void center(Stage stage, double width, double height) {
        Window owner = stage.getOwner();
        if (owner == null) {
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            stage.setX(screen.getMinX() + (screen.getWidth() - width) / 2);
            stage.setY(screen.getMinY() + (screen.getHeight() - height) / 2);
            return;
        }
        stage.setX(owner.getX() + (owner.getWidth() - width) / 2);
        stage.setY(owner.getY() + (owner.getHeight() - height) / 2);
    }

    private static Optional<Dimension2D> estimateSize(Stage stage) {
        Scene scene = stage.getScene();
        if (scene == null) {
            return Optional.empty();
        }
        Parent root = scene.getRoot();
        root.applyCss();
        root.layout();
        double width = Double.isNaN(stage.getWidth()) ? root.prefWidth(-1) : stage.getWidth();
        double height = Double.isNaN(stage.getHeight()) ? root.prefHeight(width) : stage.getHeight();
        return Optional.of(new Dimension2D(width, height));
    }
}
