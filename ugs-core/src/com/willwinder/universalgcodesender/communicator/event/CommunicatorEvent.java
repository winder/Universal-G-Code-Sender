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

import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * Simple data class used to pass data to the event thread.
 *
 * @author winder
 */
public class CommunicatorEvent {
    public final CommunicatorEventType event;
    public final GcodeCommand command;
    public final String string;

    public CommunicatorEvent(
            CommunicatorEventType event,
            String string,
            GcodeCommand command) {
        this.event = event;
        this.command = command;
        this.string = string;
    }
}
