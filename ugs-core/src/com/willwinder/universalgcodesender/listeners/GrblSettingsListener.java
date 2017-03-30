/*
    Copywrite 2014-2017 Will Winder, MerrellM

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

package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.GrblSettingMessage;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: MerrellM
 * Date: 2/12/14
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class GrblSettingsListener implements ControllerListener, SerialCommunicatorListener {

    public boolean inParsingMode = false;
    private boolean firstSettingReceived = false;

    public final HashMap<String,GrblSettingMessage> settings;

    public GrblSettingsListener() {
        this.settings = new HashMap<>();
    }

    public HashMap<String,GrblSettingMessage> getSettings() {
        return settings;
    }

    // ControllerListener

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {
        if (command.getCommandString().startsWith("$$")) {
            this.inParsingMode = true;
            this.firstSettingReceived = false;
            settings.clear();
        }
    }

    @Override
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void probeCoordinates(Position p) {
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
    }

    @Override
    public void postProcessData(int numRows) {
    }

    // SerialCommunicatorListener

    @Override
    public void rawResponseListener(String response) {
        if (this.inParsingMode) {
            if (firstSettingReceived && response.startsWith("ok")) {
                this.inParsingMode = false;
            } else if (response.startsWith("$"))  {
                firstSettingReceived = true;
                GrblSettingMessage gsm = new GrblSettingMessage(response);
                settings.put(gsm.getValue(), gsm);
            }
        }
    }

    @Override
    public void messageForConsole(String msg) {
    }

    @Override
    public void verboseMessageForConsole(String msg) {
    }

    @Override
    public void errorMessageForConsole(String msg) {
    }
}
