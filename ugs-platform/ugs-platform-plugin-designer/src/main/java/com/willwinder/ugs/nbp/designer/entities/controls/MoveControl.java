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
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.actions.MoveAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class MoveControl extends AbstractControl {
    private Point2D startOffset;
    private Point2D startPosition;

    public MoveControl(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public boolean isWithin(Point2D point) {
        return getSelectionManager().isWithin(point);
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        // There is nothing to render
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Entity target = getSelectionManager();
            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startPosition = target.getPosition();
                startOffset = new Point2D.Double(mousePosition.getX() - target.getPosition().getX(), mousePosition.getY() - target.getPosition().getY());
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - target.getPosition().getX() - startOffset.getX(), mousePosition.getY() - target.getPosition().getY() - startOffset.getY());
                if (mouseShapeEvent.isAltPressed()) {
                    Point2D newPosition = new Point2D.Double(Utils.roundToDecimals(target.getPosition().getX() + deltaMovement.getX(), 1), Utils.roundToDecimals(target.getPosition().getY() + deltaMovement.getY(), 1));
                    target.setPosition(newPosition);
                } else {
                    Point2D newPosition = new Point2D.Double(Math.round(target.getPosition().getX() + deltaMovement.getX()), Math.round(target.getPosition().getY() + deltaMovement.getY()));
                    target.setPosition(newPosition);
                }
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED && startPosition != null) {
                Point2D deltaMovementTotal = new Point2D.Double(target.getPosition().getX() - startPosition.getX(), target.getPosition().getY() - startPosition.getY());
                addUndoAction(deltaMovementTotal, target);
                startPosition = null;
                startOffset = null;
            }
        }
    }

    private void addUndoAction(Point2D deltaMovement, Entity target) {
        UndoManager undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        if (undoManager != null) {
            List<Entity> entityList = new ArrayList<>();
            if (target instanceof SelectionManager) {
                entityList.addAll(((SelectionManager) target).getSelection());
            } else {
                entityList.add(target);
            }
            undoManager.addAction(new MoveAction(entityList, deltaMovement));
        }
    }
}
