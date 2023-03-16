/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.eagle;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Point;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.willwinder.ugs.nbp.designer.Utils.parseDouble;

/**
 * Reads Eagle PNP files and imports points with their component value as labels
 *
 * @author Joacim Breiler
 */
public class EaglePnpReader implements DesignReader {
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
            String fileData = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\\R");

            List<Entity> points = pattern.splitAsStream(fileData).map(this::parsePoint).collect(Collectors.toList());
            Design design = new Design();
            design.setEntities(points);
            return Optional.of(design);
        } catch (IOException e) {
            throw new DesignReaderException("Could not parse file", e);
        }
    }

    private Point parsePoint(String line) {
        line = line.replaceAll(" {2}", " ");
        String[] columns = StringUtils.split(line, " ");

        String name = columns[0] + " - " + columns[4] + " " + columns[5];

        Point point = new Point();
        point.setName(name);
        point.setPosition(Anchor.CENTER, new Point2D.Double(parseDouble(columns[1]), parseDouble(columns[2])));
        point.setRotation(parseDouble(columns[3]));
        return point;
    }
}
