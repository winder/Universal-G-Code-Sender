/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.model.Position;

/**
 *
 * @author wwinder
 */
public class ControllerStatus {
    private final String state;
    private final Position machineCoord;
    private final Position workCoord;

    public ControllerStatus(String state, Position machineCoord, Position workCoord) {
        this.state = state;
        this.machineCoord = machineCoord;
        this.workCoord = workCoord;
    }

    public String getState() {
        return state;
    }

    public Position getMachineCoord() {
        return machineCoord;
    }

    public Position getWorkCoord() {
        return workCoord;
    }
}