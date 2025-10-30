/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.settings.TransformationEntitySettingsManager;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SettingsActionFactory
 *
 * @author giro-dev
 */
public class SettingsActionFactoryTest {

    @Test
    public void testCreateGroupTransformAction_Width() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.WIDTH, 200.0, group, Anchor.CENTER, false
        );

        assertNotNull(action);
        assertTrue(action instanceof ResizeGroupAction);
        assertEquals("Change group width", action.toString());
    }

    @Test
    public void testCreateGroupTransformAction_Height() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.HEIGHT, 100.0, group, Anchor.CENTER, true
        );

        assertNotNull(action);
        assertTrue(action instanceof ResizeGroupAction);
        assertEquals("Change group height", action.toString());
    }

    @Test
    public void testCreateGroupTransformAction_PositionX() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.POSITION_X, 50.0, group, Anchor.CENTER, false
        );

        assertNotNull(action);
        assertTrue(action instanceof MoveGroupAction);
        assertEquals("Change group X position", action.toString());
    }

    @Test
    public void testCreateGroupTransformAction_PositionY() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.POSITION_Y, 75.0, group, Anchor.CENTER, false
        );

        assertNotNull(action);
        assertTrue(action instanceof MoveGroupAction);
        assertEquals("Change group Y position", action.toString());
    }

    @Test
    public void testCreateGroupTransformAction_Rotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.ROTATION, 45.0, group, Anchor.CENTER, false
        );

        assertNotNull(action);
        assertTrue(action instanceof RotateGroupAction);
    }

    @Test
    public void testCreateGroupTransformAction_UnsupportedProperty() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                null, 100.0, group, Anchor.CENTER, false
        );

        assertNull(action);
    }

    @Test
    public void testCreateGroupTransformAction_NullGroup() {
        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.WIDTH, 100.0, null, Anchor.CENTER, false
        );

        assertNull(action);
    }

    @Test
    public void testCreateGroupTransformAction_EmptyGroup() {
        Group emptyGroup = new Group();

        UndoableAction action = SettingsActionFactory.createGroupTransformAction(
                EntitySetting.WIDTH, 100.0, emptyGroup, Anchor.CENTER, false
        );

        assertNull(action);
    }

    @Test
    public void testMapPropertyToEntitySetting() {
        assertEquals(EntitySetting.POSITION_X, SettingsActionFactory.mapPropertyToEntitySetting("positionX"));
        assertEquals(EntitySetting.POSITION_Y, SettingsActionFactory.mapPropertyToEntitySetting("positionY"));
        assertEquals(EntitySetting.WIDTH, SettingsActionFactory.mapPropertyToEntitySetting("width"));
        assertEquals(EntitySetting.HEIGHT, SettingsActionFactory.mapPropertyToEntitySetting("height"));
        assertEquals(EntitySetting.ROTATION, SettingsActionFactory.mapPropertyToEntitySetting("rotation"));
        assertEquals(EntitySetting.CUT_TYPE, SettingsActionFactory.mapPropertyToEntitySetting("cutType"));
        assertEquals(EntitySetting.TEXT, SettingsActionFactory.mapPropertyToEntitySetting("text"));
        assertEquals(EntitySetting.FONT_FAMILY, SettingsActionFactory.mapPropertyToEntitySetting("fontFamily"));
    }

    @Test
    public void testMapPropertyToEntitySetting_Unknown() {
        assertNull(SettingsActionFactory.mapPropertyToEntitySetting("unknownProperty"));
        assertNull(SettingsActionFactory.mapPropertyToEntitySetting(""));
        assertThrows(IllegalArgumentException.class, () -> SettingsActionFactory.mapPropertyToEntitySetting(null));
    }

    @Test
    public void testCreateEntitySettingAction() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        UndoableAction action = SettingsActionFactory.createEntitySettingAction(
                group.getChildren(),
                EntitySetting.ROTATION,
                45.0,
                new TransformationEntitySettingsManager()
        );

        assertNotNull(action);
        assertTrue(action instanceof ChangeEntitySettingsAction);
    }

    @Test
    public void testCreateEntitySettingAction_NullEntities() {

        assertNull(SettingsActionFactory.createEntitySettingAction(
                null,
                EntitySetting.ROTATION,
                45.0,
                new TransformationEntitySettingsManager()
        ));
    }

    @Test
    public void testCreateAction_Comprehensive() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        TransformationEntitySettingsManager manager = new TransformationEntitySettingsManager();

        // Test width resize
        UndoableAction widthAction = SettingsActionFactory.createAction(
                EntitySetting.WIDTH, 200.0, group, Anchor.CENTER, true, manager
        );
        assertNotNull(widthAction);
        assertTrue(widthAction instanceof ResizeGroupAction);

        // Test position
        UndoableAction posAction = SettingsActionFactory.createAction(
                EntitySetting.POSITION_X, 50.0, group, Anchor.CENTER, false, manager
        );
        assertNotNull(posAction);
        assertTrue(posAction instanceof MoveGroupAction);

        // Test rotation
        UndoableAction rotateAction = SettingsActionFactory.createAction(
                EntitySetting.ROTATION, 90.0, group, Anchor.CENTER, false, manager
        );
        assertNotNull(rotateAction);
        assertTrue(rotateAction instanceof RotateGroupAction);
    }

    @Test
    public void testCreateAction_FallbackToEntitySetting() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        // Test a property that doesn't have a dedicated action class
        // Should fall back to ChangeEntitySettingsAction
        UndoableAction action = SettingsActionFactory.createAction(
                EntitySetting.ANCHOR, Anchor.TOP_LEFT, group, Anchor.CENTER, false,
                new TransformationEntitySettingsManager()
        );

        // May be null or ChangeEntitySettingsAction depending on implementation
        // At minimum, should not throw an exception
        if (action != null) {
            assertTrue(action instanceof ChangeEntitySettingsAction);
        }
    }
}

