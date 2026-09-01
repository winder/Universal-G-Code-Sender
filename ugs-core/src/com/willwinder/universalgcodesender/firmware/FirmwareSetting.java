/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.firmware;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * A firmware setting model. The class is immutable to prevent consumers changing
 * its state that would not guarantee that it is persisted on the controller.
 *
 * @author Joacim Breiler
 */
public class FirmwareSetting implements Serializable {

    /**
     * The settings key for this value
     */
    private String key = "";

    /**
     * The value of the setting
     */
    private String value = "";

    /**
     * In what units are the value of this setting given
     */
    private String units = "";

    /**
     * The description of the setting
     */
    private String description = "";

    /**
     * The short description of the setting
     */
    private String shortDescription = "";

    /**
     * The name of the group that this setting belongs to. Not all controllers report groups so
     * this may be empty.
     */
    private String groupName = "";

    /**
     * A constructor with all parameters for constructing a firmware setting
     *
     * @param key              the name of the setting
     * @param value            the value of the setting
     * @param units            the type of the setting that would help users
     *                         understand which values are allowed
     * @param description      the full description of the setting
     * @param shortDescription a short description of the setting
     * @param groupName        the name of the group that the setting belongs to
     */
    public FirmwareSetting(String key, String value, String units, String description, String shortDescription, String groupName) {
        this.key = key;
        this.value = value;
        this.units = units;
        this.description = description;
        this.shortDescription = shortDescription;
        this.groupName = groupName;
    }

    public FirmwareSetting(String key, String value, String units, String description, String shortDescription) {
        this(key, value, units, description, shortDescription, "");
    }

    public FirmwareSetting(String key, String value) {
        this(key, value, "", "", "");
    }

    public String getKey() {
        return key;
    }


    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public String getUnits() {
        return units;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FirmwareSetting &&
                EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
