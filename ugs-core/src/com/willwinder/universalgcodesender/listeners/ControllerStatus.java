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
    private final Position workCoordinateOffset;
    private final Double feed;
    private final OverridePercents overrides;

    /**
     * Baseline constructor. This data should always be present. Represents the
     * controller status.
     * @param state controller state, i.e. idle/hold/running
     * @param machineCoord controller machine coordinates
     * @param workCoord controller work coordinates
     */
    public ControllerStatus(String state, Position machineCoord, Position workCoord) {
        this(state, machineCoord, workCoord, null, null, null);
    }

    /**
     * Additional parameters
     * @param state
     * @param machineCoord
     * @param workCoord 
     */
    public ControllerStatus(String state, Position machineCoord, Position workCoord, 
            Double feed, OverridePercents overrides, Position workCoordinateOffset) {
        this.state = state;
        this.machineCoord = machineCoord;
        this.workCoord = workCoord;
        this.workCoordinateOffset = workCoordinateOffset;
        this.feed = feed;
        this.overrides = overrides;
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

    public Position getWorkCoordinateOffset() {
        return workCoordinateOffset;
    }

    public Double getFeed() {
        return feed;
    }

    public OverridePercents getOverrides() {
        return overrides;
    }

    public static class OverridePercents {
        final public int feed;
        final public int rapid;
        final public int spindle;
        public OverridePercents(int f, int r, int s) {
            feed = f;
            rapid = r;
            spindle = s;
        }
    }
}