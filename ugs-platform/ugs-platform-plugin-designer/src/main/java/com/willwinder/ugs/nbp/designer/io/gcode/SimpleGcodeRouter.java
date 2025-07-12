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
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.LaserFillToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.LaserOutlineToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.OutlineToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.PocketToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.SurfaceToolPath;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathStats;
import com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils;
import com.willwinder.ugs.nbp.designer.model.Settings;
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
    private final Settings settings;

    public SimpleGcodeRouter(Settings settings) {
        this.settings = settings;
    }

    protected String toGcode(GcodePath gcodePath) throws IOException {
        ToolPathStats toolPathStats = ToolPathUtils.getToolPathStats(gcodePath);
        LOGGER.info("Generated a tool path with total length of " + Math.round(toolPathStats.getTotalFeedLength()) + "mm and " + Math.round(toolPathStats.getTotalRapidLength()) + "mm of rapid movement" );

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
                Code.G94.name() + " ; units per minute feed rate mode\n"
        );

        result.append("\n" );

        try {
            result.append(toGcode(getGcodePathFromCuttables(entities)));
        } catch (IOException e) {
            throw new RuntimeException("An error occured while trying to generate gcode", e);
        }

        result.append("\n; Turning off spindle\n" )
                .append(Code.M5.name()).append("\n" );
        return result.toString();
    }

    private GcodePath getGcodePathFromCuttables(List<Cuttable> cuttables) {
        GcodePath gcodePath = new GcodePath();
        int index = 0;
        
        for (Cuttable cuttable : cuttables) {
            
            index++;
            gcodePath.addSegment(new Segment(" " + cuttable.getName() + " - " + cuttable.getCutType().getName() + " (" + index + "/" + cuttables.size() + ")" ));
            if (cuttable.getIncludeInExport()) {                            
                switch (cuttable.getCutType()) {
                    case POCKET:
                        PocketToolPath simplePocket = new PocketToolPath(settings, cuttable);
                        simplePocket.setStartDepth(cuttable.getStartDepth());
                        simplePocket.setTargetDepth(cuttable.getTargetDepth());
                        simplePocket.appendGcodePath(gcodePath, settings);
                        break;
                    case SURFACE:
                        SurfaceToolPath surfaceToolPath = new SurfaceToolPath(settings, cuttable);
                        surfaceToolPath.setStartDepth(cuttable.getStartDepth());
                        surfaceToolPath.setTargetDepth(cuttable.getTargetDepth());
                        surfaceToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case OUTSIDE_PATH:
                        OutlineToolPath simpleOutsidePath = new OutlineToolPath(settings, cuttable);
                        simpleOutsidePath.setOffset(settings.getToolDiameter() / 2d);
                        simpleOutsidePath.setStartDepth(cuttable.getStartDepth());
                        simpleOutsidePath.setTargetDepth(cuttable.getTargetDepth());
                        simpleOutsidePath.appendGcodePath(gcodePath, settings);
                        break;
                    case INSIDE_PATH:
                        OutlineToolPath simpleInsidePath = new OutlineToolPath(settings, cuttable);
                        simpleInsidePath.setOffset(-settings.getToolDiameter() / 2d);
                        simpleInsidePath.setStartDepth(cuttable.getStartDepth());
                        simpleInsidePath.setTargetDepth(cuttable.getTargetDepth());
                        simpleInsidePath.appendGcodePath(gcodePath, settings);
                        break;
                    case ON_PATH:
                        OutlineToolPath simpleOnPath = new OutlineToolPath(settings, cuttable);
                        simpleOnPath.setStartDepth(cuttable.getStartDepth());
                        simpleOnPath.setTargetDepth(cuttable.getTargetDepth());
                        simpleOnPath.appendGcodePath(gcodePath, settings);
                        break;
                    case CENTER_DRILL:
                        DrillCenterToolPath drillToolPath = new DrillCenterToolPath(settings, cuttable);
                        drillToolPath.setStartDepth(cuttable.getStartDepth());
                        drillToolPath.setTargetDepth(cuttable.getTargetDepth());
                        drillToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case LASER_ON_PATH:
                        LaserOutlineToolPath laserOutlineToolPath = new LaserOutlineToolPath(settings, cuttable);
                        laserOutlineToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    case LASER_FILL:
                        LaserFillToolPath laserFillToolPath = new LaserFillToolPath(settings, cuttable);
                        laserFillToolPath.appendGcodePath(gcodePath, settings);
                        break;
                    default:
                }
            }
        }
        return gcodePath;
    }

    private String generateToolHeader() {
        return "; Tool: " + settings.getToolDiameter() + "mm\n" +
                "; Depth per pass: " + settings.getDepthPerPass() + "mm\n" +
                "; Plunge speed: " + settings.getPlungeSpeed() + "mm/min\n" +
                "; Safe height: " + settings.getSafeHeight() + "mm\n" +
                "; Tool step over: " + settings.getToolStepOver() + "mm\n"+
                "; Spindle Start Command: " + settings.getSpindleDirection()+ "\n";
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
                writer.write(";" + s.getLabel() + "\n" );
            }

            if (s.getSpindleSpeed() != null) {
                writer.write(settings.getSpindleDirection() + " S" + s.getSpindleSpeed() + "\n" );
            }

            switch (s.type) {
                // Seam are just markers.
                case SEAM:
                    if (!hasFeedRateSet && s.getFeedSpeed() != null) {
                        writer.write("F" );
                        writer.write(String.valueOf(s.getFeedSpeed()));
                        writer.write(' ');
                        hasFeedRateSet = true;
                    }
                    continue;

                    // Rapid move
                    // Go to safe Z height, move over the target point and plunge down
                case MOVE:
                    // The rapid over target point is skipped when we do multiple passes
                    // and the end point is the same as the starting point.
                    writer.write(SegmentType.MOVE.gcode);
                    writer.write(" " );
                    writer.write(s.point.getFormattedGCode());
                    writer.write("\n" );
                    hasFeedRateSet = false;
                    break;

                // Drill down using the plunge speed
                case POINT:
                    writer.write(SegmentType.POINT.gcode);
                    writer.write(" " );
                    writer.write("F" + settings.getPlungeSpeed() + " " );
                    writer.write(s.point.getFormattedGCode());
                    writer.write("\n" );
                    hasFeedRateSet = false;
                    break;

                // Motion at feed rate
                case LINE:
                case CWARC:
                case CCWARC:
                    writer.write(s.type.gcode);
                    writer.write(' ');

                    if (!hasFeedRateSet && s.getFeedSpeed() != null) {
                        writer.write("F" );
                        writer.write(String.valueOf(s.getFeedSpeed()));
                        writer.write(' ');
                        hasFeedRateSet = true;
                    }

                    writer.write(s.point.getFormattedGCode());
                    writer.write("\n" );
                    break;
                default:
                    throw new RuntimeException("BUG! Unhandled segment type " + s.type);
            }
        }
    }
}
