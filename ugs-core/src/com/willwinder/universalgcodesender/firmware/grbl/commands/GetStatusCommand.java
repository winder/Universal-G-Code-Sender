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
package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;

public class GetStatusCommand extends GcodeCommand {
    public static final ControllerStatus EMPTY_STATUS = new ControllerStatus(ControllerState.DISCONNECTED, Position.ZERO, Position.ZERO);
    private ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance().build();

    public GetStatusCommand() {
        super("?");
    }

    @Override
    public void appendResponse(String response) {
        super.appendResponse(response);
        if (GrblUtils.isGrblStatusString(response)) {
            parseControllerStatus(response);
        } else {
            setError(true);
        }
        setDone(true);
    }

    private void parseControllerStatus(String response) {
        if (GrblUtils.isGrblStatusStringV1(response)) {
            controllerStatus = GrblUtils.getStatusFromStatusStringV1(EMPTY_STATUS, response, UnitUtils.Units.MM);
        } else {
            controllerStatus = GrblUtils.getStatusFromStatusStringLegacy(response, UnitUtils.Units.MM);
        }
    }

    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }
}
