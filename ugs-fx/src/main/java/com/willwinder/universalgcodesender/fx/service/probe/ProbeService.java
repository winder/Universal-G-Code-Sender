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
package com.willwinder.universalgcodesender.fx.service.probe;

import com.willwinder.universalgcodesender.Utils;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class ProbeService {
    public static final ProbeStep FAST_FIND = new ProbeStep("Fast find");
    public static final ProbeStep RETRACT = new ProbeStep("Retract");
    public static final ProbeStep SLOW_FIND = new ProbeStep("Slow find");
    public static final ProbeStep MOVE_TO_ORIGIN = new ProbeStep("Move to origin");
    public static final ProbeStep WAITING = new ProbeStep("Waiting");
    private final BackendAPI backend;
    private final ProbeSettings settings;

    public ProbeService(BackendAPI backend, ProbeSettings settings) {
        this.backend = backend;
        this.settings = settings;
    }

    public UnitValue getSafeProbeZDistance() {
        return settings.probeZDistanceProperty()
                .map(u -> {
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
                })
                .getValue();
    }

    protected boolean shouldCompensateForSoftLimits() {
        try {
            return ProbeSettings.getCompensateSoftLimits() &&
                    backend.getController() != null &&
                    backend.getController().getFirmwareSettings().isSoftLimitsEnabled();
        } catch (FirmwareSettingsException e) {
            return false;
        }
    }

    public void probeZ(Flow.Subscriber<ProbeEvent> subscriber) {
        SubmissionPublisher<ProbeEvent> publisher = new SubmissionPublisher<>();
        publisher.subscribe(subscriber);
        try {
            new Thread(() -> {
                try (publisher) {
                    probeZ(publisher);
                }
            }, "probe-thread").start();
        } catch (RuntimeException threadStartFailure) {
            publisher.closeExceptionally(threadStartFailure);
            throw threadStartFailure;
        }
    }

    public void probeZ(SubmissionPublisher<ProbeEvent> publisher) {
        try {
            publisher.submit(new ProbeEvent.JobCreated(List.of(FAST_FIND, RETRACT, WAITING, SLOW_FIND, MOVE_TO_ORIGIN)));

            double previousZ = backend.getController()
                    .getControllerStatus()
                    .getMachineCoord()
                    .getPositionIn(UnitUtils.Units.MM)
                    .getZ();

            publisher.submit(new ProbeEvent.StepStarted(FAST_FIND));
            probeZ(getSafeProbeZDistance(), getFastFindRate()).get()
                    .orElseThrow(() -> {
                        publisher.submit(new ProbeEvent.StepFailed(FAST_FIND));
                        return new ProbeException("Probe did not trigger within distance");
                    });
            publisher.submit(new ProbeEvent.StepCompleted(FAST_FIND));

            PartialPosition retractDistance = PartialPosition.from(Axis.Z, ProbeSettings.getRetractDistance()).getPositionIn(UnitUtils.Units.MM);
            retractZ(publisher, RETRACT, retractDistance);


            // We should probably wait for the jog command to complete, but we don't have a way to do that yet
            publisher.submit(new ProbeEvent.StepStarted(WAITING));
            Thread.sleep(ProbeSettings.getDelayAfterRetract().convertTo(Unit.MILLISECONDS).longValue());
            publisher.submit(new ProbeEvent.StepCompleted(WAITING));

            publisher.submit(new ProbeEvent.StepStarted(SLOW_FIND));
            double probedZ = probeZ(getSafeProbeZDistance(), ProbeSettings.getSlowFindRate())
                    .get()
                    .map(p -> p.getPositionIn(UnitUtils.Units.MM).getZ())
                    .orElseThrow(() -> {
                        publisher.submit(new ProbeEvent.StepFailed(SLOW_FIND));
                        return new ProbeException("Second probe failed");
                    });
            publisher.submit(new ProbeEvent.StepCompleted(SLOW_FIND));

            // Reset zero minus the probe plate
            backend.setWorkPosition(PartialPosition.from(Axis.Z, settings.zPlateThicknessProperty().getValue()));

            // Jog back to the starting Z position
            double probedDistance = previousZ - probedZ;
            retractZ(publisher, MOVE_TO_ORIGIN, PartialPosition.from(Axis.Z, probedDistance, UnitUtils.Units.MM).getPositionIn(UnitUtils.Units.MM));

            backend.getController().restoreParserModalState();
            publisher.submit(new ProbeEvent.JobCompleted());
        } catch (Exception e) {
            publisher.submit(new ProbeEvent.JobFailed(e));
        }
    }

    private void retractZ(SubmissionPublisher<ProbeEvent> publisher, ProbeStep step, PartialPosition retractDistance) throws ProbeException {
        try {
            publisher.submit(new ProbeEvent.StepStarted(step));
            GcodeCommand command = backend.getController().createCommand("G21 G91 G0 " + retractDistance.getPositionIn(UnitUtils.Units.MM).getFormattedGCode(Utils.formatter));
            command.setTemporaryParserModalChange(true);
            ControllerUtils.sendAndWaitForCompletion(backend.getController(), command, Duration.of(1, ChronoUnit.MINUTES));

            if (command.isOk()) {
                publisher.submit(new ProbeEvent.StepCompleted(step));
            } else {
                throw new ProbeException("Could not retract the probe");
            }
        } catch (Exception e) {
            publisher.submit(new ProbeEvent.StepFailed(step));
            throw new ProbeException("Could not retract the probe", e);
        }
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
