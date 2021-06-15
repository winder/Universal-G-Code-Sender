package com.willwinder.ugs.nbp.designer.gcode;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gcode.path.*;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SimpleGcodeRouter {

    public static final int QUAD_SEGMENTS = 10;
    public static final int CUBIC_SEGMENTS = 10;

    /**
     * A position near the material to plunge to
     */
    private Coordinate nearZ;

    /**
     * The feed rate to move tool in material
     */
    private int feedRate;

    /**
     * A plunge feed for moving in Z-axis into the material
     */
    private int plungeRate;

    /**
     * If debug comments should be made
     */
    private boolean debug = true;

    public SimpleGcodeRouter() {
        nearZ = new NumericCoordinate(null, null, 1d);
        plungeRate = 400;
        feedRate = 1000;
    }

    /**
     * Find the next non-rapid motion command.
     *
     * @param list
     * @param from
     * @return
     */
    static private Segment findNext(List<Segment> list, int from) {
        Iterator<Segment> ii = list.listIterator(from);
        while (ii.hasNext()) {
            Segment s = ii.next();
            if (s.type != SegmentType.MOVE && s.type != SegmentType.SEAM)
                return s;
        }
        return null;
    }

    public GcodePath toPath(Entity shape, AffineTransform affineTransform) {
        GcodePath path = new GcodePath();
        PathIterator pathIterator = shape.getShape().getPathIterator(affineTransform);

        double[] segment = new double[8];
        Point2D currentPoint = new Point2D.Double();
        while (!pathIterator.isDone()) {
            Arrays.fill(segment, 0d);
            int type = pathIterator.currentSegment(segment);
            switch (type) {
                case PathIterator.SEG_MOVETO: {
                    currentPoint.setLocation(segment[0], segment[1]);
                    NumericCoordinate move = new NumericCoordinate(segment[0], segment[1], 0d);
                    path.addSegment(SegmentType.MOVE, move);
                    break;
                }
                case PathIterator.SEG_LINETO:
                    currentPoint.setLocation(segment[0], segment[1]);
                    NumericCoordinate line = new NumericCoordinate(segment[0], segment[1], 0d);
                    path.addSegment(SegmentType.LINE, line);
                    break;
                case PathIterator.SEG_QUADTO: {
                    Point2D controlPoint1 = new Point2D.Double(segment[0], segment[1]);
                    Point2D destination = new Point2D.Double(segment[2], segment[3]);
                    List<Point2D> points = MathTools.quadraticBezier(currentPoint, destination, controlPoint1, QUAD_SEGMENTS);

                    createLinesFromPoints(path, points);
                    currentPoint = destination;
                    break;
                }
                case PathIterator.SEG_CUBICTO: {
                    Point2D controlPoint1 = new Point2D.Double(segment[0], segment[1]);
                    Point2D controlPoint2 = new Point2D.Double(segment[2], segment[3]);
                    Point2D destination = new Point2D.Double(segment[4], segment[5]);
                    List<Point2D> points = MathTools.cubicBezier(currentPoint, destination, controlPoint1, controlPoint2, CUBIC_SEGMENTS);

                    createLinesFromPoints(path, points);
                    currentPoint = destination;
                    break;
                }

                case PathIterator.SEG_CLOSE: {
                    currentPoint.setLocation(segment[0], segment[1]);
                    NumericCoordinate move = new NumericCoordinate(segment[0], segment[1], 0d);
                    path.addSegment(SegmentType.MOVE, move);
                    path.addSegment(SegmentType.SEAM, move);
                    break;
                }

                default:
                    throw new UnsupportedOperationException();
            }
            pathIterator.next();
        }

        // Wrap in too
        /*switch (shape.getCutSettings().getCutType()) {
            case ON_PATH:
                SimpleOutline cut = new SimpleOutline(path);
                cut.setDepth(shape.getCutSettings().getDepth());
                cut.setDepth(3);
                path = cut.toGcodePath();
                break;
            case INSIDE_PATH:
                break;
            case POCKET:
                break;
            case OUTSIDE_PATH:
                break;
        }*/

        return path;
    }

    private void createLinesFromPoints(GcodePath path, List<Point2D> point2DS) {
        point2DS.forEach(point2D -> {
            NumericCoordinate cubicPoint = new NumericCoordinate(point2D.getX(), point2D.getY(), 0d);
            path.addSegment(SegmentType.LINE, cubicPoint);
        });
    }


    public String toGcode(GcodePath gcodePath) throws IOException {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write(Code.G21.name() + "\n");
        toGcode(stringWriter, gcodePath);
        stringWriter.flush();
        return stringWriter.toString();
    }

    public void toGcode(Writer writer, GcodePath path) throws IOException {


        List<Segment> segments = path.getSegments();

        // Start by moving to safe distance and to the first point
        /*writer.write("(Start working on " + path.getClass().getSimpleName() + " with " + segments.size() + " segments)\n");
        writer.write("G00 ");
        writer.write(safeZ.toGcode() + "\n");
        writer.write("\t" + segments.get(0).point.toGcode() + "\n");
        writer.write("\t" + nearZ.toGcode() + "\n");*/


        runPath(writer, segments);
        writer.flush();
    }

    private boolean isFirstAndLastNodeTheSame(List<Segment> segments) {
        Coordinate firstPoint = segments.get(0).point;
        Coordinate lastPoint = null;
        for (int i = segments.size() - 1; i >= 0; --i) {
            if (segments.get(i).point != null) {
                lastPoint = segments.get(i).point;
                break;
            }
        }
        return firstPoint.equals(lastPoint);
    }

    private void runPath(Writer writer, List<Segment> segments) throws IOException {
        boolean hasFeedRateSet = false;

        // Convert path segments to G codes
        for (ListIterator<Segment> i = segments.listIterator(); i.hasNext(); ) {
            Segment s = i.next();
            switch (s.type) {
                // Seam are just markers.
                case SEAM:
                    continue;
                    // Rapid move
                    // Go to safe Z height, move over the target point and plunge down
                case MOVE:
                    // The rapid over target point is skipped when we do multiple passes
                    // and the end point is the same as the starting point.
                    //goToSafeHeight(writer);
                    writer.write(SegmentType.MOVE.gcode);
                    writer.write(" ");
                    writer.write(s.point.set(Axis.Z, nearZ.get(Axis.Z)).toGcode());
                    writer.write(" (rapid move)\n");
                    hasFeedRateSet = false;

                    break;
                // Dab down
                case POINT:
                    // Move over the target point
                    //goToSafeHeight(writer);
                    /*writer.write("G00 ");
                    writer.write(s.point.undefined(Axis.Z).toGcode());
                    writer.write('\n');*/

                    writer.write(SegmentType.LINE.gcode);
                    writer.write(" ");
                    writer.write("F" + plungeRate + " ");
                    writer.write(s.point.toGcode());

                    /*writer.write("\nG01");
                    writer.write(" F");
                    writer.write(String.valueOf(plungeRate));
                    writer.write(" Z");*/


                    writer.write(" (point)\n");
                    // Retract back to safety (if this is not the last entry)
                    /*if (i.hasNext()) {
                        writer.write("G00 ");
                        writer.write(nearZ.toGcode());
                        writer.write('\n');
                    }*/
                    break;
                // Motion at feed rate
                case LINE:
                case CWARC:
                case CCWARC:
                    writer.write(s.type.gcode);
                    writer.write(' ');

                    if (!hasFeedRateSet) {
                        writer.write("F");
                        writer.write(String.valueOf(feedRate));
                        writer.write(' ');
                        hasFeedRateSet = true;
                    }

                    writer.write(s.point.toGcode());
                    writer.write("\n");
                    break;
                default:
                    throw new RuntimeException("BUG! Unhandled segment type " + s.type);
            }
        }

        goToSafeHeight(writer);
    }

    private void goToSafeHeight(Writer writer) throws IOException {
        writer.write(SegmentType.MOVE.gcode);
        writer.write(" ");
        writer.write(nearZ.toGcode());
        if (debug) {
            writer.write(" (moving to safety point)");
        }
        writer.write("\n\t");
    }

    public void setSafeHeight(double safeHeight) {
        safeHeight = safeHeight;
    }
}
