/**
 * A diagnostic class to test application speed, overrides the connection
 * with a handler that responds with "ok" as fast as possible.
 */
/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.connection.LoopBackConnection;

/**
 *
 * @author wwinder
 */
public class LoopBackCommunicator extends GrblCommunicator {
    public LoopBackCommunicator(int ms) {
        this.connection = new LoopBackConnection(ms);
    }

    public LoopBackCommunicator() {
        this.connection = new LoopBackConnection(0);
    }
}
