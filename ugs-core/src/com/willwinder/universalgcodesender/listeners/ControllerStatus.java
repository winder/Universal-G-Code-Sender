/*
    Copyright 2016-2021 Will Winder

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

    /**
     * Baseline constructor. This data should always be present. Represents the
     * controller status.
     *
     * @param state        controller state, i.e. {@link ControllerState#IDLE}/{@link ControllerState#HOLD}/{@link ControllerState#RUN}
     * @param machineCoord controller machine coordinates
     * @param workCoord    controller work coordinates
     */
    public ControllerStatus(ControllerState state, Position machineCoord, Position workCoord) {
        this(state, machineCoord, workCoord, 0d, UnitUtils.Units.MM, 0d, null, null, null, null);
    }

    /**
     * Additional parameters
     */
    public ControllerStatus(ControllerState state, Position machineCoord,
                            Position workCoord, Double feedSpeed, UnitUtils.Units feedSpeedUnits, Double spindleSpeed,
                            OverridePercents overrides, Position workCoordinateOffset,
                            EnabledPins pins, AccessoryStates states) {
        this.state = state;
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

    public ControllerStatus() {
        this(ControllerState.DISCONNECTED, Position.ZERO, Position.ZERO);
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

    public static class EnabledPins {
        final public boolean X;
        final public boolean Y;
        final public boolean Z;
        final public boolean A;
        final public boolean B;
        final public boolean C;
        final public boolean Probe;
        final public boolean Door;
        final public boolean Hold;
        final public boolean SoftReset;
        final public boolean CycleStart;

        public EnabledPins(String enabled) {
            String enabledUpper = enabled.toUpperCase();
            X = enabledUpper.contains("X");
            Y = enabledUpper.contains("Y");
            Z = enabledUpper.contains("Z");
            A = enabledUpper.contains("A");
            B = enabledUpper.contains("B");
            C = enabledUpper.contains("C");
            Probe = enabledUpper.contains("P");
            Door = enabledUpper.contains("D");
            Hold = enabledUpper.contains("H");
            SoftReset = enabledUpper.contains("R");
            CycleStart = enabledUpper.contains("S");
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    public static class AccessoryStates {
        final public boolean SpindleCW;
        final public boolean SpindleCCW;
        final public boolean Flood;
        final public boolean Mist;

        public AccessoryStates(String enabled) {
            String enabledUpper = enabled.toUpperCase();
            SpindleCW = enabledUpper.contains("S");
            SpindleCCW = enabledUpper.contains("C");
            Flood = enabledUpper.contains("F");
            Mist = enabledUpper.contains("M");
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    public static class OverridePercents {
        final public int feed;
        final public int rapid;
        final public int spindle;

        public OverridePercents(int feed, int rapid, int spindle) {
            this.feed = feed;
            this.rapid = rapid;
            this.spindle = spindle;
        }

        @Override
        public boolean equals(Object o) {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }
}