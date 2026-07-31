/*
    Copyright 2026 Will Winder

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ToolShapeIconTest {

    @Test
    public void everyShapeShouldHaveALightAndDarkIconResource() {
        Arrays.stream(EndmillShape.values()).forEach(shape -> {
            String path = ToolShapeIcon.getIconPath(shape);

            assertTrue("Missing icon " + path,
                    SvgIconLoader.loadImageIcon(path, 16).isPresent());
            assertTrue("Missing dark icon for " + path,
                    SvgIconLoader.loadImageIcon(path.replace(".svg", "_dark.svg"), 16).isPresent());
        });
    }

    @Test
    public void everyShapeShouldHaveADistinctIcon() {
        long distinctPaths = Arrays.stream(EndmillShape.values())
                .map(ToolShapeIcon::getIconPath)
                .distinct()
                .count();

        assertEquals(distinctPaths, EndmillShape.values().length);
    }
}
