/*
    Copyright 2023 Will Winder

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
package com.willwinder.universalgcodesender.pendantui.v1.model;

import com.willwinder.universalgcodesender.model.UGSEvent;

import java.io.Serializable;

public class Event implements Serializable {
    private final String eventType;

    private final UGSEvent event;

    public Event(UGSEvent event) {
        this.eventType = event.getClass().getSimpleName();
        this.event = event;
    }

    public String getEventType() {
        return eventType;
    }

    public UGSEvent getEvent() {
        return event;
    }
}
