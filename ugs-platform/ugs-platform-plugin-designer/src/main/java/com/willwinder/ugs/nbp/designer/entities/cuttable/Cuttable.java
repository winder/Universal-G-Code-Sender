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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.Entity;

/**
 * Defines an entity that can be cut using a cut operation.
 *
 * @author Joacim Breiler
 */
public interface Cuttable extends Entity {
    /**
     * Returns the desired cut operation use to cut the entity.
     *
     * @return the cut type to use.
     */
    CutType getCutType();

    /**
     * Sets the desired cut operation to use for cutting the entity.
     *
     * @param cutType the cut type to use
     */
    void setCutType(CutType cutType);

    /**
     * Returns the target depth that we want to end cutting to.
     *
     * @return a positive number for a depth to cut
     */
    double getTargetDepth();

    /**
     * Sets the target depth that we want to end cutting to.
     *
     * @param cutDepth a positive number for a depth to cut
     */
    void setTargetDepth(double cutDepth);

    /**
     * Returns the start depth that we want to begin cutting from.
     *
     * @return a positive number for a depth to cut from
     */
    double getStartDepth();

    /**
     * Sets the start depth to start cutting from
     *
     * @param startDepth a positive number for a depth to cut from
     */
    void setStartDepth(double startDepth);
}
