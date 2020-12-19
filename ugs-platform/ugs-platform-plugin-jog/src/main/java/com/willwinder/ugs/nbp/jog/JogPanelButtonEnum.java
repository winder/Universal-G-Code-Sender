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

import javax.swing.*;

/**
 * The buttons that can trigger events in the {@link JogPanel}
 *
 * @author Joacim Breiler
 */
public enum JogPanelButtonEnum {
    BUTTON_XPOS(1, 0, 0,"icons/xpos.png","X+", SwingConstants.CENTER, SwingConstants.LEFT),
    BUTTON_XNEG(-1, 0, 0,"icons/xneg.png","X-",  SwingConstants.CENTER, SwingConstants.RIGHT),
    BUTTON_YPOS(0, 1, 0,"icons/ypos.png","Y+", SwingConstants.BOTTOM, SwingConstants.CENTER),
    BUTTON_YNEG(0, -1, 0,"icons/yneg.png","Y-", SwingConstants.TOP, SwingConstants.CENTER),
    BUTTON_ZPOS(0, 0, 1,"icons/zpos.png","Z+", SwingConstants.BOTTOM, SwingConstants.CENTER),
    BUTTON_ZNEG(0, 0, -1,"icons/zneg.png","Z-", SwingConstants.TOP, SwingConstants.CENTER),
    BUTTON_DIAG_XNEG_YNEG(-1, -1, 0, "icons/diag-xneg-yneg.png", null, SwingConstants.TOP, SwingConstants.CENTER),
    BUTTON_DIAG_XNEG_YPOS(-1, 1, 0, "icons/diag-xneg-ypos.png", null, SwingConstants.TOP, SwingConstants.CENTER),
    BUTTON_DIAG_XPOS_YNEG(1, -1, 0, "icons/diag-xpos-yneg.png", null,  SwingConstants.TOP, SwingConstants.CENTER),
    BUTTON_DIAG_XPOS_YPOS(1, 1, 0, "icons/diag-xpos-ypos.png", null, SwingConstants.TOP, SwingConstants.CENTER);

    private final int x;
    private final int y;
    private final int z;
    private final String iconUrl;
    private final String text;
    private final Integer verticalAligment;
    private final Integer horisontalAligment;

    /**
     * Constructor
     *
     * @param x - the X direction to jog
     * @param y - the Y direction to jog
     * @param z - the Z direction to jog
     * @param iconUrl - a relative resource url
     * @param text - an optional text to b
     * @param verticalAligment   Sets the vertical position of the text relative to the icon
     *                           and can have one of the following values
     *                           <ul>
     *                           <li>{@code SwingConstants.CENTER} (the default)
     *                           <li>{@code SwingConstants.TOP}
     *                           <li>{@code SwingConstants.BOTTOM}
     *                           </ul>
     * @param horisontalAligment Sets the horizontal position of the text relative to the
     *                           icon and can have one of the following values:
     *                           <ul>
     *                           <li>{@code SwingConstants.RIGHT}
     *                           <li>{@code SwingConstants.LEFT}
     *                           <li>{@code SwingConstants.CENTER}
     *                           <li>{@code SwingConstants.LEADING}
     *                           <li>{@code SwingConstants.TRAILING} (the default)
     *                           </ul>
     */
    JogPanelButtonEnum(int x, int y, int z, String iconUrl, String text, Integer verticalAligment, Integer horisontalAligment) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.iconUrl = iconUrl;
        this.text = text;
        this.verticalAligment = verticalAligment;
        this.horisontalAligment = horisontalAligment;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public String getText() {
        return text;
    }

    public int getVerticalAligment() {
        return verticalAligment;
    }

    public int getHorisontalAlignment() {
        return horisontalAligment;
    }
}
