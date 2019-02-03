package com.willwinder.universalgcodesender.pendantui.controllers;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.model.Status;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/status")
public class StatusController {

    @Inject
    private BackendAPI backendAPI;

    @GET
    @Path("getStatus")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStatus() {
        Status status = new Status();

        IController controller = backendAPI.getController();
        if (controller != null) {

            ControllerStatus controllerStatus = controller.getControllerStatus();
            if (controllerStatus != null) {
                status.setMachineCoord(controllerStatus.getMachineCoord());
                status.setWorkCoord(controllerStatus.getWorkCoord());
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

        return Response.ok(status).build();
    }
}