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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BaudRateEnum;
import com.willwinder.universalgcodesender.pendantui.v1.model.GcodeCommands;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/machine")
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
        return ConnectionFactory.getDevices(connectionDriver).stream()
                .map(IConnectionDevice::getAddress)
                .toList();
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
    public String getSelectedBaudRate() {
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
