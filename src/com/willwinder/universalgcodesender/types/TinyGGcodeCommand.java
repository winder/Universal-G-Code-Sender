/*
 * TinyG Gcode command to deal with some JSON details.
 */

/*
    Copywrite 2013-2015 Will Winder, John Lauer

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
    
    static String convertCommandToJson(String command) {
        String ret;
        // wrap in json
        if (command.equals("\n") || 
            command.equals("\r\n") ||
            command.equals("?") || 
            command.startsWith("{\"sr")) {
            // this is a status request cmd
            ret = "{\"sr\":\"\"}";
        } else if (command.startsWith("{")) {
            // it is already json ready. leave it alone.
            ret = command.trim();
        } else if (command.startsWith("(")) {
            // it's a comment. pass it thru. this app will handle it nicely
            ret = command;
        } else {
            // assume it needs wrapping for gcode cmd
            String c = command.trim();
            ret = "{\"gc\":\"" + c + "\"}";
        }
        
        return ret;
    }
}
