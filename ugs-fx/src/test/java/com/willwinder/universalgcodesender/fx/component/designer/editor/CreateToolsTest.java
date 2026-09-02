package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import javafx.scene.input.MouseEvent;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.design;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.drag;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.pointer;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.press;
import static com.willwinder.universalgcodesender.fx.component.designer.editor.EditorTestSupport.release;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class CreateToolsTest {
    private Controller controller;
    private EditorState state;
    private ToolContext context;

    @Before
    public void setUp() {
        controller = EditorTestSupport.freshController();
        state = new EditorState();
        context = EditorTestSupport.toolContext(controller, EditorTestSupport.topDownCamera(), state);
    }

    @Test
    public void rectangleTool_shouldCreateSelectAndSwitchToSelectTool() {
        controller.setTool(Tool.RECTANGLE);
        CreateShapeTool tool = CreateShapeTool.rectangle(context);

        tool.onPressed(press(10, 10), design(10, 10));
        tool.onDragged(drag(30, 15), design(30, 15));
        assertThat(state.preview()).isNotNull();
        tool.onReleased(release(30, 15), design(30, 15));

        List<Entity> entities = controller.getModel().getEntities();
        assertThat(entities).hasSize(1).first().isInstanceOf(Rectangle.class);
        Entity rectangle = entities.get(0);
        assertThat(rectangle.getSize().getWidth()).isCloseTo(20, within(1e-6));
        assertThat(rectangle.getSize().getHeight()).isCloseTo(5, within(1e-6));
        assertThat(rectangle.getPosition().getX()).isCloseTo(10, within(1e-6));
        assertThat(controller.getTool()).isEqualTo(Tool.SELECT);
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(rectangle);
        assertThat(state.preview()).isNull();
        assertThat(controller.getUndoManager().canUndo()).isTrue();
    }

    @Test
    public void rectangleTool_shouldMakeASquareWithShift() {
        CreateShapeTool tool = CreateShapeTool.rectangle(context);

        tool.onPressed(press(0, 0), design(0, 0));
        tool.onReleased(pointer(MouseEvent.MOUSE_RELEASED, 20, 5, true, false, 1), design(20, 5));

        Entity rectangle = controller.getModel().getEntities().get(0);
        assertThat(rectangle.getSize().getWidth()).isCloseTo(20, within(1e-6));
        assertThat(rectangle.getSize().getHeight()).isCloseTo(20, within(1e-6));
    }

    @Test
    public void rectangleTool_shouldNotCreateDegenerateShapes() {
        CreateShapeTool tool = CreateShapeTool.rectangle(context);

        tool.onPressed(press(10, 10), design(10, 10));
        tool.onReleased(release(10, 10), design(10, 10));

        assertThat(controller.getModel().getEntities()).isEmpty();
    }

    @Test
    public void ellipseTool_shouldCreateAnEllipse() {
        CreateShapeTool tool = CreateShapeTool.ellipse(context);

        tool.onPressed(press(5, 5), design(5, 5));
        tool.onReleased(release(25, 15), design(25, 15));

        assertThat(controller.getModel().getEntities()).hasSize(1).first().isInstanceOf(Ellipse.class);
    }

    @Test
    public void pointTool_shouldCreateAPointOnPress() {
        controller.setTool(Tool.POINT);
        PointTool tool = new PointTool(context);

        tool.onPressed(press(12, 34), design(12, 34));

        List<Entity> entities = controller.getModel().getEntities();
        assertThat(entities).hasSize(1).first().isInstanceOf(Point.class);
        assertThat(controller.getTool()).isEqualTo(Tool.SELECT);
        assertThat(controller.getSelectionManager().getSelection()).containsExactly(entities.get(0));
    }

    @Test
    public void lineTool_shouldCreateAPathAndSwitchToTheVertexTool() {
        controller.setTool(Tool.LINE);
        LineTool tool = new LineTool(context);

        tool.onPressed(press(0, 0), design(0, 0));
        tool.onDragged(drag(10, 0), design(10, 0));
        tool.onReleased(release(10, 0), design(10, 0));

        List<Entity> entities = controller.getModel().getEntities();
        assertThat(entities).hasSize(1).first().isInstanceOf(Path.class);
        assertThat(entities.get(0).getSize().getWidth()).isCloseTo(10, within(1e-6));
        assertThat(controller.getTool()).isEqualTo(Tool.VERTEX);
    }
}
