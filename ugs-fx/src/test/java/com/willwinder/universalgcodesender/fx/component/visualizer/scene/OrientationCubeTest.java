package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import javafx.geometry.Point3D;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OrientationCubeTest {

    @Test
    public void faceAt_shouldReturnTheFaceARayHitsFirst() {
        Ray fromAbove = new Ray(new Point3D(0.2, -0.3, 10), new Point3D(0, 0, -1));
        Ray fromFront = new Ray(new Point3D(0, -10, 0.5), new Point3D(0, 1, 0));

        assertThat(OrientationCube.faceAt(fromAbove)).contains(ViewOrientation.TOP);
        assertThat(OrientationCube.faceAt(fromFront)).contains(ViewOrientation.FRONT);
    }

    @Test
    public void faceAt_shouldBeEmptyWhenTheRayMissesTheCube() {
        Ray beside = new Ray(new Point3D(3, 0, 10), new Point3D(0, 0, -1));
        Ray away = new Ray(new Point3D(0, 0, 10), new Point3D(0, 0, 1));

        assertThat(OrientationCube.faceAt(beside)).isEmpty();
        assertThat(OrientationCube.faceAt(away)).isEmpty();
    }

    @Test
    public void cubeCamera_shouldSeeTheFaceOfEachOrientationInTheCenter() {
        Camera camera = OrientationCube.createCamera();
        camera.setViewport(new Viewport(110, 110, 1));

        for (ViewOrientation orientation : ViewOrientation.values()) {
            camera.yawProperty().set(orientation.yawDegrees());
            camera.pitchProperty().set(orientation.pitchDegrees());

            Optional<ViewOrientation> seen = OrientationCube.faceAt(camera.unproject(55, 55));

            assertThat(seen).as(orientation.name()).contains(orientation);
        }
    }

    @Test
    public void isFacing_shouldOnlyAcceptFacesTurnedTowardsTheCamera() {
        Point3D lookingDown = new Point3D(0, 0, -1);

        assertThat(OrientationCube.isFacing(ViewOrientation.TOP, lookingDown)).isTrue();
        assertThat(OrientationCube.isFacing(ViewOrientation.BOTTOM, lookingDown)).isFalse();
        assertThat(OrientationCube.isFacing(ViewOrientation.FRONT, lookingDown)).isFalse();
    }

    @Test
    public void cubeCamera_shouldKeepTheWholeCubeInsideTheInset() {
        Camera camera = OrientationCube.createCamera();
        camera.setViewport(new Viewport(110, 110, 1));
        camera.yawProperty().set(45);
        camera.pitchProperty().set(35);
        double h = OrientationCube.HALF_SIZE;

        for (double x : new double[]{-h, h}) {
            for (double y : new double[]{-h, h}) {
                for (double z : new double[]{-h, h}) {
                    var pixel = camera.project(new Point3D(x, y, z)).orElseThrow();
                    assertThat(pixel.getX()).isBetween(0.0, 110.0);
                    assertThat(pixel.getY()).isBetween(0.0, 110.0);
                }
            }
        }
    }
}
