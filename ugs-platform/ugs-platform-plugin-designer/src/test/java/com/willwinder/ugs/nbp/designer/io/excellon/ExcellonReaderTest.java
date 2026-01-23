package com.willwinder.ugs.nbp.designer.io.excellon;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
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

    @Test
    public void readKicadExcellonRoutingMode() {
        String data = """
                M48
                METRIC
                T7C1.000
                %
                G90
                T7
                G00X8.58Y63.0
                M15
                G01X8.58Y61.0
                M16
                G05
                G00X8.58Y15.9
                M15
                G01X8.58Y13.9
                M16
                G05
                """;

        ExcellonReader reader = new ExcellonReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        // Holes + Routes
        assertEquals(2, design.getEntities().size());

        Entity holesEntity = design.getEntities().get(0);
        assertTrue(holesEntity instanceof Group);
        assertEquals("Holes", holesEntity.getName());

        Entity routesEntity = design.getEntities().get(1);
        assertTrue(routesEntity instanceof Group);

        Group routes = (Group) routesEntity;
        assertEquals("Routes", routes.getName());
        assertEquals(1, routes.getChildren().size());

        Entity routeDiaGroupEntity = routes.getChildren().get(0);
        assertTrue(routeDiaGroupEntity instanceof Group);

        Group routeDiaGroup = (Group) routeDiaGroupEntity;
        assertTrue(routeDiaGroup.getName().startsWith("1.0 mm"));
        assertEquals(2, routeDiaGroup.getChildren().size());

        assertTrue(routeDiaGroup.getChildren().get(0) instanceof Path);
        assertTrue(routeDiaGroup.getChildren().get(1) instanceof Path);

        Entity firstRoute = routeDiaGroup.getChildren().get(0);
        assertEquals(0.001, firstRoute.getBounds().getWidth(), 0.01);
        assertEquals(2.0, firstRoute.getBounds().getHeight(), 0.01);
    }
}
