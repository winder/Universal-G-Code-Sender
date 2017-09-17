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

import static com.willwinder.ugs.platform.probe.ProbeService.CornerProbeState.*;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.StateMachineBuilderFactory;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;

/**
 *
 * @author wwinder
 */
public class ProbeService extends AbstractProbeService {
    private final StateMachine<CornerProbeMachine, CornerProbeState, ProbeEvent, ProbeContext> fsm;
    private ProbeContext probeContext;

    public ProbeService(BackendAPI backend) {
        super(backend);

        fsm = initStateMachine();
        probeContext = new ProbeContext(0, null, 0, 0, 0, 0);
    }

    private void validateState() {
        if (!backend.isIdle()) {
            throw new IllegalStateException("Can only begin probing while IDLE.");
        }

        CornerProbeState cps = fsm.getCurrentState();
        if (cps != null  && cps != CornerProbeState.Waiting) {
            throw new IllegalStateException("A probe operation is already active.");
        }
    }

    @Override
    public void performInsideCornerProbe(ProbeContext initialContext) throws IllegalStateException {
        validateState();
        this.probeContext = initialContext;
        fsm.fire(ProbeEvent.StartInsideCornerProbe, probeContext);
    }

    @Override
    public void performOutsideCornerProbe(ProbeContext initialContext) throws IllegalStateException {
        validateState();
        this.probeContext = initialContext;
        fsm.fire(ProbeEvent.StartOutsideCornerProbe, probeContext);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        probeContext.event = evt;
        switch(evt.getEventType()){
            case STATE_EVENT:
                if (evt.getControlState() == COMM_IDLE){
                    fsm.fire(ProbeEvent.Idle, probeContext);
                }
                break;
            case PROBE_EVENT:
                fsm.fire(ProbeEvent.Probed, probeContext);
                break;
            case CONTROLLER_STATUS_EVENT:
                fsm.fire(ProbeEvent.Position, probeContext);
                break;
            case FILE_EVENT:
                default:
                return;
        }
    }

    private StateMachine<CornerProbeMachine, CornerProbeState, ProbeEvent, ProbeContext> initStateMachine() {
        StateMachineBuilder builder =
                StateMachineBuilderFactory.create(CornerProbeMachine.class, CornerProbeState.class,
                        ProbeEvent.class, ProbeContext.class, BackendAPI.class);    

        // Outside corner probe.
        builder.externalTransition().from(Waiting).to(ocSetup)
                .on(ProbeEvent.StartOutsideCornerProbe).callMethod("repositionAndStore");
                
        builder.externalTransition().from(ocSetup).to(ocProbe1)
                .on(ProbeEvent.Idle).callMethod("probe");

        builder.externalTransition().from(ocProbe1).to(ocSmallRetract1)
                .on(ProbeEvent.Probed).callMethod("retract");

        builder.externalTransition().from(ocSmallRetract1).to(ocSlow1)
                .on(ProbeEvent.Idle).callMethod("probe");

        builder.externalTransition().from(ocSlow1).to(ocProbed1)
                .on(ProbeEvent.Probed);

        builder.externalTransition().from(ocProbed1).to(ocStoreYReset)
                .on(ProbeEvent.Position).callMethod("repositionAndStore");

        builder.externalTransition().from(ocStoreYReset).to(ocProbe2)
                .on(ProbeEvent.Idle).callMethod("probe");

        builder.externalTransition().from(ocProbe2).to(ocSmallRetract2)
                .on(ProbeEvent.Probed).callMethod("retract");

        builder.externalTransition().from(ocSmallRetract2).to(ocSlow2)
                .on(ProbeEvent.Idle).callMethod("probe");

        builder.externalTransition().from(ocSlow2).to(ocProbed2)
                .on(ProbeEvent.Probed);
        builder.externalTransition().from(ocProbed2).to(ocStoreXFinalize)
                .on(ProbeEvent.Position).callMethod("repositionAndStore");

        builder.transit().fromAny().to(Waiting)
                .on(ProbeEvent.Failure).callMethod("failure");
    
        return builder.newStateMachine(Waiting, new Object[] {backend});
    }

    static enum CornerProbeState {
        Waiting,
        // Outside corner
        ocSetup, ocProbe1, ocSmallRetract1, ocSlow1, ocProbed1, ocStoreYReset, ocProbe2,
        ocSmallRetract2, ocSlow2, ocProbed2, ocStoreXFinalize,
        // Inside corner
        icProbe1, icSmallRetract, icSlow1, icProbe2, icSmallRetract2, icSlow2, icFinalizing, icFinal,
    }

    static enum ProbeEvent {
        StartInsideCornerProbe,
        StartOutsideCornerProbe,

        Probed, Position, Idle,
        
        Failure;
    }
    
    private static class CornerProbeMachine extends
            AbstractStateMachine<CornerProbeMachine, CornerProbeState, ProbeEvent, ProbeContext> {
        private final BackendAPI backend;
        private final Settings settings;

        public CornerProbeMachine(BackendAPI backend) {
            this.backend = backend;
            this.settings = backend.getSettings();
        }

        // ocSetup -> ocProbe1
        // ocSmallRetract1 -> ocSlow1
        // ocStoreYReset -> ocProbe2
        // ocSmallRetract2 -> ocSlow2
        public void probe(CornerProbeState from, CornerProbeState to, 
                ProbeEvent event, ProbeContext context) throws Exception {
            switch (to) {
                case ocProbe1:
                    backend.probe("X", context.feedRate, context.xSpacing, UnitUtils.Units.MM);
                    break;
                case ocSlow1:
                    backend.probe("X", context.feedRate/2, context.xSpacing, UnitUtils.Units.MM);
                    break;
                case ocProbe2:
                    backend.probe("Y", context.feedRate, context.ySpacing, UnitUtils.Units.MM);
                    break;
                case ocSlow2:
                    backend.probe("Y", context.feedRate/2, context.ySpacing, UnitUtils.Units.MM);
                    break;
            }
        }

        // ocProbe1 -> ocSmallRetract1
        // ocProbe2 -> ocSmallRetract2
        public void retract(CornerProbeState from, CornerProbeState to, 
                ProbeEvent event, ProbeContext context) throws Exception {
            switch (to) {
                case ocSmallRetract1:
                {
                    backend.sendGcodeCommand(true, "G91 G21 G0 X" + retractDistance(context.xSpacing));
                    break;
                }
                case ocSmallRetract2:
                {
                    double retract = (context.ySpacing < 0) ? 1 : -1;
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + retractDistance(context.ySpacing));
                    break;
                }
            }
        }

        // Waiting -> ocSetup
        // ocProbed1 -> ocStoreYReset (store result and return to start location)
        // ocProbed2 -> ocStoreXFinalize (store result, return to start location, update WCS)
        public void repositionAndStore(CornerProbeState from, CornerProbeState to, 
                ProbeEvent event, ProbeContext context) throws Exception {
            switch (to) {
                case ocSetup:
                {
                    backend.sendGcodeCommand(true, "G91 G21 G0 X" + context.xSpacing);
                    break;
                }
                case ocStoreYReset:
                {
                    context.probePosition1 = context.event.getControllerStatus().getMachineCoord();
                    double offset = context.probePosition1.y - context.startPosition.y;
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + offset);
                    backend.sendGcodeCommand(true, "G91 G21 G0 X" + -context.xSpacing);
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + context.ySpacing);
                    break;
                }
                case ocStoreXFinalize:
                {
                    context.probePosition2 = context.event.getControllerStatus().getMachineCoord();
                    double offset = context.probePosition1.y - context.startPosition.y;
                    backend.sendGcodeCommand(true, "G91 G21 G0 Y" + offset);
                    backend.sendGcodeCommand(true, "G91 G21 G0 X" + -context.xSpacing);

                    // TODO: Update WCS.
                    break;
                }
            }
        }

        public void failure(CornerProbeState from, CornerProbeState to, 
                ProbeEvent event, ProbeContext context){
            GUIHelpers.displayErrorDialog(context.errorMessage);
        }

        /**
         * If there is an exception transition back to the waiting state and display an error message.
         */
        @Override
        public void afterTransitionCausedException(CornerProbeState from, CornerProbeState to, 
                ProbeEvent event, ProbeContext context){
            Throwable targeException = getLastException().getTargetException();
            context.errorMessage = targeException.getLocalizedMessage();
            this.fire(ProbeEvent.Failure, context);
        }
    }
}
