package com.willwinder.ugs.nbp.designer.io.dxf;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DxfReaderTest  {
    @Test
    public void readDxf11() {
        DxfReader reader = new DxfReader(new Settings());
        Design design = reader.read(DxfReader.class.getResourceAsStream("/R11.dxf")).orElse(null);
        assertNotNull(design);

        Group root = (Group) design.getEntities().get(0);
        Group layer = (Group) root.getChildren().get(0);

        assertEquals("LAYER_1", layer.getName());
        assertEquals(1, layer.getChildren().size());

        Group polylines = (Group) layer.getChildren().get(0);
        assertEquals(21, polylines.getChildren().size());
    }

    @Test
    public void readDxf14() {
        DxfReader reader = new DxfReader(new Settings());
        Design design = reader.read(DxfReader.class.getResourceAsStream("/R14.dxf")).orElse(null);
        assertNotNull(design);

        Group root = (Group) design.getEntities().get(0);
        Group layer = (Group) root.getChildren().get(0);

        assertEquals("LAYER_1", layer.getName());
        assertEquals(2, layer.getChildren().size());

        Group polylines = (Group) layer.getChildren().get(0);
        assertEquals(19, polylines.getChildren().size());

        Group ellipses = (Group) layer.getChildren().get(1);
        assertEquals(2, ellipses.getChildren().size());
    }
}
