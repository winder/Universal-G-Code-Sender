package com.willwinder.universalgcodesender.visualizer;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GcodeViewParseTest {

    @Test
    public void segmentsShouldCarryTheToolSelectedByTheLastTWord() throws Exception {
        GcodeViewParse parser = new GcodeViewParse();
        List<LineSegment> segments = parser.toObjRedux(List.of(
                "G21 G90",
                "G0 X0 Y0 Z5",
                "G1 Z-1 F100",
                "M6 T2",
                "G1 X10",
                "T7",
                "G2 X20 Y0 I5 J0"), 1, 0);

        assertEquals(0, segments.get(0).getToolNumber());
        assertEquals(0, segments.get(1).getToolNumber());
        assertEquals(2, segments.get(2).getToolNumber());
        // The remaining segments belong to the expanded arc that follows the T7 word
        for (LineSegment arc : segments.subList(3, segments.size())) {
            assertEquals(7, arc.getToolNumber());
        }
    }

    @Test
    public void toCartesianShouldKeepTheToolNumber() throws Exception {
        GcodeViewParse parser = new GcodeViewParse();
        List<LineSegment> segments = parser.toObjRedux(List.of("T3", "G1 X10 F100"), 1, 0);
        assertEquals(3, VisualizerUtils.toCartesian(segments.get(0)).getToolNumber());
    }
}
