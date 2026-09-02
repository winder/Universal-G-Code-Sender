package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

public class Mat4Test {

    @Test
    public void invert_shouldGiveIdentityWhenMultipliedWithTheOriginal() {
        float[] matrix = Mat4.multiply(
                Mat4.perspective(Math.toRadians(45), 1.5, 1, 1000),
                Mat4.multiply(Mat4.rotationX(0.7), Mat4.multiply(Mat4.rotationZ(-0.3), Mat4.translation(10, -20, 5))));

        float[] product = Mat4.multiply(matrix, Mat4.invert(matrix));

        float[] identity = Mat4.identity();
        for (int i = 0; i < Mat4.ELEMENTS; i++) {
            assertThat(product[i]).isCloseTo(identity[i], within(1e-4f));
        }
    }

    @Test
    public void invert_shouldRejectSingularMatrix() {
        float[] singular = Mat4.scale(1, 0, 1);

        assertThatThrownBy(() -> Mat4.invert(singular)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void normalMatrix_shouldBeTheRotationForARigidTransform() {
        float[] model = Mat4.multiply(Mat4.translation(5, -3, 2), Mat4.rotationZ(Math.toRadians(90)));

        float[] normal = Mat4.normalMatrix(model);

        // A normal along X turns into one along Y under a quarter turn around Z.
        double x = normal[0] * 1 + normal[3] * 0 + normal[6] * 0;
        double y = normal[1] * 1 + normal[4] * 0 + normal[7] * 0;
        assertThat(x).isCloseTo(0, within(1e-6));
        assertThat(y).isCloseTo(1, within(1e-6));
    }

    @Test
    public void normalMatrix_shouldCompensateNonUniformScale() {
        float[] normal = Mat4.normalMatrix(Mat4.scale(2, 1, 1));

        // Stretching along X squashes the X component of normals, keeping them perpendicular.
        assertThat(normal[0]).isCloseTo(0.5f, within(1e-6f));
        assertThat(normal[4]).isCloseTo(1, within(1e-6f));
        assertThat(normal[8]).isCloseTo(1, within(1e-6f));
    }

    @Test
    public void transform_shouldApplyTranslation() {
        float[] translation = Mat4.translation(1, 2, 3);

        double[] result = Mat4.transform(translation, 10, 20, 30);

        assertThat(result).containsExactly(11, 22, 33, 1);
    }

    @Test
    public void orthographic_shouldMapHalfExtentsToClipEdgesWithFlippedY() {
        float[] projection = Mat4.orthographic(200, 100, -10, 10);

        double[] corner = Mat4.transform(projection, 200, 100, -5);

        assertThat(corner[0]).isCloseTo(1, within(1e-6));
        assertThat(corner[1]).isCloseTo(-1, within(1e-6));
        assertThat(corner[2]).isCloseTo(0.75, within(1e-6));
        assertThat(corner[3]).isCloseTo(1, within(1e-6));
    }
}
