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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.fx.exceptions.ProbeException;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import static com.willwinder.universalgcodesender.model.Unit.MM;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.ProbeGcodeCommand;
import com.willwinder.universalgcodesender.utils.ControllerUtils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class ProbeService {
    private final BackendAPI backend;
    private final ProbeSettings settings;

    public ProbeService(BackendAPI backend, ProbeSettings settings) {
        this.backend = backend;
        this.settings = settings;
    }

    public UnitValue getSafeProbeZDistance() {
        double probeDistance = -settings.probeZDistanceProperty().getValue().convertTo(Unit.MM).doubleValue();
        if (!shouldCompensateForSoftLimits()) {
            return new UnitValue(Unit.MM, probeDistance);
        }

        double distanceToSoftLimit = -ControllerUtils.getDistanceToSoftLimit(backend.getController(), Axis.Z);
        if (distanceToSoftLimit > probeDistance) {
            // Remove an additional small amount to avoid hitting the soft limit due to rounding errors
            probeDistance = distanceToSoftLimit - 1e-6;
        }
        return new UnitValue(Unit.MM, probeDistance);
    }

    protected boolean shouldCompensateForSoftLimits() {
        try {
            return ProbeSettings.getCompensateSoftLimits() && backend.getController().getFirmwareSettings().isSoftLimitsEnabled();
        } catch (FirmwareSettingsException e) {
            return false;
        }
    }

    public void probeZ() throws ProbeException {
        try {
            double previousZ = backend.getController()
                    .getControllerStatus()
                    .getMachineCoord()
                    .getPositionIn(UnitUtils.Units.MM)
                    .getZ();

            probeZ(getSafeProbeZDistance(), getFastFindRate()).get()
                    .orElseThrow(() -> new ProbeException("Could not find the the probe"));

            backend.getController().jogMachine(
                    PartialPosition.from(Axis.Z, ProbeSettings.getRetractDistance()).getPositionIn(UnitUtils.Units.MM),
                    getFastFeedRateInMmPerMinute());

            // We should probably wait for the jog command to complete, but we don't have a way to do that yet
            Thread.sleep(ProbeSettings.getDelayAfterRetract().convertTo(Unit.MILLISECONDS).longValue());

            double probedZ = probeZ(getSafeProbeZDistance(), ProbeSettings.getSlowFindRate())
                    .get()
                    .map(p -> p.getPositionIn(UnitUtils.Units.MM).getZ())
                    .orElseThrow(() -> new ProbeException("Second probe failed"));

            // Reset zero minus the probe plate
            backend.setWorkPosition(PartialPosition.from(Axis.Z, settings.zPlateThicknessProperty().getValue()));

            // Jog back to the starting Z position
            double probedDistance = previousZ - probedZ;
            backend.getController().jogMachine(
                    PartialPosition.from(Axis.Z, probedDistance, UnitUtils.Units.MM).getPositionIn(UnitUtils.Units.MM),
                    getFastFeedRateInMmPerMinute());

            backend.getController().restoreParserModalState();
        } catch (InterruptedException | ExecutionException e) {
            throw new ProbeException("An unexpected error occurred during probing", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int getFastFeedRateInMmPerMinute() {
        return getFastFindRate().convertTo(Unit.MM_PER_MINUTE).intValue();
    }

    private static UnitValue getFastFindRate() {
        return ProbeSettings.getFastFindRate();
    }

    private CompletableFuture<Optional<Position>> probeZ(UnitValue distance, UnitValue feedRate) {
        PartialPosition probeDistance = PartialPosition.from(Axis.Z, distance.convertTo(MM).doubleValue(), UnitUtils.Units.MM);
        return executeProbe(probeDistance, feedRate);
    }

    private CompletableFuture<Optional<Position>> executeProbe(PartialPosition distance, UnitValue feedRate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                GcodeCommand cancelToolLengthCompensation = backend.getController().createCommand(Code.G49.name());
                ControllerUtils.sendAndWaitForCompletion(backend.getController(), cancelToolLengthCompensation, Duration.of(1, ChronoUnit.MINUTES));

                ProbeGcodeCommand command = backend.getController().createProbeCommand(distance, feedRate);
                ControllerUtils.sendAndWaitForCompletion(backend.getController(), command, Duration.of(1, ChronoUnit.MINUTES));
                return command.getProbedPosition();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException(e);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
}
