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
package com.willwinder.universalgcodesender.firmware;

import com.willwinder.universalgcodesender.utils.SettingsComparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Arranges firmware settings in the groups reported by the controller.
 *
 * @author Joacim Breiler
 */
public final class FirmwareSettingGrouper {
    private static final Comparator<FirmwareSetting> BY_KEY =
            Comparator.comparing(FirmwareSetting::getKey, new SettingsComparator());

    private FirmwareSettingGrouper() {
        // Can not be instanced
    }

    /**
     * Arranges the settings in groups, keeping the group order reported by the controller. Groups
     * without any settings are omitted, and settings that do not belong to any of the reported
     * groups are collected in a final group without a name.
     *
     * @param settings   the settings to arrange
     * @param groupNames the group names in the order that the controller reported them
     * @return the settings arranged in groups
     */
    public static List<FirmwareSettingGroup> group(List<FirmwareSetting> settings, List<String> groupNames) {
        List<FirmwareSettingGroup> groups = new ArrayList<>();
        Set<String> grouped = new LinkedHashSet<>(groupNames);

        for (String groupName : grouped) {
            List<FirmwareSetting> groupSettings = settings.stream()
                    .filter(setting -> groupName.equals(setting.getGroupName()))
                    .sorted(BY_KEY)
                    .toList();

            if (!groupSettings.isEmpty()) {
                groups.add(new FirmwareSettingGroup(groupName, groupSettings));
            }
        }

        List<FirmwareSetting> ungrouped = settings.stream()
                .filter(setting -> !grouped.contains(setting.getGroupName()))
                .sorted(BY_KEY)
                .toList();

        if (!ungrouped.isEmpty()) {
            groups.add(new FirmwareSettingGroup("", ungrouped));
        }

        return groups;
    }

    /**
     * Returns every setting in the order it would be presented when grouped.
     *
     * @param settings   the settings to arrange
     * @param groupNames the group names in the order that the controller reported them
     * @return the settings in a flat list
     */
    public static List<FirmwareSetting> groupedOrder(List<FirmwareSetting> settings, List<String> groupNames) {
        return group(settings, groupNames).stream()
                .flatMap(group -> group.settings().stream())
                .toList();
    }
}
