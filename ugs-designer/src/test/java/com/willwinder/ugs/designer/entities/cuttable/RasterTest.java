package com.willwinder.ugs.designer.entities.cuttable;

import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static org.junit.Assert.assertEquals;

public class RasterTest {

    /**
     * Builds an image where every column has a constant sample value, so an intensity can be looked up
     * by its column alone.
     */
    private static BufferedImage columns(int type, int... values) {
        BufferedImage image = new BufferedImage(values.length, 2, type);
        for (int x = 0; x < values.length; x++) {
            for (int y = 0; y < 2; y++) {
                image.getRaster().setSample(x, y, 0, values[x]);
            }
        }
        return image;
    }

    @Test
    public void getIntensityAt_shouldReadSixteenBitGrayscaleVerbatim() {
        Raster raster = new Raster(columns(BufferedImage.TYPE_USHORT_GRAY, 0, 32768, 65535));

        double first = raster.getIntensityAt(new Point2D.Double(0.5, 1));
        double middle = raster.getIntensityAt(new Point2D.Double(1.5, 1));
        double last = raster.getIntensityAt(new Point2D.Double(2.5, 1));

        assertEquals(0.0, first, 1e-9);
        assertEquals(32768 / 65535.0, middle, 1e-9);
        assertEquals(1.0, last, 1e-9);
    }

    @Test
    public void getIntensityAt_shouldResolveIntensitiesFinerThanEightBits() {
        Raster raster = new Raster(columns(BufferedImage.TYPE_USHORT_GRAY, 30000, 30001));

        double first = raster.getIntensityAt(new Point2D.Double(0.5, 1));
        double second = raster.getIntensityAt(new Point2D.Double(1.5, 1));

        // An 8-bit raster would report 117/255 for both of these
        assertEquals(30000 / 65535.0, first, 1e-9);
        assertEquals(30001 / 65535.0, second, 1e-9);
    }

    @Test
    public void getIntensityAt_shouldReadEightBitGrayscaleVerbatim() {
        Raster raster = new Raster(columns(BufferedImage.TYPE_BYTE_GRAY, 0, 128, 255));

        double first = raster.getIntensityAt(new Point2D.Double(0.5, 1));
        double middle = raster.getIntensityAt(new Point2D.Double(1.5, 1));
        double last = raster.getIntensityAt(new Point2D.Double(2.5, 1));

        assertEquals(0.0, first, 1e-9);
        assertEquals(128 / 255.0, middle, 1e-9);
        assertEquals(1.0, last, 1e-9);
    }

    @Test
    public void getIntensityAt_shouldInvertSixteenBitGrayscale() {
        Raster raster = new Raster(columns(BufferedImage.TYPE_USHORT_GRAY, 30000, 30001));
        raster.setInvert(true);

        double first = raster.getIntensityAt(new Point2D.Double(0.5, 1));

        assertEquals(1.0 - 30000 / 65535.0, first, 1e-4);
    }

    @Test
    public void getIntensityAt_shouldQuantizeToTheConfiguredNumberOfLevels() {
        Raster raster = new Raster(columns(BufferedImage.TYPE_USHORT_GRAY, 30000, 40000));
        raster.setLevels(3);

        double first = raster.getIntensityAt(new Point2D.Double(0.5, 1));
        double second = raster.getIntensityAt(new Point2D.Double(1.5, 1));

        assertEquals(0.5, first, 1e-4);
        assertEquals(0.5, second, 1e-4);
    }

    @Test
    public void getImageData_shouldPreserveSixteenBitPrecision() throws IOException {
        Raster raster = new Raster(columns(BufferedImage.TYPE_USHORT_GRAY, 30000, 30001));

        BufferedImage restored = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(raster.getImageData())));

        assertEquals(BufferedImage.TYPE_USHORT_GRAY, restored.getType());
        assertEquals(30000, restored.getRaster().getSample(0, 0, 0));
        assertEquals(30001, restored.getRaster().getSample(1, 0, 0));
    }
}
