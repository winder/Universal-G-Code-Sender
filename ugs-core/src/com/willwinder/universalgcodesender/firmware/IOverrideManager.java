/*
    Copyright 2024 Will Winder

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
package com.willwinder.universalgcodesender.firmware;

import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.model.Overrides;

import java.util.List;

/**
 * The override manager is used to apply overrides to the controller
 *
 * @author Joacim Breiler
 */
public interface IOverrideManager {
    /**
     * Sets the new override percents for the given type. If the controller doesn't support given override value
     * the nearest will be used instead. If the override type does not support setting a speed step it is ignored.
     * Which {@link OverrideType} that can be used with this function is determined with the {@link #getSpeedTypes}.
     *
     * @param type  the override value to set
     * @param value new controller override value in percent
     */
    void setSpeedTarget(OverrideType type, int value);

    int getSpeedMax(OverrideType type);

    int getSpeedMin(OverrideType type);

    int getSpeedStep(OverrideType type);

    void sendOverrideCommand(Overrides command);

    int getSpeedDefault(OverrideType type);

    /**
     * Get the target speed for the given override type. Which {@link OverrideType} that can be used with this
     * function is determined with the {@link #getSpeedTypes}.
     *
     * @param type the override type to get the target speed for.
     * @return the target speed in percent
     */
    int getSpeedTargetValue(OverrideType type);

    /**
     * Returns true if the changes to be made with the override manager has settled and are done.
     *
     * @return true if the changes are done
     */
    boolean hasSettled();

    /**
     * Returns the override types that can be set with speed settings
     *
     * @return a list of override speed types
     */
    List<OverrideType> getSpeedTypes();

    /**
     * Returns the override types that can be toggled
     *
     * @return a list of toggleable override types
     */
    List<OverrideType> getToggleTypes();

    /**
     * Returns true when override functions are available
     *
     * @return true when it is available and can be used to override behaviour on the controller
     */
    boolean isAvailable();

    /**
     * Toggles the given override. Which {@link OverrideType} that can be used with this function is determined
     * with the {@link #getToggleTypes()}.
     *
     * @param type the
     */
    void toggle(OverrideType type);

    /**
     * Returns if the given override is currently toggled.
     *
     * @param type the override type
     * @return true if the override is active
     */
    boolean isToggled(OverrideType type);
}
