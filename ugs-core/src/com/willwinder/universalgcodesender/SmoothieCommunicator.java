/*
    Copyright 2016 Will Winder

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

/**
 * @author wwinder
 */
public class SmoothieCommunicator extends BufferedCommunicator {

    public SmoothieCommunicator() {
        super();
        setSingleStepMode(true);
    }

    @Override
    public int getBufferSize() {
        return 255;
    }

    @Override
    protected void sendingCommand(String command) {

    }

    @Override
    protected boolean processedCommand(String response) {
        return SmoothieUtils.isOkErrorAlarmResponse(response) || SmoothieUtils.isVersionResponse(response);
    }

    @Override
    protected boolean processedCommandIsError(String response) {
        return GrblUtils.isErrorResponse(response);
    }


    @Override
    public void setSingleStepMode(boolean enable) {
        // Never mind this
    }
}
