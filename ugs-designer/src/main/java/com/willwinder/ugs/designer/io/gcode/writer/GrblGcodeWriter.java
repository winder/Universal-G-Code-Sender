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
package com.willwinder.ugs.designer.io.gcode.writer;

import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import static com.willwinder.universalgcodesender.utils.MathUtils.isEqual;
import com.willwinder.universalgcodesender.utils.Version;
import org.apache.commons.lang3.StringUtils;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Writer;
import java.util.EnumSet;
import java.util.Set;

public class GrblGcodeWriter implements GcodeWriter {
    private static final String HEADER = "; This file was generated with \"Universal Gcode Sender " + Version.getVersionString() + "\"\n\n";

    /**
     * The axes of the plane that arcs are cut in, which is always XY since the header selects G17
     */
    private static final Set<Axis> PLANE_AXES = EnumSet.of(Axis.X, Axis.Y);

    private final Settings settings;
    private final Writer writer;
    private Integer currentSpindle = null;
    private Integer currentFeed = null;
    private PartialPosition currentPoint;
    private boolean hasFeedRateSet = false;
    private boolean hasStartedSpindle = false;

    public GrblGcodeWriter(Settings settings, Writer writer) {
        this.settings = settings;
        this.writer = writer;
    }

    @Override
    public void begin() throws IOException {
        writer.write(HEADER);
        writer.write(Code.G21.name() + " ; millimeters\n");
        writer.write(Code.G90.name() + " ; absolute coordinate\n");
        writer.write(Code.G17.name() + " ; XY plane\n");
        writer.write(Code.G94.name() + " ; units per minute feed rate mode\n");
        writer.write("\n");
        writer.write("; Depth per pass: " + settings.getDepthPerPass() + "mm\n");
        writer.write("; Plunge speed: " + settings.getPlungeSpeed() + "mm/min\n");
        writer.write("; Safe height: " + settings.getSafeHeight() + "mm\n");
        writer.write("; Tool step over: " + settings.getToolStepOver() + "mm\n");
        writer.write("; Spindle start command: " + settings.getSpindleDirection() + "\n");
        writer.write("; Max spindle speed: " + settings.getMaxSpindleSpeed() + "\n");
        writeToolHeader();
    }

    /**
     * Records which cutter the program was posted for, followed by the tool change that selects it.
     * Tool changes are opt-in, and a tool with no slot assigned in the tool library has nothing to
     * select — either way the comment is written, since a {@code T} word only names a slot.
     */
    private void writeToolHeader() throws IOException {
        if (!settings.getUseToolChanges() || !settings.hasToolNumber()) {
            writer.write("; Tool: " + describeTool() + "\n");
            return;
        }
        writer.write("\n" + Code.M6.name() + " T" + settings.getToolNumber() + " ; Tool: " + describeTool() + "\n");
    }

    /**
     * The library name of the active tool, which already identifies it well enough to review a
     * program by — "1/4" Upcut". A design that is not bound to a library tool has no name, so the
     * description is built from what the settings do carry: "6mm Ball", or "6mm V-bit 60°".
     */
    private String describeTool() {
        ToolDefinition tool = settings.getCurrentToolSnapshot();
        if (tool != null && StringUtils.isNotBlank(tool.getName())) {
            return tool.getName();
        }

        StringBuilder description = new StringBuilder(Utils.formatter.format(settings.getToolDiameter()));
        description.append("mm ").append(settings.getToolShape().getDisplayName());
        if (settings.getToolShape().requiresAngle()) {
            description.append(" ").append(Utils.formatter.format(settings.getVBitAngle())).append("°");
        }
        return description.toString();
    }

    @Override
    public void writeSegment(Segment segment) throws IOException {
        if (StringUtils.isNotEmpty(segment.getLabel())) {
            writer.write(";" + segment.getLabel() + "\n");
        }

        if (segment.getSpindleSpeed() != null && (!segment.getSpindleSpeed().equals(currentSpindle) || !hasStartedSpindle)) {
            writer.write(settings.getSpindleDirection() + " S" + segment.getSpindleSpeed() + "\n");
            hasStartedSpindle = true;
            currentSpindle = segment.getSpindleSpeed();
        }

        switch (segment.type) {
            case SEAM -> {
                if (segment.getFeedSpeed() != null) {
                    writer.write("F" + segment.getFeedSpeed() + " ");
                    hasFeedRateSet = false;
                }
            }

            case MOVE -> {
                String point = getPointFormattedGCode(segment);
                if (!point.isEmpty()) {
                    writer.write("G0 " + point + "\n");
                }
                hasFeedRateSet = false;
            }

            case POINT -> {
                writer.write("G1 F" + settings.getPlungeSpeed() + " "
                        + getPointFormattedGCode(segment) + "\n");
                hasFeedRateSet = false;
            }

            case LINE, CWARC, CCWARC -> {
                writer.write(segment.type.gcode + " ");
                if (segment.getFeedSpeed() != null && (!hasFeedRateSet || !segment.getFeedSpeed().equals(currentFeed))) {
                    writer.write("F" + segment.getFeedSpeed() + " ");
                    currentFeed = segment.getFeedSpeed();
                    hasFeedRateSet = true;
                }

                // The arc offsets are relative to the start of the arc, so they need to be
                // formatted before the current position is advanced to the end of the arc
                String arcOffsets = segment.type.isArc() ? getArcOffsetFormattedGCode(segment) : "";
                writer.write(getPointFormattedGCode(segment, segment.type.isArc()) + arcOffsets + "\n");
            }
        }
    }

    private String getPointFormattedGCode(Segment segment) {
        return getPointFormattedGCode(segment, false);
    }

    /**
     * @param alwaysWritePlaneAxes writes the axes of the current plane even when they have not
     *                             changed. Grbl rejects an arc that carries no axis words, which
     *                             happens when an arc ends where it started.
     */
    private String getPointFormattedGCode(Segment segment, boolean alwaysWritePlaneAxes) {
        StringBuilder result = new StringBuilder();
        PartialPosition newPoint = segment.getPoint();

        for (Axis axis : Axis.values()) {
            if (!newPoint.hasAxis(axis)) {
                continue;
            }

            boolean isChanged = currentPoint == null || !currentPoint.hasAxis(axis) || !isEqual(newPoint.getAxis(axis), currentPoint.getAxis(axis), 0.0001);
            if (isChanged || (alwaysWritePlaneAxes && PLANE_AXES.contains(axis))) {
                result.append(axis.name()).append(Utils.formatter.format(newPoint.getAxis(axis)));
            }
        }
        currentPoint = advanceCurrentPoint(newPoint);
        return result.toString();
    }

    /**
     * Grbl only supports incremental arc offsets, so the absolute arc center is converted to an
     * offset from the position where the arc starts.
     */
    private String getArcOffsetFormattedGCode(Segment segment) {
        if (currentPoint == null || !currentPoint.hasX() || !currentPoint.hasY()) {
            throw new IllegalStateException("An arc segment must be preceded by a position with a known X and Y");
        }

        Point2D arcCenter = segment.getArcCenter();
        return "I" + Utils.formatter.format(arcCenter.getX() - currentPoint.getX())
                + "J" + Utils.formatter.format(arcCenter.getY() - currentPoint.getY());
    }

    private PartialPosition advanceCurrentPoint(PartialPosition newPoint) {
        if (currentPoint == null) {
            return newPoint;
        }

        PartialPosition.Builder builder = PartialPosition.builder(currentPoint);
        newPoint.getPositionIn(currentPoint.getUnits()).getAll().forEach(builder::setValue);
        return builder.build();
    }

    @Override
    public void end() throws IOException {
        writer.write("\n; Turning off spindle\n");
        writer.write(Code.M5.name());
        writer.append("\n");
    }
}