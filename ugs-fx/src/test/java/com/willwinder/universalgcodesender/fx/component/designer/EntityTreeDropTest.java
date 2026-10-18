package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.universalgcodesender.fx.component.designer.EntityTreeDrop.Position;
import com.willwinder.universalgcodesender.fx.component.designer.EntityTreeDrop.Target;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class EntityTreeDropTest {

    @Test
    public void positionFor_shouldInsertBeforeOrAfterAShapeDependingOnHalf() {
        Position upper = EntityTreeDrop.positionFor(false, 10, 40);
        Position lower = EntityTreeDrop.positionFor(false, 30, 40);

        Assertions.assertThat(upper).isEqualTo(Position.BEFORE);
        Assertions.assertThat(lower).isEqualTo(Position.AFTER);
    }

    @Test
    public void positionFor_shouldDropIntoAGroupOverItsMiddle() {
        Position top = EntityTreeDrop.positionFor(true, 5, 40);
        Position middle = EntityTreeDrop.positionFor(true, 20, 40);
        Position bottom = EntityTreeDrop.positionFor(true, 35, 40);

        Assertions.assertThat(top).isEqualTo(Position.BEFORE);
        Assertions.assertThat(middle).isEqualTo(Position.INTO);
        Assertions.assertThat(bottom).isEqualTo(Position.AFTER);
    }

    @Test
    public void resolve_shouldTargetTheIndexOfTheRowWhenInsertingBefore() {
        EntityGroup parent = new EntityGroup();
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(0, 0, 10, 10);
        parent.addAll(List.of(first, second));

        Target target = EntityTreeDrop.resolve(Position.BEFORE, second, parent);

        Assertions.assertThat(target).isEqualTo(new Target(parent, 1));
    }

    @Test
    public void resolve_shouldTargetTheNextIndexWhenInsertingAfter() {
        EntityGroup parent = new EntityGroup();
        Rectangle first = new Rectangle(0, 0, 10, 10);
        Rectangle second = new Rectangle(0, 0, 10, 10);
        parent.addAll(List.of(first, second));

        Target target = EntityTreeDrop.resolve(Position.AFTER, first, parent);

        Assertions.assertThat(target).isEqualTo(new Target(parent, 1));
    }

    @Test
    public void resolve_shouldAppendToTheGroupWhenDroppingInto() {
        EntityGroup parent = new EntityGroup();
        EntityGroup group = new EntityGroup();
        group.addChild(new Rectangle(0, 0, 10, 10));
        parent.addChild(group);

        Target target = EntityTreeDrop.resolve(Position.INTO, group, parent);

        Assertions.assertThat(target).isEqualTo(new Target(group, 1));
    }

    @Test
    public void canDrop_shouldRefuseAGroupDroppedIntoItself() {
        EntityGroup group = new EntityGroup();

        boolean allowed = EntityTreeDrop.canDrop(List.of(group), new Target(group, 0));

        Assertions.assertThat(allowed).isFalse();
    }

    @Test
    public void canDrop_shouldRefuseAGroupDroppedIntoItsOwnDescendant() {
        EntityGroup group = new EntityGroup();
        EntityGroup child = new EntityGroup();
        group.addChild(child);

        boolean allowed = EntityTreeDrop.canDrop(List.of(group), new Target(child, 0));

        Assertions.assertThat(allowed).isFalse();
    }

    @Test
    public void canDrop_shouldAllowAShapeDroppedIntoAnotherGroup() {
        EntityGroup group = new EntityGroup();
        Entity shape = new Rectangle(0, 0, 10, 10);

        boolean allowed = EntityTreeDrop.canDrop(List.of(shape), new Target(group, 0));

        Assertions.assertThat(allowed).isTrue();
    }

    @Test
    public void canDrop_shouldRefuseAnEmptyDrag() {
        boolean allowed = EntityTreeDrop.canDrop(List.of(), new Target(new EntityGroup(), 0));

        Assertions.assertThat(allowed).isFalse();
    }
}
