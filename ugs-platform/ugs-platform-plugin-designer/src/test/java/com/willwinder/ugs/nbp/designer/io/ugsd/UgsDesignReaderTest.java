package com.willwinder.ugs.nbp.designer.io.ugsd;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.*;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class UgsDesignReaderTest {

    @Mock
    private Controller controller;

    @Mock
    private Drawing drawing;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readEmptyFileShouldReturnEmptyDesign() {
        UgsDesignReader reader = new UgsDesignReader();
        Optional<Design> design = reader.read(IOUtils.toInputStream(""));
        assertFalse(design.isPresent());
    }

    @Test(expected = RuntimeException.class)
    public void readFaultyFormatShouldThrowException() {
        UgsDesignReader reader = new UgsDesignReader();
        reader.read(IOUtils.toInputStream("{}"));
    }

    @Test(expected = RuntimeException.class)
    public void readFaultyVersionShouldThrowException() {
        UgsDesignReader reader = new UgsDesignReader();
        reader.read(IOUtils.toInputStream("{\"version\":1000}"));
    }

    @Test
    public void readEmptyDesignFileShouldReturnDesign() {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream("{\"version\":\"1\"}")).get();

        assertNotNull(design.getSettings());
        assertEquals(20, design.getSettings().getStockThickness(), 0.1);
        assertEquals(3, design.getSettings().getToolDiameter(), 0.1);
        assertEquals(1.0, design.getSettings().getDepthPerPass(), 0.1);
        assertEquals(10, design.getSettings().getSafeHeight(), 0.1);
        assertEquals(0.3, design.getSettings().getToolStepOver(), 0.1);
        assertEquals(UnitUtils.Units.MM, design.getSettings().getPreferredUnits());
        assertEquals(1000, design.getSettings().getFeedSpeed());

        assertNotNull(design.getEntities());
        assertEquals(0, design.getEntities().size());
    }

    @Test
    public void readDesignWithRectangle() {
        Rectangle entity = new Rectangle();
        entity.setSize(new Size(10, 10));
        entity.setCenter(new Point2D.Double(10, 10));
        entity.setName("rectangle");
        entity.setRotation(90);
        entity.setStartDepth(1);
        entity.setTargetDepth(12);
        entity.setCutType(CutType.POCKET);
        String data = convertEntityToString(entity);

        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream(data)).get();

        assertEquals(1, design.getEntities().size());
        Cuttable readEntity = (Cuttable) design.getEntities().get(0);
        assertTrue(readEntity instanceof Rectangle);
        assertEquals(entity.getPosition(), readEntity.getPosition());
        assertEquals(entity.getName(), readEntity.getName());
        assertEquals(entity.getCutType(), readEntity.getCutType());
        assertEquals(entity.getStartDepth(), readEntity.getStartDepth(), 0.1);
        assertEquals(entity.getTargetDepth(), readEntity.getTargetDepth(), 0.1);
        assertEquals(entity.getRotation(), readEntity.getRotation(), 0.1);
    }

    @Test
    public void readDesignWithEllipse() {
        Ellipse entity = new Ellipse();
        entity.setSize(new Size(50, 100));
        entity.setRotation(10);
        entity.setName("ellipse");
        entity.setTargetDepth(12);
        entity.setCutType(CutType.POCKET);
        String data = convertEntityToString(entity);

        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream(data)).get();

        Cuttable readEntity = (Cuttable) design.getEntities().get(0);
        assertTrue(readEntity instanceof Ellipse);

        assertEquals(entity.getTransform(), readEntity.getTransform());
        assertEquals(entity.getPosition().getX(), readEntity.getPosition().getX(), 0.1);
        assertEquals(entity.getPosition().getY(), readEntity.getPosition().getY(), 0.1);
        assertEquals(entity.getName(), readEntity.getName());
        assertEquals(entity.getCutType(), readEntity.getCutType());
        assertEquals(entity.getTargetDepth(), readEntity.getTargetDepth(), 0.1);
        assertEquals(entity.getRelativeShape().getBounds().getWidth(), readEntity.getRelativeShape().getBounds().getWidth(), 0.1);
        assertEquals(entity.getRelativeShape().getBounds().getHeight(), readEntity.getRelativeShape().getBounds().getHeight(), 0.1);
        assertEquals(entity.getSize().getWidth(), readEntity.getSize().getWidth(), 0.1);
        assertEquals(entity.getSize().getHeight(), readEntity.getSize().getHeight(), 0.1);
        assertEquals(entity.getRotation(), readEntity.getRotation(), 0.1);
    }

    @Test
    public void readDesignWithPath() {
        Path entity = new Path();
        entity.moveTo(0, 0);
        entity.lineTo(0, 0);
        entity.lineTo(10.1, 10.1);
        entity.lineTo(10, 0);
        entity.lineTo(0, 0);
        entity.setSize(new Size(10, 12));
        entity.setPosition(new Point2D.Double(100, 120));
        entity.setName("path");
        entity.setRotation(1);
        entity.setTargetDepth(12);
        entity.setCutType(CutType.POCKET);
        String data = convertEntityToString(entity);

        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream(data)).get();

        Cuttable readEntity = (Cuttable) design.getEntities().get(0);
        assertTrue(readEntity instanceof Path);
        assertEquals(entity.getPosition().getX(), readEntity.getPosition().getX(), 0.1);
        assertEquals(entity.getPosition().getY(), readEntity.getPosition().getY(), 0.1);
        assertEquals(entity.getName(), readEntity.getName());
        assertEquals(entity.getCutType(), readEntity.getCutType());
        assertEquals(entity.getTargetDepth(), readEntity.getTargetDepth(), 0.1);
        assertEquals(entity.getRotation(), readEntity.getRotation(), 0.1);
    }

    private String convertEntityToString(Entity entity) {
        when(controller.getSettings()).thenReturn(new Settings());
        when(controller.getDrawing()).thenReturn(drawing);

        EntityGroup group = new EntityGroup();
        group.addChild(entity);
        when(drawing.getRootEntity()).thenReturn(group);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        UgsDesignWriter writer = new UgsDesignWriter();
        writer.write(baos, controller);

        return new String(baos.toByteArray());
    }
}
