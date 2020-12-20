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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.util.Collections;
import java.util.List;
import com.willwinder.universalgcodesender.gcode.GcodeStats;

/**
 *
 * @author wwinder
 */
public class Stats implements CommandProcessor, GcodeStats {
    private static Units defaultUnits = Units.MM;

    private Position min = new Position(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Units.MM);
    private Position max = new Position(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Units.MM);

    private long commandCount = 0;

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        Position c = state.currentPoint;
        if (c != null) {
            Position p = new Position(c.x, c.y, c.z, state.isMetric ? Units.MM : Units.INCH)
                            .getPositionIn(defaultUnits);

            // Update min
            min.x = Math.min(min.x, p.x);
            min.y = Math.min(min.y, p.y);
            min.z = Math.min(min.z, p.z);

            // Update max
            max.x = Math.max(max.x, p.x);
            max.y = Math.max(max.y, p.y);
            max.z = Math.max(max.z, p.z);

            // Num commands
            commandCount++;
        }

        return Collections.singletonList(command);
    }

    @Override
    public String getHelp() {
        return "Caches program metrics, shouldn't be enabled or disabled.";
    }

    @Override
    public final Position getMin() {
        return min;
    }

    @Override
    public final Position getMax() {
        return max;
    }

    @Override
    public final long getCommandCount() {
        return commandCount;
    }
    
}
