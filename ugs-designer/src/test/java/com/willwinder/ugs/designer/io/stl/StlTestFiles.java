package com.willwinder.ugs.designer.io.stl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Builds STL files in both flavours from a flat list of triangle coordinates.
 */
final class StlTestFiles {

    private StlTestFiles() {
    }

    static byte[] ascii(float... coordinates) {
        StringBuilder builder = new StringBuilder("solid test\n");
        for (int i = 0; i < coordinates.length; i += StlMesh.COORDINATES_PER_TRIANGLE) {
            builder.append("facet normal 0 0 1\n  outer loop\n");
            for (int vertex = 0; vertex < 3; vertex++) {
                int offset = i + vertex * 3;
                builder.append("    vertex ")
                        .append(coordinates[offset]).append(' ')
                        .append(coordinates[offset + 1]).append(' ')
                        .append(coordinates[offset + 2]).append('\n');
            }
            builder.append("  endloop\nendfacet\n");
        }
        builder.append("endsolid test\n");
        return builder.toString().getBytes(StandardCharsets.US_ASCII);
    }

    static byte[] binary(float... coordinates) {
        int triangleCount = coordinates.length / StlMesh.COORDINATES_PER_TRIANGLE;
        ByteBuffer buffer = ByteBuffer.allocate(80 + 4 + triangleCount * 50).order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(new byte[80]);
        buffer.putInt(triangleCount);

        for (int i = 0; i < coordinates.length; i += StlMesh.COORDINATES_PER_TRIANGLE) {
            buffer.putFloat(0).putFloat(0).putFloat(1);
            for (int offset = 0; offset < StlMesh.COORDINATES_PER_TRIANGLE; offset++) {
                buffer.putFloat(coordinates[i + offset]);
            }
            buffer.putShort((short) 0);
        }

        return buffer.array();
    }

    /**
     * A quad spanning the given footprint lying on the plane {@code z = height * x / width}.
     */
    static float[] rampAlongX(float width, float height, float depth) {
        return new float[]{
                0, 0, 0,
                width, 0, depth,
                width, height, depth,

                0, 0, 0,
                width, height, depth,
                0, height, 0
        };
    }

    /**
     * A quad spanning the given footprint lying on the plane {@code z = depth * y / height}.
     */
    static float[] rampAlongY(float width, float height, float depth) {
        return new float[]{
                0, 0, 0,
                width, 0, 0,
                width, height, depth,

                0, 0, 0,
                width, height, depth,
                0, height, depth
        };
    }

    static float[] flatQuad(float minX, float minY, float maxX, float maxY, float z) {
        return new float[]{
                minX, minY, z,
                maxX, minY, z,
                maxX, maxY, z,

                minX, minY, z,
                maxX, maxY, z,
                minX, maxY, z
        };
    }

    static float[] concat(float[] first, float[] second) {
        float[] result = new float[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
