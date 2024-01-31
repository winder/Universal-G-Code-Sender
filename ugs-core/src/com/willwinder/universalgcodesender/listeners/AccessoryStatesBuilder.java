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

/**
 * A class for storing the accessory states
 */
public class AccessoryStatesBuilder {
    private boolean spindleCW = true;
    private boolean flood;
    private boolean mist;

    public AccessoryStatesBuilder setSpindleCW(boolean spindleCW) {
        this.spindleCW = spindleCW;
        return this;
    }

    public AccessoryStatesBuilder setFlood(boolean flood) {
        this.flood = flood;
        return this;
    }

    public AccessoryStatesBuilder setMist(boolean mist) {
        this.mist = mist;
        return this;
    }

    public AccessoryStates createAccessoryStates() {
        return new AccessoryStates(spindleCW, flood, mist);
    }
}