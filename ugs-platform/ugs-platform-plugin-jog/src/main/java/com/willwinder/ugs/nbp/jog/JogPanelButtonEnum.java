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
 * Buttons that can trigger an event in the jog panel.
 *
 * @author Joacim Breiler
 * @see JogPanelListener
 */
public enum JogPanelButtonEnum {
    BUTTON_XPOS,
    BUTTON_XNEG,
    BUTTON_YPOS,
    BUTTON_YNEG,
    BUTTON_ZPOS,
    BUTTON_ZNEG,
    BUTTON_DIAG_XNEG_YNEG,
    BUTTON_DIAG_XNEG_YPOS,
    BUTTON_DIAG_XPOS_YNEG,
    BUTTON_DIAG_XPOS_YPOS,
    BUTTON_TOGGLE_UNIT,
    BUTTON_FEED_INC,
    BUTTON_FEED_DEC,
    BUTTON_STEP_INC,
    BUTTON_STEP_DEC
}
