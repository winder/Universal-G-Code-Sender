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
package com.willwinder.ugs.nbp.designer.model.path;

import static com.willwinder.ugs.nbp.designer.model.path.SegmentType.fromPathIteratorType;

import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * An editable variant of the PathIterator
 *
 * @author Joacim Breiler
 */
public final class EditablePath {

    private final List<Segment> segments = new ArrayList<>();

    public List<Segment> getSegments() {
        return segments;
    }

    public static EditablePath fromShape(Shape shape) {
        EditablePath result = new EditablePath();
        PathIterator it = shape.getPathIterator(null);
        double[] coords = new double[6];
        Point2D start = null;
        Point2D last = null;

        while (!it.isDone()) {
            start = last;
            int type = it.currentSegment(coords);
            Point2D[] points = new Point2D[0];
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1])
                    };
                    last = start = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1])
                    };
                    last = new Point2D.Double(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1]), // ctrl
                            new Point2D.Double(coords[2], coords[3])  // end
                    };
                    last = new Point2D.Double(coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    points = new Point2D[]{
                            new Point2D.Double(coords[0], coords[1]), // ctrl
                            new Point2D.Double(coords[2], coords[3]), // ctrl
                            new Point2D.Double(coords[4], coords[5]) // end
                    };
                    last = new Point2D.Double(coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    result.getSegments().add(new Segment(fromPathIteratorType(type), last, last, new Point2D[0]));
                    start = null;
                    break;

                default:
                    points = null;
            }

            if (start != null && points != null) {
                result.getSegments().add(new Segment(fromPathIteratorType(type), start, last, points));
            }
            it.next();
        }

        return result;
    }

    public static Path2D toPath2D(EditablePath editablePath) {
        Path2D path = new Path2D.Double();

        for (Segment seg : editablePath.getSegments()) {
            switch (seg.getType()) {
                case MOVE_TO -> path.moveTo(seg.getPoint(0).getX(), seg.getPoint(0).getY());

                case LINE_TO -> path.lineTo(seg.getPoint(0).getX(), seg.getPoint(0).getY());

                case QUAD_TO -> path.quadTo(
                        seg.getPoint(0).getX(), seg.getPoint(0).getY(),
                        seg.getPoint(1).getX(), seg.getPoint(1).getY()
                );

                case CUBIC_TO -> path.curveTo(
                        seg.getPoint(0).getX(), seg.getPoint(0).getY(),
                        seg.getPoint(1).getX(), seg.getPoint(1).getY(),
                        seg.getPoint(2).getX(), seg.getPoint(2).getY()
                );

                case CLOSE -> path.closePath();
            }
        }

        return path;
    }
}