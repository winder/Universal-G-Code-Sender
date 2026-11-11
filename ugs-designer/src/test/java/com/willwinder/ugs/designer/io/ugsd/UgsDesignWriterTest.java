/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.designer.io.ugsd;

import com.willwinder.ugs.designer.constraint.AlignmentConstraint;
import com.willwinder.ugs.designer.constraint.Axis;
import com.willwinder.ugs.designer.constraint.CoincidentConstraint;
import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.constraint.EqualSizeConstraint;
import com.willwinder.ugs.designer.constraint.ShapePoint;
import com.willwinder.ugs.designer.constraint.ShapeSnapping;
import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Group;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.DesignModel;
import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * The settings of a design must survive a save and reopen. They used to be stored in the
 * application preferences only, which meant that a design opened on another machine, or after the
 * preferences had been changed, silently cut with different values than it was designed with.
 */
public class UgsDesignWriterTest {

    @Mock
    private Controller controller;

    @Mock
    private DesignModel model;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void write_shouldPersistAllSettings() {
        Settings settings = createModifiedSettings();

        Settings writtenSettings = writeAndRead(settings).getSettings();

        assertNotNull(writtenSettings);
        assertEquals(1234, writtenSettings.getFeedSpeed());
        assertEquals(321, writtenSettings.getPlungeSpeed());
        assertEquals(6.5, writtenSettings.getToolDiameter(), 0.1);
        assertEquals(EndmillShape.V_BIT, writtenSettings.getToolShape());
        assertEquals(12.5, writtenSettings.getSafeHeight(), 0.1);
        assertEquals(UnitUtils.Units.INCH, writtenSettings.getPreferredUnits());
        assertEquals(0.75, writtenSettings.getToolStepOver(), 0.1);
        assertEquals(90, writtenSettings.getVBitAngle(), 0.1);
        assertEquals(3.5, writtenSettings.getDepthPerPass(), 0.1);
        assertEquals(0.35, writtenSettings.getLaserDiameter(), 0.1);
        assertEquals(24000, writtenSettings.getMaxSpindleSpeed());
        assertFalse(writtenSettings.getDetectMaxSpindleSpeed());
        assertEquals("M4", writtenSettings.getSpindleDirection());
        assertEquals(CoolantMode.MIST, writtenSettings.getCoolantMode());
        assertEquals(0.05, writtenSettings.getFlatnessPrecision(), 0.01);
        assertFalse(writtenSettings.getArcFitting());
        assertTrue(writtenSettings.getUseToolChanges());
        assertEquals(4, writtenSettings.getToolNumber());
        assertEquals("tool-id", writtenSettings.getCurrentToolId());
    }

    @Test
    public void write_shouldPersistDepthPerPassDeviatingFromSelectedTool() {
        Settings settings = new Settings();
        settings.setCurrentToolSnapshot(createTool());
        settings.setCurrentToolId("tool-id");
        settings.setDepthPerPass(3.5);

        Design design = writeAndRead(settings);

        assertEquals("The edited depth must be stored, not the depth of the selected tool",
                3.5, design.getSettings().getDepthPerPass(), 0.1);
        assertEquals(1.0, design.getToolSnapshot().getDepthPerPass(), 0.1);
    }

    @Test
    public void write_shouldPersistConstraintsBetweenEntities() {
        DesignModel designModel = new DesignModel();
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(30, 0, 10, 10);
        Group group = new Group();
        group.addChild(target);
        designModel.insertEntities(List.of(reference, group));
        designModel.getConstraintManager().add(new DistanceConstraint(reference, at(reference, Anchor.BOTTOM_RIGHT), target, at(target, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL, 20, 4));

        Design design = writeAndRead(new Settings(), designModel);

        assertEquals(1, design.getConstraints().size());
        DistanceConstraint constraint = (DistanceConstraint) design.getConstraints().getFirst();
        Entity readGroup = design.getEntities().get(1);
        assertSame(constraint.getReference(), design.getEntities().getFirst());
        assertSame(constraint.getTarget(), ((EntityGroup) readGroup).getChildren().getFirst());
        assertEquals(new Point2D.Double(10, 0), constraint.getReferencePoint());
        assertEquals(new Point2D.Double(30, 0), constraint.getTargetPoint());
        assertEquals(Axis.HORIZONTAL, constraint.getAxis());
        assertEquals(20, constraint.getDistance(), 1e-6);
        assertEquals(4, constraint.getOffset(), 1e-6);
    }

    @Test
    public void write_shouldPersistAlignments() {
        DesignModel designModel = new DesignModel();
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(3, 30, 10, 10);
        designModel.insertEntities(List.of(reference, target));
        designModel.getConstraintManager().add(Constraint.aligned(reference, at(reference, Anchor.BOTTOM_LEFT), target, at(target, Anchor.BOTTOM_LEFT), Axis.HORIZONTAL));

        Design design = writeAndRead(new Settings(), designModel);

        assertEquals(1, design.getConstraints().size());
        AlignmentConstraint constraint = (AlignmentConstraint) design.getConstraints().getFirst();
        assertEquals(Axis.HORIZONTAL, constraint.getAxis());
        assertEquals(0, constraint.getTargetPoint().getX(), 1e-6);
    }

    @Test
    public void write_shouldPersistCoincidences() {
        DesignModel designModel = new DesignModel();
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(30, 20, 4, 4);
        designModel.insertEntities(List.of(reference, target));
        designModel.getConstraintManager().add(Constraint.coincident(reference, at(reference, Anchor.CENTER), target, at(target, Anchor.CENTER)));

        Design design = writeAndRead(new Settings(), designModel);

        CoincidentConstraint constraint = (CoincidentConstraint) design.getConstraints().getFirst();
        assertEquals(new Point2D.Double(5, 5), constraint.getTargetPoint());
    }

    @Test
    public void write_shouldSkipConstraintsOfRemovedEntities() {
        DesignModel designModel = new DesignModel();
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(30, 0, 10, 10);
        designModel.insertEntities(List.of(reference, target));
        designModel.getConstraintManager().add(new DistanceConstraint(reference, at(reference, Anchor.CENTER), target, at(target, Anchor.CENTER), Axis.HORIZONTAL, 30, 0));
        designModel.removeEntity(target);

        Design design = writeAndRead(new Settings(), designModel);

        assertTrue(design.getConstraints().isEmpty());
    }

    private Design writeAndRead(Settings settings) {
        when(model.getConstraintManager()).thenReturn(new ConstraintManager(new EntityGroup()));
        EntityGroup rootEntity = new EntityGroup();
        rootEntity.addChild(new Rectangle());
        when(model.getRootEntity()).thenReturn(rootEntity);
        return writeAndRead(settings, model);
    }

    private Design writeAndRead(Settings settings, DesignModel designModel) {
        when(controller.getSettings()).thenReturn(settings);
        when(controller.getModel()).thenReturn(designModel);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new UgsDesignWriter().write(outputStream, controller);

        return new UgsDesignReader()
                .read(new ByteArrayInputStream(outputStream.toByteArray()))
                .orElseThrow();
    }

    private static Settings createModifiedSettings() {
        Settings settings = new Settings();
        settings.setFeedSpeed(1234);
        settings.setPlungeSpeed(321);
        settings.setToolDiameter(6.5);
        settings.setToolShape(EndmillShape.V_BIT);
        settings.setSafeHeight(12.5);
        settings.setPreferredUnits(UnitUtils.Units.INCH);
        settings.setToolStepOver(0.75);
        settings.setVBitAngle(90);
        settings.setDepthPerPass(3.5);
        settings.setLaserDiameter(0.35);
        settings.setMaxSpindleSpeed(24000);
        settings.setDetectMaxSpindleSpeed(false);
        settings.setSpindleDirection("M4");
        settings.setCoolantMode(CoolantMode.MIST);
        settings.setFlatnessPrecision(0.05);
        settings.setArcFitting(false);
        settings.setUseToolChanges(true);
        settings.setToolNumber(4);
        settings.setCurrentToolId("tool-id");
        return settings;
    }

    private static ToolDefinition createTool() {
        ToolDefinition tool = new ToolDefinition();
        tool.setId("tool-id");
        tool.setName("3mm endmill");
        tool.setDiameter(3);
        tool.setFeedSpeed(1000);
        tool.setPlungeSpeed(400);
        tool.setDepthPerPass(1);
        tool.setStepOverPercent(0.3);
        tool.setMaxSpindleSpeed(255);
        return tool;
    }
    private static ShapePoint at(Entity entity, Anchor anchor) {
        return ShapeSnapping.pointAt(entity, entity.getPosition(anchor));
    }

    @Test
    public void write_shouldPersistTheAxesOfAConstraint() {
        DesignModel designModel = new DesignModel();
        Rectangle reference = new Rectangle(0, 0, 10, 10);
        Rectangle target = new Rectangle(30, 0, 10, 10);
        reference.rotate(new Point2D.Double(0, 0), 30);
        target.rotate(new Point2D.Double(0, 0), 30);
        designModel.insertEntities(List.of(reference, target));
        DistanceConstraint constraint = new DistanceConstraint(reference, at(reference, Anchor.CENTER), target, at(target, Anchor.CENTER), Axis.HORIZONTAL, 30, 0);
        designModel.getConstraintManager().add(constraint);
        target.rotate(10);

        Design design = writeAndRead(new Settings(), designModel);

        DistanceConstraint read = (DistanceConstraint) design.getConstraints().getFirst();
        assertEquals(constraint.getFrame().angle(), read.getFrame().angle(), 1e-6);
        assertEquals(-30, Math.toDegrees(read.getFrame().angle()), 1e-6);
    }

    @Test
    public void write_shouldPersistEqualSizes() {
        DesignModel designModel = new DesignModel();
        Rectangle reference = new Rectangle(0, 0, 10, 20);
        Rectangle target = new Rectangle(30, 20, 20, 4);
        designModel.insertEntities(List.of(reference, target));
        designModel.getConstraintManager().add(Constraint.equalSize(reference, Axis.VERTICAL, target, Axis.HORIZONTAL));

        Design design = writeAndRead(new Settings(), designModel);

        EqualSizeConstraint constraint = (EqualSizeConstraint) design.getConstraints().getFirst();
        assertEquals(Axis.VERTICAL, constraint.getReferenceAxis());
        assertEquals(Axis.HORIZONTAL, constraint.getTargetAxis());
    }
}
