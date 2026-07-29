package com.willwinder.ugs.designer.io.stl;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class StlDepthMapRasterizerTest {

    private static int gray(BufferedImage image, int x, int y) {
        return image.getRaster().getSample(x, y, 0);
    }

    @Test
    public void rasterize_shouldSizeImageFromTheFootprintAndResolution() {
        StlMesh mesh = new StlMesh(StlTestFiles.flatQuad(0, 0, 20, 10, 1));

        BufferedImage image = new StlDepthMapRasterizer(2).rasterize(mesh);

        assertEquals(40, image.getWidth());
        assertEquals(20, image.getHeight());
    }

    @Test
    public void rasterize_shouldCapTheLongestSideWhileKeepingTheAspectRatio() {
        StlMesh mesh = new StlMesh(StlTestFiles.flatQuad(0, 0, 2000, 1000, 1));

        BufferedImage image = new StlDepthMapRasterizer(10).rasterize(mesh);

        assertEquals(2048, image.getWidth());
        assertEquals(1024, image.getHeight());
    }

    @Test
    public void rasterize_shouldMapTheHighestPointToWhiteAndTheLowestToBlack() {
        StlMesh mesh = new StlMesh(StlTestFiles.rampAlongX(10, 10, 10));

        BufferedImage image = new StlDepthMapRasterizer(1).rasterize(mesh);

        assertEquals(3277, gray(image, 0, 5));
        assertEquals(62258, gray(image, 9, 5));
    }

    @Test
    public void rasterize_shouldPutTheTopOfTheModelInTheFirstRow() {
        StlMesh mesh = new StlMesh(StlTestFiles.rampAlongY(10, 10, 10));

        BufferedImage image = new StlDepthMapRasterizer(1).rasterize(mesh);

        assertEquals(62258, gray(image, 5, 0));
        assertEquals(3277, gray(image, 5, 9));
    }

    @Test
    public void rasterize_shouldKeepTheHighestSurfaceWhenTrianglesOverlap() {
        StlMesh mesh = new StlMesh(StlTestFiles.concat(
                StlTestFiles.flatQuad(0, 0, 10, 10, 2),
                StlTestFiles.flatQuad(0, 0, 5, 5, 5)));

        BufferedImage image = new StlDepthMapRasterizer(1).rasterize(mesh);

        assertEquals(65535, gray(image, 1, 8));
        assertEquals(0, gray(image, 8, 1));
    }

    @Test
    public void rasterize_shouldLeaveAreasOutsideTheMeshBlack() {
        StlMesh mesh = new StlMesh(new float[]{
                0, 0, 1,
                10, 0, 1,
                10, 10, 1
        });

        BufferedImage image = new StlDepthMapRasterizer(1).rasterize(mesh);

        assertEquals(65535, gray(image, 9, 9));
        assertEquals(0, gray(image, 0, 0));
    }

    @Test
    public void rasterize_shouldProduceA16BitDepthMap() {
        StlMesh mesh = new StlMesh(StlTestFiles.flatQuad(0, 0, 10, 10, 1));

        BufferedImage image = new StlDepthMapRasterizer(1).rasterize(mesh);

        assertEquals(BufferedImage.TYPE_USHORT_GRAY, image.getType());
    }

    @Test
    public void rasterize_shouldResolveHeightStepsFinerThanEightBits() {
        StlMesh mesh = new StlMesh(StlTestFiles.rampAlongX(100, 10, 1));

        BufferedImage image = new StlDepthMapRasterizer(10).rasterize(mesh);

        // Neighbouring columns differ by 1/1000 of the height, which an 8-bit map would collapse to 128
        assertEquals(32800, gray(image, 500, 50));
        assertEquals(32866, gray(image, 501, 50));
    }

    @Test
    public void rasterize_shouldRenderAFlatModelAsWhite() {
        StlMesh mesh = new StlMesh(StlTestFiles.flatQuad(0, 0, 10, 10, 3));

        BufferedImage image = new StlDepthMapRasterizer(1).rasterize(mesh);

        assertEquals(65535, gray(image, 5, 5));
        assertEquals(65535, gray(image, 0, 9));
    }
}
