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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.grbl.GrblFirmwareSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles the firmware settings on a grblHAL controller. In addition to the settings values it
 * keeps the setting metadata that the controller reports using the command $ES and the setting
 * groups it reports using $EG.
 *
 * @author Joacim Breiler
 */
public class GrblHalFirmwareSettings extends GrblFirmwareSettings {
    private static final String KEY_INVERT_LIMIT_PINS = "$5";

    /**
     * Parses a setting message containing the key and value, ie {@code $14=6}
     */
    public static final Pattern SETTING_MESSAGE_REGEX = Pattern.compile("^\\$(\\d+)=(.*)$");

    private final Map<String, GrblHalSettingDetail> settingDetails = new ConcurrentHashMap<>();
    private final Map<String, GrblHalSettingGroup> settingGroups = new ConcurrentHashMap<>();

    public GrblHalFirmwareSettings(IController controller) {
        super(controller);
    }

    /**
     * Replaces the known setting metadata with the one reported by the controller.
     *
     * @param settingDetails the setting metadata
     */
    public void updateSettingDetails(List<GrblHalSettingDetail> settingDetails) {
        this.settingDetails.clear();
        settingDetails.forEach(detail -> this.settingDetails.put(detail.key(), detail));
    }

    /**
     * Replaces the known setting groups with the ones reported by the controller.
     *
     * @param settingGroups the setting groups
     */
    public void updateSettingGroups(List<GrblHalSettingGroup> settingGroups) {
        this.settingGroups.clear();
        settingGroups.forEach(group -> this.settingGroups.put(group.id(), group));
    }

    /**
     * Returns the group reported by the controller for the given group id.
     *
     * @param id the id of the group
     * @return the setting group if the controller has reported it
     */
    public Optional<GrblHalSettingGroup> getSettingGroup(String id) {
        return Optional.ofNullable(settingGroups.get(id));
    }

    /**
     * Returns the names of the setting groups ordered by the group id that the controller reported,
     * which is the order grblHAL itself presents them in.
     */
    @Override
    public List<String> getGroupNames() {
        return settingGroups.values().stream()
                .sorted(Comparator.comparingInt(GrblHalFirmwareSettings::toSortableId))
                .map(GrblHalSettingGroup::name)
                .toList();
    }

    private static int toSortableId(GrblHalSettingGroup group) {
        return NumberUtils.toInt(group.id(), Integer.MAX_VALUE);
    }

    /**
     * Returns the metadata reported by the controller for the given settings key.
     *
     * @param key the settings key including the dollar sign, ie {@code $14}
     * @return the setting metadata if the controller has reported any
     */
    public Optional<GrblHalSettingDetail> getSettingDetail(String key) {
        return Optional.ofNullable(settingDetails.get(key));
    }

    /**
     * Converts a response message in the format {@code $[key]=[value]} to a firmware setting using
     * the name reported by the controller. Settings that the controller has not enumerated will
     * only contain their key and value.
     *
     * @param response the response message from the controller
     * @return the converted firmware setting or an empty optional if the response was unknown
     */
    public Optional<FirmwareSetting> convertMessageToSetting(String response) {
        Matcher settingMatcher = SETTING_MESSAGE_REGEX.matcher(StringUtils.trimToEmpty(response));
        if (!settingMatcher.find()) {
            return Optional.empty();
        }

        String code = settingMatcher.group(1);
        String key = "$" + code;
        String value = settingMatcher.group(2);

        return Optional.of(getSettingDetail(key)
                .map(detail -> detail.toFirmwareSetting(value, getGroupName(detail)))
                .orElseGet(() -> new FirmwareSetting(key, value)));
    }

    private String getGroupName(GrblHalSettingDetail detail) {
        return getSettingGroup(detail.groupId())
                .map(GrblHalSettingGroup::name)
                .orElse("");
    }

    @Override
    public boolean isHardLimitsInverted() throws FirmwareSettingsException {
        GrblHalDataType grblHalDataType = getSettingDetail(KEY_INVERT_LIMIT_PINS).map(GrblHalSettingDetail::dataType).orElse(GrblHalDataType.BOOLEAN);
        if (grblHalDataType == GrblHalDataType.BITFIELD) {
            int invertedLimits = getSetting(KEY_INVERT_LIMIT_PINS)
                    .map(FirmwareSetting::getValue)
                    .map(Integer::valueOf)
                    .orElse(0);
            return (invertedLimits & 1) == 1 && (invertedLimits & 2) == 2 && (invertedLimits & 4) == 4;
        }
        return super.isHardLimitsInverted();
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException {
        GrblHalDataType grblHalDataType = getSettingDetail(KEY_INVERT_LIMIT_PINS).map(GrblHalSettingDetail::dataType).orElse(GrblHalDataType.BOOLEAN);
        if (grblHalDataType == GrblHalDataType.AXIS_MASK) {
            int invertedLimits = getSetting(KEY_INVERT_LIMIT_PINS)
                    .map(FirmwareSetting::getValue)
                    .map(Integer::valueOf)
                    .orElse(0);
            if (inverted) {
                invertedLimits |= 1;
                invertedLimits |= 2;
                invertedLimits |= 4;
            } else {
                invertedLimits &= ~1;
                invertedLimits &= ~2;
                invertedLimits &= ~4;
            }
            setValue(KEY_INVERT_LIMIT_PINS, String.valueOf(invertedLimits));
        } else {
            super.setHardLimitsInverted(inverted);
        }
    }
}
