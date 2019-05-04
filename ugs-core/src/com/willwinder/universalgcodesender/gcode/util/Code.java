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

import static com.willwinder.universalgcodesender.gcode.util.Code.ModalGroup.*;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author wwinder
 */
public enum Code {
    G4(NonModal),
    G10(NonModal, true, false),
    G28(NonModal, true, true),
    G30(NonModal, true, true),
    G53(NonModal, false, false),
    G92(NonModal, true, false),
    G92_1(NonModal),
    G92_2(NonModal),
    G92_3(NonModal),

    /**
     * Rapid linear movement
     */
    G0(Motion, false, true),

    /**
     * Rapid movement
     */
    G1(Motion, false, true),
    G2(Motion, false, true),
    G3(Motion, false, true),
    G33(Motion),
    G38_2(Motion),
    G38_3(Motion),
    G38_4(Motion),
    G38_5(Motion),
    G73(Motion),
    G76(Motion),
    G80(Motion, false, true),
    G81(Motion),
    G82(Motion),
    G83(Motion),
    G84(Motion),
    G85(Motion),
    G86(Motion),
    G87(Motion),
    G88(Motion),
    G89(Motion),

    G17(Plane),
    G18(Plane),
    G19(Plane),
    G17_1(Plane),
    G18_1(Plane),
    G19_1(Plane),

    /**
     * Absolute movement
     */
    G90(Distance),

    /**
     * Relative movement
     */
    G91(Distance),

    G90_1(Arc),
    G91_1(Arc),

    G93(Feedmode),
    G94(Feedmode),
    G95(Feedmode),

    G20(Units),
    G21(Units),

    G40(Cutter),
    G41(Cutter),
    G42(Cutter),
    G41_1(Cutter),
    G42_1(Cutter),

    G43(TLO),
    G43_1(TLO),
    G49(TLO),

    G98(CannedCycle),
    G99(CannedCycle),

    G54(WCS),
    G55(WCS),
    G56(WCS),
    G57(WCS),
    G58(WCS),
    G59(WCS),
    G59_1(WCS),
    G59_2(WCS),
    G59_3(WCS),

    G61(Control),
    G61_1(Control),
    G64(Control),

    G96(SpindleSpeed),
    G97(SpindleSpeed),

    G7(LatheDiamater),
    G8(LatheDiamater),

    // MCodes
    M0(Stopping),
    M1(Stopping),
    M2(Stopping),
    M30(Stopping),
    M60(Stopping),

    M3(Spindle),
    M4(Spindle),
    M5(Spindle),

    M7(Coolant),
    M8(Coolant),
    M9(Coolant),

    M48(Override),
    M49(Override),

    UNKNOWN(Unknown);

    // http://linuxcnc.org/docs/html/gcode/overview.html#_modal_groups
    public enum ModalGroup {
        // G-Code
        NonModal,       // (Group 0)  Non-modal codes 
        Motion,         // (Group 1)  Motion
        Plane,          // (Group 2)  Plane selection
        Distance,       // (Group 3)  Distance Mode
        Arc,            // (Group 4)  Arc IJK Distance Mode
        Feedmode,       // (Group 5)  Feed Rate Mode
        Units,          // (Group 6)  Units
        Cutter,         // (Group 7)  Cutter Diameter Compensation
        TLO,            // (Group 8)  Tool Length Offset
        CannedCycle,    // (Group 10) Canned Cycles Return Mode
        WCS,            // (Group 12) Coordinate System
        Control,        // (Group 13) Control Mode
        SpindleSpeed,   // (Group 14) Spindle Speed Mode
        LatheDiamater,  // (Group 15) Lathe Diameter Mode
        
        // M-Code
        Stopping,       // (Group 4)  Stopping 
        Spindle,        // (Group 7)  Spindle
        Coolant,        // (Group 8)  Coolant
        Override,       // (Group 9)  Override Switches

        Unknown
    }

    static final Map<String,Code> codeLookup =
        Arrays.stream(Code.values())
                .collect(Collectors.toMap(Code::toString, c -> c));

    private final ModalGroup type;
    private final boolean nonModalMotionCode;
    private final boolean motionOptional;

    Code(ModalGroup type){
        this.type = type;
        this.nonModalMotionCode = false;
        this.motionOptional = false;
    }

    Code(ModalGroup type, boolean nonModalMotionCode, boolean motionOptional){
        this.type = type;
        this.nonModalMotionCode = nonModalMotionCode;
        this.motionOptional = motionOptional;
    }

    public boolean consumesMotion() {
        return type == Motion || this.nonModalMotionCode;
    }

    public boolean motionOptional() {
        return this.motionOptional;
    }

    /**
     * @return name with '_' replaced with '.'
     */
    @Override
    public String toString() {
        return this.name().replace("_", ".");
    }

    public ModalGroup getType(){
        return this.type;
    }

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
        return c == null ? UNKNOWN : c;
    }
}
