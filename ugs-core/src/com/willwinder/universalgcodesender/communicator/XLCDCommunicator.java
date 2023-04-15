/*
    Copyright 2015-2017 Will Winder

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
package com.willwinder.universalgcodesender.communicator;

/**
 * @author wwinder
 */
public class XLCDCommunicator extends GrblCommunicator {

    private int UGSCommandCount = 0;
    public XLCDCommunicator() {}

    @Override
    protected void sendingCommand(String command) {
        UGSCommandCount++;
    }

    @Override
    public void handleResponseMessage(String response) {
        // Only append responses if there are an active command
        if (UGSCommandCount > 0) {
            UGSCommandCount--;
            super.handleResponseMessage(response);
        }
    }
}
