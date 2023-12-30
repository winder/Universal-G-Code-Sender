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
package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.pendantui.v1.model.Status;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/status")
public class StatusResource {

    @Inject
    private BackendAPI backendAPI;

    @GET
    @Path("getStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Status getStatus() {
        Status status = new Status();

        IController controller = backendAPI.getController();
        if (controller != null) {
            UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();
            ControllerStatus controllerStatus = controller.getControllerStatus();
            if (controllerStatus != null) {
                status.setMachineCoord(controllerStatus.getMachineCoord().getPositionIn(preferredUnits));
                status.setWorkCoord(controllerStatus.getWorkCoord().getPositionIn(preferredUnits));
                status.setState(controllerStatus.getState());
                status.setFeedSpeed(controllerStatus.getFeedSpeed());
                status.setSpindleSpeed(controllerStatus.getSpindleSpeed());
            } else {
                // Hack, we are connected so we need to set it to an unknown state
                status.setState(ControllerState.UNKNOWN);
            }

            status.setRowCount(backendAPI.getNumRows());
            status.setCompletedRowCount(backendAPI.getNumCompletedRows());
            status.setRemainingRowCount(backendAPI.getNumRemainingRows());
            if (backendAPI.getGcodeFile() != null) {
                status.setFileName(backendAPI.getGcodeFile().getName());
            } else {
                status.setFileName("");
            }
            status.setSendDuration(backendAPI.getSendDuration());
            status.setSendRemainingDuration(backendAPI.getSendRemainingDuration());
        }

        return status;
    }
}