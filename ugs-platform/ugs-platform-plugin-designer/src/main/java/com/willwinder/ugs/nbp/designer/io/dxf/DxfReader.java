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
import org.kabeja.dxf.DXFCircle;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPoint;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

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

        if (!layerGroup.getChildren().isEmpty()) {
            group.addChild(layerGroup);
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
                    path.moveTo(convertCoordinate(line.getStartPoint().getX()), convertCoordinate(line.getStartPoint().getY()));
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
                double radius = convertCoordinate(circle.getRadius());
                Ellipse ellipse = new Ellipse(convertCoordinate(circle.getCenterPoint().getX()) - radius, convertCoordinate(circle.getCenterPoint().getY()) - radius);
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
                pointsGroup.addChild(new com.willwinder.ugs.nbp.designer.entities.cuttable.Point(convertCoordinate(point.getX()), convertCoordinate(point.getY())));
            }
        }
        return pointsGroup;
    }
}
