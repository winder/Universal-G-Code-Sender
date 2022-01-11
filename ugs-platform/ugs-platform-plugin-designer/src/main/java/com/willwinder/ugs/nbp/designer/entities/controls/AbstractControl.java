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

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

import java.awt.*;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractControl extends AbstractEntity implements Control {

    private final SelectionManager selectionManager;

    protected AbstractControl(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    @Override
    public Optional<Cursor> getHoverCursor() {
        return Optional.empty();
    }

    @Override
    public Shape getShape() {
        return selectionManager.getShape();
    }

    @Override
    public Shape getRelativeShape() {
        return selectionManager.getRelativeShape();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    @Override
    public Entity copy() {
        throw new RuntimeException("Not implemented");
    }
}
