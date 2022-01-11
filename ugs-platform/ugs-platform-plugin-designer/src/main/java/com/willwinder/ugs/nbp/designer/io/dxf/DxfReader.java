package com.willwinder.ugs.nbp.designer.io.dxf;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.kabeja.dxf.*;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

public class DxfReader implements DesignReader {

    public static final double MILLIMETERS_PER_INCH = 25.4;

    @Override
    public Optional<Design> read(File file) {
        try {
            return read(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Could not read file", e);
        }
    }

    @Override
    public Optional<Design> read(InputStream resourceAsStream) {
        Parser parser = ParserBuilder.createDefaultParser();
        try {
            parser.parse(resourceAsStream, DXFParser.DEFAULT_ENCODING);
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse file", e);
        }

        DXFDocument doc = parser.getDocument();

        Group group = new Group();
        Iterator layerIterator = doc.getDXFLayerIterator();
        while (layerIterator.hasNext()) {
            DXFLayer layer = (DXFLayer) layerIterator.next();
            Group layerGroup = new Group();
            layerGroup.setName(layer.getName());

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

        group.setPosition(new Point2D.Double(0, 0));

        Design design = new Design();
        List<Entity> entities = new ArrayList<>();
        if (!group.getChildren().isEmpty()) {
            entities.add(group);
        }
        design.setEntities(entities);
        return Optional.of(design);
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
                    path.moveTo(line.getStartPoint().getX() * MILLIMETERS_PER_INCH, line.getStartPoint().getY() * MILLIMETERS_PER_INCH);
                }

                path.lineTo(line.getEndPoint().getX() * MILLIMETERS_PER_INCH, line.getEndPoint().getY() * MILLIMETERS_PER_INCH);
                lastPoint = line.getEndPoint();
            }
            linesGroup.addChild(path);
        }
    }

    private Group parseCircles(DXFLayer layer) {
        Group circlesGroup = new Group();
        List<DXFCircle> circles = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE);
        if (circles != null) {
            for (DXFCircle circle : circles) {
                double radius = circle.getRadius() * MILLIMETERS_PER_INCH;
                Ellipse ellipse = new Ellipse((circle.getCenterPoint().getX() * MILLIMETERS_PER_INCH) - radius, (circle.getCenterPoint().getY() * MILLIMETERS_PER_INCH) - radius);
                ellipse.setSize(new Size(radius * 2, radius * 2));
                circlesGroup.addChild(ellipse);
            }
        }
        return circlesGroup;
    }
}