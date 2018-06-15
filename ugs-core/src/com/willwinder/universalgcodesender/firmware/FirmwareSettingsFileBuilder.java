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

import java.util.List;

/**
 * A builder for creating a {@link FirmwareSettingsFile}
 *
 * @author Joacim Breiler
 */
public class FirmwareSettingsFileBuilder {
    private String name;
    private String createdBy;
    private String date;
    private String firmwareName;
    private List<FirmwareSetting> settings;

    public FirmwareSettingsFileBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public FirmwareSettingsFileBuilder setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public FirmwareSettingsFileBuilder setDate(String date) {
        this.date = date;
        return this;
    }

    public FirmwareSettingsFileBuilder setFirmwareName(String firmwareName) {
        this.firmwareName = firmwareName;
        return this;
    }

    public FirmwareSettingsFileBuilder setSettings(List<FirmwareSetting> settings) {
        this.settings = settings;
        return this;
    }

    public FirmwareSettingsFile build() {
        FirmwareSettingsFile firmwareSettingsFile = new FirmwareSettingsFile();
        firmwareSettingsFile.setCreatedBy(createdBy);
        firmwareSettingsFile.setDate(date);
        firmwareSettingsFile.setFirmwareName(firmwareName);
        firmwareSettingsFile.setName(name);
        firmwareSettingsFile.setSettings(settings);
        return firmwareSettingsFile;
    }
}