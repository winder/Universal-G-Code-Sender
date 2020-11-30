/*
    Copyright 2020 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.*;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * A utils class for handling smoothie controller stuff
 *
 * @author Joacim Breiler
 */
public class SmoothieUtils {

    public static final byte RESET_COMMAND = 0x18;
    public static final byte STATUS_COMMAND = '?';
    public static final String PERFORM_HOMING_CYCLE_COMMAND = "$H";
    public static final String VIEW_PARSER_STATE_COMMAND = "$G";
    public static final String KILL_ALARM_LOCK_COMMAND = "$X";


    /**
     * Parse state out of position string.
     */
    private static final Pattern PARSER_STATE_PATTERN = Pattern.compile("\\[.*]");

    protected static ControllerStatus getStatusFromStatusString(ControllerStatus lastStatus, final String status, UnitUtils.Units reportingUnits) {
        return GrblUtils.getStatusFromStatusStringV1(lastStatus, status, reportingUnits);
    }

    public static boolean isOkErrorAlarmResponse(String response) {
        return response.startsWith("ok") || response.startsWith("error") || response.startsWith("alarm");
    }

    public static boolean isStatusResponse(final String response) {
        return GrblUtils.isGrblStatusString(response);
    }

    public static boolean isVersionResponse(String response) {
        return response.startsWith("Build version:");
    }

    public static boolean isParserStateResponse(final String response) {
        return PARSER_STATE_PATTERN.matcher(response).find();
    }

    public static String generateSetWorkPositionCommand(ControllerStatus controllerStatus, GcodeState gcodeState, PartialPosition position) {
        int offsetCode = WorkCoordinateSystem.fromGCode(gcodeState.offset).getPValue();
        Position machineCoord = controllerStatus.getMachineCoord();


        PartialPosition.Builder offsets = PartialPosition.builder().setUnits(position.getUnits());
        for (Map.Entry<Axis, Double> axisPosition : position.getAll().entrySet()) {
            double axisOffset = -(axisPosition.getValue() - machineCoord.get(axisPosition.getKey()));
            offsets.setValue(axisPosition.getKey(), axisOffset);

        }
        return "G10 L2 P" + offsetCode + " " + offsets.build().getFormattedGCode();
    }
}
