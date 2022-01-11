/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import java.awt.*;

/**
 * @author Joacim Breiler
 */
public class Colors {


    private Colors() {
        throw new IllegalStateException("Utility class");
    }

    public static final Color SHAPE_HINT = new Color(190, 190, 190);
    public static final Color SHAPE_OUTLINE = new Color(122, 161, 228);
    public static final Color CONTROL_BORDER = new Color(122, 161, 228);
    public static final Color CONTROL_HANDLE = Color.GRAY;
    public static final Color BACKGROUND = new Color(246, 246, 246);
    public static final Color CURSOR = new Color(246, 132, 38);

}
