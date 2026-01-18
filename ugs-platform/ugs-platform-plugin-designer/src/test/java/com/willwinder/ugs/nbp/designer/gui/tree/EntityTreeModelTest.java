package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

public class EntityTreeModelTest {
    @Captor
    private ArgumentCaptor<TreeModelEvent> entityEventCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getRootShouldReturnTheDrawingRoot() {
        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        EntityTreeModel model = new EntityTreeModel(controller);
        Object root = model.getRoot();
        assertEquals(controller.getDrawing().getRootEntity(), root);
    }

    @Test
    public void addChildShouldCreateTreeNodesInsertedEvent() throws Exception {
        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        EntityGroup root = controller.getDrawing().getRootEntity();

        TreeModelListener listener = Mockito.mock(TreeModelListener.class);
        EntityTreeModel model = new EntityTreeModel(controller);
        model.addTreeModelListener(listener);

        Rectangle rectangle = new Rectangle();
        root.addChild(rectangle);

        waitForEdt();
        verify(listener).treeNodesInserted(entityEventCaptor.capture());
        assertEquals(rectangle, entityEventCaptor.getValue().getChildren()[0]);
        assertEquals(root, entityEventCaptor.getValue().getPath()[0]);
    }

    @Test
    public void removeChildShouldCreateTreeNodesRemovedEvent() throws Exception {
        Controller controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        EntityGroup root = controller.getDrawing().getRootEntity();
        Rectangle rectangle = new Rectangle();
        root.addChild(rectangle);

        TreeModelListener listener = Mockito.mock(TreeModelListener.class);
        EntityTreeModel model = new EntityTreeModel(controller);
        model.addTreeModelListener(listener);

        root.removeChild(rectangle);

        waitForEdt();
        verify(listener).treeNodesRemoved(entityEventCaptor.capture());
        assertEquals(rectangle, entityEventCaptor.getValue().getChildren()[0]);
        assertEquals(root, entityEventCaptor.getValue().getPath()[0]);
    }

    private static void waitForEdt() throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            return;
        }
        SwingUtilities.invokeAndWait(() -> {
        });
    }
}