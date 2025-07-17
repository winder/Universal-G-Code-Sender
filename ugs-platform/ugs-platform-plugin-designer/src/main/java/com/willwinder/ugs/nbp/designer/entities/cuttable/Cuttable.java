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
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;

import java.util.Optional;

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

    /**
     * Returns the laser power in percent where min is 0 and max is 100.
     *
     * @return the laser power in percent
     */
    int getSpindleSpeed();

    /**
     * Sets the laser power in percent where min is 0 and max is 100
     *
     * @param power the power in percent
     */
    void setSpindleSpeed(int power);

    /**
     * Returns the laser feed rate in percent where min is 0 and max is 100.
     *
     * @return the laser feed rate in percent
     */
    int getFeedRate();

    /**
     * Sets the laser feed rate in percent where min is 0 and max is 100
     *
     * @param feedRate the feed rate in percent
     */

    void setFeedRate(int feedRate);

    /**
     * Returns the number of passes that should be done
     *
     * @return the number of passes
     */
    int getPasses();

    /**
     * Sets the number of passes that should be done in with laser
     *
     * @param laserPasses the number of passes
     */
    void setPasses(int laserPasses);

    /**
     * If the entity should be hidden in the design. When hidden the entity is not included
     * in the output or displayed in the editor.
     *
     * @return if the entity is hidden
     */
    boolean isHidden();

    /**
     * Sets if the entity should be hidden.
     *
     * @param hidden if the entity is hidden.
     */
    void setHidden(boolean hidden);

    Optional<Object> getEntitySetting(EntitySetting entitySetting);

    void setEntitySetting(EntitySetting entitySetting, Object value);

    /**
     * Sets the tool lead-in in percent of the tool diameter (ie: 100 for 100%)
     *
     * @param value the percentage value
     */
    void setLeadInPercent(int value);

    /**
     * Returns the tool lead-in in percent of the tool diameter (ie: 100 for 100%)
     *
     * @return the tool lead-in
     */
    int getLeadInPercent();


    /**
     * Sets the tool lead-out in percent of the tool diameter (ie: 100 for 100%)
     *
     * @param value the percentage value
     */
    void setLeadOutPercent(int value);

    /**
     * Returns the tool lead-out in percent of the tool diameter (ie: 100 for 100%)
     *
     * @return the tool lead-out
     */
    int getLeadOutPercent();

    /**
     * If value is false then the cuttable will not be included in the exported g-code
     *
     * @param value the percentage value
     */
    void setIncludeInExport(boolean value);

    /**
     * Returns true if true then this cuttable will be included in the exported G-Code
     *
     * @return true if true then this cuttable will be included in the exported G-Code
     */
    boolean getIncludeInExport(); 
}
