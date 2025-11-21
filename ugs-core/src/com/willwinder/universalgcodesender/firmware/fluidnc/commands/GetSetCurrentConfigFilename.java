/*
    Copyright 2025 Damian Nikodem

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
package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.GrblUtils.isGrblStatusString;

/**
 * A command which will either get or set the current config filename depending
 * on which constructor is called. 
 */
public class GetSetCurrentConfigFilename extends GcodeCommand {
    private boolean isGet;
    
    public GetSetCurrentConfigFilename(String newConfigFilename) {
        super( "$Config/Filename=" + newConfigFilename );
        isGet = false;
    }
    
    public GetSetCurrentConfigFilename() {
        super("$Config/Filename");
        isGet = true;
    }


    @Override
    public void setResponse(String response) {
        super.setResponse("");
        appendResponse(response);
    }

    @Override
    public void appendResponse(String response) {
        // In some cases the controller will echo the commands sent, do not add those to the response.
        if (response.equals(getOriginalCommandString())) {
            return;
        }

        // Do not append status strings to non status commands
        if (!StringUtils.equals(getCommandString(), "?") && isGrblStatusString(response)) {
            return;
        }

        super.appendResponse(response);
    }
    
    public String GetFilename() {
        String configToUse = "";
        if (isGet) {
            configToUse=getResponse();
            if (configToUse != null) {
                String[] split = configToUse.split("\n");
                if (split[0].startsWith("$Config/Filename=")) {
                    configToUse=split[0].split("=")[1];
                }
            } else {
                configToUse = "config.yaml";
            }
            
        }
        return configToUse;
    } 
}
