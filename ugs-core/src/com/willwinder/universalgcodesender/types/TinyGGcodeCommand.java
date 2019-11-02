/*
    Copyright 2013-2018 Will Winder, John Lauer

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
package com.willwinder.universalgcodesender.types;

/**
 * TinyG Gcode command to deal with some JSON details.
 *
 * @author wwinder
 */
public class TinyGGcodeCommand extends GcodeCommand {
    public TinyGGcodeCommand(String command) {
        this(command, -1);
    }

    public TinyGGcodeCommand(String command, int num) {
        super(convertCommandToJson(command), num);
    }
    
    public static Boolean isOkErrorResponse(String response) {
        return response.startsWith("{\"r\"") && !response.contains("\"fv\"");
    }
    
    private static String convertCommandToJson(String command) {
        String ret;
        // wrap in json
        if ("\n".equals(command) ||
                "\r\n".equals(command) ||
                "?".equals(command)) {
                // this is a status request cmd
                ret = "{\"sr\":\"\"}";
        } else if (command.startsWith("{") || command.startsWith("$")) {
            // it is already json ready or a system command. leave it alone.
            ret = command.trim();
        } else if (command.startsWith("(")) {
            // it's a comment. pass it thru. this app will handle it nicely
            ret = command;
        } else {
            ret = command.trim();
        }
        
        return ret;
    }

    public static boolean isQueueReportResponse(String response) {
        return response.startsWith("{\"qr\"");
    }

    public static boolean isRecieveQueueReportResponse(String response) {
        return response.startsWith("{\"rx\"");
    }
}
