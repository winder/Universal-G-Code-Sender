package com.willwinder.universalgcodesender.pendantui.v1.resources;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.pendantui.v1.model.Status;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/v1/status")
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