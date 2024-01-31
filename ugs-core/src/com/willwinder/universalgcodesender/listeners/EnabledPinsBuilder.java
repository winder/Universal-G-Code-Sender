/*
    Copyright 2013-2024 Will Winder

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

public class EnabledPinsBuilder {
    private boolean x = false;
    private boolean y = false;
    private boolean z = false;
    private boolean a = false;
    private boolean b = false;
    private boolean c = false;
    private boolean probe = false;
    private boolean door = false;
    private boolean hold = false;
    private boolean softReset = false;
    private boolean cycleStart = false;

    public EnabledPinsBuilder setX(boolean x) {
        this.x = x;
        return this;
    }

    public EnabledPinsBuilder setY(boolean y) {
        this.y = y;
        return this;
    }

    public EnabledPinsBuilder setZ(boolean z) {
        this.z = z;
        return this;
    }

    public EnabledPinsBuilder setA(boolean a) {
        this.a = a;
        return this;
    }

    public EnabledPinsBuilder setB(boolean b) {
        this.b = b;
        return this;
    }

    public EnabledPinsBuilder setC(boolean c) {
        this.c = c;
        return this;
    }

    public EnabledPinsBuilder setProbe(boolean probe) {
        this.probe = probe;
        return this;
    }

    public EnabledPinsBuilder setDoor(boolean door) {
        this.door = door;
        return this;
    }

    public EnabledPinsBuilder setHold(boolean hold) {
        this.hold = hold;
        return this;
    }

    public EnabledPinsBuilder setSoftReset(boolean softReset) {
        this.softReset = softReset;
        return this;
    }

    public EnabledPinsBuilder setCycleStart(boolean cycleStart) {
        this.cycleStart = cycleStart;
        return this;
    }

    public EnabledPins createEnabledPins() {
        return new EnabledPins(x, y, z, a, b, c, probe, door, hold, softReset, cycleStart);
    }
}