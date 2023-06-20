package com.willwinder.ugs.nbp.designer.io.kicad;

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

public class KiCadPosReader implements DesignReader {
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

            List<Entity> points = pattern.splitAsStream(fileData)
                    .filter(line -> !line.startsWith("#"))
                    .map(this::parsePoint)
                    .collect(Collectors.toList());

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

        String name = columns[0] + " - " + columns[1] + " " + columns[2];

        Point point = new Point();
        point.setName(name);
        point.setPosition(Anchor.CENTER, new Point2D.Double(parseDouble(columns[3]), parseDouble(columns[4])));
        point.setRotation(parseDouble(columns[5]));
        return point;
    }
}
