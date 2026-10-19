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
package com.willwinder.ugs.designer.utils;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.model.OffsetDirection;
import com.willwinder.ugs.designer.model.OffsetJoinStyle;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates offset outlines of entities, as used by the offset operation in the designer.
 */
public class OffsetUtils {
    private static final Logger LOGGER = Logger.getLogger(OffsetUtils.class.getSimpleName());
    private static final double FLATNESS = 0.1;
    private static final int QUADRANT_SEGMENTS = 16;
    private static final double MITRE_LIMIT = 10.0;

    private OffsetUtils() {
    }

    /**
     * Creates the offset outlines of the given entities. The entities themselves are left untouched.
     *
     * @param entities     the entities to offset
     * @param distance     the offset distance, must be positive to produce any result
     * @param direction    on which side of each shape the offset is drawn
     * @param joinStyle    how corners are joined
     * @param unionResults whether all offset outlines should be merged into a single path
     * @return the offset paths, empty if the distance is not positive or no entity has a shape
     */
    public static List<Path> offset(List<? extends Entity> entities, double distance, OffsetDirection direction, OffsetJoinStyle joinStyle, boolean unionResults) {
        if (distance <= 0) {
            return List.of();
        }

        List<Path> paths = new ArrayList<>();
        for (Entity entity : entities) {
            getEntityShape(entity).ifPresent(shape -> paths.addAll(offsetShape(shape, distance, direction, joinStyle)));
        }

        if (unionResults && paths.size() > 1) {
            return union(paths).map(List::of).orElse(paths);
        }
        return paths;
    }

    private static List<Path> offsetShape(Shape shape, double distance, OffsetDirection direction, OffsetJoinStyle joinStyle) {
        List<Path> paths = new ArrayList<>();
        try {
            Geometry geometry = toGeometry(shape);
            if (geometry.isEmpty()) {
                return paths;
            }

            if (direction == OffsetDirection.OUTWARD || direction == OffsetDirection.BOTH) {
                toPath(buffer(geometry, distance, joinStyle)).ifPresent(paths::add);
            }
            if (direction == OffsetDirection.INWARD || direction == OffsetDirection.BOTH) {
                toPath(buffer(geometry, -distance, joinStyle)).ifPresent(paths::add);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not offset shape", e);
        }
        return paths;
    }

    private static Optional<Path> union(List<Path> paths) {
        try {
            Geometry result = null;
            for (Path path : paths) {
                Geometry geometry = toGeometry(path.getShape());
                if (geometry.isEmpty()) {
                    continue;
                }
                result = result == null ? geometry : result.union(geometry);
            }
            return result == null ? Optional.empty() : toPath(result);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not union offset paths", e);
            return Optional.empty();
        }
    }

    private static Optional<Shape> getEntityShape(Entity entity) {
        Shape shape = entity.getShape();
        if (hasArea(shape)) {
            return Optional.of(shape);
        }
        Shape relativeShape = entity.getRelativeShape();
        if (relativeShape != null && entity.getTransform() != null) {
            Shape transformedShape = entity.getTransform().createTransformedShape(relativeShape);
            if (hasArea(transformedShape)) {
                return Optional.of(transformedShape);
            }
        }
        Rectangle2D bounds = entity.getBounds();
        if (hasArea(bounds)) {
            return Optional.of(new Rectangle2D.Double(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight()));
        }
        return Optional.empty();
    }

    private static boolean hasArea(Shape shape) {
        return shape != null && shape.getBounds2D().getWidth() > 0 && shape.getBounds2D().getHeight() > 0;
    }

    private static Geometry toGeometry(Shape shape) {
        PathIterator pathIterator = shape.getPathIterator(new AffineTransform(), FLATNESS);
        Path2D.Double flattened = new Path2D.Double();
        flattened.append(pathIterator, false);

        Geometry geometry = new ShapeReader(new GeometryFactory()).read(flattened.getPathIterator(new AffineTransform()));
        return geometry.isValid() ? geometry : geometry.buffer(0);
    }

    private static Geometry buffer(Geometry geometry, double distance, OffsetJoinStyle joinStyle) {
        try {
            return BufferOp.bufferOp(geometry, distance, bufferParameters(joinStyle));
        } catch (Exception e) {
            return BufferOp.bufferOp(geometry, distance, bufferParameters(OffsetJoinStyle.ROUND));
        }
    }

    private static BufferParameters bufferParameters(OffsetJoinStyle joinStyle) {
        BufferParameters parameters = new BufferParameters();
        parameters.setEndCapStyle(BufferParameters.CAP_FLAT);
        parameters.setQuadrantSegments(QUADRANT_SEGMENTS);
        parameters.setSingleSided(false);
        switch (joinStyle) {
            case BEVEL -> parameters.setJoinStyle(BufferParameters.JOIN_BEVEL);
            case MITRE -> {
                parameters.setJoinStyle(BufferParameters.JOIN_MITRE);
                parameters.setMitreLimit(MITRE_LIMIT);
            }
            default -> parameters.setJoinStyle(BufferParameters.JOIN_ROUND);
        }
        return parameters;
    }

    private static Optional<Path> toPath(Geometry geometry) {
        if (geometry == null || geometry.isEmpty()) {
            return Optional.empty();
        }
        Path path = new Path();
        path.append(new ShapeWriter().toShape(geometry));
        return Optional.of(path);
    }
}
