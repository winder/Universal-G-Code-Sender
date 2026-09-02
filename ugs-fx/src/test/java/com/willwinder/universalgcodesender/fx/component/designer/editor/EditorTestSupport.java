package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Viewport;
import javafx.event.EventType;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * Shared set up for the editor tests: a fresh drawing on the designer's controller, a camera
 * looking straight down and pointer events in design coordinates.
 */
final class EditorTestSupport {

    private EditorTestSupport() {
    }

    static Controller freshController() {
        Controller controller = ControllerFactory.getController();
        controller.newDrawing();
        controller.getSelectionManager().clearSelection();
        controller.setTool(Tool.SELECT);
        return controller;
    }

    static Camera topDownCamera() {
        Camera camera = new Camera();
        camera.setViewport(new Viewport(800, 600, 1));
        camera.pitchProperty().set(90);
        camera.yawProperty().set(0);
        return camera;
    }

    static ToolContext toolContext(Controller controller, Camera camera, EditorState state) {
        return new ToolContext(controller, camera, new HitTester(controller),
                new Snapper(() -> controller.getModel().getSnapToGrid()), state, () -> {
        }, text -> {
        });
    }

    static Point2D design(double x, double y) {
        return new Point2D.Double(x, y);
    }

    static PointerEvent press(double x, double y) {
        return pointer(MouseEvent.MOUSE_PRESSED, x, y, false, false, 1);
    }

    static PointerEvent drag(double x, double y) {
        return pointer(MouseEvent.MOUSE_DRAGGED, x, y, false, false, 1);
    }

    static PointerEvent release(double x, double y) {
        return pointer(MouseEvent.MOUSE_RELEASED, x, y, false, false, 1);
    }

    static PointerEvent pointer(EventType<MouseEvent> type, double x, double y, boolean shift, boolean alt, int clickCount) {
        MouseEvent mouse = new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, clickCount,
                shift, false, alt, false, true, false, false, false, false, true, null);
        return new PointerEvent(mouse, null, Optional.of(new Point3D(x, y, 0)));
    }
}
