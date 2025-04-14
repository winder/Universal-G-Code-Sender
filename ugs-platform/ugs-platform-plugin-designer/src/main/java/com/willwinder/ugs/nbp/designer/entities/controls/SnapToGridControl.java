/*
 * Copyright (C) 2025 fliptech
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

/**
 *
 * @author fliptech
 */
public abstract class SnapToGridControl extends AbstractControl implements ISnapToGridListener {
    private double gridSize = 0;
    
    public SnapToGridControl(SelectionManager selectionManager) {
        super(selectionManager);
    }
    
    @Override
    public void snapToGridUpdated(double aNewValue) {
        this.gridSize = aNewValue;
    }
    
    public double getSnapGridSize() {
        return gridSize;
    }
    
    public double snapToGrid(double aInput) {
        double result = aInput;
        if (this.gridSize != 0) {
            result = Math.round(aInput/gridSize);
            result*=gridSize;
        }
        return result;
    }
    
}
