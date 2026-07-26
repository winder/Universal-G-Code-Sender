/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.gcode;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.designer.model.Settings;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Pattern;

public class SimpleGcodeRouterArcTest {

    private static final Pattern ARC_WITH_OFFSETS = Pattern.compile("G[23] .*I-?[\\d.]+J-?[\\d.]+");

    @Test
    public void toGcode_shouldWriteArcsWithCenterOffsetsWhenArcFittingIsEnabled() throws IOException {
        Settings settings = new Settings();
        settings.setArcFitting(true);

        String gcode = routeCircle(settings);

        assertTrue(gcode, gcode.lines().anyMatch(line -> ARC_WITH_OFFSETS.matcher(line).find()));
    }

    @Test
    public void toGcode_shouldNotWriteArcsWhenArcFittingIsDisabled() throws IOException {
        Settings settings = new Settings();
        settings.setArcFitting(false);

        String gcode = routeCircle(settings);

        assertTrue(gcode, gcode.lines().noneMatch(line -> line.startsWith("G2 ") || line.startsWith("G3 ")));
    }

    @Test
    public void toGcode_shouldWriteAxisWordsOnEveryArc() throws IOException {
        // Grbl rejects an arc carrying no axis words with "error:26", which happens when an arc
        // ends where it started and the unchanged coordinates get left out
        Settings settings = new Settings();
        settings.setArcFitting(true);

        for (CutType cutType : List.of(CutType.ON_PATH, CutType.INSIDE_PATH, CutType.OUTSIDE_PATH, CutType.POCKET)) {
            for (double diameter : new double[]{5, 10, 40, 200}) {
                String gcode = route(settings, cutType, diameter);
                gcode.lines()
                        .filter(line -> line.startsWith("G2 ") || line.startsWith("G3 "))
                        .forEach(line -> assertTrue(
                                cutType + " ⌀" + diameter + " produced an arc without axis words: " + line,
                                line.contains("X") || line.contains("Y")));
            }
        }
    }

    private static String route(Settings settings, CutType cutType, double diameter) throws IOException {
        Ellipse circle = new Ellipse(0, 0, diameter, diameter);
        circle.setCutType(cutType);
        circle.setTargetDepth(1);

        StringWriter writer = new StringWriter();
        new SimpleGcodeRouter(settings).toGcode(List.of((Cuttable) circle), writer);
        return writer.toString();
    }

    private static String routeCircle(Settings settings) throws IOException {
        Ellipse circle = new Ellipse(0, 0, 40, 40);
        circle.setCutType(CutType.ON_PATH);
        circle.setTargetDepth(1);

        StringWriter writer = new StringWriter();
        new SimpleGcodeRouter(settings).toGcode(List.of((Cuttable) circle), writer);
        return writer.toString();
    }
}
