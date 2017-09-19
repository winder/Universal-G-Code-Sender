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

//import com.github.zevada.stateful.StateMachine;
//import com.github.zevada.stateful.StateMachineBuilder;
import static com.willwinder.ugs.platform.probe.ProbeService2.Event.Idle;
import static com.willwinder.ugs.platform.probe.ProbeService2.Event.Probed;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.Waiting;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;

import com.willwinder.ugs.platform.probe.stateful.StateMachine;
import com.willwinder.ugs.platform.probe.stateful.StateMachineBuilder;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import org.openide.util.Exceptions;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.Setup;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.Probe1;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.SmallRetract1;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.Slow1;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.StoreYReset;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.Probe2;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.SmallRetract2;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.Slow2;
import static com.willwinder.ugs.platform.probe.ProbeService2.Outside.StoreXFinalize;
import static com.willwinder.ugs.platform.probe.ProbeService2.Event.Start;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Position;

/**
 *
 * @author wwinder
 */
public class ProbeService2 implements UGSEventListener {
    private StateMachine stateMachine = null;
    private ProbeContext context = null;

    protected final BackendAPI backend;

    public ProbeService2(BackendAPI backend) {
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
        public final double feedRate;
        public final double feedRateSlow;
        public final double retractHeight;
        public final int wcsToUpdate;
        public final Units units;

        // Results
        public final Position startPosition;
        public Position probePosition1;
        public Position probePosition2;

        public ProbeContext(double diameter, Position start,
                double xSpacing, double ySpacing, double zSpacing,
                double feedRate, double feedRateSlow, double retractHeight,
                Units u, int wcs) {
            this.probeDiameter = diameter;
            this.startPosition = start;
            this.xSpacing = xSpacing;
            this.ySpacing = ySpacing;
            this.zSpacing = zSpacing;
            this.feedRate = feedRate;
            this.feedRateSlow = feedRateSlow;
            this.retractHeight = retractHeight;
            this.units = u;
            this.wcsToUpdate = wcs;
        }
    }

    static enum Outside {
        Waiting, Setup, Probe1, SmallRetract1, Slow1, StoreYReset, Probe2, SmallRetract2, Slow2, StoreXFinalize
    }

    static enum Z {
        Waiting, Fast1, SmallRetract1, Slow1, Finalize
    }

    static enum Event {
        Start, Probed, Idle;
    }

    private void validateState() {
        if (!backend.isIdle()) {
            throw new IllegalStateException("Can only begin probing while IDLE.");
        }

        if (stateMachine != null) {
            throw new IllegalStateException("A probe operation is already active.");
        }
    }

    private static String getG0For(Units u) {
        return u == Units.MM ? "G91 G21 G0" : "G91 G20 G0";
    }

    void performZProbe(ProbeContext context) throws IllegalStateException {
        validateState();

        String g0 = getG0For(context.units);

        this.context = context;
        stateMachine = new StateMachineBuilder<Z, Event, ProbeContext>(Z.Waiting)
                .addTransition(Z.Waiting,       Start,      Z.Fast1)
                .addTransition(Z.Fast1,         Probed,     Z.SmallRetract1)
                .addTransition(Z.SmallRetract1, Idle,       Z.Slow1)
                .addTransition(Z.Slow1,         Probed,     Z.Finalize)

                .onEnter(Z.Fast1,           c -> probe('Z', context.feedRate, context.zSpacing, context.units))
                .onEnter(Z.SmallRetract1,   c -> gcode(g0 + " Z" + retractDistance(c.zSpacing)))
                .onEnter(Z.Slow1,           c -> probe('Z', context.feedRateSlow, context.zSpacing, context.units))
                .onEnter(Z.Finalize,        c -> setup(Z.Finalize, c))

                .throwOnNoOpApply(false)
                .build();

        stateMachine.apply(Start, context);
    }

    void performOutsideCornerProbe(ProbeContext context) throws IllegalStateException {
        validateState();

        String g0 = getG0For(context.units);

        this.context = context;
        stateMachine = new StateMachineBuilder<Outside, Event, ProbeContext>(Outside.Waiting)
                .addTransition(Waiting,       Start,      Setup)
                .addTransition(Setup,         Idle,       Probe1)
                .addTransition(Probe1,        Probed,     SmallRetract1)
                .addTransition(SmallRetract1, Idle,       Slow1)
                .addTransition(Slow1,         Probed,     StoreYReset)
                .addTransition(StoreYReset,   Idle,       Probe2)
                .addTransition(Probe2,        Probed,     SmallRetract2)
                .addTransition(SmallRetract2, Idle,       Slow2)
                .addTransition(Slow2,         Probed,     StoreXFinalize)

                .onEnter(Setup,           c -> gcode(g0 + " X" + c.xSpacing))
                .onEnter(Probe1,          c -> probe('Y', c.feedRate, c.ySpacing, c.units))
                .onEnter(SmallRetract1,   c -> gcode(g0 + " Y" + retractDistance(c.ySpacing)))
                .onEnter(Slow1,           c -> probe('Y', c.feedRateSlow, c.ySpacing, c.units))
                .onEnter(StoreYReset,     c -> setup(StoreYReset, c))
                .onEnter(Probe2,          c -> probe('X', c.feedRate, c.xSpacing, c.units))
                .onEnter(SmallRetract2,   c -> gcode(g0 + " X" + retractDistance(c.xSpacing)))
                .onEnter(Slow2,           c -> probe('X', c.feedRateSlow, c.xSpacing, c.units))
                .onEnter(StoreXFinalize,  c -> setup(StoreXFinalize, c))

                .throwOnNoOpApply(false)
                .build();

        stateMachine.apply(Start, context);
    }

    public void setup(Z s, ProbeContext context) {
        String g0 = getG0For(context.units);
        context.probePosition1 = context.event.getProbePosition();
        gcode(g0 + " Z" + context.retractHeight);

        stateMachine = null;
    }

    // Outside probe callbacks.
    public void setup(Outside s, ProbeContext context) {
        String g0 = getG0For(context.units);
        try {
            switch(s) {
                case StoreYReset:
                {
                    context.probePosition1 = context.event.getProbePosition();
                    double offset =  context.startPosition.y - context.probePosition1.y;
                    backend.sendGcodeCommand(true, g0 + " Y" + offset);
                    backend.sendGcodeCommand(true, g0 + " X" + -context.xSpacing);
                    backend.sendGcodeCommand(true, g0 + " Y" + context.ySpacing);
                    break;
                }
                case StoreXFinalize:
                {
                    context.probePosition2 = context.event.getProbePosition();
                    double offset =  context.startPosition.x - context.probePosition2.x;
                    backend.sendGcodeCommand(true, g0 + " X" + offset);
                    backend.sendGcodeCommand(true, g0 + " Y" + -context.ySpacing);

                    // TODO: Update WCS.
                    // Done.
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
