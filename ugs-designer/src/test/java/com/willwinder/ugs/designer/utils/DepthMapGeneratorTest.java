package com.willwinder.ugs.designer.utils;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class DepthMapGeneratorTest {

    @Test
    public void isModelAvailable_ShouldBeFalseForMissingModel() {
        DepthMapGenerator generator = new DepthMapGenerator(Paths.get("does", "not", "exist.onnx"));

        boolean available = generator.isModelAvailable();

        assertFalse(available);
    }

    @Test
    public void generateDepthMap_ShouldThrowWhenModelMissing() {
        DepthMapGenerator generator = new DepthMapGenerator(Paths.get("does", "not", "exist.onnx"));
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        assertThrows(IllegalStateException.class, () -> generator.generateDepthMap(image));
    }
}
