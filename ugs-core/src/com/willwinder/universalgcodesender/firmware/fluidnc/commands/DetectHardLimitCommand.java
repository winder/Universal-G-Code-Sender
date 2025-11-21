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

import static com.willwinder.universalgcodesender.GrblUtils.GRBL_RESET_COMMAND;
import java.util.HashSet;
import java.util.Set;

/**
 * A command for detecting if a FluidNC controller has hard limit warnings
 *
 * @author Damian Nikodem
 */
public class DetectHardLimitCommand extends SystemCommand {
    private final static String FLUIDNC_LIMIT_SWITCH_WARNING = "Active limit switch on ";
    private final static String FLUIDNC_HARD_SWITCH_WARNING = "ALARM: Hard Limit";
    private final static String FLUIDNC_EXTRATEXT_IN_WARNING = " axis motor ";
    private final static String FLUIDNC_UNLOCK_COMMAND_TEMPLATE = "$/axes/%s/motor%s/hard_limits=0";
    private final static String FLUIDNC_RELOCK_COMMAND_TEMPLATE = "$/axes/%s/motor%s/hard_limits=1";    
    private Set<String> alarmStrings;
    
    public DetectHardLimitCommand(boolean doReset) {        
        super(doReset? new String(new byte[GRBL_RESET_COMMAND]):"");   
        /* 
        Grbl 3.9 [FluidNC v3.9.9 (bt) '$' for help]
        [MSG:WARN: Active limit switch on X axis motor 0]
        [MSG:WARN: Active limit switch on Y axis motor 1]
        [MSG:WARN: Active limit switch on Z axis motor 0]
        [MSG:WARN: Active limit switch on Z axis motor 1]
        [MSG:INFO: ALARM: Hard Limit]
        ALARM:1
        */

    }
    private String cleanupActiveLimitString(String s) {
         String result = extractStringAfterPrefix(s, FLUIDNC_LIMIT_SWITCH_WARNING);
         result = result.replace(FLUIDNC_EXTRATEXT_IN_WARNING, "");
         return result.replaceAll("[^a-zA-Z0-9]", "");
    }
    
    public boolean hasHardLimit() {
        return getResponse().contains(FLUIDNC_HARD_SWITCH_WARNING);
    }
    
    private String extractStringAfterPrefix(String in, String prefix) {
        return in.substring(in.indexOf(prefix) + prefix.length() , in.length());
    }

    private Set<String> getAlarmStrings() {     
        if (alarmStrings == null) {
            String[] split = getResponse().split("\n");
            alarmStrings = new HashSet<>();

            for (String s: split) {
                if (s.contains(FLUIDNC_LIMIT_SWITCH_WARNING)) {                
                    alarmStrings.add(cleanupActiveLimitString(s));
                }
            }
        }
        return alarmStrings;
    }
    
    public Set<SystemCommand> getUnlockCommands() {
        Set<SystemCommand> result = new HashSet<>();
        for (String s : getAlarmStrings()) { 
            String split[] = s.split("");
            result.add(new SystemCommand(String.format(FLUIDNC_UNLOCK_COMMAND_TEMPLATE,split[0],split[1])));
        }
        return result;
    }
    
    public Set<SystemCommand> getRelockCommands() {
        Set<SystemCommand> result = new HashSet<>();
        for (String s : getAlarmStrings()) { 
            String split[] = s.split("");
            result.add(new SystemCommand(String.format(FLUIDNC_RELOCK_COMMAND_TEMPLATE,split[0],split[1])));
        }
        return result;
    }
    
}
