package com.willwinder.ugs.designer.io.stl;

import com.willwinder.ugs.designer.io.DesignReaderException;
import org.junit.Test;

import java.awt.geom.Rectangle2D;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class StlParserTest {

    @Test
    public void parse_shouldReadTrianglesFromAsciiFile() {
        byte[] data = StlTestFiles.ascii(StlTestFiles.rampAlongX(20, 10, 4));

        StlMesh mesh = new StlParser().parse(data);

        assertEquals(2, mesh.getTriangleCount());
        assertEquals(new Rectangle2D.Double(0, 0, 20, 10), mesh.getBounds());
        assertEquals(0, mesh.getMinZ(), 0.0001);
        assertEquals(4, mesh.getMaxZ(), 0.0001);
    }

    @Test
    public void parse_shouldReadTrianglesFromBinaryFile() {
        byte[] data = StlTestFiles.binary(StlTestFiles.rampAlongX(20, 10, 4));

        StlMesh mesh = new StlParser().parse(data);

        assertEquals(2, mesh.getTriangleCount());
        assertEquals(new Rectangle2D.Double(0, 0, 20, 10), mesh.getBounds());
        assertEquals(4, mesh.getHeight(), 0.0001);
    }

    @Test
    public void parse_shouldReadBinaryFileWithHeaderStartingWithSolid() {
        byte[] data = StlTestFiles.binary(StlTestFiles.flatQuad(0, 0, 10, 10, 2));
        System.arraycopy("solid".getBytes(StandardCharsets.US_ASCII), 0, data, 0, 5);

        StlMesh mesh = new StlParser().parse(data);

        assertEquals(2, mesh.getTriangleCount());
    }

    @Test
    public void parse_shouldHandleNegativeCoordinates() {
        byte[] data = StlTestFiles.ascii(StlTestFiles.flatQuad(-5, -2, 5, 2, -3));

        StlMesh mesh = new StlParser().parse(data);

        assertEquals(new Rectangle2D.Double(-5, -2, 10, 4), mesh.getBounds());
        assertEquals(-3, mesh.getMinZ(), 0.0001);
    }

    @Test
    public void parse_shouldReturnEmptyMeshForFileWithoutTriangles() {
        byte[] data = "solid empty\nendsolid empty\n".getBytes(StandardCharsets.US_ASCII);

        StlMesh mesh = new StlParser().parse(data);

        assertTrue(mesh.isEmpty());
    }

    @Test
    public void parse_shouldThrowExceptionForIncompleteTriangle() {
        byte[] data = "solid broken\nfacet normal 0 0 1\nouter loop\nvertex 0 0 0\nvertex 1 0 0\nendloop\nendfacet\nendsolid broken\n"
                .getBytes(StandardCharsets.US_ASCII);
        StlParser parser = new StlParser();

        DesignReaderException exception = assertThrows(DesignReaderException.class, () -> parser.parse(data));

        assertEquals("The STL file contains an incomplete triangle", exception.getMessage());
    }
}
