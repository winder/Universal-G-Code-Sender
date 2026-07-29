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
package com.willwinder.ugs.designer.io.stl;

import com.willwinder.ugs.designer.io.DesignReaderException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads both the binary and the ASCII flavour of STL into a {@link StlMesh}.
 *
 * @author Joacim Breiler
 */
public class StlParser {
    private static final int BINARY_HEADER_SIZE = 80;
    private static final int BINARY_TRIANGLE_SIZE = 50;
    private static final Pattern VERTEX = Pattern.compile("vertex\\s+(\\S+)\\s+(\\S+)\\s+(\\S+)", Pattern.CASE_INSENSITIVE);

    public StlMesh parse(byte[] data) {
        if (isBinary(data)) {
            return parseBinary(data);
        }
        return parseAscii(new String(data, StandardCharsets.US_ASCII));
    }

    /**
     * An ASCII file starts with {@code solid}, but so do binary files written by some tools, so the
     * declared triangle count is checked against the actual file length instead.
     */
    private static boolean isBinary(byte[] data) {
        if (data.length < BINARY_HEADER_SIZE + 4) {
            return false;
        }

        long triangleCount = ByteBuffer.wrap(data, BINARY_HEADER_SIZE, 4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .getInt() & 0xFFFFFFFFL;
        return data.length == BINARY_HEADER_SIZE + 4 + triangleCount * BINARY_TRIANGLE_SIZE;
    }

    private static StlMesh parseBinary(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(BINARY_HEADER_SIZE);
        int triangleCount = buffer.getInt();

        float[] coordinates = new float[triangleCount * StlMesh.COORDINATES_PER_TRIANGLE];
        int index = 0;
        for (int triangle = 0; triangle < triangleCount; triangle++) {
            buffer.position(buffer.position() + 12); // The facet normal is derived from the winding instead
            for (int i = 0; i < StlMesh.COORDINATES_PER_TRIANGLE; i++) {
                coordinates[index++] = buffer.getFloat();
            }
            buffer.position(buffer.position() + 2); // Attribute byte count
        }

        return new StlMesh(coordinates);
    }

    private static StlMesh parseAscii(String text) {
        Matcher matcher = VERTEX.matcher(text);
        float[] coordinates = new float[StlMesh.COORDINATES_PER_TRIANGLE * 64];
        int index = 0;

        while (matcher.find()) {
            if (index + 3 > coordinates.length) {
                float[] grown = new float[coordinates.length * 2];
                System.arraycopy(coordinates, 0, grown, 0, index);
                coordinates = grown;
            }
            for (int axis = 0; axis < 3; axis++) {
                coordinates[index++] = parseFloat(matcher.group(axis + 1));
            }
        }

        if (index % StlMesh.COORDINATES_PER_TRIANGLE != 0) {
            throw new DesignReaderException("The STL file contains an incomplete triangle");
        }

        float[] trimmed = new float[index];
        System.arraycopy(coordinates, 0, trimmed, 0, index);
        return new StlMesh(trimmed);
    }

    private static float parseFloat(String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new DesignReaderException("Could not parse the vertex coordinate '" + value + "'", e);
        }
    }
}
