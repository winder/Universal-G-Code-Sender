package com.willwinder.ugs.nbp.designer.io.kicad;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KiCadPosReaderTest {
    @Test
    public void readValidFile() {
        KiCadPosReader reader = new KiCadPosReader();
        Optional<Design> designOptional = reader.read(IOUtils.toInputStream("### Footprint positions - created on 2023 June 20, Tuesday 14:16:48 ###\n" +
                "### Printed by KiCad version 7.0.5-0\n" +
                "## Unit = mm, Angle = deg.\n" +
                "## Side : top\n" +
                "# Ref     Val       Package                                                PosX       PosY       Rot  Side\n" +
                "C1        10uF      CP_Radial_D10.0mm_P5.00mm                          141.6050   -99.6950   90.0000  top\n" +
                "C2        680nF     C_Disc_D4.7mm_W2.5mm_P5.00mm                       137.1600  -125.0950   90.0000  top\n" +
                "P1        IN        Altech_AK300_1x02_P5.00mm_45-Degree                166.3700  -105.4100   0.0000  top", StandardCharsets.UTF_8));

        assertTrue(designOptional.isPresent());

        Design design = designOptional.get();
        List<Entity> entities = design.getEntities();
        assertEquals(3, entities.size());

        assertEquals("C1 - 10uF CP_Radial_D10.0mm_P5.00mm", entities.get(0).getName());
        assertEquals(141.6050, entities.get(0).getCenter().getX(), 0.001);
        assertEquals(-99.6950, entities.get(0).getCenter().getY(), 0.001);
        assertEquals(90, entities.get(0).getRotation(), 0.001);

        assertEquals("C2 - 680nF C_Disc_D4.7mm_W2.5mm_P5.00mm", entities.get(1).getName());
        assertEquals(137.1600, entities.get(1).getCenter().getX(), 0.001);
        assertEquals(-125.0950, entities.get(1).getCenter().getY(), 0.001);
        assertEquals(90, entities.get(1).getRotation(), 0.001);

        assertEquals("P1 - IN Altech_AK300_1x02_P5.00mm_45-Degree", entities.get(2).getName());
        assertEquals(166.3700, entities.get(2).getCenter().getX(), 0.001);
        assertEquals(-105.4100, entities.get(2).getCenter().getY(), 0.001);
        assertEquals(0, entities.get(2).getRotation(), 0.001);
    }
}
