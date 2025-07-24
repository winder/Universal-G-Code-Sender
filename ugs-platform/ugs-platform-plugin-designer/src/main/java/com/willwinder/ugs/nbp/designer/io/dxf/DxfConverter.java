package com.willwinder.ugs.nbp.designer.io.dxf;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import static com.willwinder.ugs.nbp.designer.io.dxf.DxfReader.MILLIMETERS_PER_INCH;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFEllipse;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFInsert;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class DxfConverter {

    private final Settings settings;

    public DxfConverter(Settings settings) {
        this.settings = settings;
    }

    public List<Entity> convertDxfIterator(Iterator<DXFEntity> dxfEntitiesIterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(dxfEntitiesIterator, Spliterator.ORDERED), false)
                .flatMap(dxfEntity -> convertDxfEntity(dxfEntity).stream())
                .collect(Collectors.toList());
    }

    public List<Entity> convertDxfEntity(DXFEntity dxfEntity) {
        return switch (dxfEntity.getType()) {
            case DXFConstants.ENTITY_TYPE_ARC -> Collections.singletonList(convertArc((DXFArc) dxfEntity));
            case DXFConstants.ENTITY_TYPE_SPLINE ->
                    Collections.singletonList(convertPolyline(DXFSplineConverter.toDXFPolyline((DXFSpline) dxfEntity)));
            case DXFConstants.ENTITY_TYPE_POLYLINE ->
                    Collections.singletonList(convertPolyline((DXFPolyline) dxfEntity));
            case DXFConstants.ENTITY_TYPE_LWPOLYLINE ->
                    Collections.singletonList(convertPolyline((DXFLWPolyline) dxfEntity));
            case DXFConstants.ENTITY_TYPE_INSERT -> Collections.singletonList(convertInsert((DXFInsert) dxfEntity));
            case DXFConstants.ENTITY_TYPE_ELLIPSE -> Collections.singletonList(convertEllipse((DXFEllipse) dxfEntity));
            case DXFConstants.ENTITY_TYPE_CIRCLE -> Collections.singletonList(convertCircle((DXFCircle) dxfEntity));
            case DXFConstants.ENTITY_TYPE_POINT -> Collections.singletonList(convertPoint((DXFPoint) dxfEntity));
            case DXFConstants.ENTITY_TYPE_LINE -> Collections.singletonList(convertLine((DXFLine) dxfEntity));

            default -> Collections.emptyList();
        };
    }

    public Entity convertLine(DXFLine line) {
        Path path = new Path();
        path.moveTo(convertCoordinate(line.getStartPoint().getX()),
                convertCoordinate(line.getStartPoint().getY()));
        path.lineTo(convertCoordinate(line.getEndPoint().getX()), convertCoordinate(line.getEndPoint().getY()));
        return path;
    }

    public List<Path> convertLines(List<DXFLine> lines) {
        if (lines == null) {
            return Collections.emptyList();
        }
        List<Path> result = new ArrayList<>();
        Path path = new Path();
        Point lastPoint = null;
        for (DXFLine line : lines) {
            if (lastPoint != null && !lastPoint.equals(line.getStartPoint())) {
                result.add(path);
                path = new Path();
                lastPoint = null;
            }

            if (lastPoint == null) {
                path.moveTo(convertCoordinate(line.getStartPoint().getX()),
                        convertCoordinate(line.getStartPoint().getY()));
            }

            path.lineTo(convertCoordinate(line.getEndPoint().getX()), convertCoordinate(line.getEndPoint().getY()));
            lastPoint = line.getEndPoint();
        }
        return result;
    }

    public Entity convertPoint(DXFPoint point) {
        return new com.willwinder.ugs.nbp.designer.entities.cuttable.Point(convertCoordinate(point.getX()),
                convertCoordinate(point.getY()));
    }

    public Entity convertPolyline(DXFPolyline polyline) {
        Path path = new Path();
        DXFVertex vertex = polyline.getVertex(0);
        path.moveTo(convertCoordinate(vertex.getX()), convertCoordinate(vertex.getY()));
        for (int i = 1; i < polyline.getVertexCount(); i++) {
            vertex = polyline.getVertex(i);
            path.lineTo(convertCoordinate(vertex.getX()), convertCoordinate(vertex.getY()));
        }
        path.close();
        return path;
    }

    private double convertCoordinate(double value) {
        if (settings.getPreferredUnits() == UnitUtils.Units.INCH) {
            return value * MILLIMETERS_PER_INCH;
        }
        return value;
    }

    public Entity convertArc(DXFArc arc) {
        double startAngle = -arc.getStartAngle();
        double extent = -arc.getTotalAngle();
        if (startAngle < 0) {
            startAngle += 360;
        }

        Path path1 = new Path();
        Arc2D arc2D = new Arc2D.Double();
        Point center = arc.getCenterPoint();
        arc2D.setArcByCenter(center.getX(), center.getY(), arc.getRadius(), startAngle, extent,
                Arc2D.OPEN);
        path1.append(arc2D);
        return path1;
    }

    public List<Entity> convertArcs(List<DXFArc> arcs) {
        if (arcs == null) {
            return Collections.emptyList();
        }

        return arcs.stream().map(this::convertArc).collect(Collectors.toList());
    }

    public Ellipse convertCircle(DXFCircle circle) {
        Bounds circleBound = circle.getBounds();
        double centerX =
                (circleBound.getMaximumX() - circleBound.getMinimumX()) / 2 + circleBound.getMinimumX();
        double centerY =
                (circleBound.getMaximumY() - circleBound.getMinimumY()) / 2 + circleBound.getMinimumY();
        double radius = convertCoordinate(circle.getRadius());
        Ellipse ellipse = new Ellipse(convertCoordinate(centerX) - radius, convertCoordinate(centerY) - radius);
        ellipse.setSize(new Size(radius * 2, radius * 2));
        return ellipse;
    }

    public List<Entity> convertCircles(List<DXFCircle> circles) {
        if (circles == null) {
            return Collections.emptyList();
        }

        return circles.stream().map(this::convertCircle).collect(Collectors.toList());
    }

    public Ellipse convertEllipse(DXFEllipse ellipse) {
        // Major axis vector
        Vector majorAxis = ellipse.getMajorAxisDirection();
        double majorX = majorAxis.getX();
        double majorY = majorAxis.getY();

        double width = Math.hypot(majorX, majorY);
        double height = width * ellipse.getRatio();

        // Rotation angle (in radians)
        double angle = Math.atan2(majorY, majorX);
        double angleDegrees = Math.toDegrees(angle);

        // To get the upper-left corner, compute the bounding box
        // First get unit vectors for major and minor axes
        double ux = majorX / width;
        double uy = majorY / width;
        double vx = -uy;

        // Corners in local space
        double centerX = ellipse.getCenterPoint().getX();
        double centerY = ellipse.getCenterPoint().getY();
        double[] cornerX = new double[]{
                centerX + ux * width + vx * height,
                centerX + ux * width - vx * height,
                centerX - ux * width + vx * height,
                centerX - ux * width - vx * height
        };

        double[] cornerY = new double[]{
                centerY + uy * width + ux * height,
                centerY + uy * width - ux * height,
                centerY - uy * width + ux * height,
                centerY - uy * width - ux * height
        };

        // Compute bounding box
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < 4; i++) {
            if (cornerX[i] < minX) {
                minX = cornerX[i];
            }
            if (cornerY[i] < minY) {
                minY = cornerY[i];
            }
            if (cornerX[i] > maxX) {
                maxX = cornerX[i];
            }
            if (cornerY[i] > maxY) {
                maxY = cornerY[i];
            }
        }

        Ellipse entity = new Ellipse(minX, minY);
        entity.setSize(new Size(maxX - minX, maxY - minY));
        entity.setRotation(angleDegrees);
        return entity;
    }

    public List<Entity> convertEllipses(List<DXFEllipse> ellipses) {
        if (ellipses == null) {
            return Collections.emptyList();
        }

        return ellipses.stream().map(this::convertEllipse).collect(Collectors.toList());
    }

    public Group convertInsert(DXFInsert insert) {
        DXFBlock dxfBlock = insert.getDXFDocument().getDXFBlock(insert.getBlockID());
        List<Entity> entitiesResult = convertDxfIterator(dxfBlock.getDXFEntitiesIterator());

        Group group = new Group();
        group.addAll(entitiesResult);
        group.setName(dxfBlock.getName());
        group.setPosition(Anchor.BOTTOM_LEFT, new Point2D.Double(insert.getPoint().getX(), insert.getPoint().getY()));
        return group;
    }

    public List<Group> convertInserts(List<DXFInsert> inserts) {
        if (inserts == null) {
            return Collections.emptyList();
        }
        return inserts.stream().map(this::convertInsert).collect(Collectors.toList());
    }

    public List<Entity> convertPoints(List<DXFPoint> points) {
        if (points == null) {
            return Collections.emptyList();
        }

        return points.stream().map(this::convertPoint).collect(Collectors.toList());
    }

    public List<Entity> convert(List<DXFEntity> dxfEntities) {
        if(dxfEntities == null) {
            return Collections.emptyList();
        }
        return convertDxfIterator(dxfEntities.iterator());
    }
}
