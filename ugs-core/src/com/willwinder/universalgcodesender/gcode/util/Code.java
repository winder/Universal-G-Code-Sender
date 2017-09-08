/*
    Copyright 2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.util;

import static com.willwinder.universalgcodesender.gcode.util.Code.Type.*;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author wwinder
 */
public enum Code {
    G0(Motion),
    G1(Motion),
    G2(Motion),
    G3(Motion),

    G4(Dwell),

    G10(WCS), // combine with 'L' codes to set WCS

    G17(Plane),
    G18(Plane),
    G19(Plane),
    G17_1(Plane),
    G18_1(Plane),
    G19_1(Plane),

    G20(Units),
    G21(Units),

    G28(Motion),

    G38_2(Motion),
    G38_3(Motion),
    G38_4(Motion),
    G38_5(Motion),

    G40(Cutter),

    G43_1(TLO),
    G49(TLO),

    G54(WCS),
    G55(WCS),
    G56(WCS),
    G57(WCS),
    G58(WCS),
    G59(WCS),

    G80(Motion), // Canned cycle

    G90(Distance),
    G91(Distance),

    G90_1(Arc),
    G91_1(Arc),

    G92(WCS),

    G93(Feedmode),
    G94(Feedmode),
    G95(Feedmode),

    // MCodes
    M0(Program),
    M1(Program),
    M2(Program),

    M3(Spindle),
    M4(Spindle),
    M5(Spindle),

    M7(Coolant),
    M8(Coolant),
    M9(Coolant),

    M30(Program),
    UNKNOWN(Unknown);

    public enum Type {
        // Gcode Types
        Motion,
        WCS,
        Plane,
        Distance,
        Arc,
        Feedmode,
        Units,
        Cutter,
        TLO,
        Dwell,
        
        // Mcode Types
        Program,
        Spindle,
        Coolant,

        Unknown;
    };

    static final Map<String,Code> codeLookup =
        Arrays.stream(Code.values())
                .collect(Collectors.toMap(Code::toString, c -> c));

    private final Type type;

    private Code(Type type){
        this.type = type;
    }

    /**
     * @return name with '_' replaced with '.'
     */
    @Override
    public String toString() {
        return this.name().replace("_", ".");
    }

    public Type getType(){
        return this.type;
    }


    public boolean requiresCoordinates() {
        return this.type == Motion && this != G28;
    }

    private static final Pattern CODE_PATTERN = Pattern.compile("(?:0?)+((?:\\d*\\.)?\\d+)");
    /**
     * Lookup code from lookup map.
     * @param code String representation of an enum, like G1 or G38.2
     * @return the enum value
     */
    public static Code lookupCode(String code) {
        if (code == null || code.length() < 1){
            return null;
        }

        char type = Character.toUpperCase(code.charAt(0));

        // Strip leading zeros in a way that leaves the last zero in case of 'G0'
        int subStr = 1;
        for (int i = 1; i < code.length(); i++) {
            subStr = i;
            if (code.charAt(i) != '0') {
                break;
            }
        }
        String rest = code.substring(subStr);

        Code c = codeLookup.get(type + rest);
        //return c;
        //return codeLookup.get(code.toUpperCase());
        return c == null ? UNKNOWN : c;
    }
}
