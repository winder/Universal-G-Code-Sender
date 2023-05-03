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
package com.willwinder.universalgcodesender.firmware.tinyg.commands;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

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

    public static Integer getResponseStatusCode(String response) {
        String statusCodes = StringUtils.substringBetween(response, "\"f\":[", "]");
        String[] codes = StringUtils.split(statusCodes, ",");
        if (codes.length < 2) {
            return -1;
        }

        return Integer.valueOf(codes[1]);
    }

    private static String convertCommandToJson(String command) {
        String ret;

        // wrap in json
        if (StringUtils.isEmpty(command) ||
                "\n".equals(command) ||
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

    @Override
    public void appendResponse(String response) {
        super.appendResponse(response);

        if (TinyGGcodeCommand.isOkErrorResponse(response)) {
            Integer responseStatusCode = getResponseStatusCode(response);
            if (responseStatusCode == 0) {
                setOk(true);
            } else {
                setError(true);
            }
            setDone(true);
        }

        if (isOkTextResponse(response)) {
            setOk(true);
            setDone(true);
        } else if (isErrorTextResponse(response)) {
            setError(true);
            setDone(true);
        }
    }

    private static boolean isErrorTextResponse(String response) {
        return response.startsWith("tinyg [mm] err") || response.startsWith("tinyg [inch] err");
    }

    private static boolean isOkTextResponse(String response) {
        return response.startsWith("tinyg [mm] ok") || response.startsWith("tinyg [inch] ok");
    }
}
