/*
    Copyright 2012-2022 Will Winder

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
package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.HashSet;
import java.util.Set;

/**
 * A synchronous communicator event dispatcher that will dispatch events
 * from the communicator to all listeners.
 * <p>
 * This dispatcher should generally not be used as it will make the
 * communication flow really slow and require that all
 * listeners have processed the command before the next one can be sent.
 * <p>
 * You would normally want to use {@link AsyncCommunicatorEventDispatcher}.
 *
 * @author winder
 * @author Joacim Breiler
 */
public class CommunicatorEventDispatcher implements ICommunicatorEventDispatcher {
    protected final Set<CommunicatorListener> communicatorListeners = new HashSet<>();

    @Override
    public void start() {
        // Not implemented
    }

    @Override
    public void stop() {
        // Not implemented
    }

    @Override
    public void removeListener(CommunicatorListener listener) {
        communicatorListeners.remove(listener);
    }

    @Override
    public void addListener(CommunicatorListener listener) {
        communicatorListeners.add(listener);
    }

    @Override
    public void dispatch(CommunicatorEvent event) {
        sendEvent(event.event, event.string, event.command);
    }

    protected final void sendEvent(CommunicatorEventType event, String string, GcodeCommand command) {
        switch (event) {
            case COMMAND_SENT:
                for (CommunicatorListener scl : communicatorListeners)
                    scl.commandSent(command);
                break;
            case COMMAND_SKIPPED:
                for (CommunicatorListener scl : communicatorListeners)
                    scl.commandSkipped(command);
                break;
            case RAW_RESPONSE:
                for (CommunicatorListener scl : communicatorListeners)
                    scl.rawResponseListener(string);
                break;
            case PAUSED:
                communicatorListeners.forEach(CommunicatorListener::communicatorPausedOnError);
                break;
            default:
        }
    }

    @Override
    public void reset() {
        // Not implemented
    }
}
