package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class HeightFieldMesherTest {
    private static final int STRIDE = VertexLayout.MESH.floatsPerVertex();

    @Test
    public void untouchedStockShouldCollapseToAFewTrianglesPerRow() {
        HeightField field = new HeightField(new Bounds3(0, 0, -5, 10, 10, 0), 1);
        HeightFieldMesher.Mesh mesh = HeightFieldMesher.mesh(field);

        int surfaceRows = field.rows() - 1;
        // One quad per row on top and underneath, plus four walls
        int expectedTriangles = surfaceRows * 2 + 4 * 2 + surfaceRows * 2;
        assertThat(mesh.triangleCount()).isEqualTo(expectedTriangles);
        assertThat(mesh.vertices().length).isEqualTo(mesh.vertexCount() * STRIDE);
        assertWellFormed(mesh, field);
    }

    @Test
    public void carvedStockShouldProduceMoreTrianglesWithinTheBounds() {
        HeightField untouched = new HeightField(new Bounds3(0, 0, -5, 20, 10, 0), 0.5);
        HeightField carved = untouched.copy();
        SegmentSweep.sweep(carved, ToolProfile.ball(4), 5, 5, -2, 15, 5, -2);

        HeightFieldMesher.Mesh untouchedMesh = HeightFieldMesher.mesh(untouched);
        HeightFieldMesher.Mesh carvedMesh = HeightFieldMesher.mesh(carved);
        assertThat(carvedMesh.triangleCount()).isGreaterThan(untouchedMesh.triangleCount());
        assertWellFormed(carvedMesh, carved);

        boolean hasTiltedNormal = false;
        for (int i = 0; i < carvedMesh.vertexCount(); i++) {
            if (Math.abs(carvedMesh.vertices()[i * STRIDE + 3]) > 0.01 || Math.abs(carvedMesh.vertices()[i * STRIDE + 4]) > 0.01) {
                hasTiltedNormal = true;
            }
        }
        assertThat(hasTiltedNormal).as("a groove should produce tilted normals").isTrue();
    }

    @Test
    public void wallsShouldFollowTheSurfaceWhereACutReachesTheEdge() {
        HeightField field = new HeightField(new Bounds3(0, 0, -5, 10, 10, 0), 0.5);
        SegmentSweep.sweep(field, ToolProfile.flat(2), 0, 5, -3, 10, 5, -3);
        HeightFieldMesher.Mesh mesh = HeightFieldMesher.mesh(field);
        assertWellFormed(mesh, field);

        // The wall at x = 0 must drop to the slot depth where the slot exits, between y = 4 and y = 6
        boolean found = false;
        for (int i = 0; i < mesh.vertexCount(); i++) {
            float x = mesh.vertices()[i * STRIDE];
            float y = mesh.vertices()[i * STRIDE + 1];
            float z = mesh.vertices()[i * STRIDE + 2];
            float nx = mesh.vertices()[i * STRIDE + 3];
            if (x == 0 && nx == -1 && y >= 3.99 && y <= 6.01 && Math.abs(z + 3) < 1e-5) {
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    @Test
    public void cutsReachingTheBottomShouldLeaveAHole() {
        HeightField field = new HeightField(new Bounds3(0, 0, -5, 20, 20, 0), 0.5);
        // A 6 mm pocket in the middle, cut down to the stock bottom
        SegmentSweep.sweep(field, ToolProfile.flat(4), 8, 10, -5, 12, 10, -5);
        SegmentSweep.sweep(field, ToolProfile.flat(4), 8, 11, -5, 12, 11, -5);
        SegmentSweep.sweep(field, ToolProfile.flat(4), 8, 9, -5, 12, 9, -5);
        assertThat(field.isThrough(field.columnFloor(10), field.rowFloor(10))).isTrue();

        HeightFieldMesher.Mesh mesh = HeightFieldMesher.mesh(field);
        assertWellFormed(mesh, field);

        // No floor at the bottom inside the hole, and nothing underneath it either
        assertThat(coversPoint(mesh, 10, 10, 1)).as("top face over the hole").isFalse();
        assertThat(coversPoint(mesh, 10, 10, -1)).as("bottom face under the hole").isFalse();
        // Material around the hole still has both faces
        assertThat(coversPoint(mesh, 2, 2, 1)).isTrue();
        assertThat(coversPoint(mesh, 2, 2, -1)).isTrue();
    }

    /**
     * Whether a horizontal triangle facing {@code normalZ} contains the XY point.
     */
    private static boolean coversPoint(HeightFieldMesher.Mesh mesh, double px, double py, int normalZ) {
        float[] v = mesh.vertices();
        for (int t = 0; t < mesh.triangleCount(); t++) {
            int base = t * 3 * STRIDE;
            if (Math.round(v[base + 5]) != normalZ || v[base + 3] != 0 || v[base + 4] != 0) {
                continue;
            }
            double x0 = v[base], y0 = v[base + 1];
            double x1 = v[base + STRIDE], y1 = v[base + STRIDE + 1];
            double x2 = v[base + 2 * STRIDE], y2 = v[base + 2 * STRIDE + 1];
            double d0 = (x1 - x0) * (py - y0) - (y1 - y0) * (px - x0);
            double d1 = (x2 - x1) * (py - y1) - (y2 - y1) * (px - x1);
            double d2 = (x0 - x2) * (py - y2) - (y0 - y2) * (px - x2);
            boolean hasNegative = d0 < 0 || d1 < 0 || d2 < 0;
            boolean hasPositive = d0 > 0 || d1 > 0 || d2 > 0;
            if (!(hasNegative && hasPositive)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void bandsShouldSeparateUntouchedFromShallowAndDeepCuts() {
        HeightField field = new HeightField(new Bounds3(0, 0, -8, 10, 10, 0), 1);
        assertThat(HeightFieldMesher.band(field, 0, 8)).isZero();
        assertThat(HeightFieldMesher.band(field, -0.01, 8)).isEqualTo(1);
        assertThat(HeightFieldMesher.band(field, -4, 8)).isEqualTo(4);
        assertThat(HeightFieldMesher.band(field, -8, 8)).isEqualTo(7);
        assertThat(HeightFieldMesher.band(field, -8, 1)).isZero();
    }

    @Test
    public void meshByDepthShouldPutCutsInDeeperBandsAndKeepEveryTriangle() {
        HeightField field = new HeightField(new Bounds3(0, 0, -8, 20, 20, 0), 0.5);
        List<HeightFieldMesher.Mesh> untouched = HeightFieldMesher.meshByDepth(field, 8);
        assertThat(untouched.get(0).isEmpty()).isFalse();
        assertThat(untouched.subList(1, 8)).allMatch(HeightFieldMesher.Mesh::isEmpty);

        SegmentSweep.sweep(field, ToolProfile.flat(4), 5, 10, -4, 15, 10, -4);
        List<HeightFieldMesher.Mesh> banded = HeightFieldMesher.meshByDepth(field, 8);
        HeightFieldMesher.Mesh whole = HeightFieldMesher.mesh(field);

        int total = banded.stream().mapToInt(HeightFieldMesher.Mesh::triangleCount).sum();
        assertThat(total).isEqualTo(whole.triangleCount());
        // The slot floor at half depth lands in the middle band, its walls in the bands between
        assertThat(banded.get(4).isEmpty()).isFalse();
        assertThat(banded.get(7).isEmpty()).isTrue();
        for (HeightFieldMesher.Mesh mesh : banded) {
            assertWellFormed(mesh, field);
        }
    }

    private static void assertWellFormed(HeightFieldMesher.Mesh mesh, HeightField field) {
        assertThat(mesh.vertexCount() % 3).isZero();
        Bounds3 bounds = field.bounds();
        for (int i = 0; i < mesh.vertexCount(); i++) {
            float x = mesh.vertices()[i * STRIDE];
            float y = mesh.vertices()[i * STRIDE + 1];
            float z = mesh.vertices()[i * STRIDE + 2];
            float nx = mesh.vertices()[i * STRIDE + 3];
            float ny = mesh.vertices()[i * STRIDE + 4];
            float nz = mesh.vertices()[i * STRIDE + 5];
            assertThat(x).isBetween((float) bounds.minX() - 1e-3f, (float) field.x(field.columns() - 1) + 1e-3f);
            assertThat(y).isBetween((float) bounds.minY() - 1e-3f, (float) field.y(field.rows() - 1) + 1e-3f);
            assertThat(z).isBetween((float) bounds.minZ() - 1e-3f, (float) bounds.maxZ() + 1e-3f);
            assertThat(Math.sqrt(nx * nx + ny * ny + nz * nz)).isCloseTo(1, within(1e-4));
        }
    }
}
