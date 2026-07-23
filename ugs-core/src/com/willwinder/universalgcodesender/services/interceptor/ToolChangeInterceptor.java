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
package com.willwinder.universalgcodesender.services.interceptor;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.ProbeGcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * A {@link CommandInterceptor} that pauses the stream on tool change commands ({@code M6}), moves the
 * machine to a safe height and to a tool change location, waits for the operator to change the tool and
 * optionally runs a tool length probe before the stream is resumed.
 *
 * <p>The offsets from the probe are not applied automatically since that depends on the machine setup
 * (tool setter position, reference tool datum). The probed position is published through the context so
 * that machine specific handling can be added on top.
 *
 * @author Joacim Breiler
 */
public class ToolChangeInterceptor implements CommandInterceptor {
    private static final Pattern TOOL_CHANGE_PATTERN = Pattern.compile("(?:^|\\s)M0?6(?:\\s|$)");
    private static final Duration MOVE_TIMEOUT = Duration.ofMinutes(2);

    private boolean probeEnabled = false;
    private double safeHeightMm = -1;
    private double toolChangeX = 0;
    private double toolChangeY = 0;
    private double probeDistanceMm = 30;
    private double probeFeedMmPerMin = 100;

    @Override
    public String getName() {
        return "Tool change";
    }

    @Override
    public boolean matches(GcodeCommand command) {
        return TOOL_CHANGE_PATTERN.matcher(command.getCommandString()).find()
                || TOOL_CHANGE_PATTERN.matcher(command.getOriginalCommandString()).find();
    }

    @Override
    public void execute(InterceptContext context) throws InterceptException {
        IController controller = context.getController();
        try {
            context.log("Retracting to safe height");
            sendAndWait(controller, "G53 G0 Z" + formatCoordinate(safeHeightMm));

            context.log("Moving to tool change location");
            sendAndWait(controller, "G53 G0 X" + formatCoordinate(toolChangeX) + " Y" + formatCoordinate(toolChangeY));

            Optional<Integer> tool = context.getRequestedTool();
            context.awaitUserConfirmation(tool
                    .map(t -> "Insert tool " + t + " and continue")
                    .orElse("Change the tool and continue"));

            if (probeEnabled) {
                probeToolLength(context, controller);
                context.log("Retracting to safe height");
                sendAndWait(controller, "G53 G0 Z" + formatCoordinate(safeHeightMm));
            }
        } catch (InterceptAbortedException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterceptException("Tool change routine was interrupted", e);
        } catch (Exception e) {
            throw new InterceptException("Tool change routine failed: " + e.getMessage(), e);
        }
    }

    private void probeToolLength(InterceptContext context, IController controller) throws Exception {
        context.log("Probing tool length");
        PartialPosition distance = PartialPosition.from(Axis.Z, -probeDistanceMm, UnitUtils.Units.MM);
        UnitValue feedRate = new UnitValue(Unit.MM, probeFeedMmPerMin);
        ProbeGcodeCommand command = controller.createProbeCommand(distance, feedRate);
        ControllerUtils.sendAndWaitForCompletion(controller, command, MOVE_TIMEOUT);

        Optional<Position> probedPosition = command.getProbedPosition();
        probedPosition.ifPresentOrElse(
                position -> context.log("Probed tool length at Z" + formatCoordinate(position.get(Axis.Z))),
                () -> context.log("Probe did not trigger"));
    }

    private void sendAndWait(IController controller, String gcode) throws Exception {
        GcodeCommand command = controller.createCommand(gcode);
        ControllerUtils.sendAndWaitForCompletion(controller, command, MOVE_TIMEOUT);
        if (command.isError()) {
            throw new InterceptException("Controller rejected command: " + gcode);
        }
    }

    private static String formatCoordinate(double value) {
        return String.format(java.util.Locale.ROOT, "%.3f", value);
    }

    public boolean isProbeEnabled() {
        return probeEnabled;
    }

    public void setProbeEnabled(boolean probeEnabled) {
        this.probeEnabled = probeEnabled;
    }

    public double getSafeHeightMm() {
        return safeHeightMm;
    }

    public void setSafeHeightMm(double safeHeightMm) {
        this.safeHeightMm = safeHeightMm;
    }

    public double getToolChangeX() {
        return toolChangeX;
    }

    public void setToolChangeX(double toolChangeX) {
        this.toolChangeX = toolChangeX;
    }

    public double getToolChangeY() {
        return toolChangeY;
    }

    public void setToolChangeY(double toolChangeY) {
        this.toolChangeY = toolChangeY;
    }

    public double getProbeDistanceMm() {
        return probeDistanceMm;
    }

    public void setProbeDistanceMm(double probeDistanceMm) {
        this.probeDistanceMm = probeDistanceMm;
    }

    public double getProbeFeedMmPerMin() {
        return probeFeedMmPerMin;
    }

    public void setProbeFeedMmPerMin(double probeFeedMmPerMin) {
        this.probeFeedMmPerMin = probeFeedMmPerMin;
    }
}
