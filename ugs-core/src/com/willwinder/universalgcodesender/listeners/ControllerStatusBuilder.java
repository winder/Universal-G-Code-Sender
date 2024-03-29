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

/**
 * A builder for creating controller status
 *
 * @author Joacim Breiler
 */
public class ControllerStatusBuilder {
    private ControllerState state = ControllerState.DISCONNECTED;
    private Position machineCoord = Position.ZERO;
    private Position workCoord = Position.ZERO;
    private Double feedSpeed = 0d;
    private UnitUtils.Units feedSpeedUnits = UnitUtils.Units.MM;
    private Double spindleSpeed = 0d;
    private OverridePercents overrides = OverridePercents.EMTPY_OVERRIDE_PERCENTS;
    private Position workCoordinateOffset = Position.ZERO;
    private EnabledPins pins = EnabledPins.EMPTY_PINS;
    private AccessoryStates states = AccessoryStates.EMPTY_ACCESSORY_STATE;
    private String subState = "";

    public static ControllerStatusBuilder newInstance() {
        return new ControllerStatusBuilder();
    }

    public static ControllerStatusBuilder newInstance(ControllerStatus controllerStatus) {
        ControllerStatusBuilder controllerStatusBuilder = new ControllerStatusBuilder();
        if(controllerStatus != null) {
            controllerStatusBuilder
                .setState(controllerStatus.getState())
                .setSubState(controllerStatus.getSubState())
                .setMachineCoord(controllerStatus.getMachineCoord())
                .setWorkCoord(controllerStatus.getWorkCoord())
                .setFeedSpeed(controllerStatus.getFeedSpeed())
                .setFeedSpeedUnits(controllerStatus.getFeedSpeedUnits())
                .setSpindleSpeed(controllerStatus.getSpindleSpeed())
                .setOverrides(controllerStatus.getOverrides())
                .setWorkCoordinateOffset(controllerStatus.getWorkCoordinateOffset())
                .setPins(controllerStatus.getEnabledPins())
                .setStates(controllerStatus.getAccessoryStates());
        }
        return controllerStatusBuilder;
    }

    private ControllerStatusBuilder setSubState(String subState) {
        this.subState = subState;
        return this;
    }

    public ControllerStatusBuilder setState(ControllerState state) {
        this.state = state;
        return this;
    }

    public ControllerStatusBuilder setMachineCoord(Position machineCoord) {
        this.machineCoord = machineCoord;
        return this;
    }

    public ControllerStatusBuilder setWorkCoord(Position workCoord) {
        this.workCoord = workCoord;
        return this;
    }

    public ControllerStatusBuilder setFeedSpeed(Double feedSpeed) {
        this.feedSpeed = feedSpeed;
        return this;
    }

    public ControllerStatusBuilder setFeedSpeedUnits(UnitUtils.Units feedSpeedUnits) {
        this.feedSpeedUnits = feedSpeedUnits;
        return this;
    }

    public ControllerStatusBuilder setSpindleSpeed(Double spindleSpeed) {
        this.spindleSpeed = spindleSpeed;
        return this;
    }

    public ControllerStatusBuilder setOverrides(OverridePercents overrides) {
        this.overrides = overrides;
        return this;
    }

    public ControllerStatusBuilder setWorkCoordinateOffset(Position workCoordinateOffset) {
        this.workCoordinateOffset = workCoordinateOffset;
        return this;
    }

    public ControllerStatusBuilder setPins(EnabledPins pins) {
        this.pins = pins;
        return this;
    }

    public ControllerStatusBuilder setStates(AccessoryStates states) {
        this.states = states;
        return this;
    }

    public ControllerStatus build() {
        return new ControllerStatus(state, subState, machineCoord, workCoord, feedSpeed, feedSpeedUnits, spindleSpeed, overrides, workCoordinateOffset, pins, states);
    }
}
