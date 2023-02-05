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
package com.willwinder.ugs.nbp.designer.io.gcode;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.DrillCenterToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.OutlineToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.PocketToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathStats;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.utils.Version;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Calle Laakkonen
 * @author Joacim Breiler
 */
public class SimpleGcodeRouter {
    private static final Logger LOGGER = Logger.getLogger(SimpleGcodeRouter.class.getSimpleName());
    private static final String HEADER = "; This file was generated with \"Universal Gcode Sender " + Version.getVersionString() + "\"\n;\n";

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

    /**
     * The spindle speed in RPM
     */
    private double spindleSpeed = 1000;

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

    private double getSpindleSpeed() {
        return this.spindleSpeed;
    }

    public void setSpindleSpeed(double spindleSpeed) {
        this.spindleSpeed = spindleSpeed;
    }

    protected String toGcode(GcodePath gcodePath) throws IOException {
        ToolPathStats toolPathStats = ToolPathUtils.getToolPathStats(gcodePath);
        LOGGER.info("Generated a tool path with total length of " +  Math.round(toolPathStats.getTotalFeedLength()) + "mm and " + Math.round(toolPathStats.getTotalRapidLength()) + "mm of rapid movement");

        StringWriter stringWriter = new StringWriter();
        toGcode(stringWriter, gcodePath);
        stringWriter.flush();
        return stringWriter.toString();
    }

    public String toGcode(List<Cuttable> entities) {
        StringBuilder result = new StringBuilder(HEADER +
                generateToolHeader() + "\n" +
                Code.G21.name() + " ; millimeters\n" +
                Code.G90.name() + " ; absolute coordinate\n" +
                Code.G17.name() + " ; XY plane\n" +
                Code.G94.name() + " ; units per minute feed rate mode\n" +
                Code.M3.name() + " S" + Math.round(getSpindleSpeed()) + " ; Turning on spindle\n\n"
        );

        try {
            result.append(toGcode(getGcodePathFromCuttables(entities)));
        } catch (IOException e) {
            throw new RuntimeException("An error occured while trying to generate gcode", e);
        }

        result.append("\n" + "; Turning off spindle\n")
                .append(Code.M5.name()).append("\n");
        return result.toString();
    }

    private GcodePath getGcodePathFromCuttables(List<Cuttable> cuttables) {
        GcodePath gcodePath = new GcodePath();
        int index = 0;
        for (Cuttable cuttable : cuttables) {
            index++;
            gcodePath.addSegment(new Segment(" " + cuttable.getName() + " - " + cuttable.getCutType().getName() + " (" + index + "/" + cuttables.size() + ")"));
            switch (cuttable.getCutType()) {
                case POCKET:
                    PocketToolPath simplePocket = new PocketToolPath(cuttable);
                    simplePocket.setStartDepth(cuttable.getStartDepth());
                    simplePocket.setTargetDepth(cuttable.getTargetDepth());
                    simplePocket.setToolDiameter(toolDiameter);
                    simplePocket.setDepthPerPass(depthPerPass);
                    simplePocket.setSafeHeight(safeHeight);
                    simplePocket.setStepOver(toolStepOver);

                    gcodePath.appendGcodePath(simplePocket.toGcodePath());
                    break;
                case OUTSIDE_PATH:
                    OutlineToolPath simpleOutsidePath = new OutlineToolPath(cuttable);
                    simpleOutsidePath.setOffset(toolDiameter / 2d);
                    simpleOutsidePath.setStartDepth(cuttable.getStartDepth());
                    simpleOutsidePath.setTargetDepth(cuttable.getTargetDepth());
                    simpleOutsidePath.setToolDiameter(toolDiameter);
                    simpleOutsidePath.setDepthPerPass(depthPerPass);
                    simpleOutsidePath.setSafeHeight(safeHeight);
                    gcodePath.appendGcodePath(simpleOutsidePath.toGcodePath());
                    break;
                case INSIDE_PATH:
                    OutlineToolPath simpleInsidePath = new OutlineToolPath(cuttable);
                    simpleInsidePath.setOffset(-toolDiameter / 2d);
                    simpleInsidePath.setStartDepth(cuttable.getStartDepth());
                    simpleInsidePath.setTargetDepth(cuttable.getTargetDepth());
                    simpleInsidePath.setToolDiameter(toolDiameter);
                    simpleInsidePath.setDepthPerPass(depthPerPass);
                    simpleInsidePath.setSafeHeight(safeHeight);
                    gcodePath.appendGcodePath(simpleInsidePath.toGcodePath());
                    break;
                case ON_PATH:
                    OutlineToolPath simpleOnPath = new OutlineToolPath(cuttable);
                    simpleOnPath.setStartDepth(cuttable.getStartDepth());
                    simpleOnPath.setTargetDepth(cuttable.getTargetDepth());
                    simpleOnPath.setToolDiameter(toolDiameter);
                    simpleOnPath.setDepthPerPass(depthPerPass);
                    simpleOnPath.setSafeHeight(safeHeight);
                    gcodePath.appendGcodePath(simpleOnPath.toGcodePath());
                    break;
                case CENTER_DRILL:
                    DrillCenterToolPath drillToolPath = new DrillCenterToolPath(cuttable);
                    drillToolPath.setStartDepth(cuttable.getStartDepth());
                    drillToolPath.setTargetDepth(cuttable.getTargetDepth());
                    drillToolPath.setToolDiameter(toolDiameter);
                    drillToolPath.setDepthPerPass(depthPerPass);
                    drillToolPath.setSafeHeight(safeHeight);
                    gcodePath.appendGcodePath(drillToolPath.toGcodePath());
                    break;
                default:
            }
        }
        return gcodePath;
    }

    private String generateToolHeader() {
        return "; Tool: " + getToolDiameter() + "mm\n" +
                "; Depth per pass: " + getDepthPerPass() + "mm\n" +
                "; Feed speed: " + getFeedSpeed() + "mm/min\n" +
                "; Plunge speed: " + getPlungeSpeed() + "mm/min\n" +
                "; Safe height: " + getSafeHeight() + "mm\n" +
                "; Tool step over: " + getToolStepOver() + "mm\n" +
                "; Spindle speed: " + Math.round(getSpindleSpeed()) + "rpm\n";
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
                writer.write(";" + s.getLabel() + "\n");
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
