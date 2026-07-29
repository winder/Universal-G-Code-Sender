package com.willwinder.ugs.designer.io.stl;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.model.Design;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StlReaderTest {

    private static Raster readRaster(byte[] data) {
        Optional<Design> design = new StlReader().read(new ByteArrayInputStream(data));
        assertTrue(design.isPresent());

        List<Entity> entities = design.get().getEntities();
        assertEquals(1, entities.size());
        assertTrue(entities.get(0) instanceof Raster);
        return (Raster) entities.get(0);
    }

    @Test
    public void read_shouldCreateRasterSizedToTheModelFootprint() {
        Raster raster = readRaster(StlTestFiles.binary(StlTestFiles.rampAlongX(20, 10, 4)));

        assertEquals(20, raster.getSize().getWidth(), 0.0001);
        assertEquals(10, raster.getSize().getHeight(), 0.0001);
    }

    @Test
    public void read_shouldSetTargetDepthToTheModelHeight() {
        Raster raster = readRaster(StlTestFiles.binary(StlTestFiles.rampAlongX(20, 10, 4)));

        assertEquals(4, raster.getTargetDepth(), 0.0001);
    }

    @Test
    public void read_shouldCutTheRasterAsAHeightMap() {
        Raster raster = readRaster(StlTestFiles.ascii(StlTestFiles.rampAlongX(20, 10, 4)));

        assertEquals(CutType.HEIGHT_MAP, raster.getCutType());
    }

    @Test
    public void read_shouldNotUseTheEstimatedDepthMapping() {
        Raster raster = readRaster(StlTestFiles.ascii(StlTestFiles.rampAlongX(20, 10, 4)));

        assertFalse(raster.isDepthMapping());
    }

    @Test
    public void read_shouldMakeTheTopOfTheModelUncut() {
        Raster raster = readRaster(StlTestFiles.binary(StlTestFiles.rampAlongX(20, 10, 4)));

        assertEquals(1.0, raster.getIntensityAt(new Point2D.Double(19.9, 5)), 0.02);
        assertEquals(0.0, raster.getIntensityAt(new Point2D.Double(0.1, 5)), 0.02);
    }

    @Test
    public void read_shouldResolveHeightsFinerThanEightBits() {
        Raster raster = readRaster(StlTestFiles.binary(StlTestFiles.rampAlongX(100, 10, 25)));

        double first = raster.getIntensityAt(new Point2D.Double(50.05, 5));
        double second = raster.getIntensityAt(new Point2D.Double(50.15, 5));

        // These lie 0.025mm apart on a 25mm tall model, well inside a single 8-bit height step
        assertEquals(0.5005, first, 0.0002);
        assertEquals(0.5015, second, 0.0002);
    }

    @Test
    public void read_shouldNameTheEntityAfterTheFile() throws Exception {
        File file = Files.createTempFile("model", ".stl").toFile();
        Files.write(file.toPath(), StlTestFiles.binary(StlTestFiles.rampAlongX(20, 10, 4)));

        Optional<Design> design = new StlReader().read(file);

        assertEquals(file.getName(), design.orElseThrow().getEntities().get(0).getName());
    }

    @Test
    public void read_shouldReturnEmptyForMeshWithoutTriangles() {
        Optional<Design> design = new StlReader().read(new ByteArrayInputStream(StlTestFiles.binary()));

        assertTrue(design.isEmpty());
    }
}
