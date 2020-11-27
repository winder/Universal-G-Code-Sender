/*
    Copyright 2012-2017 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.i18n.Language;
import com.willwinder.universalgcodesender.i18n.Localization;

/**
 *
 * @author will
 */
public class UnitUtils {
    public enum Units {
        MM("mm", Localization.getString("millimeters")),
        INCH("\"", Localization.getString("inches")),
        UNKNOWN("", Localization.getString("unknown"));

        public final String abbreviation;
        private final String description;

        Units(String abbreviation, String description) {
            this.abbreviation = abbreviation;
            this.description = description;
        }

        public static Units getUnit(String abbrev) {
            for (Units u : values()) {
                if (u.abbreviation.equals(abbrev)) {
                    return u;
                }
            }
            return null;
        }

        public static Units getUnits(Code code) {
          switch (code) {
            case G20:
              return INCH;
            case G21:
              return MM;
            default:
              throw new RuntimeException("Not a units gcode: " + code);
          }
        }

        public String getDescription() {
            return description;
        }
    }

    public static double scaleUnits(Units from, Units to) {
        final double mmPerInch = 25.4;
        switch (from) {
            case MM:
                switch(to) {
                    case MM: return 1.0;
                    case INCH: return 1.0 / mmPerInch;
                    default: break;
                }
            case INCH:
                switch(to) {
                    case MM: return mmPerInch;
                    case INCH: return 1.0;
                    default: break;
                }
                default:
                    break;
        }
        return 1.0;
    }
}
