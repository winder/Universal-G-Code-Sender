package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.List;

public class ReorderEntitiesActionTest {

    private final EntityGroup root = new EntityGroup();
    private final Rectangle a = new Rectangle(0, 0, 10, 10);
    private final Rectangle b = new Rectangle(0, 0, 10, 10);
    private final Rectangle c = new Rectangle(0, 0, 10, 10);
    private final Rectangle d = new Rectangle(0, 0, 10, 10);

    @Test
    public void redo_shouldMoveALaterEntityBeforeAnEarlierOne() {
        root.addAll(List.of(a, b, c, d));
        ReorderEntitiesAction action = new ReorderEntitiesAction(root, List.of(c), root, 1);

        action.redo();

        assertEquals(List.of(a, c, b, d), root.getChildren());
    }

    @Test
    public void redo_shouldMoveAnEarlierEntityAfterALaterOne() {
        root.addAll(List.of(a, b, c, d));
        ReorderEntitiesAction action = new ReorderEntitiesAction(root, List.of(a), root, 3);

        action.redo();

        assertEquals(List.of(b, c, a, d), root.getChildren());
    }

    @Test
    public void redo_shouldKeepSeveralEntitiesTogetherInOrder() {
        root.addAll(List.of(a, b, c, d));
        ReorderEntitiesAction action = new ReorderEntitiesAction(root, List.of(a, c), root, 4);

        action.redo();

        assertEquals(List.of(b, d, a, c), root.getChildren());
    }

    @Test
    public void redo_shouldMoveEntitiesIntoAGroup() {
        EntityGroup group = new EntityGroup();
        group.addChild(d);
        root.addAll(List.of(a, b, c, group));
        ReorderEntitiesAction action = new ReorderEntitiesAction(root, List.of(a, c), group, 0);

        action.redo();

        assertEquals(List.of(b, group), root.getChildren());
        assertEquals(List.of(a, c, d), group.getChildren());
    }

    @Test
    public void undo_shouldPutEntitiesBackWhereTheyCameFrom() {
        EntityGroup group = new EntityGroup();
        group.addChild(d);
        root.addAll(List.of(a, b, c, group));
        ReorderEntitiesAction action = new ReorderEntitiesAction(root, List.of(a, c), group, 1);
        action.redo();

        action.undo();

        assertEquals(List.of(a, b, c, group), root.getChildren());
        assertEquals(List.<Entity>of(d), group.getChildren());
    }

    @Test
    public void undo_shouldRestoreAReorderingWithinTheSameGroup() {
        root.addAll(List.of(a, b, c, d));
        ReorderEntitiesAction action = new ReorderEntitiesAction(root, List.of(b, d), root, 0);
        action.redo();

        action.undo();

        assertEquals(List.of(a, b, c, d), root.getChildren());
    }
}
