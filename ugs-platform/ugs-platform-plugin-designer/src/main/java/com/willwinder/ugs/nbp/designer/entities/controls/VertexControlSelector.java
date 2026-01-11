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
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.ControllerListener;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.path.EditablePath;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * When the vertex tool is activated this control will listen for click events
 * on entities to select them.
 *
 * @author Joacim Breiler
 */
public class VertexControlSelector extends AbstractControl implements ControllerListener {

    private final VertexControlGroup vertexControlGroup;
    private final Controller controller;

    public VertexControlSelector(Controller controller, VertexControlGroup vertexControlGroup) {
        super(controller.getSelectionManager());
        this.controller = controller;
        this.controller.addListener(this);
        this.vertexControlGroup = vertexControlGroup;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
    }

    @Override
    public boolean isWithin(Point2D point) {
        return controller.getTool() == Tool.VERTEX &&
                !getSelectionManager().isWithin(point); // If we have an object selected we won't activate this control
    }

    @Override
    public void onEvent(EntityEvent event) {
        if (event instanceof MouseEntityEvent mouseEvent) {
            Point2D mousePosition = mouseEvent.getCurrentMousePosition();

            if (mouseEvent.getType() == EventType.MOUSE_PRESSED) {
                selectOne(mousePosition);
            }
        }
    }

    private void selectOne(Point2D mousePosition) {
        Optional<Entity> entitiesAt = controller.getDrawing()
                .getEntitiesAt(mousePosition)
                .stream()
                .filter(e -> e != this)
                .filter(e -> !(e instanceof Control))
                .filter(e -> !(e instanceof Cuttable && ((Cuttable) e).isHidden()))
                .filter(e -> e != controller.getSelectionManager())
                .findFirst();

        if (entitiesAt.isEmpty()) {
            vertexControlGroup.removeVertexControls();
            controller.getSelectionManager().clearSelection();
        }

        entitiesAt.ifPresent(entity -> {
            if (entity instanceof Path path) {
                controller.getSelectionManager().setSelection(List.of(path));

                createVertexControls(path);
            }
        });
    }

    private void createVertexControls(Path path) {
        vertexControlGroup.removeVertexControls();

        EditablePath editablePath = EditablePath.fromShape(path.getShape());
        editablePath.getSegments().forEach(segment -> {
            for (int p = 0; p < segment.getPoints().length; p++) {
                VertexControl vc = new VertexControl(
                        controller,
                        path,
                        editablePath,
                        segment,
                        p
                );
                vertexControlGroup.addVertexControl(vc);
            }
        });
    }

    @Override
    public void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.TOOL_SELECTED && controller.getTool() == Tool.VERTEX) {
            System.out.println("Tool selected " + getSelectionManager().getSelection());
            getSelectionManager().getSelection().stream()
                    .filter(c -> c instanceof Path)
                    .map(c -> (Path) c)
                    .findFirst()
                    .ifPresent(this::createVertexControls);
        }
    }
}
