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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gcode.path.NumericCoordinate;
import com.willwinder.ugs.nbp.designer.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractCuttable extends AbstractEntity implements Cuttable {
    public static final int QUAD_SEGMENTS = 10;
    public static final int CUBIC_SEGMENTS = 10;

    private CutType cutType = CutType.NONE;
    private double cutDepth;

    protected AbstractCuttable() {
        this(0, 0);
    }

    protected AbstractCuttable(double relativeX, double relativeY) {
        super(relativeX, relativeY);
    }

    @Override
    public CutType getCutType() {
        return cutType;
    }

    @Override
    public void setCutType(CutType cutType) {
        this.cutType = cutType;
        notifyEvent(new EntityEvent(this, EventType.SETTINGS_CHANGED));
    }

    @Override
    public double getCutDepth() {
        return cutDepth;
    }

    @Override
    public void setCutDepth(double cutDepth) {
        this.cutDepth = cutDepth;
    }

    @Override
    public void render(Graphics2D graphics) {
        if (getCutType() != CutType.NONE && getCutDepth() == 0) {
            graphics.setStroke(new BasicStroke(0.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{0.8f, 0.8f}, 0));
            graphics.setColor(Colors.SHAPE_HINT);
            graphics.draw(getShape());
        } else if (getCutType() == CutType.POCKET) {
            graphics.setStroke(new BasicStroke(0.4f));
            graphics.setColor(getCutColor());
            graphics.fill(getShape());
            graphics.draw(getShape());
        } else if (getCutType() == CutType.INSIDE_PATH || getCutType() == CutType.ON_PATH || getCutType() == CutType.OUTSIDE_PATH) {
            graphics.setStroke(new BasicStroke(0.4f));
            graphics.setColor(getCutColor());
            graphics.draw(getShape());
        } else {
            graphics.setStroke(new BasicStroke(0.4f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{0.8f, 0.8f}, 0));
            graphics.setColor(Colors.SHAPE_OUTLINE);
            graphics.draw(getShape());
        }
    }

    private Color getCutColor() {
        int color = Math.max(0, Math.min(255, (int) Math.round(255d * getCutAlpha()) - 25));
        return new Color(color, color, color);
    }

    private double getCutAlpha() {
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        if (getCutDepth() == 0) {
            return 1d;
        }
        return 1d - Math.max(Float.MIN_VALUE, getCutDepth() / controller.getSettings().getStockThickness());
    }

    public GcodePath toGcodePath() {
        GcodePath path = new GcodePath();
        PathIterator pathIterator = getShape().getPathIterator(new AffineTransform());

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
                    java.util.List<Point2D> points = Utils.quadraticBezier(currentPoint, destination, controlPoint1, QUAD_SEGMENTS);

                    createLinesFromPoints(path, points);
                    currentPoint = destination;
                    break;
                }
                case PathIterator.SEG_CUBICTO: {
                    Point2D controlPoint1 = new Point2D.Double(segment[0], segment[1]);
                    Point2D controlPoint2 = new Point2D.Double(segment[2], segment[3]);
                    Point2D destination = new Point2D.Double(segment[4], segment[5]);
                    java.util.List<Point2D> points = Utils.cubicBezier(currentPoint, destination, controlPoint1, controlPoint2, CUBIC_SEGMENTS);

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

        return path;
    }

    private void createLinesFromPoints(GcodePath path, List<Point2D> point2DS) {
        point2DS.forEach(point2D -> {
            NumericCoordinate cubicPoint = new NumericCoordinate(point2D.getX(), point2D.getY());
            path.addSegment(SegmentType.LINE, cubicPoint);
        });
    }
}
