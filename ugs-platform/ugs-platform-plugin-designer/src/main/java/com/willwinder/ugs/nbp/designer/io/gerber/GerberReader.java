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
package com.willwinder.ugs.nbp.designer.io.gerber;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Path;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.DesignReaderException;
import com.willwinder.ugs.nbp.designer.io.gerber.expressions.Add;
import com.willwinder.ugs.nbp.designer.io.gerber.expressions.Constant;
import com.willwinder.ugs.nbp.designer.io.gerber.expressions.Expression;
import com.willwinder.ugs.nbp.designer.io.gerber.expressions.Paramameter;
import com.willwinder.ugs.nbp.designer.io.gerber.primitives.CenterLine;
import com.willwinder.ugs.nbp.designer.io.gerber.primitives.Circle;
import com.willwinder.ugs.nbp.designer.io.gerber.primitives.Polygon;
import com.willwinder.ugs.nbp.designer.io.gerber.primitives.Primitive;
import com.willwinder.ugs.nbp.designer.io.gerber.primitives.VectorLine;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.utils.StitchPathUtils;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.MathUtils;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GerberReader implements DesignReader {
    private static final Logger LOGGER = Logger.getLogger(GerberReader.class.getName());

    /**
     * A threshold for when a line should be considered to be one line or should be padded
     */
    private static final double CENTER_LINE_THRESHOLD_MM = 0.1;

    private final State state = new State();
    private final boolean importDrillHoles;

    /**
     * The current name of the macro being read
     */
    private String currentMacroName = null;

    /**
     * Is in a multiline macro definition
     */
    private boolean inMacro = false;

    /**
     * The rows of the current multiline macro
     */
    private final StringBuilder macroBuffer = new StringBuilder();

    public GerberReader() {
        this(false);
    }

    /**
     * @param importDrillHoles import as gerber drill holes
     */
    public GerberReader(boolean importDrillHoles) {
        this.importDrillHoles = importDrillHoles;
    }

    @Override
    public Optional<Design> read(File file) {
        try (InputStream in = new FileInputStream(file)) {
            return read(in);
        } catch (IOException e) {
            throw new DesignReaderException("Failed to read Gerber file", e);
        }
    }

    @Override
    public Optional<Design> read(InputStream inputStream) {
        state.reset();
        parseInputStream(inputStream);

        List<Entity> entities = new ArrayList<>();
        if (importDrillHoles) {
            Group group = new Group();
            group.setName("Holes");
            group.addAll(getDrillHoles());
            entities.add(group);
        } else {
            entities.add(createCopperShape());
            Group group = createLinesGroup();
            if (!group.getChildren().isEmpty()) {
                entities.add(group);
            }
        }

        Design design = new Design();
        design.setEntities(entities);
        return Optional.of(design);
    }

    private Collection<? extends Entity> getDrillHoles() {
        List<Ellipse> holes = state.getShapes().stream()
                .map(GerberEntity::entity)
                .filter(s -> s instanceof Ellipse)
                .map(s -> (Ellipse) s)
                .filter(Ellipse::isCircle)
                .toList();

        Map<String, Group> holeGroups = new HashMap<>();

        for (Ellipse s : holes) {
            String key = String.valueOf(MathUtils.round(s.getSize().getWidth(), 3));
            Group group = holeGroups.getOrDefault(key, new Group());
            group.setName(key + " mm");
            group.addChild(s);
            holeGroups.put(key, group);
        }
        return holeGroups.values();
    }

    private Group createLinesGroup() {
        Group group = new Group();
        group.addAll(StitchPathUtils.stitchEntities(state.getCenterLines().stream().map(s -> (Entity) new Path(s)).toList()));
        group.setName("Lines");
        group.setStartDepth(0.1);
        group.setTargetDepth(0.1);
        group.setCutType(CutType.ON_PATH);
        return group;
    }

    private Path createCopperShape() {
        Area copper = new Area();
        for (GerberEntity s : state.getShapes()) {
            if (s.polarity() == Polarity.DARK) {
                copper.add(new Area(s.entity().getShape()));
            } else {
                copper.subtract(new Area(s.entity().getShape()));
            }
        }

        Path path = new Path();
        path.append(copper);
        path.setName("Copper");
        path.setCutType(CutType.OUTSIDE_PATH);
        path.setStartDepth(0.1);
        path.setTargetDepth(0.1);
        return path;
    }

    private void parseInputStream(InputStream stream) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("%") || inMacro) {
                    parseExtended(line);
                } else {
                    parseStandard(line);
                }
            }
        } catch (IOException e) {
            throw new DesignReaderException("Gerber parse error", e);
        }
    }

    private void parseExtended(String line) {
        if (inMacro) {
            parseMacro(line);
            return;
        }

        if (line.startsWith("%MO")) {
            state.setUnits(line.contains("IN") ? UnitUtils.Units.INCH : UnitUtils.Units.MM);
        } else if (line.startsWith("%FS")) {
            Matcher m = Pattern.compile("X(\\d)(\\d)Y(\\d)(\\d)").matcher(line);
            if (m.find()) {
                state.setIntDigits(Integer.parseInt(m.group(1)));
                state.setDecDigits(Integer.parseInt(m.group(2)));
            }
        } else if (line.startsWith("%ADD")) {
            parseAperature(line);
        } else if (line.startsWith("%TO.N,")) {
            state.setCurrentNet(extractNetName(line));
        } else if (line.startsWith("%LPD")) {
            state.setPolarity(Polarity.DARK);
        } else if (line.startsWith("%LPC")) {
            state.setPolarity(Polarity.CLEAR);
        } else if (line.startsWith("%AM")) {
            parseMacro(line);
        }
    }

    private void parseAperature(String line) {
        // 1️⃣ Standard apertures: C, R, O, P
        Matcher std = Pattern.compile("%ADD(\\d+)([CROP]),(.+)\\*%").matcher(line);
        if (std.matches()) {
            String code = "D" + std.group(1);
            Code shape = Code.fromCode(std.group(2).charAt(0));
            double[] params = Arrays.stream(std.group(3).split("X")).mapToDouble(Double::parseDouble).map(this::scaleToMM).toArray();

            state.getApertures().put(code, new Aperture(code, shape, params));
            return;
        }

        // 2️⃣ Macro apertures
        Matcher macro = Pattern.compile("%ADD(\\d+)([A-Za-z0-9_]+),(.+)\\*%").matcher(line);

        if (macro.matches()) {
            String code = "D" + macro.group(1);
            String macroName = macro.group(2);
            double[] params = Arrays.stream(macro.group(3).split("X")).mapToDouble(Double::parseDouble).map(this::scaleToMM).toArray();

            Macro am = state.getMacros().get(macroName);
            if (am == null) {
                LOGGER.warning("Unknown aperture macro: " + macroName);
                return;
            }

            state.getApertures().put(code, new Aperture(code, am, params));
        }
    }

    private void parseMacro(String line) {
        // Case 1: single-line macro: %AMname*body*%
        if (line.startsWith("%AM") && line.endsWith("*%") && line.indexOf('*', 3) != -1) {
            String body = line.substring(3, line.length() - 2); // strip %AM and *%
            String[] parts = body.split("\\*", 2);

            String name = parts[0];
            String content = parts.length > 1 ? parts[1] : "";

            finalizeMacro(name, content);
            return;
        }

        // Case 2: start of multi-line macro: %AMname*
        if (currentMacroName == null) {
            currentMacroName = line.substring(3, line.length() - 1); // strip %AM and trailing *
            macroBuffer.setLength(0);
            inMacro = true;
            return;
        }

        // Case 3: end of multi-line macro: *%
        if (line.endsWith("*%")) {
            macroBuffer.append(line, 0, line.length() - 2); // strip *%
            finalizeMacro(currentMacroName, macroBuffer.toString());
            currentMacroName = null;
            macroBuffer.setLength(0);
            inMacro = false;
            return;
        }

        // Case 4: middle of multi-line macro
        macroBuffer.append(line);
    }

    private void finalizeMacro(String name, String content) {
        List<Primitive> primitives = new ArrayList<>();

        for (String prim : content.split("\\*")) {
            prim = prim.trim();
            if (prim.isEmpty()) continue;

            String[] p = prim.split(",");

            // 🔒 Guard: primitive opcode must be numeric
            if (!isInteger(p[0])) {
                LOGGER.fine("Skipping non-primitive macro line: " + prim);
                continue;
            }

            int code = Integer.parseInt(p[0]);

            switch (code) {
                case 0 -> {
                    // Comment — ignore
                }

                case 1 -> {
                    // Circle: 1, exposure, diameter, cx, cy
                    if (p.length < 5) break;
                    primitives.add(new Circle(
                            parseExpr(p[2]),
                            parseExpr(p[3]),
                            parseExpr(p[4])
                    ));
                }

                case 4 -> {
                    // Polygon: 4, exposure, vertices, x1,y1,...
                    if (p.length < 6) break;
                    int vertices = Integer.parseInt(p[2]);
                    primitives.add(new Polygon(
                            vertices,
                            Arrays.stream(Arrays.copyOfRange(p, 3, p.length - 1))
                                    .map(this::parseExpr)
                                    .toList()
                    ));
                }

                case 20 -> {
                    // Vector line
                    if (p.length < 8) break;
                    primitives.add(new VectorLine(
                            parseExpr(p[2]),
                            parseExpr(p[3]),
                            parseExpr(p[4]),
                            parseExpr(p[5]),
                            parseExpr(p[6]),
                            parseExpr(p[7])
                    ));
                }

                case 21 -> {
                    // Center line
                    if (p.length < 7) break;
                    primitives.add(new CenterLine(
                            parseExpr(p[2]),
                            parseExpr(p[3]),
                            parseExpr(p[4]),
                            parseExpr(p[5]),
                            parseExpr(p[6])
                    ));
                }

                default -> LOGGER.warning("Unsupported macro primitive: " + code);
            }
        }

        state.getMacros().put(name, new Macro(name, primitives));
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String extractNetName(String line) {
        Pattern NET_ATTR_PATTERN = Pattern.compile("%TO\\.N,([^*]+)\\*%");
        Matcher m = NET_ATTR_PATTERN.matcher(line);
        if (!m.matches()) {
            return null;
        }

        String raw = m.group(1);
        return decodeUnicodeEscapes(raw);
    }

    private String decodeUnicodeEscapes(String s) {
        StringBuilder out = new StringBuilder(s.length());
        int i = 0;

        while (i < s.length()) {
            char c = s.charAt(i);

            if (c == '\\' && i + 5 < s.length() && s.charAt(i + 1) == 'u') {
                String hex = s.substring(i + 2, i + 6);
                try {
                    int code = Integer.parseInt(hex, 16);
                    out.append((char) code);
                    i += 6;
                    continue;
                } catch (NumberFormatException ignored) {
                    // fall through
                }
            }

            out.append(c);
            i++;
        }

        return out.toString();
    }

    private void parseStandard(String block) {
        if (block.contains("G36")) {
            state.setInRegion(true);
            state.getRegionPoints().clear();
        }

        if (block.contains("G37")) {
            finalizeRegion();
            state.setInRegion(false);
        }

        if (block.contains("G01")) state.setInterpolation(Interpolation.LINEAR);
        if (block.contains("G02")) state.setInterpolation(Interpolation.CW_ARC);
        if (block.contains("G03")) state.setInterpolation(Interpolation.CCW_ARC);

        Matcher dSel = Pattern.compile("D(\\d{2,})").matcher(block);
        if (dSel.find()) {
            String code = "D" + dSel.group(1);
            if (state.getApertures().containsKey(code)) {
                state.setCurrentAperture(state.getApertures().get(code));
            }
        }

        Double nx = extractCoord(block, 'X');
        Double ny = extractCoord(block, 'Y');
        Double ni = extractCoord(block, 'I');
        Double nj = extractCoord(block, 'J');

        double px = state.getX();
        double py = state.getY();

        if (nx != null) state.setX(nx);
        if (ny != null) state.setY(ny);

        if (block.contains("D02")) {
            if (state.isInRegion() && state.getRegionPoints().isEmpty()) {
                state.getRegionPoints().add(new Point2D.Double(state.getX(), state.getY()));
            }
            return;
        }

        if (block.contains("D01")) {
            if (state.isInRegion()) {
                state.getRegionPoints().add(new Point2D.Double(state.getX(), state.getY()));
            } else {
                draw(px, py, state.getX(), state.getY(), ni, nj);
            }
        }

        if (block.contains("D03")) {
            flash(state.getX(), state.getY());
        }
    }

    private boolean isCenterLineAperture() {
        return state.getCurrentAperture() != null && state.getCurrentAperture().params()[0] <= CENTER_LINE_THRESHOLD_MM;
    }

    private void draw(double x1, double y1, double x2, double y2, Double i, Double j) {
        if (state.getCurrentAperture() == null) return;

        Shape shape;

        if (state.getInterpolation() == Interpolation.LINEAR || i == null || j == null) {
            shape = new Line2D.Double(x1, y1, x2, y2);
        } else {
            double cx = x1 + i;
            double cy = y1 + j;

            double r = Point2D.distance(cx, cy, x1, y1);
            double a1 = Math.atan2(y1 - cy, x1 - cx);
            double a2 = Math.atan2(y2 - cy, x2 - cx);

            double extent = Math.toDegrees(a2 - a1);
            if (state.getInterpolation() == Interpolation.CW_ARC && extent > 0) extent -= 360;
            if (state.getInterpolation() == Interpolation.CCW_ARC && extent < 0) extent += 360;
            shape = new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, Math.toDegrees(a1), extent, Arc2D.OPEN);
        }

        if (isCenterLineAperture()) {
            Path2D p = new Path2D.Double();

            if (shape instanceof Line2D l) {
                p.moveTo(l.getX1(), l.getY1());
                p.lineTo(l.getX2(), l.getY2());
            } else if (shape instanceof Arc2D a) {
                p.append(a, true);
            }

            state.getCenterLines().add(p);
        } else {
            BasicStroke stroke = new BasicStroke((float) state.getCurrentAperture().params()[0], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            addEntity(new Path(new Path2D.Double(stroke.createStrokedShape(shape))));
        }
    }

    private void addEntity(Entity entity) {
        state.getShapes().add(new GerberEntity(state.getPolarity(), entity));
    }

    private void flash(double x, double y) {
        if (state.getCurrentAperture() == null) return;

        if (state.getCurrentAperture().code() == Code.CIRCLE) {
            double r = state.getCurrentAperture().params()[0] / 2;
            addEntity(new Ellipse(x - r, y - r, r * 2, r * 2));
        } else if (state.getCurrentAperture().code() == Code.RECTANGLE) {
            double w = state.getCurrentAperture().params()[0];
            double h = state.getCurrentAperture().params()[1];
            addEntity(new Rectangle(x - (w / 2), y - (h / 2), w, h));
        } else if (state.getCurrentAperture().code() == Code.OBROUND) {
            double w = state.getCurrentAperture().params()[0];
            double h = state.getCurrentAperture().params()[1];
            addEntity(new Ellipse(x - (w / 2), y - (h / 2), w, h));
        } else if (state.getCurrentAperture().isMacro()) {
            flashMacro(x, y, state.getCurrentAperture());
        }
    }

    private void flashMacro(double x, double y, Aperture aperture) {
        if (aperture.macro() == null) {
            return;
        }

        for (Primitive prim : aperture.macro().primitives()) {
            if (prim instanceof CenterLine cl) {
                double[] params = aperture.params();
                double w = cl.width().eval(params);
                double h = cl.height().eval(params);
                double cx = cl.cx().eval(params);
                double cy = cl.cy().eval(params);
                double rot = cl.rotation().eval(params);

                Shape rect = new Rectangle2D.Double(cx - w / 2, cy - h / 2, w, h);

                AffineTransform tx = new AffineTransform();
                tx.rotate(Math.toRadians(rot));
                tx.translate(x, y);
                addEntity(new Path(tx.createTransformedShape(rect)));
            } else if (prim instanceof Circle cp) {
                double diameter = cp.diameter().eval(aperture.params());
                double cx = cp.cx().eval(aperture.params());
                double cy = cp.cy().eval(aperture.params());
                addEntity(new Ellipse(x + cx - (diameter / 2), y + cy - (diameter / 2), diameter, diameter));
            } else if (prim instanceof Polygon pp) {
                double[] params = aperture.params();

                int n = pp.vertices();
                List<Expression> exprs = pp.params();

                if (exprs.size() < n * 2 + 1) {
                    LOGGER.warning("Invalid polygon primitive parameter count");
                    return;
                }

                double[] vx = new double[n];
                double[] vy = new double[n];

                int idx = 0;
                for (int i = 0; i < n; i++) {
                    vx[i] = exprs.get(idx++).eval(params);
                    vy[i] = exprs.get(idx++).eval(params);
                }


                Path2D p = new Path2D.Double();
                p.moveTo(vx[0], vy[0]);

                for (int i = 1; i < n; i++) {
                    p.lineTo(vx[i], vy[i]);
                }
                p.closePath();

                AffineTransform tx = new AffineTransform();
                tx.translate(x, y);
                addEntity(new Path(tx.createTransformedShape(p)));
            } else if (prim instanceof VectorLine vl) {
                double[] params = aperture.params();
                double x1 = vl.x1().eval(params);
                double y1 = vl.y1().eval(params);
                double x2 = vl.x2().eval(params);
                double y2 = vl.y2().eval(params);
                double width = vl.width().eval(params);
                double rot = vl.rotation().eval(params);

                Shape line = new Line2D.Double(x1, y1, x2, y2);
                BasicStroke stroke = new BasicStroke(
                        (float) width,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER
                );

                Shape stroked = stroke.createStrokedShape(line);

                AffineTransform tx = new AffineTransform();
                tx.rotate(Math.toRadians(rot));
                tx.translate(x, y);
                addEntity(new Path(tx.createTransformedShape(stroked)));
            }
        }
    }

    private void finalizeRegion() {
        if (state.getRegionPoints().size() < 3) return;

        Path2D p = new Path2D.Double();
        Point2D first = state.getRegionPoints().get(0);
        p.moveTo(first.getX(), first.getY());

        for (int i = 1; i < state.getRegionPoints().size(); i++) {
            Point2D pt = state.getRegionPoints().get(i);
            p.lineTo(pt.getX(), pt.getY());
        }
        p.closePath();
        addEntity(new Path(p));
    }

    private Double extractCoord(String text, char axis) {
        Matcher m = Pattern.compile(axis + "([+-]?\\d+)").matcher(text);
        if (!m.find()) return null;
        return scaleToMM(parseFixed(m.group(1)));
    }

    private double parseFixed(String s) {
        return Integer.parseInt(s) / Math.pow(10, state.getDecDigits());
    }

    private double scaleToMM(double value) {
        return UnitUtils.scaleUnits(state.getUnits(), UnitUtils.Units.MM) * value;
    }

    private Expression parseExpr(String s) {
        if (s.contains("+")) {
            String[] p = s.split("\\+");
            return new Add(parseExpr(p[0]), parseExpr(p[1]));
        }
        if (s.startsWith("$")) {
            return new Paramameter(Integer.parseInt(s.substring(1)) - 1);
        }
        return new Constant(scaleToMM(Double.parseDouble(s)));
    }
}