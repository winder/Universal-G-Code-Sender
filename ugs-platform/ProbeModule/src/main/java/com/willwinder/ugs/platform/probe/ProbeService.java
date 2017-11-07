/*
    Copyright 2017 Will Winder

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

import static com.willwinder.ugs.platform.probe.ProbeService.Event.Idle;
import static com.willwinder.ugs.platform.probe.ProbeService.Event.Probed;
import static com.willwinder.ugs.platform.probe.ProbeService.Event.Start;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.ProbeX;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.ProbeY;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.RetractX;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.RetractY;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.SeekX;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.SeekY;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.Setup;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.StoreXFinalize;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.StoreYReset;
import static com.willwinder.ugs.platform.probe.ProbeService.Outside.Waiting;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_DISCONNECTED;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;

import com.willwinder.ugs.platform.probe.stateful.StateMachine;
import com.willwinder.ugs.platform.probe.stateful.StateMachineBuilder;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;

import org.openide.util.Exceptions;

/**
 *
 * @author wwinder
 */
public class ProbeService implements UGSEventListener {
    private StateMachine stateMachine = null;
    private ProbeContext context = null;

    protected final BackendAPI backend;

    public ProbeService(BackendAPI backend) {
        this.backend = backend;
        this.backend.addUGSEventListener(this);
    }

    protected static double retractDistance(double spacing) {
        return (spacing < 0) ? 1 : -1;
    }

    /**
     * Context passed into state machine for each transition.
     */
    public static class ProbeContext {
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
        public Position probePositionX;
        public Position probePositionY;
        public Position probePositionZ;
        public Double xWcsOffset;
        public Double yWcsOffset;
        public Double zWcsOffset;

        public ProbeContext(double diameter, Position start,
                double xSpacing, double ySpacing, double zSpacing,
                double xOffset, double yOffset, double zOffset,
                double feedRate, double feedRateSlow, double retractHeight,
                Units u, WorkCoordinateSystem wcs) {
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

            this.probePositionY = null;
            this.probePositionX = null;
            this.xWcsOffset = null;
            this.yWcsOffset = null;
            this.zWcsOffset = null;
        }
    }

    public boolean probeCycleActive() {
        return this.stateMachine != null;
    }

    static enum Outside {
        Waiting, Setup,
        SeekY, RetractY, ProbeY, StoreYReset,
        SeekX, RetractX, ProbeX, StoreXFinalize
    }

    static enum Z {
        Waiting,
        SeekZ, RetractZ, ProbeZ,
        Finalize
    }

    static enum OutsideXYZ {
        Waiting,
        SeekZ, RetractZ, ProbeZ, StoreZReset,
        SeekX, RetractX, ProbeX, StoreXReset,
        SeekY, RetractY, PorbeY, StoreYFinalize
    }

    static enum Event {
        Start, Probed, Idle;
    }

    private void validateState() {
        if (!backend.isIdle()) {
            throw new IllegalStateException("Can only begin probing while IDLE.");
        }

        /*
        if (stateMachine != null) {
            throw new IllegalStateException("A probe operation is already active.");
        }
        */
    }

    void performZProbe(ProbeContext context) throws IllegalStateException {
        validateState();

        String g = GcodeUtils.unitCommand(context.units);
        String g0 = "G91 " + g + " G0";

        this.context = context;
        stateMachine = new StateMachineBuilder<Z, Event, ProbeContext>(Z.Waiting)
                .addTransition(Z.Waiting,  Start,  Z.SeekZ)
                .addTransition(Z.SeekZ,    Probed, Z.RetractZ)
                .addTransition(Z.RetractZ, Idle,   Z.ProbeZ)
                .addTransition(Z.ProbeZ,   Probed, Z.Finalize)

                .onEnter(Z.SeekZ,    c -> probe('Z', context.feedRate, context.zSpacing, context.units))
                .onEnter(Z.RetractZ, c -> gcode(g0 + " Z" + retractDistance(c.zSpacing)))
                .onEnter(Z.ProbeZ,   c -> probe('Z', context.feedRateSlow, context.zSpacing, context.units))
                .onEnter(Z.Finalize, c -> finalizeZProbe(c))

                .throwOnNoOpApply(false)
                .build();

        stateMachine.apply(Start, context);
    }

    void performOutsideCornerProbe(ProbeContext context) throws IllegalStateException {
        validateState();

        String g = GcodeUtils.unitCommand(context.units);
        String g0 = "G91 " + g + " G0";

        this.context = context;
        stateMachine = new StateMachineBuilder<Outside, Event, ProbeContext>(Outside.Waiting)
                .addTransition(Waiting,     Start,  Setup)
                .addTransition(Setup,       Idle,   SeekY)
                .addTransition(SeekY,       Probed, RetractY)
                .addTransition(RetractY,    Idle,   ProbeY)
                .addTransition(ProbeY,      Probed, StoreYReset)
                .addTransition(StoreYReset, Idle,   SeekX)
                .addTransition(SeekX,       Probed, RetractX)
                .addTransition(RetractX,    Idle,   ProbeX)
                .addTransition(ProbeX,      Probed, StoreXFinalize)

                .onEnter(Setup,          c -> gcode(g0 + " X" + c.xSpacing))
                .onEnter(SeekY,          c -> probe('Y', c.feedRate, c.ySpacing, c.units))
                .onEnter(RetractY,       c -> gcode(g0 + " Y" + retractDistance(c.ySpacing)))
                .onEnter(ProbeY,         c -> probe('Y', c.feedRateSlow, c.ySpacing, c.units))
                .onEnter(StoreYReset,    c -> setup(StoreYReset, c))
                .onEnter(SeekX,          c -> probe('X', c.feedRate, c.xSpacing, c.units))
                .onEnter(RetractX,       c -> gcode(g0 + " X" + retractDistance(c.xSpacing)))
                .onEnter(ProbeX,         c -> probe('X', c.feedRateSlow, c.xSpacing, c.units))
                .onEnter(StoreXFinalize, c -> setup(StoreXFinalize, c))

                .throwOnNoOpApply(false)
                .build();

        stateMachine.apply(Start, context);
    }

    void performXYZProbe(ProbeContext context) throws IllegalStateException {
        validateState();

        String g = GcodeUtils.unitCommand(context.units);
        String g0 = "G91 " + g + " G0";

        this.context = context;
        stateMachine = new StateMachineBuilder<OutsideXYZ, Event, ProbeContext>(OutsideXYZ.Waiting)
                .addTransition(OutsideXYZ.Waiting,     Start,  OutsideXYZ.SeekZ)
                // Z Transitions
                .addTransition(OutsideXYZ.SeekZ,       Probed, OutsideXYZ.RetractZ)
                .addTransition(OutsideXYZ.RetractZ,    Idle,   OutsideXYZ.ProbeZ)
                .addTransition(OutsideXYZ.ProbeZ,      Probed, OutsideXYZ.StoreZReset)

                // X Transitions
                .addTransition(OutsideXYZ.StoreZReset, Idle,   OutsideXYZ.SeekX)
                .addTransition(OutsideXYZ.SeekX,       Probed, OutsideXYZ.RetractX)
                .addTransition(OutsideXYZ.RetractX,    Idle,   OutsideXYZ.ProbeX)
                .addTransition(OutsideXYZ.ProbeX,      Probed, OutsideXYZ.StoreXReset)

                // Y Transitions
                .addTransition(OutsideXYZ.StoreXReset, Idle,   OutsideXYZ.SeekY)
                .addTransition(OutsideXYZ.SeekY,       Probed, OutsideXYZ.RetractY)
                .addTransition(OutsideXYZ.RetractY,    Idle,   OutsideXYZ.PorbeY)
                .addTransition(OutsideXYZ.PorbeY,      Probed, OutsideXYZ.StoreYFinalize)

                // Z Callbacks
                .onEnter(OutsideXYZ.SeekZ,          c -> probe('Z', c.feedRate, c.zSpacing, c.units))
                .onEnter(OutsideXYZ.RetractZ,       c -> gcode(g0 + " Z" + retractDistance(c.zSpacing)))
                .onEnter(OutsideXYZ.ProbeZ,         c -> probe('Z', c.feedRateSlow, c.zSpacing, c.units))
                .onEnter(OutsideXYZ.StoreZReset,    c -> setup(OutsideXYZ.StoreZReset, c))

                // X Callbacks
                .onEnter(OutsideXYZ.SeekX,          c -> probe('X', c.feedRate, c.xSpacing, c.units))
                .onEnter(OutsideXYZ.RetractX,       c -> gcode(g0 + " X" + retractDistance(c.xSpacing)))
                .onEnter(OutsideXYZ.ProbeX,         c -> probe('X', c.feedRateSlow, c.xSpacing, c.units))
                .onEnter(OutsideXYZ.StoreXReset,    c -> setup(OutsideXYZ.StoreXReset, c))

                // Y Callbacks
                .onEnter(OutsideXYZ.SeekY,          c -> probe('Y', c.feedRate, c.ySpacing, c.units))
                .onEnter(OutsideXYZ.RetractY,       c -> gcode(g0 + " Y" + retractDistance(c.ySpacing)))
                .onEnter(OutsideXYZ.PorbeY,         c -> probe('Y', c.feedRateSlow, c.ySpacing, c.units))
                .onEnter(OutsideXYZ.StoreYFinalize, c -> setup(OutsideXYZ.StoreYFinalize, c))

                .throwOnNoOpApply(false)
                .build();

        stateMachine.apply(Start, context);
    }

    public void finalizeZProbe(ProbeContext context) {
        // Update WCS
        gcode("G10 L20 P" +context.wcsToUpdate.getPValue() + " Z"+ context.zOffset);

        String g = GcodeUtils.unitCommand(context.units);
        String g0 = "G90 " + g + " G0";
        if (context.zSpacing < 0) {
            gcode(g0 + " Z" + (context.retractHeight - context.zSpacing));
        }
        stateMachine = null;
    }

    // Outside probe callbacks.
    public void setup(Outside s, ProbeContext context) {
        String g = GcodeUtils.unitCommand(context.units);
        String g0 = "G91 " + g + " G0";
        double radius = context.probeDiameter / 2;
        double xDir = Math.signum(context.xSpacing) * -1;
        double yDir = Math.signum(context.ySpacing) * -1;
        double xProbedOffset = xDir * (radius + context.xOffset);
        double yProbedOffset = yDir * (radius + context.yOffset);
        try {
            switch(s) {
                case StoreYReset:
                {
                    //gcode("G10 L20 P" +context.wcsToUpdate.getPValue() + " Y"+ (context.yOffset + yRadiusOffset));

                    context.probePositionY = context.event.getProbePosition();
                    double offset =  context.startPosition.y - context.probePositionY.y;
                    backend.sendGcodeCommand(true, g0 + " Y" + offset);
                    backend.sendGcodeCommand(true, g0 + " X" + -context.xSpacing);
                    backend.sendGcodeCommand(true, g0 + " Y" + context.ySpacing);
                    break;
                }
                case StoreXFinalize:
                {
                    //gcode("G10 L20 P" +context.wcsToUpdate.getPValue() + " X"+ (context.xOffset + xRadiusOffset));

                    context.probePositionX = context.event.getProbePosition();
                    double offset =  context.startPosition.x - context.probePositionX.x;
                    backend.sendGcodeCommand(true, g0 + " X" + offset);
                    backend.sendGcodeCommand(true, g0 + " Y" + -context.ySpacing);

                    context.yWcsOffset = context.startPosition.y - context.probePositionY.y + yProbedOffset;
                    context.xWcsOffset = context.startPosition.x - context.probePositionX.x + xProbedOffset;
                    context.zWcsOffset = 0.;
                    gcode("G10 L20 P" +context.wcsToUpdate.getPValue()
                            + " X"+ context.xWcsOffset+ " Y"+ context.yWcsOffset);

                    stateMachine = null;
                    break;
                }
            }
        } catch (Exception ex) {
            stateMachine = null;
            Exceptions.printStackTrace(ex);
        }
    }

    // Outside probe callbacks.
    public void setup(OutsideXYZ s, ProbeContext context) {
        String g = GcodeUtils.unitCommand(context.units);
        String g0 = "G91 " + g + " G0";
        double radius = context.probeDiameter / 2;
        double xDir = Math.signum(context.xSpacing) * -1;
        double yDir = Math.signum(context.ySpacing) * -1;
        double zDir = Math.signum(context.zSpacing) * -1;
        double xProbedOffset = xDir * (radius + context.xOffset);
        double yProbedOffset = yDir * (radius + context.yOffset);
        double zProbedOffset = zDir * context.zOffset;
        try {
            switch(s) {
                case StoreZReset:
                {
                    //gcode("G10 L20 P" +context.wcsToUpdate.getPValue() + " Y"+ (context.yOffset + yRadiusOffset));

                    context.probePositionZ = context.event.getProbePosition();
                    double offset =  context.startPosition.z - context.probePositionZ.z;
                    backend.sendGcodeCommand(true, g0 + " Z" + offset);
                    backend.sendGcodeCommand(true, g0 + " X" + -context.xSpacing);
                    backend.sendGcodeCommand(true, g0 + " Z" + context.zSpacing);
                    break;
                }

                case StoreXReset:
                {
                    context.probePositionX = context.event.getProbePosition();
                    double offset =  context.startPosition.x - context.xSpacing - context.probePositionX.x;
                    backend.sendGcodeCommand(true, g0 + " X" + offset);
                    backend.sendGcodeCommand(true, g0 + " Y" + -context.ySpacing);
                    backend.sendGcodeCommand(true, g0 + " X" + context.xSpacing);
                    break;
                }

                case StoreYFinalize:
                {
                    context.probePositionY = context.event.getProbePosition();
                    double offset =  context.startPosition.y - context.ySpacing - context.probePositionY.y;
                    backend.sendGcodeCommand(true, g0 + " Y" + offset);
                    backend.sendGcodeCommand(true, g0 + " Z" + -context.zSpacing);
                    backend.sendGcodeCommand(true, g0 + " Y" + context.ySpacing);

                    //gcode("G10 L20 P" +context.wcsToUpdate.getPValue() + " X"+ (context.xOffset + xRadiusOffset));
                    context.yWcsOffset = context.startPosition.y - context.probePositionY.y + yProbedOffset;
                    context.xWcsOffset = context.startPosition.x - context.probePositionX.x + xProbedOffset;
                    context.zWcsOffset = context.startPosition.z - context.probePositionZ.z + zProbedOffset;
                    gcode("G10 L20 P" +context.wcsToUpdate.getPValue()
                            + " X" + context.xWcsOffset + " Y" + context.yWcsOffset + " Z" + context.zWcsOffset);

                    stateMachine = null;
                    break;
                }
            }
        } catch (Exception ex) {
            stateMachine = null;
            Exceptions.printStackTrace(ex);
        }
    }

    public void gcode(String s) {
        try {
            backend.sendGcodeCommand(true, s);
        } catch (Exception ex) {
            stateMachine = null;
            Exceptions.printStackTrace(ex);
        }
    }

    public void probe(char axis, double rate, double distance, Units u) {
        try {
            backend.probe(String.valueOf(axis), rate, distance, u);
        } catch (Exception ex) {
            stateMachine = null;
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (stateMachine == null) return;
        context.event = evt;

        switch(evt.getEventType()){
            case STATE_EVENT:
                if (evt.getControlState() == COMM_IDLE){
                    stateMachine.apply(Idle, context);
                } if (evt.getControlState() == COMM_DISCONNECTED) {
                    stateMachine = null;
                }
                break;
            case PROBE_EVENT:
                stateMachine.apply(Probed, context);
                break;
            case FILE_EVENT:
                default:
                return;
        }
    }
}
