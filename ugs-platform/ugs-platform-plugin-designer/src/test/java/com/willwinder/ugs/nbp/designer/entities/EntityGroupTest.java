package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

public class EntityGroupTest {

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getChildrenAtShouldReturnEntitiesWithinPoint() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.move(new Point2D.Double(10, 10));

        List<Entity> childrenAt = entityGroup.getChildrenAt(new Point2D.Double(11, 11));
        assertEquals(1, childrenAt.size());
        assertEquals(rectangle, childrenAt.get(0));

        childrenAt = entityGroup.getChildrenAt(new Point2D.Double(9, 9));
        assertEquals(0, childrenAt.size());
    }

    @Test
    public void getPositionOfChildrenShouldReturnRealPosition() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);
        entityGroup.move(new Point2D.Double(10, 10));

        assertEquals(20, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getPosition().getX(), 0.1);
    }

    @Test
    public void moveShouldMoveChildren() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.move(new Point2D.Double(-10, -10));

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getX(), 0.1);
    }

    @Test
    public void positionShouldBeDeterminedByChildren() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.move(new Point2D.Double(10, 10));

        assertEquals(0, entityGroup.getPosition().getX(), 0.1);
        assertEquals(0, entityGroup.getPosition().getX(), 0.1);
    }

    @Test
    public void movingChildrenShouldIgnoreParentLocation() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.move(new Point2D.Double(10, 10));

        Rectangle rectangle = new Rectangle(100, 100);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        rectangle.move(new Point2D.Double(-5, -5));
        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(90, rectangle.getPosition().getX(), 0.1);
        assertEquals(90, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, entityGroup.getPosition().getX(), 0.1);
        assertEquals(90, entityGroup.getPosition().getX(), 0.1);
    }

    @Test
    public void scalingGroupShouldScaleChild() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.scale(2, 2);

        assertEquals(10, entityGroup.getPosition().getX(), 0.1);
        assertEquals(10, entityGroup.getPosition().getY(), 0.1);
        assertEquals(20, entityGroup.getSize().getWidth(), 0.1);
        assertEquals(20, entityGroup.getSize().getHeight(), 0.1);

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle.getSize().getHeight(), 0.1);
    }

    @Test
    public void scalingGroupShouldScaleChildrenAndTheirRelativePosition() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle1 = new Rectangle(10, 10);
        rectangle1.setWidth(10);
        rectangle1.setHeight(10);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(20, 20);
        rectangle2.setWidth(10);
        rectangle2.setHeight(10);
        entityGroup.addChild(rectangle2);

        entityGroup.scale(2, 2);

        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(20, rectangle1.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle1.getSize().getHeight(), 0.1);

        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(20, rectangle2.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle2.getSize().getHeight(), 0.1);
    }

    @Test
    public void setSizeShouldResizeChildrenRelativly() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle1 = new Rectangle(10, 10);
        rectangle1.setWidth(10);
        rectangle1.setHeight(10);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(20, 20);
        rectangle2.setWidth(10);
        rectangle2.setHeight(10);
        entityGroup.addChild(rectangle2);

        assertEquals(20, entityGroup.getSize().getWidth(), 0.1);
        assertEquals(20, entityGroup.getSize().getHeight(), 0.1);

        entityGroup.setSize(new Size(40, 40));

        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(20, rectangle1.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle1.getSize().getHeight(), 0.1);

        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(20, rectangle2.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle2.getSize().getHeight(), 0.1);
    }

    @Test
    public void rotateShouldRotateChildAsWell() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle = new Rectangle(10, 0);
        rectangle.setWidth(1);
        rectangle.setHeight(1);
        entityGroup.addChild(rectangle);

        entityGroup.rotate(90);

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, rectangle.getRotation(), 0.1);
    }

    @Test
    public void rotateShouldRotateChildrenAsWell() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle1 = new Rectangle(10, 0);
        rectangle1.setWidth(1);
        rectangle1.setHeight(1);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(0, 0);
        rectangle2.setWidth(1);
        rectangle2.setHeight(1);
        entityGroup.addChild(rectangle2);

        entityGroup.rotate(90);

        assertEquals(5, rectangle1.getPosition().getX(), 0.1);
        assertEquals(-5, rectangle1.getPosition().getY(), 0.1);
        assertEquals(90, rectangle1.getRotation(), 0.1);

        assertEquals(5, rectangle2.getPosition().getX(), 0.1);
        assertEquals(5, rectangle2.getPosition().getY(), 0.1);
        assertEquals(90, rectangle2.getRotation(), 0.1);
        assertEquals(90, entityGroup.getRotation(), 0.1);
    }

    @Test
    public void setRotationShouldRotateChildrenAsWell() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle1 = new Rectangle(10, 0);
        rectangle1.setWidth(1);
        rectangle1.setHeight(1);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(0, 0);
        rectangle2.setWidth(1);
        rectangle2.setHeight(1);
        entityGroup.addChild(rectangle2);

        entityGroup.setRotation(90);

        assertEquals(5, rectangle1.getPosition().getX(), 0.1);
        assertEquals(-5, rectangle1.getPosition().getY(), 0.1);
        assertEquals(90, rectangle1.getRotation(), 0.1);

        assertEquals(5, rectangle2.getPosition().getX(), 0.1);
        assertEquals(5, rectangle2.getPosition().getY(), 0.1);
        assertEquals(90, rectangle2.getRotation(), 0.1);
        assertEquals(90, entityGroup.getRotation(), 0.1);
    }

    @Test
    public void containsChildShouldReturnFalseWhenEntityNotChild() {
        EntityGroup entityGroup = new EntityGroup();
        assertFalse(entityGroup.containsChild(new Point()));
    }

    @Test
    public void containsChildShouldReturnTrueWhenEntityIsChild() {
        EntityGroup entityGroup = new EntityGroup();
        Entity point = new Point();
        entityGroup.addChild(point);
        assertTrue(entityGroup.containsChild(point));
    }

    @Test
    public void containsChildShouldReturnTrueWhenEntityIsGrandChild() {
        EntityGroup subGroup = new EntityGroup();
        Entity point = new Point();
        subGroup.addChild(point);

        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addChild(subGroup);

        assertTrue(entityGroup.containsChild(point));
    }

    @Test
    public void addEntityAtNonExistingIndexShouldAddLast() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addChild(new Point(), 10);
        assertEquals(1, entityGroup.getChildren().size());
    }

    @Test
    public void addEntityAtIndexShouldInsert() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addChild(new Point());
        entityGroup.addChild(new Point());

        Point point = new Point();
        entityGroup.addChild(point, 1);
        assertEquals(3, entityGroup.getChildren().size());
        assertEquals(1, entityGroup.getChildren().indexOf(point));
    }

    @Test
    public void copyShouldCopyProperties() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.setName("My name");

        Entity copy = entityGroup.copy();
        assertEquals("My name", copy.getName());
    }

    @Test
    public void findParentForShouldReturnItsClosestParent() {
        EntityGroup entityGroup = new EntityGroup();
        Point point = new Point();
        entityGroup.addChild(point);

        Optional<EntityGroup> parentFor = entityGroup.findParentFor(point);
        assertTrue(parentFor.isPresent());
        assertEquals(entityGroup, parentFor.get());
    }

    @Test
    public void findParentForShouldFindParentInNestedGroups() {
        EntityGroup subGroup = new EntityGroup();
        Point point = new Point();
        subGroup.addChild(point);

        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addChild(subGroup);

        Optional<EntityGroup> parentFor = entityGroup.findParentFor(point);
        assertTrue(parentFor.isPresent());
        assertEquals(subGroup, parentFor.get());
    }

    @Test
    public void findParentForShouldNotReturnParentIfNotFound() {
        Point point = new Point();
        EntityGroup entityGroup = new EntityGroup();

        Optional<EntityGroup> parentFor = entityGroup.findParentFor(point);
        assertFalse(parentFor.isPresent());
    }

    @Test
    public void onEventShouldUpdateBounds() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle1 = new Rectangle(0, 0);
        rectangle1.setSize(new Size(10, 10));
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(5, 5);
        entityGroup.addChild(rectangle2);
        assertEquals(0, entityGroup.getBounds().getX(), 0.1);
        assertEquals(0, entityGroup.getBounds().getY(), 0.1);
        assertEquals(10, entityGroup.getBounds().getWidth(), 0.1);
        assertEquals(10, entityGroup.getBounds().getHeight(), 0.1);

        // Trigger an onEvent which should update the bounds
        rectangle2.setSize(new Size(10, 10));
        assertEquals(0, entityGroup.getBounds().getX(), 0.1);
        assertEquals(0, entityGroup.getBounds().getY(), 0.1);
        assertEquals(15, entityGroup.getBounds().getWidth(), 0.1);
        assertEquals(15, entityGroup.getBounds().getHeight(), 0.1);
    }

    @Test
    public void setSizeShouldChangeAllChildEntites() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle1 = new Rectangle(0, 0);
        rectangle1.setSize(new Size(10, 10));
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(10, 0);
        rectangle2.setSize(new Size(10, 10));
        entityGroup.addChild(rectangle2);

        entityGroup.setSize(new Size(10, 5));
        assertEquals(new Size(10, 5), entityGroup.getSize());
        assertEquals(new Size(5, 5), rectangle1.getSize());
        assertEquals(new Size(5, 5), rectangle2.getSize());
    }

    @Captor
    private ArgumentCaptor<EntityEvent> entityEventCaptor;

    @Test
    public void removeChildShouldShouldNotifyListeners() {
        EntityGroup parent = new EntityGroup();
        Rectangle child = new Rectangle();
        parent.addChild(child);

        EntityListener entityListener = mock(EntityListener.class);
        parent.addListener(entityListener);

        parent.removeChild(child);

        verify(entityListener).onEvent(entityEventCaptor.capture());
        assertEquals(1, entityEventCaptor.getAllValues().size());
        assertEquals(parent, entityEventCaptor.getValue().getParent().orElse(null));
        assertEquals(child, entityEventCaptor.getValue().getTarget());
        assertEquals(EventType.CHILD_REMOVED, entityEventCaptor.getValue().getType());
    }

    @Test
    public void removeAllShouldShouldNotifyListeners() {
        EntityGroup parent = new EntityGroup();
        Rectangle child1 = new Rectangle();
        parent.addChild(child1);

        Rectangle child2 = new Rectangle();
        parent.addChild(child2);


        EntityListener entityListener = mock(EntityListener.class);
        parent.addListener(entityListener);

        parent.removeAll();

        verify(entityListener, times(2)).onEvent(entityEventCaptor.capture());
        assertEquals(2, entityEventCaptor.getAllValues().size());

        assertEquals(parent, entityEventCaptor.getAllValues().get(0).getParent().orElse(null));
        assertEquals(child1, entityEventCaptor.getAllValues().get(0).getTarget());
        assertEquals(EventType.CHILD_REMOVED, entityEventCaptor.getAllValues().get(0).getType());

        assertEquals(parent, entityEventCaptor.getAllValues().get(1).getParent().orElse(null));
        assertEquals(child2, entityEventCaptor.getAllValues().get(1).getTarget());
        assertEquals(EventType.CHILD_REMOVED, entityEventCaptor.getAllValues().get(1).getType());
    }

    @Test
    public void removeChildShouldPropagateEventsToRootParent() {
        EntityGroup root = new EntityGroup();
        EntityGroup parent = new EntityGroup();
        root.addChild(parent);
        Rectangle child = new Rectangle();
        parent.addChild(child);

        EntityListener entityListener = mock(EntityListener.class);
        root.addListener(entityListener);

        parent.removeChild(child);

        verify(entityListener).onEvent(entityEventCaptor.capture());
        assertEquals(1, entityEventCaptor.getAllValues().size());
        assertEquals(parent, entityEventCaptor.getValue().getParent().orElse(null));
        assertEquals(child, entityEventCaptor.getValue().getTarget());
        assertEquals(EventType.CHILD_REMOVED, entityEventCaptor.getValue().getType());
    }

    @Test
    public void addChildShouldNotifyEventListeners() {
        EntityGroup parent = new EntityGroup();
        EntityListener entityListener = mock(EntityListener.class);
        parent.addListener(entityListener);

        Rectangle child = new Rectangle();
        parent.addChild(child);

        verify(entityListener).onEvent(entityEventCaptor.capture());
        assertEquals(1, entityEventCaptor.getAllValues().size());
        assertEquals(parent, entityEventCaptor.getValue().getParent().orElse(null));
        assertEquals(child, entityEventCaptor.getValue().getTarget());
        assertEquals(EventType.CHILD_ADDED, entityEventCaptor.getValue().getType());
    }

    @Test
    public void addChildWithIndexShouldNotifyEventListeners() {
        EntityGroup parent = new EntityGroup();
        EntityListener entityListener = mock(EntityListener.class);
        parent.addListener(entityListener);

        Rectangle child = new Rectangle();
        parent.addChild(child, 0);

        verify(entityListener).onEvent(entityEventCaptor.capture());
        assertEquals(1, entityEventCaptor.getAllValues().size());
        assertEquals(parent, entityEventCaptor.getValue().getParent().orElse(null));
        assertEquals(child, entityEventCaptor.getValue().getTarget());
        assertEquals(EventType.CHILD_ADDED, entityEventCaptor.getValue().getType());
    }

    @Test
    public void addAllShouldNotifyEventListeners() {
        EntityGroup parent = new EntityGroup();
        EntityListener entityListener = mock(EntityListener.class);
        parent.addListener(entityListener);

        Rectangle child1 = new Rectangle();
        Rectangle child2 = new Rectangle();
        parent.addAll(List.of(child1, child2));

        verify(entityListener, times(2)).onEvent(entityEventCaptor.capture());
        assertEquals(2, entityEventCaptor.getAllValues().size());
        assertEquals(parent, entityEventCaptor.getAllValues().get(0).getParent().orElse(null));
        assertEquals(child1, entityEventCaptor.getAllValues().get(0).getTarget());
        assertEquals(EventType.CHILD_ADDED, entityEventCaptor.getAllValues().get(0).getType());

        assertEquals(parent, entityEventCaptor.getAllValues().get(1).getParent().orElse(null));
        assertEquals(child2, entityEventCaptor.getAllValues().get(1).getTarget());
        assertEquals(EventType.CHILD_ADDED, entityEventCaptor.getAllValues().get(1).getType());
    }
}
