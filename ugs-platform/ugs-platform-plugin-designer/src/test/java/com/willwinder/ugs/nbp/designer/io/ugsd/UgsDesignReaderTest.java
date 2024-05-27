package com.willwinder.ugs.nbp.designer.io.ugsd;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

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
        Optional<Design> design = reader.read(IOUtils.toInputStream("", Charset.defaultCharset()));
        assertFalse(design.isPresent());
    }

    @Test(expected = RuntimeException.class)
    public void readFaultyFormatShouldThrowException() {
        UgsDesignReader reader = new UgsDesignReader();
        reader.read(IOUtils.toInputStream("{}", Charset.defaultCharset()));
    }

    @Test(expected = RuntimeException.class)
    public void readFaultyVersionShouldThrowException() {
        UgsDesignReader reader = new UgsDesignReader();
        reader.read(IOUtils.toInputStream("{\"version\":1000}", Charset.defaultCharset()));
    }

    @Test
    public void readEmptyDesignFileShouldReturnDesign() {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream("{\"version\":\"1\"}", Charset.defaultCharset())).get();

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
        Design design = reader.read(IOUtils.toInputStream(data, Charset.defaultCharset())).get();

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
    public void readDesignWithHiddenObject() {
        Rectangle entity = new Rectangle();
        entity.setHidden(true);
        String data = convertEntityToString(entity);

        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream(data, Charset.defaultCharset())).get();

        assertEquals(1, design.getEntities().size());
        Cuttable readEntity = (Cuttable) design.getEntities().get(0);
        assertTrue(readEntity instanceof Rectangle);
        assertEquals(entity.isHidden(), readEntity.isHidden());
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
        Design design = reader.read(IOUtils.toInputStream(data, Charset.defaultCharset())).get();

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
        Design design = reader.read(IOUtils.toInputStream(data, Charset.defaultCharset())).get();

        Cuttable readEntity = (Cuttable) design.getEntities().get(0);
        assertTrue(readEntity instanceof Path);
        assertEquals(entity.getPosition().getX(), readEntity.getPosition().getX(), 0.1);
        assertEquals(entity.getPosition().getY(), readEntity.getPosition().getY(), 0.1);
        assertEquals(entity.getName(), readEntity.getName());
        assertEquals(entity.getCutType(), readEntity.getCutType());
        assertEquals(entity.getTargetDepth(), readEntity.getTargetDepth(), 0.1);
        assertEquals(entity.getRotation(), readEntity.getRotation(), 0.1);
    }


    @Test
    public void readDesignWithGroup() {
        Point point = new Point(10, 20);
        Group entity = new Group();
        entity.addChild(point);
        String data = convertEntityToString(entity);

        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(IOUtils.toInputStream(data, Charset.defaultCharset())).get();

        Cuttable readEntity = (Cuttable) design.getEntities().get(0);
        assertTrue(readEntity instanceof Group);
        assertEquals(entity.getPosition().getX(), readEntity.getPosition().getX(), 0.1);
        assertEquals(entity.getPosition().getY(), readEntity.getPosition().getY(), 0.1);
        assertEquals(entity.getName(), readEntity.getName());
        assertEquals(entity.getCutType(), readEntity.getCutType());
        assertEquals(entity.getTargetDepth(), readEntity.getTargetDepth(), 0.1);
        assertEquals(entity.getRotation(), readEntity.getRotation(), 0.1);

        List<Entity> readGroupEntities = ((Group) readEntity).getChildren();
        assertEquals(1, readGroupEntities.size());
        assertTrue(readGroupEntities.get(0) instanceof Point);
        assertEquals(point.getPosition().getX(), readGroupEntities.get(0).getPosition().getX(), 0.1);
        assertEquals(point.getPosition().getY(), readGroupEntities.get(0).getPosition().getY(), 0.1);
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

        return baos.toString();
    }
}
