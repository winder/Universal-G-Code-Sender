/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.types;

import com.willwinder.universalgcodesender.model.Position;

import java.util.Optional;

/**
 * A base class for probe commands that are responsible for
 * parsing the probed coordinate
 */
public abstract class ProbeGcodeCommand extends GcodeCommand{
    public ProbeGcodeCommand(String command) {
        super(command);
    }

    /**
     * Returns the probed machine position
     *
     * @return an optional position
     */
    public abstract Optional<Position> getProbedPosition();
}
