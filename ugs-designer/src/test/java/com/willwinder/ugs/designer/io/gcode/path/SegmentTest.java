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
package com.willwinder.ugs.designer.io.gcode.path;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import org.junit.Test;

import java.awt.geom.Point2D;

public class SegmentTest {

    @Test
    public void arc_shouldCreateSegmentWithCenter() {
        Segment segment = Segment.arc(SegmentType.CCWARC, position(), new Point2D.Double(1, 2), 500);

        assertEquals(SegmentType.CCWARC, segment.getType());
        assertEquals(new Point2D.Double(1, 2), segment.getArcCenter());
        assertEquals(Integer.valueOf(500), segment.getFeedSpeed());
    }

    @Test
    public void arc_shouldFailForSegmentTypeThatIsNotAnArc() {
        assertThrows(IllegalArgumentException.class,
                () -> Segment.arc(SegmentType.LINE, position(), new Point2D.Double(1, 2), 500));
    }

    @Test
    public void constructor_shouldFailForArcWithoutCenter() {
        assertThrows(IllegalArgumentException.class,
                () -> new Segment(SegmentType.CWARC, position(), null, null, 500, null));
    }

    @Test
    public void constructor_shouldFailForNonArcWithCenter() {
        assertThrows(IllegalArgumentException.class,
                () -> new Segment(SegmentType.LINE, position(), null, null, 500, new Point2D.Double(1, 2)));
    }

    @Test
    public void arc_shouldFailForPointWithoutXAndY() {
        PartialPosition zOnly = PartialPosition.builder(UnitUtils.Units.MM).setZ(-1d).build();

        assertThrows(IllegalArgumentException.class,
                () -> Segment.arc(SegmentType.CWARC, zOnly, new Point2D.Double(1, 2), 500));
    }

    @Test
    public void getArcCenter_shouldReturnNullForNonArcSegment() {
        Segment segment = new Segment(SegmentType.LINE, position());

        assertNull(segment.getArcCenter());
    }

    @Test
    public void getArcCenter_shouldNotBeAffectedByChangesToTheGivenCenter() {
        Point2D.Double center = new Point2D.Double(1, 2);
        Segment segment = Segment.arc(SegmentType.CWARC, position(), center, 500);

        center.setLocation(10, 20);

        assertEquals(new Point2D.Double(1, 2), segment.getArcCenter());
    }

    private static PartialPosition position() {
        return PartialPosition.builder(UnitUtils.Units.MM).setX(0d).setY(10d).build();
    }
}
