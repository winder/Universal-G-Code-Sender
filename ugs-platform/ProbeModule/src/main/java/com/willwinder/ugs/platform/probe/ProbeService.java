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

import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.*;

import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;
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
    private static Logger logger = Logger.getLogger(ProbeService.class.getName());
    protected static BackendAPI backend;
    private List<Position> probePositions = new ArrayList<>();
    static String WCS_PATTERN = "G10 L20 P%d %s";
    private ProbeOperation currentOperation = ProbeOperation.NONE;
    private ProbeParameters params = null;
    private Finalizer finalizer = null;

    @FunctionalInterface
    private interface Finalizer {
        public void finalize(ProbeParameters params, List<Position> probePositions)
                throws Exception;
    }

    private enum ProbeOperation {
        NONE         (0                       ),
        Z            (2, zFinalizer           ),
        OUTSIDE_XY   (4, outsideXYFinalizer   ),
        OUTSIDE_XYZ  (6, outsideXYZFinalizer  ),
        //INSIDE_XY    (4, insideXYFinalizer    ),
        //INSIDE_CIRCLE(4, insideCircleFinalizer)
        ;

        private final int numProbes;
        private final Finalizer finalizer;

        private ProbeOperation(int probes) {
            this(probes, null);
        }

        private ProbeOperation(int probes, Finalizer finalizer) {
            this.numProbes = probes;
            this.finalizer = finalizer;
        }

        /**
         * @return Expected number of probes.
         */
        public int getNumProbes() { return numProbes; }

        public void finalize(ProbeParameters params, List<Position> probePositions) throws Exception {
          if (finalizer != null) {
            finalizer.finalize(params, probePositions);
          }
        }
    };

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
        public final double retractHeight;
        public final WorkCoordinateSystem wcsToUpdate;
        public final Units units;

        // Results
        public final Position startPosition;
        public Position endPosition;

        public ProbeParameters(double diameter, Position start,
                double xSpacing, double ySpacing, double zSpacing,
                double xOffset, double yOffset, double zOffset,
                double feedRate, double feedRateSlow, double retractHeight,
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
            this.retractHeight = retractHeight;
            this.units = u;
            this.wcsToUpdate = wcs;
        }
    }

    public ProbeService(BackendAPI backend) {
        this.backend = backend;
        this.backend.addUGSEventListener(this);
    }

    protected static double retractDistance(double spacing) {
        return (spacing < 0) ? 1 : -1;
    }

    private void resetProbe() {
        this.probePositions.clear();
        this.finalizer = null;
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

        try {
            String unit = GcodeUtils.unitCommand(params.units);
            probe('Z', params.feedRate, params.zSpacing, params.units);
            gcode("G91 " + unit + " G0 Z" + retractDistance(params.zSpacing));
            probe('Z', params.feedRateSlow, params.zSpacing, params.units);

            this.finalizer = zFinalizer;
            this.params = params;
            this.currentOperation = ProbeOperation.Z;
        } catch (Exception e) {
            resetProbe();
            logger.log(Level.SEVERE, "Exception during z probe operation.", e);
        }
    }

    private static Finalizer zFinalizer = (params, list) -> {
        if (list.size() != 2) throw new IllegalArgumentException("Unexpected number of probe positions.");
        Position probe = list.get(1);
        updateWCS(params.wcsToUpdate, null, null, params.zOffset);

        // Final retract
        String g = GcodeUtils.unitCommand(params.units);
        String g0 = "G91 " + g + " G0";
        if (params.zSpacing < 0 || params.retractHeight < params.zOffset) {
            gcode(g0 + " Z" + params.retractHeight);
        }
    };

    void performOutsideCornerProbe(ProbeParameters params) throws IllegalStateException {
        validateState();

        String g = GcodeUtils.unitCommand(params.units);
        String g0Abs = "G90 " + g + " G0";
        String g0Rel = "G91 " + g + " G0";

        try {
            // Reset (0,0,_) to make it easier to retract.
            updateWCS(params.wcsToUpdate, 0.0, 0.0, null);

            gcode(g0Abs + " X" + params.xSpacing);
            probe('Y', params.feedRate, params.ySpacing, params.units);
            gcode(g0Rel + " Y" + retractDistance(params.ySpacing));
            probe('Y', params.feedRateSlow, params.ySpacing, params.units);
            gcode(g0Abs + " Y0.0");
            gcode(g0Abs + " X0.0");
            gcode(g0Abs + " Y" + params.ySpacing);
            probe('X', params.feedRate, params.xSpacing, params.units);
            gcode(g0Rel + " X" + retractDistance(params.xSpacing));
            probe('X', params.feedRateSlow, params.xSpacing, params.units);
            gcode(g0Abs + " X0.0");
            gcode(g0Abs + " Y0.0");

            this.finalizer = outsideXYFinalizer;
            this.params = params;
            this.currentOperation = ProbeOperation.OUTSIDE_XY;
        } catch (Exception e) {
            resetProbe();
            logger.log(Level.SEVERE, "Exception during outside corner probe operation.", e);
        }
    }

    private static Finalizer outsideXYFinalizer = (params, list) -> {
        if (list.size() != 4) throw new IllegalArgumentException("Unexpected number of probe positions.");

        Position probeY = list.get(1);
        Position probeX = list.get(3);

        double radius = params.probeDiameter / 2;
        double xDir = Math.signum(params.xSpacing) * -1;
        double yDir = Math.signum(params.ySpacing) * -1;
        double xProbedOffset = xDir * (radius + params.xOffset);
        double yProbedOffset = yDir * (radius + params.yOffset);

        updateWCS(params.wcsToUpdate,
                params.startPosition.x - probeX.x + xProbedOffset,
                params.startPosition.y - probeY.y + yProbedOffset,
                null);
    };

    void performXYZProbe(ProbeParameters params) throws IllegalStateException {
        validateState();

        String g = GcodeUtils.unitCommand(params.units);

        String g0Abs = "G90 " + g + " G0";
        String g0Rel = "G91 " + g + " G0";

        try {
            // Reset (0,0,0) to make it easier to retract.
            updateWCS(params.wcsToUpdate, 0.0, 0.0, 0.0);

            // Z
            probe('Z', params.feedRate, params.zSpacing, params.units);
            gcode(g0Rel + " Z" + retractDistance(params.zSpacing));
            probe('Z', params.feedRateSlow, params.zSpacing, params.units);
            gcode(g0Abs + " Z0.0");
            gcode(g0Abs + " X" + -params.xSpacing);
            gcode(g0Abs + " Z" + params.zSpacing); // Probe motion for safety?

            // X
            probe('X', params.feedRate, params.xSpacing, params.units);
            gcode(g0Rel + " X" + retractDistance(params.xSpacing));
            probe('X', params.feedRateSlow, params.xSpacing, params.units);
            gcode(g0Abs + " X" + -params.xSpacing);
            gcode(g0Abs + " Y" + -params.ySpacing);
            gcode(g0Abs + " X" + params.xSpacing);

            // Y
            probe('Y', params.feedRate, params.ySpacing, params.units);
            gcode(g0Rel + " Y" + retractDistance(params.ySpacing));
            probe('Y', params.feedRateSlow, params.ySpacing, params.units);
            gcode(g0Abs + " Y" + -params.ySpacing);

            // Back to zero
            gcode(g0Abs + " Z0.0");
            gcode(g0Abs + " X0.0 Y0.0");

            this.finalizer = outsideXYZFinalizer;
            this.params = params;
            this.currentOperation = ProbeOperation.OUTSIDE_XYZ;
        } catch (Exception e) {
            resetProbe();
            logger.log(Level.SEVERE, "Exception during XYZ probe operation.", e);
        }
    }

    private static Finalizer outsideXYZFinalizer = (params, list) -> {
        if (list.size() != 6) {
          throw new IllegalArgumentException("Unexpected number of probe positions.");
        }

        Position probeX = list.get(3);
        Position probeY = list.get(5);
        Position probeZ = list.get(1);

        double radius = params.probeDiameter / 2;
        double xDir = Math.signum(params.xSpacing) * -1;
        double yDir = Math.signum(params.ySpacing) * -1;
        double zDir = Math.signum(params.zSpacing) * -1;
        double xProbedOffset = xDir * (radius + params.xOffset);
        double yProbedOffset = yDir * (radius + params.yOffset);
        double zProbedOffset = zDir * params.zOffset;

        updateWCS(params.wcsToUpdate,
                params.startPosition.x - probeX.x + xProbedOffset,
                params.startPosition.y - probeY.y + yProbedOffset,
                params.startPosition.z - probeZ.z + zProbedOffset);
    };

    private static void updateWCS(WorkCoordinateSystem wcs, Double x, Double y, Double z) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (x != null) {
            sb.append("X").append(x);
        }
        if (y != null) {
            sb.append("Y").append(y);
        }
        if (z != null) {
            sb.append("Z").append(z);
        }

        gcode(String.format(WCS_PATTERN, wcs.getPValue(), sb.toString()));
    }

    /**
     * Send a gcode command and handle any possible error.
     */
    private static void gcode(String s) throws Exception {
        backend.sendGcodeCommand(true, s);
    }

    /**
     * Send a probe command and handle any possible error.
     */
    private static void probe(char axis, double rate, double distance, Units u) throws Exception {
        backend.probe(String.valueOf(axis), rate, distance, u);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (this.currentOperation == ProbeOperation.NONE) return;

        switch(evt.getEventType()){
            case STATE_EVENT:
                switch(evt.getControlState()) {
                  case COMM_DISCONNECTED:
                      resetProbe();
                      break;
                  case COMM_IDLE:
                      // Finalize
                      if (this.currentOperation.getNumProbes() <= this.probePositions.size()) {
                          try {
                              this.currentOperation.finalize(params, probePositions);
                          } catch (Exception e) {
                              logger.log(Level.SEVERE,
                                      "Exception finalizing " + this.currentOperation + " probe operation.", e);
                          } finally {
                              params.endPosition = this.backend.getMachinePosition();
                              this.resetProbe();
                          }
                      }
                      break;
                  default:
                      break;
                }
                break;
            case PROBE_EVENT:
                this.probePositions.add(evt.getProbePosition());
                break;
            case FILE_EVENT:
                default:
                return;
        }
    }
}
