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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.MathUtils;

import java.awt.geom.Path2D;
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

    private UnitUtils.Units units = UnitUtils.Units.MM;
    private Integer currentTool = null;

    // Routing state
    private boolean routing = false;
    private double currentX = 0.0;
    private double currentY = 0.0;
    private Path2D currentRoute = null;
    private Double currentRouteToolDiaMm = null;

    private final Map<Integer, Double> toolDiametersMm = new HashMap<>();
    private final Map<String, Group> holeGroups = new HashMap<>();
    private final Map<String, Group> routeGroups = new HashMap<>();

    private void reset() {
        toolDiametersMm.clear();
        holeGroups.clear();
        routeGroups.clear();
        routing = false;
        currentX = 0.0;
        currentY = 0.0;
        currentRoute = null;
        currentRouteToolDiaMm = null;
        currentTool = null;
        units = UnitUtils.Units.MM;
    }

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
        reset();
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

                if (line.startsWith("M15")) {
                    startRouting();
                    continue;
                }

                if (line.startsWith("M16")) {
                    stopRouting();
                    continue;
                }

                boolean isLinear = line.startsWith("G01") || line.startsWith("G1");
                boolean isRapid = !isLinear && (line.startsWith("G00") || line.startsWith("G0"));

                Matcher hit = HIT_XY.matcher(line);
                if (hit.find()) {
                    double xMm = scaleToMm(units, Double.parseDouble(hit.group(1)));
                    double yMm = scaleToMm(units, Double.parseDouble(hit.group(2)));

                    if (isRapid) {
                        // Rapid move updates the current position, no drawing
                        currentX = xMm;
                        currentY = yMm;

                        // If routing is active, a rapid move should break the current route segment.
                        if (routing && currentRoute != null) {
                            currentRoute.lineTo(currentX, currentY);
                        }
                        continue;
                    }

                    if (isLinear) {
                        // Linear move: if routing is active, draw route centerline; else treat as a drill hit
                        if (routing && currentRoute != null) {
                            currentX = xMm;
                            currentY = yMm;
                            currentRoute.lineTo(currentX, currentY);
                        } else {
                            // Some files may emit hits without explicit drill command; keep old behavior.
                            addHole(holeGroups, toolDiametersMm, currentTool, xMm, yMm, line);
                            currentX = xMm;
                            currentY = yMm;
                        }
                        continue;
                    }

                    // No explicit G-code prefix: treat as drill hit (existing behavior)
                    addHole(holeGroups, toolDiametersMm, currentTool, xMm, yMm, line);
                    currentX = xMm;
                    currentY = yMm;
                }
            }

            // If file ends while routing is active, finalize what we have
            if (routing && currentRoute != null && currentRouteToolDiaMm != null) {
                addRoute(currentRouteToolDiaMm, currentRoute);
            }
        } catch (IOException e) {
            throw new DesignReaderException("Could not parse file", e);
        }

        Group holes = new Group();
        holes.setName("Holes");
        holes.addAll(holeGroups.values());

        Design design = new Design();
        if (routeGroups.isEmpty()) {
            design.setEntities(List.of(holes));
        } else {
            Group routes = new Group();
            routes.setName("Routes");
            routes.addAll(routeGroups.values());
            design.setEntities(List.of(holes, routes));
        }

        return Optional.of(design);
    }

    private void stopRouting() {
        if (routing && currentRoute != null && currentRouteToolDiaMm != null) {
            addRoute(currentRouteToolDiaMm, currentRoute);
        }
        routing = false;
        currentRoute = null;
        currentRouteToolDiaMm = null;
    }

    private void startRouting() {
        if (currentTool == null) {
            LOGGER.warning("M15 (route start) before tool selection");
            routing = false;
            return;
        }
        Double diaMm = toolDiametersMm.get(currentTool);
        if (diaMm == null) {
            LOGGER.warning("M15 (route start) with unknown tool T" + currentTool);
            routing = false;
            return;
        }

        routing = true;
        currentRouteToolDiaMm = diaMm;
        currentRoute = new Path2D.Double();
        currentRoute.moveTo(currentX, currentY);
    }

    private void addHole(Map<String, Group> holeGroups,
                         Map<Integer, Double> toolDiametersMm,
                         Integer currentTool,
                         double xMm,
                         double yMm,
                         String line) {
        if (currentTool == null) {
            LOGGER.warning("Excellon hit before tool selection: " + line);
            return;
        }
        Double diaMm = toolDiametersMm.get(currentTool);
        if (diaMm == null) {
            LOGGER.warning("Unknown tool T" + currentTool + " for hit: " + line);
            return;
        }

        double r = diaMm / 2.0;
        Ellipse hole = new Ellipse(xMm - r, yMm - r, diaMm, diaMm);

        String key = String.valueOf(MathUtils.round(diaMm, 3));
        Group g = holeGroups.getOrDefault(key, new Group());
        g.setName(key + " mm");
        g.addChild(hole);
        holeGroups.put(key, g);
    }

    private void addRoute(double diaMm, Path2D path) {
        if (path == null) return;
        Path routed = new Path(new Path2D.Double(path));

        String key = String.valueOf(MathUtils.round(diaMm, 3));
        Group g = routeGroups.getOrDefault(key, new Group());
        g.setName(key + " mm");
        g.addChild(routed);
        routeGroups.put(key, g);
    }

    private static double scaleToMm(UnitUtils.Units units, double value) {
        return UnitUtils.scaleUnits(units, UnitUtils.Units.MM) * value;
    }
}
