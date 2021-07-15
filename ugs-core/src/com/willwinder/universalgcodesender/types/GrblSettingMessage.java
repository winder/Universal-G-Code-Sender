/*
    Copyright 2016 Will Winder, Phil

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

import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.GrblLookups;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by Phil on 1/15/2016.
 */
public class GrblSettingMessage {
    final private static GrblLookups lookups = new GrblLookups("setting_codes");
    final String message;

    // Starting in GRBL 1.1 the description is disabled by default.
    final private static Pattern MESSAGE_REGEX =
            Pattern.compile("\\$(\\d+)=([^ ]*)\\s?\\(?([^\\)]*)?\\)?");

    // Setting number
    private String setting;
    // Setting value
    private String value;
    // Units of setting
    private String units;
    // Long description of setting
    private String description;
    // Short description of setting
    private String shortDescription;

/* Sample settings.
$0=10 (step pulse, usec)
$1=25 (step idle delay, msec)
$2=0 (step port invert mask:00000000)
$3=0 (dir port invert mask:00000000)
$4=0 (step enable invert, bool)
$5=0 (limit pins invert, bool)
$6=0 (probe pin invert, bool)
$10=3 (status report mask:00000011)
$11=0.010 (junction deviation, mm)
$12=0.002 (arc tolerance, mm)
$13=0 (report inches, bool)
$20=0 (soft limits, bool)
$21=0 (hard limits, bool)
$22=0 (homing cycle, bool)
$23=0 (homing dir invert mask:00000000)
$24=25.000 (homing feed, mm/min)
$25=500.000 (homing seek, mm/min)
$26=250 (homing debounce, msec)
$27=1.000 (homing pull-off, mm)
$100=250.000 (x, step/mm)
$101=250.000 (y, step/mm)
$102=250.000 (z, step/mm)
$110=5000.000 (x max rate, mm/min)
$111=5000.000 (y max rate, mm/min)
$112=500.000 (z max rate, mm/min)
$120=400.000 (x accel, mm/sec^2)
$121=400.000 (y accel, mm/sec^2)
$122=10.000 (z accel, mm/sec^2)
$130=200.000 (x max travel, mm)
$131=200.000 (y max travel, mm)
$132=200.000 (z max travel, mm)
*/

    public GrblSettingMessage(String message) {
        this.message = message;
        parse();
    }

    @Override
    public String toString() {
        String descriptionStr = "";
        if (!StringUtils.isEmpty(description)) {
            if (!StringUtils.isEmpty(units)) {
                descriptionStr = " (" + shortDescription + ", " + units + ")";
            } else {
                descriptionStr = " (" + description + ")";
            }
        }

        return String.format("$%s = %s   %s", setting, value, descriptionStr);
    }

    public String getSetting() {
        return setting;
    }

    public String getUnits() {
        return units;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    private void parse() {
        Matcher m = MESSAGE_REGEX.matcher(message);
        if (m.find()) {
            setting = m.group(1);
            value = m.group(2);
            if (m.groupCount() == 3 && !StringUtils.isEmpty(m.group(3))) {
                description = m.group(3);
            } else {
                String[] lookup = lookups.lookup(setting);
                if (lookup != null) {
                    units = lookup[2];
                    description = lookup[3];
                    shortDescription = lookup[1];
                }
            }
        }
    }

    public boolean isReportingUnits() {
        return "13".equals(setting);
    }

    public UnitUtils.Units getReportingUnits() {
        if (isReportingUnits()) {
            if ("0".equals(value)) {
                return UnitUtils.Units.MM;
            } else if ("1".equals(value)) {
                return UnitUtils.Units.INCH;
            }
        }
        return UnitUtils.Units.UNKNOWN;
    }
}
