/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.ugs.platform.probe;

import com.google.common.base.Preconditions;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.ProbeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Methods that run various probe routines.
 *
 * @author wwinder
 */
public class ProbeService implements UGSEventListener {
    private static final Logger logger = Logger.getLogger(ProbeService.class.getName());
    private static final String WCS_PATTERN = "G10 L20 P%d %s";

    private final BackendAPI backend;
    private final List<Position> probePositions = new ArrayList<>();
    private ProbeOperation currentOperation = ProbeOperation.NONE;
    private ProbeParameters params = null;
    private Continuation continuation = null;

    @FunctionalInterface
    private interface Continuation {
        void execute() throws Exception;
    }

    private enum ProbeOperation {
        NONE(0),
        Z(2),
        OUTSIDE_XY(4),
        OUTSIDE_XYZ(6),
        //INSIDE_XY    (4),
        //INSIDE_CIRCLE(4)
        ;

        private final int numProbes;

        ProbeOperation(int probes) {
            this.numProbes = probes;
        }

        /**
         * @return Expected number of probes.
         */
        public int getNumProbes() {
            return numProbes;
        }
    }

    /**
     * Parameters passed into the probe operations.
     */
    public static class ProbeParameters {
        public String errorMessage;
        public UGSEvent event;
        public final double probeDiameter;
        public final double xSpacing;
        public final double ySpacing;
        public final double zSpacing;
        public final double xOffset;
        public final double yOffset;
        public final double zOffset;
        public final double feedRate;
        public final double feedRateSlow;
        public final double retractAmount;
        public final WorkCoordinateSystem wcsToUpdate;
        public final Units units;

        // Results
        public final Position startPosition;
        public Position endPosition;

        public ProbeParameters(double diameter, Position start,
                double xSpacing, double ySpacing, double zSpacing,
                double xOffset, double yOffset, double zOffset,
                double feedRate, double feedRateSlow, double retractAmount,
                Units u, WorkCoordinateSystem wcs) {
            this.endPosition = null;
            this.probeDiameter = diameter;
            this.startPosition = start;
            this.xSpacing = xSpacing;
            this.ySpacing = ySpacing;
            this.zSpacing = zSpacing;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.feedRate = feedRate;
            this.feedRateSlow = feedRateSlow;
            this.retractAmount = retractAmount;
            this.units = u;
            this.wcsToUpdate = wcs;
        }
    }

    public ProbeService(BackendAPI backend) {
        this.backend = backend;
        this.backend.addUGSEventListener(this);
    }

    protected static double retractDistance(double spacing, double retractAmount) {
        return (spacing < 0) ? retractAmount : -1 * retractAmount;
    }

    private void resetProbe() {
        this.probePositions.clear();
        this.continuation = null;
        this.params = null;
        this.currentOperation = ProbeOperation.NONE;
    }

    public boolean probeCycleActive() {
        return this.currentOperation != ProbeOperation.NONE;
    }

    private void validateState() {
        if (!backend.isIdle()) {
            throw new IllegalStateException("Can only begin probing while IDLE.");
        }
    }

    void performZProbe(ProbeParameters params) throws IllegalStateException {
        validateState();
        currentOperation = ProbeOperation.Z;
        this.params = params;
        performZProbeInternal(0);
    }

    private void performZProbeInternal(int stepNumber) throws IllegalStateException {
        String unit = GcodeUtils.unitCommand(params.units);

        continuation = () -> performZProbeInternal(stepNumber + 1);
        try {
            switch (stepNumber) {
                case 0: {
                    // Reset (_, _, 0) to make it easier to retract.
                    updateWCS(params.wcsToUpdate, null, null, 0.0);

                    probe('Z', params.feedRate, params.zSpacing, params.units);
                    break;
                }
                case 1: {
                    gcode("G91 " + unit + " G0 Z" + retractDistance(params.zSpacing, params.retractAmount));
                    probe('Z', params.feedRateSlow, params.zSpacing, params.units);
                    break;
                }
                case 2: {
                    // Back to zero
                    String g0Abs = "G90 " + unit + " G0";
                    gcode(g0Abs + " Z0.0");
                    break;
                }
                case 3: {
                    // Once idle, perform calculations.
                    Preconditions.checkState(probePositions.size() == 2, "Unexpected number of probe positions.");
                    Position probe = probePositions.get(1).getPositionIn(params.units);

                    double zDir = Math.signum(params.zSpacing) * -1;
                    double zProbedOffset = zDir * params.zOffset;

                    Position startPositionInUnits = params.startPosition.getPositionIn(params.units);
                    updateWCS(params.wcsToUpdate,
                            null,
                            null,
                            startPositionInUnits.z - probe.z + zProbedOffset);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Invalid step number: " + stepNumber);
            }
        } catch (Exception e) {
            resetProbe();
            logger.log(Level.SEVERE, "Exception during z probe operation.", e);
        }
    }

    void performOutsideCornerProbe(ProbeParameters params) throws IllegalStateException {
        validateState();
        currentOperation = ProbeOperation.OUTSIDE_XY;
        this.params = params;
        performOutsideCornerProbeInternal(0);
    }

    private void performOutsideCornerProbeInternal(int stepNumber) throws IllegalStateException {

        String g = GcodeUtils.unitCommand(params.units);
        String g0Abs = "G90 " + g + " G0";
        String g0Rel = "G91 " + g + " G0";

        continuation = () -> performOutsideCornerProbeInternal(stepNumber + 1);

        try {
            switch (stepNumber) {
                case 0: {
                    // Reset (0,0,_) to make it easier to retract.
                    updateWCS(params.wcsToUpdate, 0.0, 0.0, null);

                    gcode(g0Abs + " X" + params.xSpacing);
                    probe('Y', params.feedRate, params.ySpacing, params.units);
                    break;
                }
                case 1: {
                    gcode(g0Rel + " Y" + retractDistance(params.ySpacing, params.retractAmount));
                    probe('Y', params.feedRateSlow, params.ySpacing, params.units);
                    break;
                }
                case 2: {
                    gcode(g0Abs + " Y0.0");
                    gcode(g0Abs + " X0.0");
                    gcode(g0Abs + " Y" + params.ySpacing);
                    probe('X', params.feedRate, params.xSpacing, params.units);
                    break;
                }
                case 3: {
                    gcode(g0Rel + " X" + retractDistance(params.xSpacing, params.retractAmount));
                    probe('X', params.feedRateSlow, params.xSpacing, params.units);
                    break;
                }
                case 4: {
                    gcode(g0Abs + " X0.0");
                    gcode(g0Abs + " Y0.0");
                    break;
                }
                case 5: {
                    // Once idle, perform calculations.
                    Preconditions.checkState(probePositions.size() == 4, "Unexpected number of probe positions.");

                    Position probeY = probePositions.get(1).getPositionIn(params.units);
                    Position probeX = probePositions.get(3).getPositionIn(params.units);

                    double radius = params.probeDiameter / 2;
                    double xDir = Math.signum(params.xSpacing) * -1;
                    double yDir = Math.signum(params.ySpacing) * -1;
                    double xProbedOffset = xDir * (radius + params.xOffset);
                    double yProbedOffset = yDir * (radius + params.yOffset);

                    Position startPositionInUnits = params.startPosition.getPositionIn(params.units);
                    updateWCS(params.wcsToUpdate,
                            startPositionInUnits.x - probeX.x + xProbedOffset,
                            startPositionInUnits.y - probeY.y + yProbedOffset,
                            null);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Invalid step number: " + stepNumber);
            }
        } catch (Exception e) {
            resetProbe();
            logger.log(Level.SEVERE, "Exception during outside corner probe operation.", e);
        }
    }

    void performXYZProbe(ProbeParameters params) throws IllegalStateException {
        validateState();
        currentOperation = ProbeOperation.OUTSIDE_XYZ;
        this.params = params;
        performXYZProbeInternal(0);
    }

    private void performXYZProbeInternal(int stepNumber) throws IllegalStateException {
        String g = GcodeUtils.unitCommand(params.units);

        String g0Abs = "G90 " + g + " G0";
        String g0Rel = "G91 " + g + " G0";

        continuation = () -> performXYZProbeInternal(stepNumber + 1);
        try {
            switch (stepNumber) {
                case 0: {
                    // Reset (0,0,0) to make it easier to retract.
                    updateWCS(params.wcsToUpdate, 0.0, 0.0, 0.0);

                    // Z
                    probe('Z', params.feedRate, params.zSpacing, params.units);
                    break;
                }
                case 1: {
                    gcode(g0Rel + " Z" + retractDistance(params.zSpacing, params.retractAmount));
                    probe('Z', params.feedRateSlow, params.zSpacing, params.units);
                    break;
                }
                case 2: {
                    gcode(g0Abs + " Z0.0");
                    gcode(g0Abs + " X" + -params.xSpacing);
                    gcode(g0Abs + " Z" + params.zSpacing); // Probe motion for safety?

                    // X
                    probe('X', params.feedRate, params.xSpacing, params.units);
                    break;
                }
                case 3: {
                    gcode(g0Rel + " X" + retractDistance(params.xSpacing, params.retractAmount));
                    probe('X', params.feedRateSlow, params.xSpacing, params.units);
                    break;
                }
                case 4: {
                    gcode(g0Abs + " X" + -params.xSpacing);
                    gcode(g0Abs + " Y" + -params.ySpacing);
                    gcode(g0Abs + " X" + params.xSpacing);

                    // Y
                    probe('Y', params.feedRate, params.ySpacing, params.units);
                    break;
                }
                case 5: {
                    gcode(g0Rel + " Y" + retractDistance(params.ySpacing, params.retractAmount));
                    probe('Y', params.feedRateSlow, params.ySpacing, params.units);
                    break;
                }
                case 6: {
                    gcode(g0Abs + " Y" + -params.ySpacing);

                    // Back to zero
                    gcode(g0Abs + " Z0.0");
                    gcode(g0Abs + " X0.0 Y0.0");
                    break;
                }
                case 7: {
                    // Once idle, perform calculations.
                    Preconditions.checkState(probePositions.size() == 6, "Unexpected number of probe positions.");

                    Position probeX = probePositions.get(3).getPositionIn(params.units);
                    Position probeY = probePositions.get(5).getPositionIn(params.units);
                    Position probeZ = probePositions.get(1).getPositionIn(params.units);

                    double radius = params.probeDiameter / 2;
                    double xDir = Math.signum(params.xSpacing) * -1;
                    double yDir = Math.signum(params.ySpacing) * -1;
                    double zDir = Math.signum(params.zSpacing) * -1;
                    double xProbedOffset = xDir * (radius + params.xOffset);
                    double yProbedOffset = yDir * (radius + params.yOffset);
                    double zProbedOffset = zDir * params.zOffset;

                    Position startPositionInUnits = params.startPosition.getPositionIn(params.units);
                    updateWCS(params.wcsToUpdate,
                            startPositionInUnits.x - probeX.x + xProbedOffset,
                            startPositionInUnits.y - probeY.y + yProbedOffset,
                            startPositionInUnits.z - probeZ.z + zProbedOffset);
                    break;
                }
                default:
                    throw new UnsupportedOperationException("Invalid step number: " + stepNumber);
            }
        } catch (Exception e) {
            resetProbe();
            logger.log(Level.SEVERE, "Exception during XYZ probe operation.", e);
        }
    }

    private void updateWCS(WorkCoordinateSystem wcs, Double x, Double y, Double z) throws Exception {
        StringBuilder sb = new StringBuilder();
        // Format the x, y, and z to prevent printing with double "E" notation.
        if (x != null) {
            sb.append("X").append(Utils.formatter.format(x));
        }
        if (y != null) {
            sb.append("Y").append(Utils.formatter.format(y));
        }
        if (z != null) {
            sb.append("Z").append(Utils.formatter.format(z));
        }

        gcode(String.format(WCS_PATTERN, wcs.getPValue(), sb.toString()));
    }

    /**
     * Send a gcode command and handle any possible error.
     */
    private void gcode(String s) throws Exception {
        backend.sendGcodeCommand(true, s);
    }

    /**
     * Send a probe command and handle any possible error.
     */
    private void probe(char axis, double rate, double distance, Units u) throws Exception {
        backend.probe(String.valueOf(axis), rate, distance, u);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (this.currentOperation == ProbeOperation.NONE) return;

        if (evt instanceof ControllerStateEvent) {
            ControllerStateEvent controllerStateEvent = (ControllerStateEvent) evt;
            ControllerState state = controllerStateEvent.getState();
            if (state == ControllerState.DISCONNECTED || state == ControllerState.UNKNOWN) {
                resetProbe();
            } else if (state == ControllerState.IDLE) {
                // Finalize
                if (this.currentOperation.getNumProbes() <= this.probePositions.size()) {
                    try {
                        continuation.execute();
                    } catch (Exception e) {
                        logger.log(Level.SEVERE,
                                "Exception finalizing " + this.currentOperation + " probe operation.", e);
                    } finally {
                        params.endPosition = this.backend.getMachinePosition();
                        this.resetProbe();
                    }
                }
            }
        } else if (evt instanceof ProbeEvent) {
            this.probePositions.add(((ProbeEvent)evt).getProbePosition());
            try {
                continuation.execute();
            } catch (Exception e) {
                logger.log(Level.SEVERE,
                        "Exception during " + this.currentOperation + " probe operation.", e);
                resetProbe();
            }
        }
    }
}
