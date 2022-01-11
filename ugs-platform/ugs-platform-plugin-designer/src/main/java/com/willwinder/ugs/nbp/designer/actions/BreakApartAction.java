/*
    Copyright 2021 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gcode.toolpaths.ToolPathUtils;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An action for creating an intersection between two entities.
 * This will also add the operation to the undo stack.
 *
 * @author Joacim Breiler
 */
public class BreakApartAction extends AbstractAction implements SelectionListener {
    private static final String SMALL_ICON_PATH = "img/break.svg";
    private static final String LARGE_ICON_PATH = "img/break24.svg";
    private transient final Controller controller;

    public BreakApartAction(Controller controller) {
        putValue("menuText", "Break apart");
        putValue(NAME, "Break apart");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        this.controller = controller;
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        onSelectionEvent(new SelectionEvent());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> selection = controller.getSelectionManager().getSelection();
        UndoableBreakApartAction action = new UndoableBreakApartAction((Cuttable) selection.get(0));
        action.redo();
        controller.getUndoManager().addAction(action);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        boolean isSingleEntity = selectionManager.getSelection().size() == 1;
        if (!isSingleEntity) {
            setEnabled(false);
            return;
        }

        boolean isCompoundPath = false;
        Entity entity = selectionManager.getSelection().get(0);
        if (entity instanceof Path) {
            isCompoundPath = ((Path) entity).isCompoundPath();
        }
        setEnabled(isCompoundPath);
    }

    private class UndoableBreakApartAction implements UndoableAction {
        private final Cuttable entity;
        private List<Entity> newEntities;

        public UndoableBreakApartAction(Cuttable entity) {
            this.entity = entity;
        }

        @Override
        public void redo() {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(entity.getShape()), new GeometryFactory());
            List<Shape> shapeList = breakShapeApart(geometry);
            newEntities = shapeList.stream().map(shape -> {
                Path path = new Path();
                path.append(shape);
                path.setCutType(entity.getCutType());
                path.setStartDepth(entity.getStartDepth());
                path.setTargetDepth(entity.getTargetDepth());
                path.setName(entity.getName());
                return path;
            }).collect(Collectors.toList());

            controller.getSelectionManager().clearSelection();
            controller.getDrawing().removeEntity(entity);
            controller.getDrawing().insertEntities(newEntities);
            controller.getSelectionManager().addSelection(newEntities);
        }

        private List<Shape> breakShapeApart(Geometry geometry) {
            List<Shape> shapeList = new ArrayList<>();
            ShapeWriter shapeWriter = new ShapeWriter();

            if (geometry.getNumGeometries() > 1) { // If the shape consists of multiple geometries
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    shapeList.add(shapeWriter.toShape(geometry.getGeometryN(i)));
                }
            } else if (geometry instanceof Polygon) { // If the shape consists of a polygon
                Polygon polygon = ((Polygon) geometry);
                shapeList.add(shapeWriter.toShape(polygon.getExteriorRing()));
                for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
                    shapeList.add(shapeWriter.toShape(polygon.getInteriorRingN(i)));
                }
            }
            return shapeList;
        }

        @Override
        public void undo() {
            if (entity != null) {
                controller.getSelectionManager().clearSelection();
                controller.getDrawing().removeEntities(newEntities);
                controller.getDrawing().insertEntity(entity);
                controller.getSelectionManager().setSelection(Collections.singletonList(entity));
            }
        }

        @Override
        public String toString() {
            return "entity break apart";
        }
    }
}
