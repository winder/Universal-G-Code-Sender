/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.tracking;

/**
 * An interface for a tracker that can be used to report usage statistics
 *
 * @author Joacim Breiler
 */
public interface ITracker {

    /**
     * Reports a event to the tracker server
     *
     * @param module the module that this event occured in.
     * @param action the action that triggered the event. Ex. "Started", "File stream complete"
     */
    void report(Class module, String action);

    /**
     * Reports a event with the given event type to a tracker server.
     *  @param module        the module that this event occured in.
     * @param action        the action that triggered the event. Ex. "Started", "File stream complete"
     * @param resourceName  the name of an extra resource we want to register for this event. Example "Rows sent" for the number of rows when a file completes.
     * @param resourceValue a optional number value for the resource. Example a number of rows sent when a file completes.
     */
    void report(Class module, String action, String resourceName, int resourceValue);
}
