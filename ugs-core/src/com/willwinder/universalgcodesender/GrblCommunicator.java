/*
    Copyright 2012-2017 Will Winder

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

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Pattern;

/**
 * @author wwinder
 */
public class GrblCommunicator extends BufferedCommunicator {

    private boolean temporarySingleStepMode;
    private final static String EEPROM_COMMAND_PATTERN = "G10|G28|G30|\\$x=|\\$I|\\$N|\\$RST=|G5[456789]|\\$\\$|\\$#";
    private final static Pattern EEPROM_COMMAND = Pattern.compile(EEPROM_COMMAND_PATTERN, Pattern.CASE_INSENSITIVE);
    
    protected GrblCommunicator() {}

    /**
     * This constructor is for dependency injection so a mock serial device can
     * act as GRBL.
     */
    protected GrblCommunicator(LinkedBlockingDeque<GcodeCommand> cb, LinkedBlockingDeque<GcodeCommand> asl, Connection c) {
        super(cb, asl);
        this.connection = c;
        this.connection.addListener(this);
    }

    @Override
    public int getBufferSize() {
        return GrblUtils.GRBL_RX_BUFFER_SIZE;
    }

    @Override
    protected boolean processedCommand(String response) {
        return GrblUtils.isOkErrorAlarmResponse(response);
    }

    @Override
    protected boolean processedCommandIsError(String response) {
        return response.startsWith("error");
    }

    /**
     * When a command is sent, check if it is one of the special commands which writes to the EEPROM.
     * If it is temporarily setSingleStepMode(true) to avoid corruption.
     */
    @Override
    protected void sendingCommand(String response) {
        // If this is an EEPROM command switch to single step mode temporarily.
        if (EEPROM_COMMAND.matcher(response).find()) {
            this.temporarySingleStepMode = !this.getSingleStepMode() || this.temporarySingleStepMode;
            this.setSingleStepMode(true);
        } else if (this.temporarySingleStepMode) {
            this.temporarySingleStepMode = false;
            this.setSingleStepMode(false);
        }
    }
}
