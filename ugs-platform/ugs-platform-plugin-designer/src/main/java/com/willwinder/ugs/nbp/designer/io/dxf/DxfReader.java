package com.willwinder.ugs.nbp.designer.io.dxf;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.jetbrains.annotations.NotNull;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DxfReader implements DesignReader {

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

        Design design = new Design();
        List<Entity> entities = new ArrayList<>();
        Group group = new Group();
        entities.add(group);
        design.setEntities(entities);

        Iterator layerIterator = doc.getDXFLayerIterator();
        while (layerIterator.hasNext()) {
            DXFLayer layer = (DXFLayer) layerIterator.next();

            Group circlesGroup = parseCircles(layer);
            circlesGroup.setName("Circles " + layer.getName());
            if (!circlesGroup.getChildren().isEmpty()) {
                group.addChild(circlesGroup);
            }

            Group linesGroup = new Group();
            linesGroup.setName("Lines " + layer.getName());
            parseLines(layer, linesGroup);
            if (!linesGroup.getChildren().isEmpty()) {
                group.addChild(linesGroup);
            }
        }

        group.setPosition(new Point2D.Double(0, 0));


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
                    path.moveTo(line.getStartPoint().getX() * 25.4, line.getStartPoint().getY() * 25.4);
                }

                path.lineTo(line.getEndPoint().getX() * 25.4, line.getEndPoint().getY() * 25.4);
                lastPoint = line.getEndPoint();
            }
            linesGroup.addChild(path);
        }
    }

    @NotNull
    private Group parseCircles(DXFLayer layer) {
        Group circlesGroup = new Group();
        List<DXFCircle> circles = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE);
        if (circles != null) {
            for (DXFCircle circle : circles) {
                double radius = circle.getRadius() * 25.4;
                Ellipse ellipse = new Ellipse((circle.getCenterPoint().getX() * 25.4) - radius, (circle.getCenterPoint().getY() * 25.4) - radius);
                ellipse.setSize(new Size(radius * 2, radius * 2));
                circlesGroup.addChild(ellipse);
            }
        }
        return circlesGroup;
    }
}