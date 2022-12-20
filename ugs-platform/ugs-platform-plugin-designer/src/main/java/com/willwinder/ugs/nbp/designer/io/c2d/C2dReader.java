/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.c2d;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.io.c2d.model.C2dFile;
import com.willwinder.ugs.nbp.designer.io.c2d.model.C2dPointType;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A design reader for Carbide Create file format.
 * This class will only import the geometries
 *
 * @author Joacim Breiler
 */
public class C2dReader implements DesignReader {
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
        try {
            String designFileContent = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            designFileContent = StringUtils.substringBefore(designFileContent, "MODELV1");
            if (StringUtils.isEmpty(designFileContent)) {
                return Optional.empty();
            }

            Gson gson = new GsonBuilder().create();
            C2dFile c2dFile = gson.fromJson(designFileContent, C2dFile.class);

            List<Entity> entities = new ArrayList<>();
            entities.addAll(parseCurveObjects(c2dFile));
            entities.addAll(parseCircleObjects(c2dFile));
            entities.addAll(parseRectangleObjects(c2dFile));

            Design design = new Design();
            design.setEntities(entities);
            return Optional.of(design);
        } catch (Exception e) {
            throw new DesignReaderException("Couldn't read from stream", e);
        }
    }

    private List<Rectangle> parseRectangleObjects(C2dFile c2dFile) {
        return c2dFile.getRectangleObjects().stream().map(curveObject -> {
            Rectangle rectangle = new Rectangle();
            rectangle.setSize(new Size(curveObject.getWidth(), curveObject.getHeight()));
            rectangle.setPosition(Anchor.CENTER, new Point2D.Double(curveObject.getPosition()[0], curveObject.getPosition()[1]));

            return rectangle;
        }).collect(Collectors.toList());
    }

    private List<Ellipse> parseCircleObjects(C2dFile c2dFile) {
        return c2dFile.getCircleObjects().stream().map(curveObject -> {
            Ellipse ellipse = new Ellipse(curveObject.getPosition()[0], curveObject.getPosition()[1]);
            ellipse.setSize(new Size(curveObject.getRadius() * 2, curveObject.getRadius() * 2));
            return ellipse;
        }).collect(Collectors.toList());
    }

    private Collection<? extends Entity> parseCurveObjects(C2dFile c2dFile) {
        return c2dFile.getCurveObjects().stream().map(curveObject -> {
            Path path = new Path();
            Double[] position = curveObject.getPosition();

            // Make sure the first coordinate is a "Move to"
            if (curveObject.getPointType()[0] != 0) {
                Double[] point = curveObject.getPoints().get(0);
                path.moveTo(position[0] + point[0], position[1] + point[1]);
            }

            for (int index = 0; index < curveObject.getPoints().size(); index++) {
                Double[] point = curveObject.getPoints().get(index);
                if (curveObject.getPointType()[index] == C2dPointType.MOVE_TO.getTypeId()) {
                    path.moveTo(position[0] + point[0], position[1] + point[1]);
                } else if (curveObject.getPointType()[index] == C2dPointType.LINE_TO.getTypeId()) {
                    path.lineTo(position[0] + point[0], position[1] + point[1]);
                } else if (curveObject.getPointType()[index] == C2dPointType.CURVE_TO.getTypeId()) {
                    Double[] cp1 = curveObject.getControlPoints1().get(index);
                    Double[] cp2 = curveObject.getControlPoints2().get(index);
                    path.curveTo(position[0] + cp1[0], position[1] + cp1[1], position[0] + cp2[0], position[1] + cp2[1], position[0] + point[0], position[1] + point[1]);
                } else if (curveObject.getPointType()[index] == C2dPointType.CLOSE.getTypeId()) {
                    path.close();
                } else {
                    throw new DesignReaderException("Unknown point type " + curveObject.getPointType()[index]);
                }
            }

            return path;
        }).collect(Collectors.toList());
    }
}
