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
import static com.willwinder.ugs.platform.probe.ProbeService2.Event.Position;
import static com.willwinder.ugs.platform.probe.ProbeService2.Event.Probed;
import static com.willwinder.ugs.platform.probe.ProbeService2.Event.StartOC;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.Waiting;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocProbe1;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocProbe2;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocProbed1;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocProbed2;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocSetup;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocSlow1;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocSlow2;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocSmallRetract1;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocSmallRetract2;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocStoreXFinalize;
import static com.willwinder.ugs.platform.probe.ProbeService2.State.ocStoreYReset;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;

import com.willwinder.ugs.platform.probe.stateful.StateMachine;
import com.willwinder.ugs.platform.probe.stateful.StateMachineBuilder;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import org.openide.util.Exceptions;

/**
 *
 * @author wwinder
 */
public class ProbeService2 extends AbstractProbeService {
    private StateMachine<State, Event, ProbeContext> stateMachine = null;
    private ProbeContext context = null;

    public ProbeService2(BackendAPI backend) {
        super(backend);
    }

    static enum State {
        Waiting,
        // Outside corner
        ocSetup, ocProbe1, ocSmallRetract1, ocSlow1, ocProbed1, ocStoreYReset, ocProbe2,
        ocSmallRetract2, ocSlow2, ocProbed2, ocStoreXFinalize,
        // Inside corner
        icProbe1, icSmallRetract, icSlow1, icProbe2, icSmallRetract2, icSlow2, icFinalizing, icFinal,
    }

    static enum Event {
        StartInsideCornerProbe,
        StartOC,

        Probed, Position, Idle,

        Failure;
    }

    private void validateState() {
        if (!backend.isIdle()) {
            throw new IllegalStateException("Can only begin probing while IDLE.");
        }

        if (stateMachine != null) {
            throw new IllegalStateException("A probe operation is already active.");
        }
    }

    @Override
    void performOutsideCornerProbe(ProbeContext context) throws IllegalStateException {
        validateState();

        this.context = context;
        stateMachine = new StateMachineBuilder<State, Event, ProbeContext>(State.Waiting)
                .addTransition(Waiting,         StartOC,    ocSetup)
                .addTransition(ocSetup,         Idle,       ocProbe1)
                .addTransition(ocProbe1,        Probed,     ocSmallRetract1)
                .addTransition(ocSmallRetract1, Idle,       ocSlow1)
                .addTransition(ocSlow1,         Probed,     ocProbed1)
                .addTransition(ocProbed1,       Position,   ocStoreYReset)
                .addTransition(ocStoreYReset,   Idle,       ocProbe2)
                .addTransition(ocProbe2,        Probed,     ocSmallRetract2)
                .addTransition(ocSmallRetract2, Idle,       ocSlow2)
                .addTransition(ocSlow2,         Probed,     ocProbed2)
                .addTransition(ocProbed2,       Position,   ocStoreXFinalize)

                .onEnter(ocSetup,           c -> gcode("G91 G21 G0 X" + c.xSpacing))
                .onEnter(ocProbe1,          c -> probe('Y', c.feedRate, c.ySpacing, Units.MM))
                .onEnter(ocSmallRetract1,   c -> gcode("G91 G21 G0 Y" + retractDistance(c.ySpacing)))
                .onEnter(ocSlow1,           c -> probe('Y', c.feedRate/2, c.ySpacing, Units.MM))
                //.onEnter(ocProbed1,        c -> null);
                .onEnter(ocStoreYReset,     c -> setup(ocStoreYReset, c))
                .onEnter(ocProbe2,          c -> probe('X', c.feedRate, c.xSpacing, Units.MM))
                .onEnter(ocSmallRetract2,   c -> gcode("G91 G21 G0 X" + retractDistance(c.xSpacing)))
                .onEnter(ocSlow2,           c -> probe('X', c.feedRate/2, c.xSpacing, Units.MM))
                //.onEnter(ocProbed2,        c -> null);
                .onEnter(ocStoreXFinalize,  c -> setup(ocStoreXFinalize, c))

                .throwOnNoOpApply(false)
                .build();

        stateMachine.apply(StartOC, context);
    }

    public void gcode(String s) {
        try {
            backend.sendGcodeCommand(true, s);
        } catch (Exception ex) {
            stateMachine = null;
            Exceptions.printStackTrace(ex);
        }
    }

    public void setup(State s, ProbeContext context) {
        try {
            switch(s) {
                case ocStoreYReset:
                {
                    context.probePosition1 = context.event.getControllerStatus().getMachineCoord();
                    double offset =  context.startPosition.y - context.probePosition1.y;
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + offset);
                    backend.sendGcodeCommand(true, "G91 G21 G0 X" + -context.xSpacing);
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + context.ySpacing);
                    break;
                }
                case ocStoreXFinalize:
                {
                    context.probePosition2 = context.event.getControllerStatus().getMachineCoord();
                    double offset =  context.startPosition.x - context.probePosition2.x;
                    backend.sendGcodeCommand(true, "G91 G21 G0 X" + offset);
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + -context.ySpacing);

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

    public void probe(char axis, double rate, double distance, Units u) {
        try {
            backend.probe(String.valueOf(axis), rate, distance, u);
        } catch (Exception ex) {
            stateMachine = null;
            Exceptions.printStackTrace(ex);
        }
    }


    @Override
    void performInsideCornerProbe(ProbeContext context) throws IllegalStateException {
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
            case CONTROLLER_STATUS_EVENT:
                stateMachine.apply(Position, context);
                break;
            case FILE_EVENT:
                default:
                return;
        }
    }
}
