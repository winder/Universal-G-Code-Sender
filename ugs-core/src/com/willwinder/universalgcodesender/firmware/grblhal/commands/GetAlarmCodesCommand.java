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
package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalUtils;

/**
 * Enumerates the alarm codes supported by the controller using the command $EA:
 *
 * <pre>
 * [ALARMCODE:1|Hard limit|Hard limit has been triggered. Machine position is likely lost due to sudden halt.]
 * ok
 * </pre>
 *
 * @author Joacim Breiler
 */
public class GetAlarmCodesCommand extends AbstractGetCodesCommand {
    public GetAlarmCodesCommand() {
        super(GrblHalUtils.GRBLHAL_ALARM_CODES_COMMAND, "[ALARMCODE:");
    }
}
