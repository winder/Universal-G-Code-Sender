/*
    Copyright 2026 Will Winder

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
package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.pendantui.v1.model.ProbeOperation;
import com.willwinder.universalgcodesender.pendantui.v1.model.ProbeRunRequest;
import com.willwinder.universalgcodesender.pendantui.v1.model.ProbeResult;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.ProbeGcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Duration;
import java.util.Optional;

/**
 * Endpoints for the touch-off/center-finding probe operations. Every probe move is sent
 * synchronously (send, block until done, read the result) rather than fired-and-forgotten over
 * the event bus - see {@link com.willwinder.universalgcodesender.types.ProbeGcodeCommand} and
 * {@code ugs-fx}'s {@code ProbeService}, which this mirrors.
 */
@Tag(name = "Probe", description = "Endpoints for probe/touch-off operations")
@Path("/probe")
public class ProbeResource {

    private static final Duration COMMAND_TIMEOUT = Duration.ofMinutes(1);

    @Inject
    private BackendAPI backendAPI;

    @GET
    @Path("getSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public com.willwinder.universalgcodesender.pendantui.v1.model.ProbeSettings getSettings() {
        com.willwinder.universalgcodesender.utils.ProbeSettings settings = backendAPI.getSettings().getProbeSettings();
        com.willwinder.universalgcodesender.pendantui.v1.model.ProbeSettings response = new com.willwinder.universalgcodesender.pendantui.v1.model.ProbeSettings();
        response.setFeedRateFast(settings.getFeedRateFast());
        response.setFeedRateSlow(settings.getFeedRateSlow());
        response.setRetractDistance(settings.getRetractDistance());
        response.setDelayAfterRetract(settings.getDelayAfterRetract());
        response.setProbeDiameter(settings.getProbeDiameter());
        response.setPlateThickness(settings.getPlateThickness());
        response.setMaxTravel(settings.getMaxTravel());
        response.setCompensateSoftLimits(settings.isCompensateSoftLimits());
        return response;
    }

    @POST
    @Path("saveSettings")
    @Consumes(MediaType.APPLICATION_JSON)
    public void saveSettings(com.willwinder.universalgcodesender.pendantui.v1.model.ProbeSettings settings) {
        com.willwinder.universalgcodesender.utils.ProbeSettings backendSettings = backendAPI.getSettings().getProbeSettings();
        backendSettings.setFeedRateFast(settings.getFeedRateFast());
        backendSettings.setFeedRateSlow(settings.getFeedRateSlow());
        backendSettings.setRetractDistance(settings.getRetractDistance());
        backendSettings.setDelayAfterRetract(settings.getDelayAfterRetract());
        backendSettings.setProbeDiameter(settings.getProbeDiameter());
        backendSettings.setPlateThickness(settings.getPlateThickness());
        backendSettings.setMaxTravel(settings.getMaxTravel());
        backendSettings.setCompensateSoftLimits(settings.isCompensateSoftLimits());
    }

    @POST
    @Path("run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ProbeResult run(ProbeRunRequest request) {
        if (request == null || request.getOperation() == null) {
            throw new BadRequestException("Missing probe operation");
        }
        if (!backendAPI.isConnected() || backendAPI.getControllerState() != ControllerState.IDLE) {
            throw new NotAcceptableException("The machine must be connected and idle to probe");
        }

        IController controller = backendAPI.getController();
        com.willwinder.universalgcodesender.utils.ProbeSettings settings = backendAPI.getSettings().getProbeSettings();
        double maxTravel = Math.abs(request.getMaxTravel() != null ? request.getMaxTravel() : settings.getMaxTravel());

        try {
            return switch (request.getOperation()) {
                case Z -> probeZ(controller, settings, maxTravel);
                case X_NEG -> probeSingleFace(controller, settings, Axis.X, -maxTravel);
                case X_POS -> probeSingleFace(controller, settings, Axis.X, maxTravel);
                case Y_NEG -> probeSingleFace(controller, settings, Axis.Y, -maxTravel);
                case Y_POS -> probeSingleFace(controller, settings, Axis.Y, maxTravel);
                case X_CENTER -> probeCenter(controller, settings, maxTravel, true, false);
                case Y_CENTER -> probeCenter(controller, settings, maxTravel, false, true);
                case CENTER -> probeCenter(controller, settings, maxTravel, true, true);
            };
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ProbeResult.failed("Probe was interrupted");
        } catch (Exception e) {
            return ProbeResult.failed(e.getMessage() != null ? e.getMessage() : "Probe failed");
        } finally {
            // Every probe move above is sent in relative mode (see relativeMoveGcode) and never
            // switched back - restore absolute mode here, regardless of how the operation above
            // ended, so a probe never silently leaves the machine in G91 for whatever runs next.
            restoreAbsoluteMode(controller);
        }
    }

    private void restoreAbsoluteMode(IController controller) {
        try {
            sendSync(controller, "G90");
        } catch (Exception e) {
            // Best-effort: the probe's own result/error above is what matters here.
        }
    }

    private ProbeResult probeZ(IController controller, com.willwinder.universalgcodesender.utils.ProbeSettings settings, double maxTravel) throws Exception {
        Optional<Position> probed = probeAxisTwoStage(controller, Axis.Z, -maxTravel, settings);
        if (probed.isEmpty()) {
            return ProbeResult.failed("Probe did not make contact within " + maxTravel + "mm");
        }

        // Zero the work Z at the plate's surface, i.e. the current (probed) position reads as
        // the plate thickness rather than zero - so the material surface underneath it is Z0.
        backendAPI.setWorkPosition(PartialPosition.from(Axis.Z, settings.getPlateThickness(), UnitUtils.Units.MM));

        // Clear the plate/material before the operator moves anything else.
        sendSync(controller, relativeMoveGcode(Axis.Z, settings.getRetractDistance() + settings.getPlateThickness()));
        return ProbeResult.ok(probed.get());
    }

    private ProbeResult probeSingleFace(IController controller, com.willwinder.universalgcodesender.utils.ProbeSettings settings, Axis axis, double signedMaxTravel) throws Exception {
        Optional<Position> probed = probeAxisTwoStage(controller, axis, signedMaxTravel, settings);
        if (probed.isEmpty()) {
            return ProbeResult.failed("Probe did not make contact within " + Math.abs(signedMaxTravel) + "mm");
        }

        // The reported contact position is the tool's center, not the edge that actually
        // touched the surface - compensate by the tool radius so the work zero lands at the
        // true surface, not offset into the material (or air) by half the tool's diameter.
        double dir = Math.signum(signedMaxTravel);
        double radius = settings.getProbeDiameter() / 2;
        double workValue = -dir * radius;
        backendAPI.setWorkPosition(PartialPosition.from(axis, workValue, UnitUtils.Units.MM));

        // Retract back off the surface, toward where the probe started.
        sendSync(controller, relativeMoveGcode(axis, -dir * settings.getRetractDistance()));
        return ProbeResult.ok(probed.get());
    }

    /**
     * Shared routine for X-center, Y-center, and bore/rectangle-center: probe both sides of a
     * feature the tool starts inside (a slot, hole, or pocket), average the two contact
     * positions per axis, move there, and zero. A round bore and a rectangular pocket use
     * exactly the same math - only the starting/approach distance differs, which is just
     * `maxTravel` either way.
     */
    private ProbeResult probeCenter(IController controller, com.willwinder.universalgcodesender.utils.ProbeSettings settings, double maxTravel, boolean doX, boolean doY) throws Exception {
        Double xOffset = null;
        Double yOffset = null;

        if (doX) {
            Optional<Double> offset = probeAxisCenterOffset(controller, settings, Axis.X, maxTravel);
            if (offset.isEmpty()) {
                return ProbeResult.failed("X-center probe did not make contact on both sides within " + maxTravel + "mm");
            }
            xOffset = offset.get();
        }

        if (doY) {
            Optional<Double> offset = probeAxisCenterOffset(controller, settings, Axis.Y, maxTravel);
            if (offset.isEmpty()) {
                return ProbeResult.failed("Y-center probe did not make contact on both sides within " + maxTravel + "mm");
            }
            yOffset = offset.get();
        }

        PartialPosition.Builder builder = PartialPosition.builder(UnitUtils.Units.MM);
        if (xOffset != null) {
            builder.setX(0.0);
        }
        if (yOffset != null) {
            builder.setY(0.0);
        }

        if (xOffset != null) {
            sendSync(controller, relativeMoveGcode(Axis.X, xOffset));
        }
        if (yOffset != null) {
            sendSync(controller, relativeMoveGcode(Axis.Y, yOffset));
        }
        backendAPI.setWorkPosition(builder.build());

        return ProbeResult.ok(controller.getControllerStatus().getMachineCoord());
    }

    /**
     * Probes one axis from the current position, returns to the current position, probes the
     * opposite direction, returns again, and reports how far (relative to the starting
     * position) the midpoint between the two contact points is - the caller decides what to do
     * with that (move there and zero, for center-finding).
     */
    private Optional<Double> probeAxisCenterOffset(IController controller, com.willwinder.universalgcodesender.utils.ProbeSettings settings, Axis axis, double maxTravel) throws Exception {
        double startMachine = getMachineAxisPosition(controller, axis);

        Optional<Position> negProbe = probeAxisTwoStage(controller, axis, -maxTravel, settings);
        if (negProbe.isEmpty()) {
            return Optional.empty();
        }
        double negMachine = negProbe.get().get(axis);
        returnToMachinePosition(controller, axis, startMachine);

        Optional<Position> posProbe = probeAxisTwoStage(controller, axis, maxTravel, settings);
        if (posProbe.isEmpty()) {
            return Optional.empty();
        }
        double posMachine = posProbe.get().get(axis);
        returnToMachinePosition(controller, axis, startMachine);

        double centerMachine = (negMachine + posMachine) / 2;
        return Optional.of(centerMachine - startMachine);
    }

    private void returnToMachinePosition(IController controller, Axis axis, double targetMachineValue) throws Exception {
        double delta = targetMachineValue - getMachineAxisPosition(controller, axis);
        if (Math.abs(delta) > 1e-6) {
            sendSync(controller, relativeMoveGcode(axis, delta));
        }
    }

    /**
     * A fast probe to find the approximate contact point, then a retract-and-reprobe at a
     * slower rate for a precise final position - standard two-stage touch-off, matches both the
     * existing desktop Probe module and ugs-fx's probe service.
     */
    private Optional<Position> probeAxisTwoStage(IController controller, Axis axis, double signedDistanceMm, com.willwinder.universalgcodesender.utils.ProbeSettings settings) throws Exception {
        double fastDistance = clampNegativeTravel(controller, axis, signedDistanceMm, settings);
        Optional<Position> fast = doProbe(controller, axis, fastDistance, settings.getFeedRateFast());
        if (fast.isEmpty()) {
            return Optional.empty();
        }

        double retract = -Math.signum(signedDistanceMm) * settings.getRetractDistance();
        sendSync(controller, relativeMoveGcode(axis, retract));

        if (settings.getDelayAfterRetract() > 0) {
            Thread.sleep((long) (settings.getDelayAfterRetract() * 1000));
        }

        double slowDistance = clampNegativeTravel(controller, axis, Math.signum(signedDistanceMm) * settings.getRetractDistance() * 1.2, settings);
        return doProbe(controller, axis, slowDistance, settings.getFeedRateSlow());
    }

    private Optional<Position> doProbe(IController controller, Axis axis, double distanceMm, double feedRateMm) throws Exception {
        // Cancel any active tool length offset first, same as ugs-fx - a stale TLO would throw
        // off every computed offset below.
        sendSync(controller, "G49");

        PartialPosition distance = PartialPosition.from(axis, distanceMm, UnitUtils.Units.MM);
        UnitValue feedRate = new UnitValue(Unit.MM_PER_MINUTE, feedRateMm);
        ProbeGcodeCommand command = controller.createProbeCommand(distance, feedRate);
        command.setTemporaryParserModalChange(true);

        try {
            ControllerUtils.sendAndWaitForCompletion(controller, command, COMMAND_TIMEOUT);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }

        // A failed probe can also leave the controller in ALARM (FluidNC aborts the queue on a
        // probe-fail alarm) - the existing status stream/AlarmModal already surfaces that to the
        // user, so here it's just another signal that this probe didn't succeed.
        if (backendAPI.getControllerState() == ControllerState.ALARM) {
            return Optional.empty();
        }

        return command.getProbedPosition();
    }

    private GcodeCommand sendSync(IController controller, String gcode) throws Exception {
        GcodeCommand command = controller.createCommand(gcode);
        command.setTemporaryParserModalChange(true);
        ControllerUtils.sendAndWaitForCompletion(controller, command, COMMAND_TIMEOUT);
        return command;
    }

    private String relativeMoveGcode(Axis axis, double distanceMm) {
        return "G21 G91 G0 " + axis.name() + Utils.formatter.format(distanceMm);
    }

    private double getMachineAxisPosition(IController controller, Axis axis) {
        return controller.getControllerStatus().getMachineCoord().getPositionIn(UnitUtils.Units.MM).get(axis);
    }

    /**
     * Clamps a negative-direction probe distance to the machine's configured soft limit
     * (mirrors ugs-fx's getSafeProbeZDistance, generalized to any axis). Positive-direction
     * probes (X+/Y+) aren't clamped here - GRBL/FluidNC's usual soft-limit convention only
     * meaningfully bounds travel in the negative direction from the homed corner, so there's no
     * equivalent positive-side distance to compute with confidence; the configured maxTravel
     * plus the firmware's own runtime soft-limit alarm are the safety net for those instead.
     */
    private double clampNegativeTravel(IController controller, Axis axis, double distanceMm, com.willwinder.universalgcodesender.utils.ProbeSettings settings) {
        if (distanceMm >= 0 || !settings.isCompensateSoftLimits()) {
            return distanceMm;
        }
        try {
            if (!controller.getFirmwareSettings().isSoftLimitsEnabled()) {
                return distanceMm;
            }
        } catch (FirmwareSettingsException e) {
            return distanceMm;
        }

        double distanceToSoftLimit = -ControllerUtils.getDistanceToSoftLimit(controller, axis);
        if (distanceToSoftLimit > distanceMm) {
            return distanceToSoftLimit - 1e-6;
        }
        return distanceMm;
    }
}
