package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Viewport;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings.ModifierKey;
import javafx.geometry.Point3D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class CameraNavigationHandlerTest {
    private static final MouseMapping PAN = new MouseMapping(MouseButton.SECONDARY, ModifierKey.NONE);
    private static final MouseMapping ROTATE = new MouseMapping(MouseButton.SECONDARY, ModifierKey.SHIFT);

    private Camera camera;
    private boolean invertZoom;
    private boolean invertRotation;
    private CameraNavigationHandler handler;

    @Before
    public void setUp() {
        camera = new Camera();
        camera.setViewport(new Viewport(800, 600, 1));
        handler = new CameraNavigationHandler(camera, () -> PAN, () -> ROTATE, () -> invertZoom, () -> invertRotation);
    }

    @Test
    public void onPressed_shouldIgnoreButtonsThatAreNotMapped() {
        assertThat(handler.onPressed(pointer(MouseEvent.MOUSE_PRESSED, 10, 10, MouseButton.PRIMARY, false))).isFalse();
    }

    @Test
    public void onDragged_shouldRotateWhenPressedWithTheRotateMapping() {
        double yaw = camera.yawProperty().get();
        double pitch = camera.pitchProperty().get();
        PointerEvent pressed = pointer(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.SECONDARY, true);

        assertThat(handler.onPressed(pressed)).isTrue();
        handler.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 120, 90, MouseButton.SECONDARY, true), pressed);

        assertThat(camera.yawProperty().get()).isCloseTo(yaw - 20 * CameraNavigationHandler.ROTATE_DEGREES_PER_PIXEL, within(1e-9));
        assertThat(camera.pitchProperty().get()).isCloseTo(pitch - 10 * CameraNavigationHandler.ROTATE_DEGREES_PER_PIXEL, within(1e-9));
        assertThat(camera.target()).isEqualTo(new Point3D(100, 100, 0));
    }

    @Test
    public void onDragged_shouldRotateTheOtherWayWhenRotationIsInverted() {
        invertRotation = true;
        double yaw = camera.yawProperty().get();
        double pitch = camera.pitchProperty().get();
        PointerEvent pressed = pointer(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.SECONDARY, true);

        handler.onPressed(pressed);
        handler.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 120, 90, MouseButton.SECONDARY, true), pressed);

        assertThat(camera.yawProperty().get()).isCloseTo(yaw + 20 * CameraNavigationHandler.ROTATE_DEGREES_PER_PIXEL, within(1e-9));
        assertThat(camera.pitchProperty().get()).isCloseTo(pitch + 10 * CameraNavigationHandler.ROTATE_DEGREES_PER_PIXEL, within(1e-9));
    }

    @Test
    public void onDragged_shouldPanWhenPressedWithThePanMapping() {
        camera.pitchProperty().set(90);
        camera.yawProperty().set(0);
        Point3D before = camera.target();
        PointerEvent pressed = pointer(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.SECONDARY, false);

        assertThat(handler.onPressed(pressed)).isTrue();
        handler.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 150, 100, MouseButton.SECONDARY, false), pressed);

        Point3D after = camera.target();
        assertThat(after.getX()).isCloseTo(before.getX() - 50 * camera.worldUnitsPerPixel(), within(1e-6));
        assertThat(after.getY()).isCloseTo(before.getY(), within(1e-6));
        assertThat(camera.yawProperty().get()).isEqualTo(0);
    }

    @Test
    public void onDragged_shouldUseTheDeltaSinceTheLastEvent() {
        camera.pitchProperty().set(90);
        camera.yawProperty().set(0);
        Point3D before = camera.target();
        PointerEvent pressed = pointer(MouseEvent.MOUSE_PRESSED, 100, 100, MouseButton.SECONDARY, false);
        handler.onPressed(pressed);

        handler.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 110, 100, MouseButton.SECONDARY, false), pressed);
        handler.onDragged(pointer(MouseEvent.MOUSE_DRAGGED, 120, 100, MouseButton.SECONDARY, false), pressed);

        assertThat(camera.target().getX()).isCloseTo(before.getX() - 20 * camera.worldUnitsPerPixel(), within(1e-6));
    }

    @Test
    public void onScroll_shouldZoomTowardsTheCursorAndHonourInversion() {
        double distance = camera.distanceProperty().get();

        assertThat(handler.onScroll(new ScrollInput(200, 150, 0, 300, false, false, false, false))).isTrue();
        assertThat(camera.distanceProperty().get()).isLessThan(distance);

        invertZoom = true;
        handler.onScroll(new ScrollInput(200, 150, 0, 300, false, false, false, false));
        assertThat(camera.distanceProperty().get()).isCloseTo(distance, within(1e-6));
    }

    private static PointerEvent pointer(javafx.event.EventType<MouseEvent> type, double x, double y,
                                        MouseButton button, boolean shift) {
        MouseEvent mouse = new MouseEvent(type, x, y, x, y, button, 1,
                shift, false, false, false,
                button == MouseButton.PRIMARY, button == MouseButton.MIDDLE, button == MouseButton.SECONDARY,
                false, false, false, null);
        return new PointerEvent(mouse, null, java.util.Optional.empty());
    }
}
