package com.willwinder.ugs.nbp.designer.io.excellon;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class ExcellonReaderTest {

    @Test
    public void readKicadExcellonMetric() {
        String data = """
                M48
                ; DRILL file {KiCad 8.0.1} date 2024-09-11T16:52:45+0200
                ; FORMAT={-:-/ absolute / metric / decimal}
                FMAT,2
                METRIC
                T1C0.400
                %
                G90
                T1
                X13.0Y57.35
                X15.9Y58.65
                X18.9Y69.5
                """;

        ExcellonReader reader = new ExcellonReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        assertEquals(1, design.getEntities().size());
        Entity root = design.getEntities().get(0);
        assertTrue(root instanceof Group);

        Group holes = (Group) root;
        assertEquals("Holes", holes.getName());
        assertEquals(1, holes.getChildren().size());

        Entity diaGroupEntity = holes.getChildren().get(0);
        assertTrue(diaGroupEntity instanceof Group);

        Group diaGroup = (Group) diaGroupEntity;
        assertTrue(diaGroup.getName().startsWith("0.4"));
        assertEquals(3, diaGroup.getChildren().size());

        assertTrue(diaGroup.getChildren().get(0) instanceof Ellipse);

        // First hole centered at (13.0, 57.35) with dia 0.4 -> bounds x=12.8 y=57.15 w=h=0.4
        Ellipse first = (Ellipse) diaGroup.getChildren().get(0);
        assertEquals(0.4, first.getBounds().getWidth(), 0.0001);
        assertEquals(0.4, first.getBounds().getHeight(), 0.0001);
        assertEquals(12.8, first.getBounds().getX(), 0.0001);
        assertEquals(57.15, first.getBounds().getY(), 0.0001);
    }
}
