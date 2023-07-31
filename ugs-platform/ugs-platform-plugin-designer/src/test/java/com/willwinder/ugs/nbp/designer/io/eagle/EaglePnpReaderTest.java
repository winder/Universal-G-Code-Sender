package com.willwinder.ugs.nbp.designer.io.eagle;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EaglePnpReaderTest {

    @Test
    public void readValidFile() {
        EaglePnpReader reader = new EaglePnpReader();
        Optional<Design> designOptional = reader.read(IOUtils.toInputStream("C1 26.23  2.92 225 0.47uF C0402\n" +
                "C2 25.44  3.57  45 0.1uF C0402", StandardCharsets.UTF_8));

        assertTrue(designOptional.isPresent());

        Design design = designOptional.get();
        List<Entity> entities = design.getEntities();
        assertEquals(2, entities.size());

        assertEquals("C1 - 0.47uF C0402", entities.get(0).getName());
        assertEquals(26.23, entities.get(0).getCenter().getX(), 0.001);
        assertEquals(2.92, entities.get(0).getCenter().getY(), 0.001);
        assertEquals(225, entities.get(0).getRotation(), 0.001);

        assertEquals("C2 - 0.1uF C0402", entities.get(1).getName());
        assertEquals(25.44, entities.get(1).getCenter().getX(), 0.001);
        assertEquals(3.57, entities.get(1).getCenter().getY(), 0.001);
        assertEquals(45, entities.get(1).getRotation(), 0.001);
    }

}
