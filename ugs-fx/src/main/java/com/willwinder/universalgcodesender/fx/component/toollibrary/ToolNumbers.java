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
package com.willwinder.universalgcodesender.fx.component.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.util.function.IntPredicate;

/**
 * Stepping through tool numbers while skipping the ones other tools hold.
 */
public final class ToolNumbers {
    private ToolNumbers() {
    }

    /**
     * The tool number reached by taking {@code steps} steps from {@code current}, positive steps
     * upwards and negative downwards, where every step lands on a number no other tool holds.
     * {@link ToolDefinition#UNASSIGNED_TOOL_NUMBER} always counts as free. When the range runs out
     * before all steps are taken, the last free number reached is returned, or {@code current}
     * when there was none.
     */
    public static int nextFree(int current, int steps, IntPredicate occupied, int max) {
        int direction = Integer.signum(steps);
        int remaining = Math.abs(steps);
        int result = current;
        int candidate = current;
        while (remaining > 0) {
            candidate += direction;
            if (candidate < ToolDefinition.UNASSIGNED_TOOL_NUMBER || candidate > max) {
                break;
            }
            if (candidate == ToolDefinition.UNASSIGNED_TOOL_NUMBER || !occupied.test(candidate)) {
                result = candidate;
                remaining--;
            }
        }
        return result;
    }
}
