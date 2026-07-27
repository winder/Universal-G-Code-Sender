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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import org.locationtech.jts.densify.Densifier;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.triangulate.polygon.ConstrainedDelaunayTriangulator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Approximates the medial axis of a polygon — the line running down the middle of the shape, where
 * every point is as far from one edge as it is from the other.
 * <p>
 * The shape is triangulated and the axis is traced through the triangles that came out of it: a
 * triangle resting one side against the outline hands the axis across to its neighbours, a
 * triangle wedged into a corner runs the axis out to the tip of that corner, and a triangle
 * bordering three neighbours joins them together at its middle. Because the triangles are built
 * from the points of the outline, the outline is first resampled at {@code resolution} so that
 * even a plain rectangle has enough points to describe a middle at all.
 *
 * @author Joacim Breiler
 */
public final class MedialAxis {
    /**
     * Resampling a large outline at a fine resolution can produce enormous numbers of points, so
     * the resolution is loosened rather than letting the triangulation grow without bounds.
     */
    private static final int MAX_OUTLINE_POINTS = 20000;

    private MedialAxis() {
    }

    /**
     * Traces the centre lines of a polygonal geometry.
     *
     * @param polygon    the polygonal geometry to find the centre lines of
     * @param resolution how finely the outline should be resampled, in the units of the geometry
     * @return the centre lines, as long as they could be traced through the shape
     */
    public static List<LineString> compute(Geometry polygon, double resolution) {
        if (polygon.isEmpty() || polygon.getArea() <= 0) {
            return List.of();
        }

        double spacing = fitSpacingToOutline(polygon, resolution);
        Geometry resampled = Densifier.densify(polygon, spacing);
        Set<LineSegment> outlineSegments = collectOutlineSegments(resampled);

        Geometry triangles = ConstrainedDelaunayTriangulator.triangulate(resampled);
        List<LineString> chords = new ArrayList<>();
        for (int i = 0; i < triangles.getNumGeometries(); i++) {
            addChords(chords, triangles.getGeometryN(i), outlineSegments);
        }

        return prune(merge(chords), spacing * 2);
    }

    private static double fitSpacingToOutline(Geometry polygon, double resolution) {
        double spacing = Math.max(resolution, 1e-6);
        double outlineLength = polygon.getBoundary().getLength();
        return Math.max(spacing, outlineLength / MAX_OUTLINE_POINTS);
    }

    private static Set<LineSegment> collectOutlineSegments(Geometry resampled) {
        Set<LineSegment> segments = new HashSet<>();
        Geometry boundary = resampled.getBoundary();
        for (int i = 0; i < boundary.getNumGeometries(); i++) {
            Coordinate[] coordinates = boundary.getGeometryN(i).getCoordinates();
            for (int c = 1; c < coordinates.length; c++) {
                segments.add(normalized(coordinates[c - 1], coordinates[c]));
            }
        }
        return segments;
    }

    private static void addChords(List<LineString> chords, Geometry triangle, Set<LineSegment> outlineSegments) {
        Coordinate[] corners = triangle.getCoordinates();
        List<Integer> chordEdges = new ArrayList<>(3);
        for (int edge = 0; edge < 3; edge++) {
            if (!outlineSegments.contains(normalized(corners[edge], corners[(edge + 1) % 3]))) {
                chordEdges.add(edge);
            }
        }

        switch (chordEdges.size()) {
            case 1 -> {
                // Wedged into a corner: run the axis from the corner out to the one open side
                int edge = chordEdges.get(0);
                chords.add(lineBetween(corners[(edge + 2) % 3], midpoint(corners, edge)));
            }
            case 2 -> chords.add(lineBetween(midpoint(corners, chordEdges.get(0)), midpoint(corners, chordEdges.get(1))));
            case 3 -> {
                Coordinate centre = centre(corners);
                chordEdges.forEach(edge -> chords.add(lineBetween(midpoint(corners, edge), centre)));
            }
            default -> {
                // A triangle enclosed by the outline on all sides has no neighbours to hand over to
            }
        }
    }

    private static LineSegment normalized(Coordinate from, Coordinate to) {
        LineSegment segment = new LineSegment(from, to);
        segment.normalize();
        return segment;
    }

    private static Coordinate midpoint(Coordinate[] corners, int edge) {
        return new LineSegment(corners[edge], corners[(edge + 1) % 3]).midPoint();
    }

    private static Coordinate centre(Coordinate[] corners) {
        return new Coordinate((corners[0].x + corners[1].x + corners[2].x) / 3d,
                (corners[0].y + corners[1].y + corners[2].y) / 3d);
    }

    private static LineString lineBetween(Coordinate from, Coordinate to) {
        return ToolPathUtils.GEOMETRY_FACTORY.createLineString(new Coordinate[]{from, to});
    }

    private static List<LineString> merge(Collection<LineString> lines) {
        LineMerger merger = new LineMerger();
        lines.forEach(merger::add);
        return new ArrayList<LineString>(merger.getMergedLineStrings());
    }

    /**
     * Drops the stubs that resampling leaves behind wherever the outline bends, which are the
     * branches too short to describe a feature of the shape. Whatever remains is merged again so
     * that the branches a stub used to separate become a single continuous line.
     */
    private static List<LineString> prune(List<LineString> lines, double minimumBranchLength) {
        Map<Coordinate, Integer> endpointCount = new HashMap<>();
        lines.forEach(line -> {
            endpointCount.merge(line.getCoordinateN(0), 1, Integer::sum);
            endpointCount.merge(line.getCoordinateN(line.getNumPoints() - 1), 1, Integer::sum);
        });

        List<LineString> kept = lines.stream()
                .filter(line -> line.getLength() >= minimumBranchLength || !isBranchEnd(line, endpointCount))
                .toList();

        return kept.size() == lines.size() ? lines : merge(kept);
    }

    private static boolean isBranchEnd(LineString line, Map<Coordinate, Integer> endpointCount) {
        return endpointCount.get(line.getCoordinateN(0)) == 1
                || endpointCount.get(line.getCoordinateN(line.getNumPoints() - 1)) == 1;
    }
}
