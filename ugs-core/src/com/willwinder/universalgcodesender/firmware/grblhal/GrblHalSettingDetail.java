/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;

/**
 * The metadata that grblHAL reports for one of its settings using the command $ES.
 *
 * @param key             the settings key including the dollar sign, ie {@code $14}
 * @param groupId         the id of the setting group this setting belongs to, as enumerated by $EG
 * @param name            a short name for the setting, ie {@code Invert control inputs}
 * @param units           the units of the value, ie {@code mm/min}. May be empty
 * @param dataType        the type of the value
 * @param format          how the value should be presented. For numeric settings this is a number
 *                        format and for bitfields and radio buttons it is a comma separated list of
 *                        labels, ie {@code N/A,Feed hold,Cycle start,N/A,N/A,N/A,EStop}. May be empty
 * @param min             the smallest allowed value. May be empty
 * @param max             the largest allowed value. May be empty
 * @param rebootRequired  if the controller needs to be reset for a new value to take effect
 * @param allowNull       if zero is allowed in addition to the min-max range of values
 * @author Joacim Breiler
 */
public record GrblHalSettingDetail(String key, String groupId, String name, String units,
                                   GrblHalDataType dataType, String format, String min, String max,
                                   boolean rebootRequired, boolean allowNull) {

    /**
     * Creates a firmware setting for the given value. The controller does not report a long
     * description as part of the settings enumeration, only the name is available.
     *
     * @param value     the current value of the setting
     * @param groupName the name of the group that the setting belongs to, may be empty
     * @return a firmware setting
     */
    public FirmwareSetting toFirmwareSetting(String value, String groupName) {
        return new FirmwareSetting(key, value, units, "", name, groupName);
    }
}
