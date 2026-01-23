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
package com.willwinder.ugs.nbp.designer.io.excellon;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.MathUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Excellon drill reader focused on KiCad output (e.g. .drl).
 * Produces a grouped list of circular Ellipse entities (holes).
 */
public class ExcellonReader implements DesignReader {
    private static final Logger LOGGER = Logger.getLogger(ExcellonReader.class.getName());

    private static final Pattern TOOL_DEF = Pattern.compile("^T(\\d+)C([+-]?[0-9]*\\.?[0-9]+)\\s*$");
    private static final Pattern TOOL_SEL = Pattern.compile("^T(\\d+)\\s*$");
    private static final Pattern HIT_XY = Pattern.compile("X([+-]?[0-9]*\\.?[0-9]+)Y([+-]?[0-9]*\\.?[0-9]+)");

    @Override
    public Optional<Design> read(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return read(in);
        } catch (IOException e) {
            throw new DesignReaderException("Could not read file", e);
        }
    }

    @Override
    public Optional<Design> read(InputStream resourceAsStream) {
        Map<Integer, Double> toolDiametersMm = new HashMap<>();
        Map<String, Group> holeGroups = new HashMap<>();

        UnitUtils.Units units = UnitUtils.Units.MM;
        Integer currentTool = null;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Comments
                if (line.startsWith(";") || line.startsWith("#")) continue;

                // Unit selection
                if (line.equalsIgnoreCase("METRIC")) {
                    units = UnitUtils.Units.MM;
                    continue;
                }
                if (line.equalsIgnoreCase("INCH")) {
                    units = UnitUtils.Units.INCH;
                    continue;
                }

                // Tool definition: TnCdiameter
                Matcher td = TOOL_DEF.matcher(line);
                if (td.matches()) {
                    int tool = Integer.parseInt(td.group(1));
                    double dia = Double.parseDouble(td.group(2));
                    double diaMm = scaleToMm(units, dia);
                    toolDiametersMm.put(tool, diaMm);
                    continue;
                }

                // Tool select: Tn
                Matcher ts = TOOL_SEL.matcher(line);
                if (ts.matches()) {
                    currentTool = Integer.parseInt(ts.group(1));
                    continue;
                }

                // Drill hit: X..Y..
                Matcher hit = HIT_XY.matcher(line);
                if (hit.find()) {
                    if (currentTool == null) {
                        LOGGER.warning("Excellon hit before tool selection: " + line);
                        continue;
                    }
                    Double diaMm = toolDiametersMm.get(currentTool);
                    if (diaMm == null) {
                        LOGGER.warning("Unknown tool T" + currentTool + " for hit: " + line);
                        continue;
                    }

                    double xMm = scaleToMm(units, Double.parseDouble(hit.group(1)));
                    double yMm = scaleToMm(units, Double.parseDouble(hit.group(2)));

                    double r = diaMm / 2.0;
                    Ellipse hole = new Ellipse(xMm - r, yMm - r, diaMm, diaMm);

                    String key = String.valueOf(MathUtils.round(diaMm, 3));
                    Group g = holeGroups.getOrDefault(key, new Group());
                    g.setName(key + " mm");
                    g.addChild(hole);
                    holeGroups.put(key, g);
                }
            }
        } catch (IOException e) {
            throw new DesignReaderException("Could not parse file", e);
        }

        Group holes = new Group();
        holes.setName("Holes");
        holes.addAll(holeGroups.values());

        Design design = new Design();
        design.setEntities(List.of(holes));
        return Optional.of(design);
    }

    private static double scaleToMm(UnitUtils.Units units, double value) {
        return UnitUtils.scaleUnits(units, UnitUtils.Units.MM) * value;
    }
}
