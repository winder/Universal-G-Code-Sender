/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.model.Axis;
import javafx.scene.control.ContentDisplay;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The buttons that can trigger events in the JogPane
 *
 * @author Joacim Breiler
 */
public enum JogButtonEnum {
    BUTTON_XPOS(1, 0, 0, "icons/xpos.svg", "X+", "Jog X+", Collections.singletonList(Axis.X), ContentDisplay.RIGHT),
    BUTTON_XNEG(-1, 0, 0, "icons/xneg.svg", "X-", "Jog X-",Collections.singletonList(Axis.X), ContentDisplay.LEFT),
    BUTTON_YPOS(0, 1, 0, "icons/ypos.svg", "Y+",  "Jog Y+",Collections.singletonList(Axis.Y), ContentDisplay.TOP),
    BUTTON_YNEG(0, -1, 0, "icons/yneg.svg", "Y-", "Jog Y-",Collections.singletonList(Axis.Y), ContentDisplay.BOTTOM),
    BUTTON_ZPOS(0, 0, 1, "icons/zpos.svg", "Z+", "Jog Z+",Collections.singletonList(Axis.Z), ContentDisplay.TOP),
    BUTTON_ZNEG(0, 0, -1, "icons/zneg.svg", "Z-", "Jog Z-",Collections.singletonList(Axis.Z), ContentDisplay.BOTTOM),
    BUTTON_DIAG_XNEG_YNEG(-1, -1, 0, "icons/diag-xneg-yneg.svg", null, "Jog X- Y-", Arrays.asList(Axis.X, Axis.Y), ContentDisplay.CENTER),
    BUTTON_DIAG_XNEG_YPOS(-1, 1, 0, "icons/diag-xneg-ypos.svg", null, "Jog X- Y+", Arrays.asList(Axis.X, Axis.Y), ContentDisplay.CENTER),
    BUTTON_DIAG_XPOS_YNEG(1, -1, 0, "icons/diag-xpos-yneg.svg", null, "Jog X+ Y-", Arrays.asList(Axis.X, Axis.Y), ContentDisplay.CENTER),
    BUTTON_DIAG_XPOS_YPOS(1, 1, 0, "icons/diag-xpos-ypos.svg", null, "Jog X+ Y+", Arrays.asList(Axis.X, Axis.Y), ContentDisplay.CENTER),
    BUTTON_APOS(0, 0, 0, 1, 0, 0, null, "A+", "Jog A+",Collections.singletonList(Axis.A), ContentDisplay.CENTER),
    BUTTON_ANEG(0, 0, 0, -1, 0, 0, null, "A-",  "Jog A-",Collections.singletonList(Axis.A), ContentDisplay.CENTER),
    BUTTON_BPOS(0, 0, 0, 0, 1, 0, null, "B+", "Jog B+",Collections.singletonList(Axis.B), ContentDisplay.CENTER),
    BUTTON_BNEG(0, 0, 0, 0, -1, 0, null, "B-",  "Jog B-",Collections.singletonList(Axis.B), ContentDisplay.CENTER),
    BUTTON_CPOS(0, 0, 0, 0, 0, 1, null, "C+", "Jog C+", Collections.singletonList(Axis.C), ContentDisplay.CENTER),
    BUTTON_CNEG(0, 0, 0, 0, 0, -1, null, "C-", "Jog C-",Collections.singletonList(Axis.C), ContentDisplay.CENTER),
    BUTTON_CANCEL(0, 0, 0, 0, 0, 0, null, null, "Jog cancel", Collections.emptyList(), ContentDisplay.CENTER);

    private final int x;
    private final int y;
    private final int z;
    private final int a;
    private final int b;
    private final int c;
    private final String iconUrl;
    private final String label;
    private final String title;
    private final List<Axis> axes;
    private final ContentDisplay contentDisplay;

    /**
     * Constructor
     *
     * @param x       - the X direction to jog
     * @param y       - the Y direction to jog
     * @param z       - the Z direction to jog
     * @param iconUrl - a relative resource url
     * @param label    - an optional text to b
     * @param axes    - The axes that this button controls
     */
    JogButtonEnum(int x, int y, int z, String iconUrl, String label, String title, List<Axis> axes, ContentDisplay contentDisplay) {
        this(x, y, z, 0, 0, 0, iconUrl, label, title, axes, contentDisplay);
    }

    /**
     * Constructor
     *
     * @param x       - the X direction to jog
     * @param y       - the Y direction to jog
     * @param z       - the Z direction to jog
     * @param a       - the X direction to jog
     * @param b       - the Y direction to jog
     * @param c       - the Z direction to jog
     * @param iconUrl - a relative resource url
     * @param label   - an optional text to be shown on buttons
     * @param title   - an text to be shown in menus
     * @param axes    - The axes that this button controls
     */
    JogButtonEnum(int x, int y, int z, int a, int b, int c, String iconUrl, String label, String title, List<Axis> axes, ContentDisplay contentDisplay) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = a;
        this.b = b;
        this.c = c;
        this.iconUrl = iconUrl;
        this.label = label;
        this.title = title;
        this.axes = axes;
        this.contentDisplay = contentDisplay;
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

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getC() {
        return c;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getTitle() {
        return title;
    }

    public List<Axis> getAxes() {
        return axes;
    }

    public ContentDisplay getContentDisplay() {
        return contentDisplay;
    }

    public String getLabel() {
        return label;
    }
}
