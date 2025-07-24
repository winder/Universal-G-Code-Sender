package com.willwinder.ugs.nbp.designer.io.dxf;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.utils.Settings;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLayer;
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
    private final DxfConverter converter;

    public DxfReader(Settings settings) {
        this.converter = new DxfConverter(settings);
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

        Group pointsGroup = new Group();
        pointsGroup.addAll(converter.convertPoints(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_POINT)));
        pointsGroup.setName("Points");
        if (!pointsGroup.getChildren().isEmpty()) {
            layerGroup.addChild(pointsGroup);
        }

        Group circlesGroup = new Group();
        circlesGroup.setName("Circles");
        circlesGroup.addAll(converter.convertCircles(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE)));
        if (!circlesGroup.getChildren().isEmpty()) {
            layerGroup.addChild(circlesGroup);
        }

        Group linesGroup = new Group();
        linesGroup.setName("Lines");
        linesGroup.addAll(converter.convertLines(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE)));
        if (!linesGroup.getChildren().isEmpty()) {
            layerGroup.addChild(linesGroup);
        }

        Group arcsGroup = new Group();
        arcsGroup.setName("Arcs");
        arcsGroup.addAll(converter.convertArcs(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_ARC)));
        if (!arcsGroup.getChildren().isEmpty()) {
            layerGroup.addChild(arcsGroup);
        }

        Group polylinesGroup = new Group();
        polylinesGroup.setName("Polyline");
        polylinesGroup.addAll(converter.convert(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_POLYLINE)));
        polylinesGroup.addAll(converter.convert(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_SPLINE)));
        polylinesGroup.addAll(converter.convert(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE)));
        if (!polylinesGroup.getChildren().isEmpty()) {
            layerGroup.addChild(polylinesGroup);
        }

        Group ellipseGroup = new Group();
        ellipseGroup.setName("Ellipse");
        ellipseGroup.addAll(converter.convertEllipses(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_ELLIPSE)));
        if (!ellipseGroup.getChildren().isEmpty()) {
            layerGroup.addChild(ellipseGroup);
        }

        Group insertsGroup = new Group();
        insertsGroup.setName("Blocks");
        insertsGroup.addAll(converter.convertInserts(layer.getDXFEntities(DXFConstants.ENTITY_TYPE_INSERT)));
        if (!insertsGroup.getChildren().isEmpty()) {
            layerGroup.addChild(insertsGroup);
        }

        if (!layerGroup.getChildren().isEmpty()) {
            group.addChild(layerGroup);
        }
    }
}
