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

import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A default implementation of the firmware settings to be used for controllers that
 * doesn't yet support this.
 *
 * @author Joacim Breiler
 */
public class DefaultFirmwareSettings implements IFirmwareSettings {
    /**
     * All listeners for listening to changed settings
     */
    private final Set<IFirmwareSettingsListener> listeners = new HashSet<>();

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
        listeners.add(listener);
    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {
        listeners.remove(listener);
    }

    @Override
    public boolean isHomingEnabled() {
        return false;
    }

    @Override
    public void setHomingEnabled(boolean enabled) {
    }

    @Override
    public boolean isHardLimitsEnabled() {
        return false;
    }

    @Override
    public void setHardLimitsEnabled(boolean enabled) {
    }

    @Override
    public boolean isSoftLimitsEnabled() {
        return false;
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) {
    }

    @Override
    public boolean isInvertDirection(Axis axis) {
        return false;
    }

    @Override
    public void setInvertDirection(Axis axis, boolean inverted) {

    }

    @Override
    public void setStepsPerMillimeter(Axis axis, double stepsPerMillimeter) {
    }

    @Override
    public double getStepsPerMillimeter(Axis axis) {
        return 0;
    }

    @Override
    public void setSoftLimit(Axis axis, double limit) {
    }

    @Override
    public double getSoftLimit(Axis axis) {
        return 0;
    }

    @Override
    public boolean isHomingDirectionInverted(Axis axis) {
        return false;
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) {

    }

    @Override
    public boolean isHardLimitsInverted() {
        return false;
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) {

    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) {

    }

    @Override
    public double getMaximumRate(Axis axis) {
        return 0;
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
