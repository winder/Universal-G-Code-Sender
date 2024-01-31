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
 * A default empty implementation of an override manager which can be used if the
 * controller does not support overrides.
 *
 * @author Joacim Breiler
 */
public class DefaultOverrideManager implements IOverrideManager {
    @Override
    public void setSpeedTarget(OverrideType type, int value) {
        // Not implemented
    }

    @Override
    public boolean hasSettled() {
        return true;
    }

    @Override
    public int getSpeedMax(OverrideType type) {
        return 0;
    }

    @Override
    public int getSpeedMin(OverrideType type) {
        return 0;
    }

    @Override
    public int getSpeedStep(OverrideType type) {
        return 0;
    }

    @Override
    public void sendOverrideCommand(Overrides command) {
        // Not implemented
    }

    @Override
    public int getSpeedDefault(OverrideType overrideType) {
        return 0;
    }

    @Override
    public int getSpeedTargetValue(OverrideType type) {
        return 0;
    }

    @Override
    public List<OverrideType> getSpeedTypes() {
        return List.of();
    }

    @Override
    public List<OverrideType> getToggleTypes() {
        return List.of();
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void toggle(OverrideType overrideType) {
        // Not implemented
    }

    @Override
    public boolean isToggled(OverrideType overrideType) {
        return false;
    }
}
