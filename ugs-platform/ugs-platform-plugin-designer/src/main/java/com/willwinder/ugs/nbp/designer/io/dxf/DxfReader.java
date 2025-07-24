package com.willwinder.ugs.nbp.designer.io.dxf;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import org.kabeja.dxf.Bounds;
import org.kabeja.dxf.DXFArc;
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEllipse;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFSpline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.DXFSplineConverter;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.awt.geom.Arc2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class DxfReader implements DesignReader {

    public static final double MILLIMETERS_PER_INCH = 25.4;
    private final Settings settings;

    public DxfReader(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Optional<Design> read(File file) {
        try {
            return read(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new DesignReaderException("Could not read file", e);
        }
    }

    @Override
    public Optional<Design> read(InputStream resourceAsStream) {
        Parser parser = ParserBuilder.createDefaultParser();
        try {
            parser.parse(resourceAsStream, DXFParser.DEFAULT_ENCODING);
        } catch (ParseException e) {
            throw new DesignReaderException("Could not parse file", e);
        }

        DXFDocument doc = parser.getDocument();

        Group group = new Group();
        Iterator layerIterator = doc.getDXFLayerIterator();
        while (layerIterator.hasNext()) {
            DXFLayer layer = (DXFLayer) layerIterator.next();
            parseAndAddLayerGroup(group, layer);
        }

        Design design = new Design();
        List<Entity> entities = new ArrayList<>();
        if (!group.getChildren().isEmpty()) {
            entities.add(group);
        }
        design.setEntities(entities);
        return Optional.of(design);
    }

    private void parseAndAddLayerGroup(Group group, DXFLayer layer) {
        Group layerGroup = new Group();
        layerGroup.setName(layer.getName());

        Group pointsGroup = parsePoints(layer);
        pointsGroup.setName("Points");
        if (!pointsGroup.getChildren().isEmpty()) {
            layerGroup.addChild(pointsGroup);
        }

        Group circlesGroup = parseCircles(layer);
        circlesGroup.setName("Circles");
        if (!circlesGroup.getChildren().isEmpty()) {
            layerGroup.addChild(circlesGroup);
        }

        Group linesGroup = new Group();
        linesGroup.setName("Lines");
        parseLines(layer, linesGroup);
        if (!linesGroup.getChildren().isEmpty()) {
            layerGroup.addChild(linesGroup);
        }

        Group arcsGroup = new Group();
        arcsGroup.setName("Arcs");
        parseArcs(layer, arcsGroup);
        if (!arcsGroup.getChildren().isEmpty()) {
            layerGroup.addChild(arcsGroup);
        }

        Group polylinesGroup = new Group();
        polylinesGroup.setName("Polyline");
        parsePolylines(layer, polylinesGroup);
        if (!polylinesGroup.getChildren().isEmpty()) {
            layerGroup.addChild(polylinesGroup);
        }

        Group ellipseGroup = new Group();
        ellipseGroup.setName("Ellipse");
        parseEllipses(layer, ellipseGroup);
        if (!ellipseGroup.getChildren().isEmpty()) {
            layerGroup.addChild(ellipseGroup);
        }

        if (!layerGroup.getChildren().isEmpty()) {
            group.addChild(layerGroup);
        }
    }

    private void parseEllipses(DXFLayer layer, Group ellipseGroup) {
        List<DXFEllipse> entities = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_ELLIPSE);
        if (entities == null) {
            return;
        }

        for (DXFEllipse ellipse : entities) {

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
            ellipseGroup.addChild(entity);
        }
    }

    private void parsePolylines(DXFLayer layer, Group polylinesGroup) {
        List<DXFPolyline> polylines = getDxfPolylines(layer);

        if (polylines.isEmpty()) {
            return;
        }

        for (DXFPolyline polyline : polylines) {
            Path path = new Path();
            DXFVertex vertex = polyline.getVertex(0);
            path.moveTo(convertCoordinate(vertex.getX()), convertCoordinate(vertex.getY()));
            for (int i = 1; i < polyline.getVertexCount(); i++) {
                vertex = polyline.getVertex(i);
                path.lineTo(convertCoordinate(vertex.getX()), convertCoordinate(vertex.getY()));
            }
            path.close();

            polylinesGroup.addChild(path);
        }
    }

    private static List<DXFPolyline> getDxfPolylines(DXFLayer layer) {
        List<DXFPolyline> polylines = Optional.ofNullable(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_POLYLINE))
                .orElse(new ArrayList<>());
        polylines.addAll(
                Optional.ofNullable(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_SPLINE)).orElse(new ArrayList<>())
                        .stream()
                        .map(spline -> DXFSplineConverter.toDXFPolyline((DXFSpline) spline))
                        .toList());
        return polylines;
    }

    private void parseArcs(DXFLayer layer, Group arcsGroup) {
        List<DXFArc> arcs = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_ARC);
        if (arcs == null) {
            return;
        }

        for (DXFArc arc : arcs) {
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
            arcsGroup.addChild(path1);
        }
    }

    private void parseLines(DXFLayer layer, Group linesGroup) {
        List<DXFLine> lines = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);
        if (lines != null) {
            Path path = new Path();
            Point lastPoint = null;
            for (DXFLine line : lines) {
                if (lastPoint != null && !lastPoint.equals(line.getStartPoint())) {
                    linesGroup.addChild(path);
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
            linesGroup.addChild(path);
        }
    }

    private double convertCoordinate(double value) {
        if (settings.getPreferredUnits() == UnitUtils.Units.INCH) {
            return value * MILLIMETERS_PER_INCH;
        }
        return value;
    }

    private Group parseCircles(DXFLayer layer) {
        Group circlesGroup = new Group();
        List<DXFCircle> circles = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE);
        if (circles != null) {
            for (DXFCircle circle : circles) {
                Bounds circleBound = circle.getBounds();
                double centerX =
                        (circleBound.getMaximumX() - circleBound.getMinimumX()) / 2 + circleBound.getMinimumX();
                double centerY =
                        (circleBound.getMaximumY() - circleBound.getMinimumY()) / 2 + circleBound.getMinimumY();
                double radius = convertCoordinate(circle.getRadius());
                Ellipse ellipse = new Ellipse(convertCoordinate(centerX) - radius, convertCoordinate(centerY) - radius);
                ellipse.setSize(new Size(radius * 2, radius * 2));
                circlesGroup.addChild(ellipse);
            }
        }
        return circlesGroup;
    }

    private Group parsePoints(DXFLayer layer) {
        Group pointsGroup = new Group();
        List<DXFPoint> points = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_POINT);
        if (points != null) {
            for (DXFPoint point : points) {
                pointsGroup.addChild(
                        new com.willwinder.ugs.nbp.designer.entities.cuttable.Point(convertCoordinate(point.getX()),
                                convertCoordinate(point.getY())));
            }
        }
        return pointsGroup;
    }
}
