package com.willwinder.ugs.designer.gui.tree;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

public class EntityCellRendererTest {
    private EntityCellRenderer renderer;
    private JTree tree;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        renderer = new EntityCellRenderer();
        tree = new JTree();
    }

    @Test
    public void openPathShouldBeIndicatedWithATrailingIcon() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.setName("A path");

        JLabel label = render(path);

        assertNotNull(renderer.getTrailingIcon());
        assertEquals("A path", label.getText());
        assertTrue(label.getToolTipText().contains("not closed"));
    }

    @Test
    public void openPathShouldReserveSpaceForTheTrailingIcon() {
        Path openPath = new Path();
        openPath.moveTo(0, 0);
        openPath.lineTo(10, 0);
        openPath.setName("A path");
        Path closedPath = new Path();
        closedPath.moveTo(0, 0);
        closedPath.lineTo(10, 0);
        closedPath.lineTo(0, 0);
        closedPath.setName("A path");

        Dimension openPathSize = render(openPath).getPreferredSize();

        assertTrue(openPathSize.width > render(closedPath).getPreferredSize().width);
    }

    @Test
    public void closedPathShouldNotBeIndicated() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.lineTo(10, 10);
        path.lineTo(0, 0);
        path.setName("A path");

        JLabel label = render(path);

        assertNull(renderer.getTrailingIcon());
        assertNull(label.getToolTipText());
    }

    @Test
    public void openPathShouldBeIndicatedTogetherWithTheCutSettings() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 0);
        path.setName("A path");
        path.setCutType(CutType.POCKET);
        path.setTargetDepth(3);

        JLabel label = render(path);

        assertNotNull(renderer.getTrailingIcon());
        assertTrue(label.getText().contains("mm/min"));
    }

    @Test
    public void otherEntitiesShouldNotBeIndicatedAsOpenPaths() {
        Rectangle rectangle = new Rectangle();
        rectangle.setName("A rectangle");

        JLabel label = render(rectangle);

        assertNull(renderer.getTrailingIcon());
        assertNull(label.getToolTipText());
    }

    @Test
    public void trailingIconShouldBeClearedWhenRenderingAClosedPathAfterAnOpenPath() {
        Path openPath = new Path();
        openPath.moveTo(0, 0);
        openPath.lineTo(10, 0);
        render(openPath);

        Path closedPath = new Path();
        closedPath.moveTo(0, 0);
        closedPath.lineTo(10, 0);
        closedPath.lineTo(0, 0);
        JLabel label = render(closedPath);

        assertNull(renderer.getTrailingIcon());
        assertNull(label.getToolTipText());
    }

    private JLabel render(Object entity) {
        return (JLabel) renderer.getTreeCellRendererComponent(
                tree, new DefaultMutableTreeNode(entity), false, false, true, 0, false);
    }
}
