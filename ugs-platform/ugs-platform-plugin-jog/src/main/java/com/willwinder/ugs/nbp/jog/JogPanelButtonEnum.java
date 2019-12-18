/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.jog;

/**
 * The buttons that can trigger events in the {@link JogPanel}
 *
 * @author Joacim Breiler
 */
public enum JogPanelButtonEnum {
    BUTTON_XPOS(1, 0, 0),
    BUTTON_XNEG(-1, 0, 0),
    BUTTON_YPOS(0, 1, 0),
    BUTTON_YNEG(0, -1, 0),
    BUTTON_ZPOS(0, 0, 1),
    BUTTON_ZNEG(0, 0, -1),
    BUTTON_DIAG_XNEG_YNEG(-1, -1, 0),
    BUTTON_DIAG_XNEG_YPOS(-1, 1, 0),
    BUTTON_DIAG_XPOS_YNEG(1, -1, 0),
    BUTTON_DIAG_XPOS_YPOS(1, 1, 0),
    BUTTON_TOGGLE_UNIT(0, 0, 0),
    BUTTON_LARGER_STEP(0, 0, 0),
    BUTTON_SMALLER_STEP(0, 0, 0);

    private final int x;
    private final int y;
    private final int z;

    JogPanelButtonEnum(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
