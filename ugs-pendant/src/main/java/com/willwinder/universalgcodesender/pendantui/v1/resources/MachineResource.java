package com.willwinder.universalgcodesender.pendantui.v1.resources;

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
import javax.ws.rs.NotAcceptableException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/v1/machine")
public class MachineResource {

    @Inject
    private BackendAPI backendAPI;

    @Inject
    private JogService jogService;

    @GET
    @Path("connect")
    @Produces(MediaType.APPLICATION_JSON)
    public void connect() throws Exception {
        if (backendAPI.isConnected()) {
            throw new NotAcceptableException("Already connected");
        }
        Settings settings = SettingsFactory.loadSettings();
        backendAPI.connect(settings.getFirmwareVersion(), settings.getPort(), Integer.valueOf(settings.getPortRate()));
    }

    @GET
    @Path("disconnect")
    @Produces(MediaType.APPLICATION_JSON)
    public void disconnect() throws Exception {
        backendAPI.disconnect();
    }

    @GET
    @Path("getPortList")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getPortList() {
        ConnectionDriver connectionDriver = SettingsFactory.loadSettings().getConnectionDriver();
        return ConnectionFactory.getPortNames(connectionDriver);
    }

    @GET
    @Path("getSelectedPort")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSelectedPort() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("selectedPort", new JsonPrimitive(SettingsFactory.loadSettings().getPort()));
        return jsonObject.toString();
    }

    @POST
    @Path("setSelectedPort")
    @Produces(MediaType.APPLICATION_JSON)
    public void setSelectedPort(@QueryParam("port") String port) {
        SettingsFactory.loadSettings().setPort(port);
    }

    @GET
    @Path("getBaudRateList")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getBaudRateList() {
        return Arrays.asList(BaudRateEnum.getAllBaudRates());
    }

    @GET
    @Path("getSelectedBaudRate")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSelectedFBaudRate() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("selectedBaudRate", new JsonPrimitive(SettingsFactory.loadSettings().getPortRate()));
        return jsonObject.toString();
    }

    @POST
    @Path("setSelectedBaudRate")
    @Produces(MediaType.APPLICATION_JSON)
    public void setSelectedBaudRate(@QueryParam("baudRate") String baudRate) {
        SettingsFactory.loadSettings().setPortRate(baudRate);
    }

    @GET
    @Path("getFirmwareList")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getFirmwareList() {
        return FirmwareUtils.getFirmwareList();
    }

    @GET
    @Path("getSelectedFirmware")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSelectedFirmware() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("selectedFirmware", new JsonPrimitive(SettingsFactory.loadSettings().getFirmwareVersion()));
        return jsonObject.toString();
    }

    @POST
    @Path("setSelectedFirmware")
    @Produces(MediaType.APPLICATION_JSON)
    public void setSelectedFirmware(@QueryParam("firmware") String firmware) {
        Optional<IController> controller = FirmwareUtils.getControllerFor(firmware);
        if (controller.isPresent()) {
            SettingsFactory.loadSettings().setFirmwareVersion(firmware);
        } else {
            throw new NotFoundException();
        }
    }

    @GET
    @Path("killAlarm")
    @Produces(MediaType.APPLICATION_JSON)
    public void killAlarm() throws Exception {
        backendAPI.killAlarmLock();
    }

    @GET
    @Path("resetToZero")
    @Produces(MediaType.APPLICATION_JSON)
    public void resetToZero(@QueryParam("axis") Axis axis) throws Exception {
        if (axis == null) {
            backendAPI.resetCoordinatesToZero();
        } else {
            backendAPI.resetCoordinateToZero(axis);
        }
    }

    @GET
    @Path("returnToZero")
    @Produces(MediaType.APPLICATION_JSON)
    public void returnToZero() throws Exception {
        backendAPI.returnToZero();
    }

    @GET
    @Path("homeMachine")
    @Produces(MediaType.APPLICATION_JSON)
    public void homeMachine() throws Exception {
        backendAPI.performHomingCycle();
    }

    @GET
    @Path("softReset")
    @Produces(MediaType.APPLICATION_JSON)
    public void softReset() throws Exception {
        backendAPI.issueSoftReset();
    }

    @POST
    @Path("sendGcode")
    @Consumes(MediaType.APPLICATION_JSON)
    public void sendGcode(GcodeCommands gcode) throws Exception {
        List<String> gcodeCommands = new BufferedReader(new StringReader(gcode.getCommands()))
                .lines()
                .collect(Collectors.toList());

        for (String gcodeCommand : gcodeCommands) {
            backendAPI.sendGcodeCommand(gcodeCommand);
        }
    }

    @GET
    @Path("jog")
    @Produces(MediaType.APPLICATION_JSON)
    public void jog(@QueryParam("x") int x, @QueryParam("y") int y, @QueryParam("z") int z) {
        jogService.adjustManualLocationXY(x, y);
        jogService.adjustManualLocationZ(z);
    }
}
