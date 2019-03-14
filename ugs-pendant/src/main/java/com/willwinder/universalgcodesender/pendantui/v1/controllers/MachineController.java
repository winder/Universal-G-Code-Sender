package com.willwinder.universalgcodesender.pendantui.v1.controllers;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BaudRateEnum;
import com.willwinder.universalgcodesender.pendantui.v1.model.GcodeCommands;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/v1/machine")
public class MachineController {

    @Inject
    private BackendAPI backendAPI;

    @Inject
    private JogService jogService;

    @GET
    @Path("connect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response connect() throws Exception {
        if (backendAPI.isConnected()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        Settings settings = SettingsFactory.loadSettings();
        backendAPI.connect(settings.getFirmwareVersion(), settings.getPort(), Integer.valueOf(settings.getPortRate()));
        return Response.ok().build();
    }

    @GET
    @Path("disconnect")
    @Produces(MediaType.APPLICATION_JSON)
    public Response disconnect() throws Exception {
        if (!backendAPI.isConnected()) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).build();
        }
        backendAPI.disconnect();
        return Response.ok().build();
    }

    @GET
    @Path("getPortList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPortList() {
        ConnectionDriver connectionDriver = SettingsFactory.loadSettings().getConnectionDriver();
        List<String> portNames = ConnectionFactory.getPortNames(connectionDriver);
        return Response.ok(portNames).build();
    }

    @GET
    @Path("getSelectedPort")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelectedPort() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("selectedPort", new JsonPrimitive(SettingsFactory.loadSettings().getPort()));
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("setSelectedPort")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setSelectedPort(@QueryParam("port") String port) {
        SettingsFactory.loadSettings().setPort(port);
        return Response.ok().build();
    }

    @GET
    @Path("getBaudRateList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBaudRateList() {
        return Response.ok(BaudRateEnum.getAllBaudRates()).build();
    }

    @GET
    @Path("getSelectedBaudRate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelectedFBaudRate() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("selectedBaudRate", new JsonPrimitive(SettingsFactory.loadSettings().getPortRate()));
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("setSelectedBaudRate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setSelectedBaudRate(@QueryParam("baudRate") String baudRate) {
        SettingsFactory.loadSettings().setPortRate(baudRate);
        return Response.ok().build();
    }

    @GET
    @Path("getFirmwareList")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFirmwareList() {
        return Response.ok(FirmwareUtils.getFirmwareList()).build();
    }

    @GET
    @Path("getSelectedFirmware")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSelectedFirmware() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("selectedFirmware", new JsonPrimitive(SettingsFactory.loadSettings().getFirmwareVersion()));
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("setSelectedFirmware")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setSelectedFirmware(@QueryParam("firmware") String firmware) {
        Optional<IController> controller = FirmwareUtils.getControllerFor(firmware);
        if (controller.isPresent()) {
            SettingsFactory.loadSettings().setFirmwareVersion(firmware);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("killAlarm")
    @Produces(MediaType.APPLICATION_JSON)
    public Response killAlarm() {
        try {
            backendAPI.killAlarmLock();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("resetToZero")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetToZero(@QueryParam("axis") Axis axis) {
        try {
            if (axis == null) {
                backendAPI.resetCoordinatesToZero();
            } else {
                backendAPI.resetCoordinateToZero(axis);
            }
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("returnToZero")
    @Produces(MediaType.APPLICATION_JSON)
    public Response returnToZero() {
        try {
            backendAPI.returnToZero();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("homeMachine")
    @Produces(MediaType.APPLICATION_JSON)
    public Response homeMachine() {
        try {
            backendAPI.performHomingCycle();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("softReset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response softReset() {
        try {
            backendAPI.issueSoftReset();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("sendGcode")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendGcode(GcodeCommands gcode) {
        try {
            List<String> gcodeCommands = new BufferedReader(new StringReader(gcode.getCommands()))
                    .lines()
                    .collect(Collectors.toList());

            for (String gcodeCommand : gcodeCommands) {
                backendAPI.sendGcodeCommand(gcodeCommand);
            }

            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("jog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response jog(@QueryParam("x") int x, @QueryParam("y") int y, @QueryParam("z") int z) {
        try {
            jogService.adjustManualLocationXY(x, y);
            jogService.adjustManualLocationZ(z);
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }
}
