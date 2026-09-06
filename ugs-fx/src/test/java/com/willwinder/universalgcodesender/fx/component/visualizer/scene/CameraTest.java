package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class CameraTest {
    private static final double PIXEL_TOLERANCE = 0.01;
    private static final double WORLD_TOLERANCE = 0.01;

    private Camera camera;

    @Before
    public void setUp() {
        camera = new Camera();
        camera.setViewport(new Viewport(800, 600, 1));
    }

    @Test
    public void project_shouldPlaceTargetAtViewportCenter() {
        camera.targetXProperty().set(120);
        camera.targetYProperty().set(-40);
        camera.targetZProperty().set(3);

        Point2D pixel = camera.project(new Point3D(120, -40, 3)).orElseThrow();

        assertThat(pixel.getX()).isCloseTo(400, within(PIXEL_TOLERANCE));
        assertThat(pixel.getY()).isCloseTo(300, within(PIXEL_TOLERANCE));
    }

    @Test
    public void unproject_shouldRoundTripThroughProjectInPerspective() {
        camera.projectionProperty().set(Camera.Projection.PERSPECTIVE);
        Point3D world = new Point3D(37, 150, 12);

        Point2D pixel = camera.project(world).orElseThrow();
        Ray ray = camera.unproject(pixel.getX(), pixel.getY());

        Point3D closest = ray.pointAt(world.subtract(ray.origin()).dotProduct(ray.direction()));
        assertThat(closest.distance(world)).isLessThan(WORLD_TOLERANCE);
    }

    @Test
    public void unproject_shouldRoundTripThroughProjectInOrthographic() {
        camera.projectionProperty().set(Camera.Projection.ORTHOGRAPHIC);
        Point3D world = new Point3D(37, 150, 12);

        Point2D pixel = camera.project(world).orElseThrow();
        Ray ray = camera.unproject(pixel.getX(), pixel.getY());

        Point3D closest = ray.pointAt(world.subtract(ray.origin()).dotProduct(ray.direction()));
        assertThat(closest.distance(world)).isLessThan(WORLD_TOLERANCE);
    }

    @Test
    public void intersectWorkPlane_shouldFindPointUnderPixel() {
        Point3D onPlane = new Point3D(55, 80, 0);
        Point2D pixel = camera.project(onPlane).orElseThrow();

        Point3D hit = camera.intersectWorkPlane(pixel.getX(), pixel.getY()).orElseThrow();

        assertThat(hit.distance(onPlane)).isLessThan(WORLD_TOLERANCE);
    }

    @Test
    public void intersectWorkPlane_shouldBeEmptyWhenLookingAwayFromThePlane() {
        camera.pitchProperty().set(-30);
        camera.targetZProperty().set(500);

        assertThat(camera.intersectWorkPlane(400, 10)).isEmpty();
    }

    @Test
    public void worldUnitsPerPixel_shouldMatchProjectedDistanceAtTarget() {
        camera.pitchProperty().set(90);
        Point3D target = camera.target();
        double worldPerPixel = camera.worldUnitsPerPixel();

        Point2D a = camera.project(target).orElseThrow();
        Point2D b = camera.project(target.add(worldPerPixel * 100, 0, 0)).orElseThrow();

        assertThat(a.distance(b)).isCloseTo(100, within(0.5));
    }

    @Test
    public void worldUnitsPerPixelAt_shouldBeConstantInOrthographic() {
        camera.projectionProperty().set(Camera.Projection.ORTHOGRAPHIC);

        double near = camera.worldUnitsPerPixelAt(camera.target());
        double far = camera.worldUnitsPerPixelAt(camera.target().add(-500, -500, 0));

        assertThat(near).isCloseTo(far, within(1e-9));
    }

    @Test
    public void zoomAt_shouldKeepThePointUnderTheCursorFixed() {
        Point3D before = camera.intersectWorkPlane(200, 150).orElseThrow();

        camera.zoomAt(600, 200, 150);

        Point3D after = camera.intersectWorkPlane(200, 150).orElseThrow();
        assertThat(after.distance(before)).isLessThan(WORLD_TOLERANCE);
        assertThat(camera.distanceProperty().get()).isLessThan(420);
    }

    @Test
    public void zoomAt_shouldKeepThePointUnderTheCursorFixedInASideView() {
        camera.pitchProperty().set(0);
        camera.yawProperty().set(0);
        Point3D before = camera.intersectTargetPlane(600, 200).orElseThrow();

        camera.zoomAt(600, 600, 200);

        Point3D after = camera.intersectTargetPlane(600, 200).orElseThrow();
        assertThat(after.distance(before)).isLessThan(WORLD_TOLERANCE);
        assertThat(camera.distanceProperty().get()).isLessThan(420);
    }

    @Test
    public void viewDirection_shouldFollowPitchAndYaw() {
        camera.pitchProperty().set(90);
        assertThat(camera.viewDirection().distance(new Point3D(0, 0, -1))).isLessThan(1e-9);

        camera.pitchProperty().set(0);
        camera.yawProperty().set(0);
        assertThat(camera.viewDirection().distance(new Point3D(0, 1, 0))).isLessThan(1e-9);
    }

    @Test
    public void frame_shouldCenterOnBoundsAndFitThem() {
        Bounds3 bounds = new Bounds3(0, 0, 0, 100, 50, 10);

        camera.frame(bounds);

        assertThat(camera.target()).isEqualTo(new Point3D(50, 25, 5));
        Point2D min = camera.project(new Point3D(0, 0, 5)).orElseThrow();
        Point2D max = camera.project(new Point3D(100, 50, 5)).orElseThrow();
        assertThat(min.getX()).isBetween(0.0, 800.0);
        assertThat(max.getX()).isBetween(0.0, 800.0);
        assertThat(min.getY()).isBetween(0.0, 600.0);
        assertThat(max.getY()).isBetween(0.0, 600.0);
    }

    @Test
    public void switchingProjection_shouldKeepTargetScale() {
        camera.pitchProperty().set(90);
        Point3D offset = camera.target().add(50, 0, 0);
        Point2D perspective = camera.project(offset).orElseThrow();

        camera.projectionProperty().set(Camera.Projection.ORTHOGRAPHIC);
        Point2D orthographic = camera.project(offset).orElseThrow();

        assertThat(orthographic.distance(perspective)).isLessThan(PIXEL_TOLERANCE);
    }

    @Test
    public void orbit_shouldClampPitchAndNotifyListeners() {
        AtomicInteger changes = new AtomicInteger();
        camera.addChangeListener(changes::incrementAndGet);

        camera.orbit(10, 200);

        assertThat(camera.pitchProperty().get()).isEqualTo(89);
        assertThat(changes.get()).isEqualTo(2);
    }
}
