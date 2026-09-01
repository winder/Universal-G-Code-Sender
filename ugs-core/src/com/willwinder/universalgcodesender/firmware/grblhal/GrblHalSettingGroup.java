/*
    Copyright 2026 Will Winder

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

/**
 * A setting group as reported by the enumeration command $EG. Groups are hierarchical and may be
 * used for presenting the settings as a tree, ie the group {@code X-axis} has the group
 * {@code Axis} as its parent.
 *
 * @param id       the id of the group, referenced by {@link GrblHalSettingDetail#groupId()}
 * @param parentId the id of the parent group, the root group is its own parent
 * @param name     the name of the group, ie {@code Control signals}
 * @author Joacim Breiler
 */
public record GrblHalSettingGroup(String id, String parentId, String name) {
}
