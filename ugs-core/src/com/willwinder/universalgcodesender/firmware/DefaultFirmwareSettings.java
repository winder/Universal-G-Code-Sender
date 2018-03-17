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

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A default implementation of the firmware settings to be used for controllers that
 * doesn't yet support this.
 *
 * @author Joacim Breiler
 */
public class DefaultFirmwareSettings implements IFirmwareSettings {
    @Override
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.empty();
    }

    @Override
    public FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException {
        throw new FirmwareSettingsException("Saving settings is not supported by this controller");
    }

    @Override
    public void addListener(IFirmwareSettingsListener listener) {
    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {
    }

    @Override
    public boolean isHomingEnabled() {
        return false;
    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        return UnitUtils.Units.UNKNOWN;
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return Collections.emptyList();
    }
}
