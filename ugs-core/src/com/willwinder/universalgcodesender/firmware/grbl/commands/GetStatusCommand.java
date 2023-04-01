package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;

public class GetStatusCommand extends GcodeCommand {
    private ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance().build();

    public GetStatusCommand() {
        super("?");
    }

    @Override
    public void appendResponse(String response) {
        super.appendResponse(response);
        if (GrblUtils.isGrblStatusString(response)) {
            controllerStatus = GrblUtils.getStatusFromStatusStringV1(new ControllerStatus(ControllerState.DISCONNECTED, Position.ZERO, Position.ZERO), response, UnitUtils.Units.MM);
        } else {
            setError(true);
        }
        setDone(true);
    }

    public ControllerStatus getControllerStatus() {
        return controllerStatus;
    }
}
