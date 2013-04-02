/*
 * FirmwareUtils.java
 *
 * Created on April 2, 2013
 */

/*
    Copywrite 2012-2013 Will Winder

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

import java.util.ArrayList;

/**
 *
 * @author wwinder
 */
public class FirmwareUtils {
    final private static String GRBL     = "GRBL";
    final private static String Smoothie = "SmoothieBoard";
    final private static String TinyG    = "TinyG";
    
    public static ArrayList<String> getFirmwareList() {
        ArrayList<String> ret = new ArrayList<String>();
        ret.add("GRBL");
        ret.add("SmoothieBoard");
        ret.add("TinyG");
        return ret;
    }
    
    public static GrblController getControllerFor(String firmware) {
        if (firmware.equals(GRBL)) {
            return new GrblController();
        }
        if (firmware.equals(Smoothie)) {
            
        }
        if (firmware.equals(TinyG)) {
            
        }
        
        return null;
    }
}
