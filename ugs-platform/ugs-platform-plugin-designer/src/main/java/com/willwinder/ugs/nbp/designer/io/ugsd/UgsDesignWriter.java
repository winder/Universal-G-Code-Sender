package com.willwinder.ugs.nbp.designer.io.ugsd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.cuttable.*;
import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.io.ugsd.v1.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Writes the design to a stream or file in the latest design format.
 *
 * @author Joacim Breiler
 */
public class UgsDesignWriter implements DesignWriter {
    @Override
    public void write(File file, Controller controller) {
        try {
            write(new FileOutputStream(file), controller);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(OutputStream outputStream, Controller controller) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            DesignV1 design = new DesignV1();
            design.setSettings(convertSettings(controller));

            EntityGroup rootEntity = (EntityGroup) controller.getDrawing().getRootEntity();
            design.setEntities(rootEntity.getChildren().stream().map(this::convertToEntity).collect(Collectors.toList()));
            IOUtils.write(gson.toJson(design), outputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private EntityV1 convertToEntity(Entity entity) {
        EntityV1 result;
        if (entity instanceof EntityGroup) {
            result = parseGroup((EntityGroup) entity);
        } else if (entity instanceof Rectangle) {
            result = parseRectangle(entity);
        } else if (entity instanceof Ellipse) {
            result = parseEllipse(entity);
        } else if (entity instanceof Path) {
            result = parsePath(entity);
        } else if (entity instanceof Text) {
            result = parseText((Text) entity);
        } else {
            return null;
        }

        if (StringUtils.isNotEmpty(entity.getName())) {
            result.setName(entity.getName());
        }

        if (entity instanceof Cuttable && result instanceof CuttableEntityV1) {
            ((CuttableEntityV1) result).setStartDepth(((Cuttable) entity).getStartDepth());
            ((CuttableEntityV1) result).setCutDepth(((Cuttable) entity).getTargetDepth());
            ((CuttableEntityV1) result).setCutType(CutTypeV1.fromCutType(((Cuttable) entity).getCutType()));
        }
        return result;
    }

    private EntityV1 parseText(Text entity) {
        EntityTextV1 text = new EntityTextV1();
        text.setTransform(entity.getTransform());
        text.setText(entity.getText());
        text.setFontName(entity.getFontFamily());
        return text;
    }

    private EntityV1 parsePath(Entity entity) {
        EntityPathV1 path = new EntityPathV1();
        path.setTransform(entity.getTransform());
        path.setSegments(convertPathToSegments(entity));
        return path;
    }

    private EntityV1 parseEllipse(Entity entity) {
        EntityEllipseV1 ellipse = new EntityEllipseV1();
        ellipse.setTransform(entity.getTransform());
        return ellipse;
    }

    private EntityV1 parseRectangle(Entity entity) {
        EntityRectangleV1 rectangle = new EntityRectangleV1();
        rectangle.setTransform(entity.getTransform());
        return rectangle;
    }

    private EntityV1 parseGroup(EntityGroup entity) {
        EntityGroupV1 group = new EntityGroupV1();
        group.setChildren(entity.getChildren().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .map(this::convertToEntity)
                .collect(Collectors.toList()));
        return group;
    }

    private List<EntityPathSegmentV1> convertPathToSegments(Entity entity) {
        List<EntityPathSegmentV1> segments = new ArrayList<>();
        Shape shape = entity.getRelativeShape();
        PathIterator pathIterator = shape.getPathIterator(new AffineTransform());
        double[] coordinates = new double[8];
        while (!pathIterator.isDone()) {
            Arrays.fill(coordinates, 0);
            int segmentType = pathIterator.currentSegment(coordinates);
            segments.add(convertCoordinatesToPathSegment(segmentType, coordinates));
            pathIterator.next();
        }
        return segments;
    }

    private SettingsV1 convertSettings(Controller controller) {
        SettingsV1 settings = new SettingsV1();
        settings.setFeedSpeed(controller.getSettings().getFeedSpeed());
        settings.setDepthPerPass(controller.getSettings().getDepthPerPass());
        settings.setStockThickness(controller.getSettings().getStockThickness());
        settings.setPlungeSpeed(controller.getSettings().getPlungeSpeed());
        settings.setPreferredUnits(controller.getSettings().getPreferredUnits());
        settings.setSafeHeight(controller.getSettings().getSafeHeight());
        settings.setToolDiameter(controller.getSettings().getToolDiameter());
        settings.setToolStepOver(controller.getSettings().getToolStepOver());
        return settings;
    }

    private EntityPathSegmentV1 convertCoordinatesToPathSegment(int segmentType, double[] coordinates) {
        EntityPathSegmentV1 segment = new EntityPathSegmentV1();
        List<Double[]> coordinateList = new ArrayList<>();

        if (segmentType == PathIterator.SEG_CUBICTO) {
            segment.setType(EntityPathTypeV1.CUBIC_TO);
            coordinateList.add(new Double[]{coordinates[0], coordinates[1]});
            coordinateList.add(new Double[]{coordinates[2], coordinates[3]});
            coordinateList.add(new Double[]{coordinates[4], coordinates[5]});
        } else if (segmentType == PathIterator.SEG_QUADTO) {
            segment.setType(EntityPathTypeV1.QUAD_TO);
            coordinateList.add(new Double[]{coordinates[0], coordinates[1]});
            coordinateList.add(new Double[]{coordinates[2], coordinates[3]});
        } else if (segmentType == PathIterator.SEG_LINETO) {
            segment.setType(EntityPathTypeV1.LINE_TO);
            coordinateList.add(new Double[]{coordinates[0], coordinates[1]});
        } else if (segmentType == PathIterator.SEG_MOVETO) {
            segment.setType(EntityPathTypeV1.MOVE_TO);
            coordinateList.add(new Double[]{coordinates[0], coordinates[1]});
        } else {
            segment.setType(EntityPathTypeV1.CLOSE);
        }
        segment.setCoordinates(coordinateList);
        return segment;
    }

    public String serialize(List<Entity> entities) {
        Gson gson = new Gson();
        return gson.toJson(entities.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList()));
    }
}
