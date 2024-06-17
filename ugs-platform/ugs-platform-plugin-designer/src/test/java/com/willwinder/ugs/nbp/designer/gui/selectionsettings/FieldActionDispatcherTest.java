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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.actions.ResizeAction;
import com.willwinder.ugs.nbp.designer.actions.UndoActionList;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.actions.UndoableAction;
import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class FieldActionDispatcherTest {

    @Mock
    private SelectionSettingsModel model;
    @Mock
    private SelectionManager selectionManager;
    @Mock
    private Controller controller;
    @Mock
    private UndoManager undoManager;
    private FieldActionDispatcher target;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(controller.getSelectionManager()).thenReturn(selectionManager);
        when(controller.getUndoManager()).thenReturn(undoManager);
        this.target = new FieldActionDispatcher(model, controller);
    }

    @Test
    public void onFieldUpdateSettingWidthShouldAddAndUndoableAction() {
        Entity entity = new Rectangle(0, 0);
        mockSelectionManager(entity);
        when(model.get(EntitySetting.WIDTH)).thenReturn(0);
        when(model.getAnchor()).thenReturn(Anchor.CENTER);

        ArgumentCaptor<UndoableAction> undoableActionArgumentCaptor = ArgumentCaptor.forClass(UndoableAction.class);
        doNothing().when(undoManager).addAction(undoableActionArgumentCaptor.capture());

        target.onFieldUpdate(EntitySetting.WIDTH, 10.0);

        verify(undoManager, times(1)).addAction(any(UndoableAction.class));
        UndoActionList actionList = (UndoActionList) undoableActionArgumentCaptor.getValue();
        assertEquals(1, actionList.getActions().size());
        assertTrue(actionList.getActions().get(0) instanceof ResizeAction);
    }

    private void mockSelectionManager(Entity entity) {
        Group group = new Group();
        group.addChild(entity);
        when(selectionManager.getSelectionGroup()).thenReturn(group);
    }
}