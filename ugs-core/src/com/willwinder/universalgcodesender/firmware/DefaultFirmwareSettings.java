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
    public void setInvertDirectionX(boolean inverted) {
    }

    @Override
    public void setInvertDirectionY(boolean inverted) {
    }

    @Override
    public void setInvertDirectionZ(boolean inverted) {
    }

    @Override
    public boolean isInvertDirectionX() {
        return false;
    }

    @Override
    public boolean isInvertDirectionY() {
        return false;
    }

    @Override
    public boolean isInvertDirectionZ() {
        return false;
    }

    @Override
    public int getStepsPerMillimeter(Axis axis) {
        return 0;
    }

    @Override
    public double getSoftLimitX() {
        return 0;
    }

    @Override
    public void setSoftLimitX(double limit) {

    }

    @Override
    public double getSoftLimitY() {
        return 0;
    }

    @Override
    public void setSoftLimitY(double limit) {

    }

    @Override
    public double getSoftLimitZ() {
        return 0;
    }

    @Override
    public void setSoftLimitZ(double limit) {

    }

    @Override
    public double getSoftLimit(Axis axis) {
        return 0;
    }

    @Override
    public boolean isHomingDirectionInvertedX() {
        return false;
    }

    @Override
    public void setHomingDirectionInvertedX(boolean inverted) {

    }

    @Override
    public boolean isHomingDirectionInvertedY() {
        return false;
    }

    @Override
    public void setHomingDirectionInvertedY(boolean inverted) {

    }

    @Override
    public boolean isHomingDirectionInvertedZ() {
        return false;
    }

    @Override
    public void setHomingDirectionInvertedZ(boolean inverted) {

    }

    @Override
    public void setHardLimitsInverted(boolean inverted) {

    }

    @Override
    public boolean isHardLimitsInverted() {
        return false;
    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException {

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
