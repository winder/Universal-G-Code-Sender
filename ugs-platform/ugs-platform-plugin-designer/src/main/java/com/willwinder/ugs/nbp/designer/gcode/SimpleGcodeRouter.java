/*
 * This file is part of JGCGen.
 *
 * JGCGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JGCGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGCGen.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.gcode;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.gcode.toolpaths.SimplePath;
import com.willwinder.ugs.nbp.designer.gcode.toolpaths.SimplePocket;
import com.willwinder.universalgcodesender.gcode.util.Code;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Calle Laakkonen
 * @author Joacim Breiler
 */
public class SimpleGcodeRouter {
    /**
     * The feed rate to move tool in material as mm/min
     */
    private int feedSpeed = 1000;

    /**
     * A plunge feed for moving in Z-axis into the material as mm/min
     */
    private int plungeSpeed = 400;

    /**
     * The diameter of the tool in millimeters
     */
    private double toolDiameter = 3;

    /**
     * The percentage of tool step over to make each pass, the smaller the value the finer the results.
     * Should be larger than 0 and smaller than 1 where 0.1 would cut 10% of the tool diameter for each
     * pass and 1 would cut 100% of the tool diameter.
     */
    private double toolStepOver = 0.3;

    /**
     * The depth to plunge into the material for each pass in millimeters
     */
    private double depthPerPass = 1;

    /**
     * The safe height over the material in millimeters which allow the machine to move freely without scratching it
     */
    private double safeHeight = 1;

    public int getFeedSpeed() {
        return feedSpeed;
    }

    public void setFeedSpeed(int feedSpeed) {
        this.feedSpeed = feedSpeed;
    }

    public int getPlungeSpeed() {
        return plungeSpeed;
    }

    public void setPlungeSpeed(int plungeSpeed) {
        this.plungeSpeed = plungeSpeed;
    }

    public double getSafeHeight() {
        return safeHeight;
    }

    public void setSafeHeight(double safeHeight) {
        this.safeHeight = safeHeight;
    }

    public double getToolDiameter() {
        return toolDiameter;
    }

    public void setToolDiameter(double toolDiameter) {
        this.toolDiameter = toolDiameter;
    }

    public double getToolStepOver() {
        return toolStepOver;
    }

    public void setToolStepOver(double toolStepOver) {
        this.toolStepOver = toolStepOver;
    }

    public double getDepthPerPass() {
        return depthPerPass;
    }

    public void setDepthPerPass(double depthPerPass) {
        this.depthPerPass = depthPerPass;
    }

    protected String toGcode(GcodePath gcodePath) throws IOException {
        StringWriter stringWriter = new StringWriter();
        toGcode(stringWriter, gcodePath);
        stringWriter.flush();
        return stringWriter.toString();
    }

    public String toGcode(List<Entity> entities) {
        // Try to figure out the size of the drawing
        double width = entities.stream().map(e -> e.getBounds().getMaxX()).max(Double::compareTo).orElse((double) 0);
        double height = entities.stream().map(e -> e.getBounds().getMaxX()).max(Double::compareTo).orElse((double) 0);

        List<String> collect = entities.stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .sorted(new EntityComparator(width, height))
                .map(cuttable -> {
                    switch (cuttable.getCutType()) {
                        case POCKET:
                            SimplePocket simplePocket = new SimplePocket(cuttable);
                            simplePocket.setStartDepth(cuttable.getStartDepth());
                            simplePocket.setTargetDepth(cuttable.getTargetDepth());
                            simplePocket.setToolDiameter(toolDiameter);
                            simplePocket.setDepthPerPass(depthPerPass);
                            simplePocket.setSafeHeight(safeHeight);
                            simplePocket.setStepOver(toolStepOver);
                            return simplePocket.toGcodePath();
                        case OUTSIDE_PATH:
                            SimplePath simpleOutsidePath = new SimplePath(cuttable);
                            simpleOutsidePath.setOffset(toolDiameter / 2d);
                            simpleOutsidePath.setStartDepth(cuttable.getStartDepth());
                            simpleOutsidePath.setTargetDepth(cuttable.getTargetDepth());
                            simpleOutsidePath.setToolDiameter(toolDiameter);
                            simpleOutsidePath.setDepthPerPass(depthPerPass);
                            simpleOutsidePath.setSafeHeight(safeHeight);
                            return simpleOutsidePath.toGcodePath();
                        case INSIDE_PATH:
                            SimplePath simpleInsidePath = new SimplePath(cuttable);
                            simpleInsidePath.setOffset(-toolDiameter / 2d);
                            simpleInsidePath.setStartDepth(cuttable.getStartDepth());
                            simpleInsidePath.setTargetDepth(cuttable.getTargetDepth());
                            simpleInsidePath.setToolDiameter(toolDiameter);
                            simpleInsidePath.setDepthPerPass(depthPerPass);
                            simpleInsidePath.setSafeHeight(safeHeight);
                            return simpleInsidePath.toGcodePath();
                        case ON_PATH:
                            SimplePath simpleOnPath = new SimplePath(cuttable);
                            simpleOnPath.setStartDepth(cuttable.getStartDepth());
                            simpleOnPath.setTargetDepth(cuttable.getTargetDepth());
                            simpleOnPath.setToolDiameter(toolDiameter);
                            simpleOnPath.setDepthPerPass(depthPerPass);
                            simpleOnPath.setSafeHeight(safeHeight);
                            return simpleOnPath.toGcodePath();
                        default:
                            return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(gcodePath -> {
                    try {
                        return toGcode(gcodePath);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    return "";
                })
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        return Code.G21.name() + "\n" +
                String.join("\n", collect);
    }

    protected void toGcode(Writer writer, GcodePath path) throws IOException {
        List<Segment> segments = path.getSegments();
        runPath(writer, segments);
        writer.flush();
    }

    protected void runPath(Writer writer, List<Segment> segments) throws IOException {
        boolean hasFeedRateSet = false;
        // Convert path segments to G codes
        for (Segment s : segments) {
            // Write any label
            if (StringUtils.isNotEmpty(s.getLabel())) {
                writer.write("(" + s.getLabel() + ")\n");
            }

            switch (s.type) {
                // Seam are just markers.
                case SEAM:
                    continue;

                    // Rapid move
                    // Go to safe Z height, move over the target point and plunge down
                case MOVE:
                    // The rapid over target point is skipped when we do multiple passes
                    // and the end point is the same as the starting point.
                    writer.write(SegmentType.MOVE.gcode);
                    writer.write(" ");
                    writer.write(s.point.getFormattedGCode());
                    writer.write("\n");
                    hasFeedRateSet = false;

                    break;

                // Drill down using the plunge speed
                case POINT:
                    writer.write(SegmentType.POINT.gcode);
                    writer.write(" ");
                    writer.write("F" + plungeSpeed + " ");
                    writer.write(s.point.getFormattedGCode());
                    writer.write("\n");
                    break;

                // Motion at feed rate
                case LINE:
                case CWARC:
                case CCWARC:
                    writer.write(s.type.gcode);
                    writer.write(' ');

                    if (!hasFeedRateSet) {
                        writer.write("F");
                        writer.write(String.valueOf(feedSpeed));
                        writer.write(' ');
                        hasFeedRateSet = true;
                    }

                    writer.write(s.point.getFormattedGCode());
                    writer.write("\n");
                    break;
                default:
                    throw new RuntimeException("BUG! Unhandled segment type " + s.type);
            }
        }
    }
}
