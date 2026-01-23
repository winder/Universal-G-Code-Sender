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
package com.willwinder.ugs.nbp.designer.io.gerber;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GerberReaderTest {

    public static final double POINT_EQUAL_TOLERANCE = 1.0E-4;

    @Test
    public void readCenterLine() {
        // Use a very small aperture (0.05mm) which is <= CENTER_LINE_THRESHOLD_MM
        String data = """
                %LPD*%
                %ADD10C,0.05000*%
                %FSLAX46Y46*%
                D10*
                X000000Y000000D02*
                X1000000Y1000000D01*
                """;
        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        // One entity for "Copper" (empty) and one "Lines" group
        assertEquals(2, design.getEntities().size());

        Entity linesGroup = design.getEntities().stream()
                .filter(e -> "Lines".equals(e.getName()))
                .findFirst()
                .orElseThrow();

        assertTrue(linesGroup instanceof Group);
        Group group = (Group) linesGroup;

        // Should contain one path representing the center line
        assertEquals(1, group.getChildren().size());
        Entity path = group.getChildren().get(0);
        assertTrue(path instanceof Path);

        // Verify bounds of the line from (0,0) to (1,1)
        assertEquals(1.0, path.getBounds().getWidth(), 0.001);
        assertEquals(1.0, path.getBounds().getHeight(), 0.001);
    }

    @Test
    public void readLine() {
        String data = """
                %LPD*%
                %ADD10C,1.00000*%
                %FSLAX46Y46*%
                D10*
                X5080000Y3810000D02*
                X8890000Y3810000D01*
                """;
        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();
        assertEquals(1, design.getEntities().size());

        // Should create a single line with the width 1mm
        Entity entity = design.getEntities().get(0);
        assertEquals(3.81d - 0.5, entity.getBounds().getY(), 0.001);
        assertEquals(5.08d - 0.5, entity.getBounds().getX(), 0.001);
        assertEquals(4.81, entity.getBounds().getWidth(), 0.001);
        assertEquals(1, entity.getBounds().getHeight(), 0.001);
    }

    @Test
    public void readCircleAsDrillHole() {
        String data = """
                %LPD*%
                %ADD10C,2.00000*%
                %FSLAX46Y46*%
                D10*
                X5080000Y3810000D03*
                """;
        GerberReader reader = new GerberReader(true);
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();
        assertEquals(1, design.getEntities().size());

        Entity entity = design.getEntities().get(0);
        assertTrue(entity instanceof Group);
        Group group = (Group) entity;
        assertEquals("Holes", group.getName());
        assertEquals(1, group.getChildren().size());

        entity = group.getChildren().get(0);
        assertTrue(entity instanceof Group);
        Group subGroup = (Group) entity;
        assertEquals("2.0 mm", subGroup.getName());
        assertEquals(1, group.getChildren().size());

        entity = subGroup.getChildren().get(0);
        assertTrue(entity instanceof Ellipse);
        assertEquals(2.0, group.getBounds().getWidth(), 0.001);
        assertEquals(2.0, group.getBounds().getHeight(), 0.001);
    }

    @Test
    public void readFilledPolygonRegion() {
        String data = """
                %LPD*%
                %FSLAX46Y46*%
                G36*
                X000000Y000000D02*
                X100000Y000000D01*
                X100000Y100000D01*
                X000000Y100000D01*
                G37*
                """;
        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        assertEquals(1, design.getEntities().size());
        Entity entity = design.getEntities().get(0);

        List<Point2D> vertices = new ArrayList<>();
        Shape shape = entity.getShape();
        PathIterator iterator = shape.getPathIterator(null);
        double[] coords = new double[6];
        while (!iterator.isDone()) {
            int type = iterator.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                vertices.add(new Point2D.Double(coords[0], coords[1]));
            }
            iterator.next();
        }

        assertEquals(4, vertices.size());
        assertTrue(hasPoint(vertices, 0.0, 0.0));
        assertTrue(hasPoint(vertices, 0.1, 0.0));
        assertTrue(hasPoint(vertices, 0.1, 0.1));
        assertTrue(hasPoint(vertices, 0.0, 0.1));
    }

    private static boolean hasPoint(List<Point2D> points, double x, double y) {
        return points.stream().anyMatch(p ->
                Math.abs(p.getX() - x) <= 1.0E-4 && Math.abs(p.getY() - y) <= POINT_EQUAL_TOLERANCE
        );
    }

    @Test
    public void readRectangle() {
        String data = """
                %LPD*%
                %ADD10R,2.5X1.5*%
                %FSLAX46Y46*%
                D10*
                X5000000Y3000000D03*
                """;
        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        assertEquals(1, design.getEntities().size());
        Entity entity = design.getEntities().get(0);

        assertEquals(2.5, entity.getBounds().getWidth(), 0.001);
        assertEquals(1.5, entity.getBounds().getHeight(), 0.001);

        assertEquals(3.75, entity.getBounds().getX(), 0.001);
        assertEquals(2.25, entity.getBounds().getY(), 0.001);
    }

    @Test
    public void readObroundAperture() {
        String data = """
                %LPD*%
                %ADD10O,3.0X1.0*%
                %FSLAX46Y46*%
                D10*
                X2000000Y2000000D03*
                """;
        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        assertEquals(1, design.getEntities().size());
        Entity entity = design.getEntities().get(0);

        assertEquals(3.0, entity.getBounds().getWidth(), 0.001);
        assertEquals(1.0, entity.getBounds().getHeight(), 0.001);

        assertEquals(0.5, entity.getBounds().getX(), 0.001);
        assertEquals(1.5, entity.getBounds().getY(), 0.001);
    }

    @Test
    public void readRotatedRectangleMacro() {
        // Macro 'ROTRECT': primitive 21 (Center Line/Rectangle)
        // Parameters: exposure(1), width(2), height(3), center_x(4), center_y(5), rotation(6)
        String data = """
                %LPD*%
                %AMMACRO1*21,1,$1,$2,0,0,$3*%
                %ADD10MACRO1,2X1X90*%
                %FSLAX46Y46*%
                D10*
                X000000Y000000D03*
                """;
        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        assertEquals(1, design.getEntities().size());
        Entity entity = design.getEntities().get(0);


        assertEquals(1, entity.getBounds().getWidth(), 0.001);
        assertEquals(2, entity.getBounds().getHeight(), 0.001);

        assertEquals(-0.5, entity.getBounds().getX(), 0.001);
        assertEquals(-1, entity.getBounds().getY(), 0.001);
    }

    @Test
    public void readRoundRectMacro() {
        String data = """
                %LPD*%
                %FSLAX46Y46*%
                %MOMM*%
                %AMRoundRect*
                0 Rectangle with rounded corners*
                0 $1 Rounding radius*
                0 $2 $3 $4 $5 $6 $7 $8 $9 X,Y pos of 4 corners*
                4,1,4,$2,$3,$4,$5,$6,$7,$8,$9,$2,$3,0*
                1,1,$1+$1,$2,$3*
                1,1,$1+$1,$4,$5*
                1,1,$1+$1,$6,$7*
                1,1,$1+$1,$8,$9*
                20,1,$1+$1,$2,$3,$4,$5,0*
                20,1,$1+$1,$4,$5,$6,$7,0*
                20,1,$1+$1,$6,$7,$8,$9,0*
                20,1,$1+$1,$8,$9,$2,$3,0*%
                %ADD10RoundRect,0.243902X0.256098X0.456098X-0.256098X0.456098X-0.256098X-0.456098X0.256098X-0.456098X0*%
                D10*
                X000000Y000000D03*
                """;

        GerberReader reader = new GerberReader();
        Design design = reader.read(IOUtils.toInputStream(data, StandardCharsets.UTF_8)).orElseThrow();

        assertEquals(1, design.getEntities().size());
        Entity e = design.getEntities().get(0);

        assertEquals(1.0, e.getBounds().getWidth(), 0.01);
        assertEquals(1.4, e.getBounds().getHeight(), 0.01);
    }
}
