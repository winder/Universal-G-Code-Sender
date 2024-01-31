/*
    Copyright 2016-2024 Will Winder

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
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author wwinder
 */
public class ControllerStatus {
    public static final ControllerStatus EMPTY_CONTROLLER_STATUS = ControllerStatusBuilder.newInstance().build();
    private final Position machineCoord;
    private final Position workCoord;
    private final Position workCoordinateOffset;
    private final Double feedSpeed;
    private final Double spindleSpeed;
    private final OverridePercents overrides;
    private final EnabledPins pins;
    private final AccessoryStates accessoryStates;
    private final ControllerState state;
    private final UnitUtils.Units feedSpeedUnits;
    private final String subState;

    /**
     * Baseline constructor. This data should always be present. Represents the
     * controller status.
     *
     * @param state        controller state, i.e. {@link ControllerState#IDLE}/{@link ControllerState#HOLD}/{@link ControllerState#RUN}
     * @param machineCoord controller machine coordinates
     * @param workCoord    controller work coordinates
     */
    public ControllerStatus(ControllerState state, Position machineCoord, Position workCoord) {
        this(state, "", machineCoord, workCoord, 0d, UnitUtils.Units.MM, 0d, null, null, null, null);
    }

    /**
     * Additional parameters
     */
    public ControllerStatus(ControllerState state, String subState, Position machineCoord,
                            Position workCoord, Double feedSpeed, UnitUtils.Units feedSpeedUnits, Double spindleSpeed,
                            OverridePercents overrides, Position workCoordinateOffset,
                            EnabledPins pins, AccessoryStates states) {
        this.state = state;
        this.subState = subState;
        this.machineCoord = machineCoord;
        this.workCoord = workCoord;
        this.workCoordinateOffset = workCoordinateOffset;
        this.feedSpeed = feedSpeed;
        this.feedSpeedUnits = feedSpeedUnits;
        this.spindleSpeed = spindleSpeed;
        this.overrides = overrides;
        this.pins = pins;
        this.accessoryStates = states;
    }

    public ControllerState getState() {
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

    public Double getFeedSpeed() {
        return feedSpeed;
    }

    public Double getSpindleSpeed() {
        return spindleSpeed;
    }

    public OverridePercents getOverrides() {
        return overrides;
    }

    public EnabledPins getEnabledPins() {
        return pins;
    }

    public AccessoryStates getAccessoryStates() {
        return accessoryStates;
    }

    public UnitUtils.Units getFeedSpeedUnits() {
        return feedSpeedUnits;
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    public String getSubState() {
        return subState;
    }

}
