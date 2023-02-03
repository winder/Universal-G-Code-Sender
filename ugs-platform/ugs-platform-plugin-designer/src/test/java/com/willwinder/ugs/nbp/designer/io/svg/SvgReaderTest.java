package com.willwinder.ugs.nbp.designer.io.svg;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SvgReaderTest {

    @Test
    public void readShouldCorrectlyConvertToMM() {
        SvgReader reader = new SvgReader();
        Design design = reader.read(SvgReaderTest.class.getResourceAsStream("/20x20mm.svg")).orElseThrow(() -> new RuntimeException("Could not find SVG"));
        Entity entity = design.getEntities().get(0);

        assertEquals(20, entity.getSize().getWidth(), 0.1);
        assertEquals(20, entity.getSize().getHeight(), 0.1);
        assertEquals(0, entity.getPosition().getX(), 0.1);
        assertEquals(0, entity.getPosition().getY(), 0.1);
    }

    @Test
    public void readShouldCorrectlyConvertToMMFromInch() {
        SvgReader reader = new SvgReader();
        Design design = reader.read(SvgReaderTest.class.getResourceAsStream("/1x1inch.svg")).orElseThrow(() -> new RuntimeException("Could not find SVG"));
        Entity entity = design.getEntities().get(0);
        assertEquals(25.4, entity.getSize().getWidth(), 0.1);
        assertEquals(25.4, entity.getSize().getHeight(), 0.1);
        assertEquals(0, entity.getPosition().getX(), 0.1);
        assertEquals(0, entity.getPosition().getY(), 0.1);
    }

    @Test
    public void readLinesShouldCorrectlyConvertToMMFromInch() {
        SvgReader reader = new SvgReader();
        Design design = reader.read(SvgReaderTest.class.getResourceAsStream("/lines.svg")).orElseThrow(() -> new RuntimeException("Could not find SVG"));
        Entity entity = design.getEntities().get(0);
        assertEquals(50, entity.getSize().getHeight(), 0.1);
        assertEquals(70, entity.getSize().getWidth(), 0.1);
        assertEquals(0, entity.getPosition().getX(), 0.1);
        assertEquals(0, entity.getPosition().getY(), 0.1);
    }
}
