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
package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the firmware settings on a GRBL controller. It needs to be registered as a listener
 * to {@link com.willwinder.universalgcodesender.AbstractCommunicator#setListenAll(CommunicatorListener)}
 * for it to be able to process all commands to/from the controller.
 *
 * @author Joacim Breiler
 * @author MerrellM
 */
public class GrblFirmwareSettings implements CommunicatorListener, IFirmwareSettingsListener, IFirmwareSettings {
    private static final Logger LOGGER = Logger.getLogger(GrblFirmwareSettings.class.getName());

    /**
     * Setting keys for GRBL
     */
    private static final String KEY_REPORTING_UNITS_IN_INCHES = "$13";
    private static final String KEY_SOFT_LIMITS_ENABLED = "$20";
    private static final String KEY_HARD_LIMITS_ENABLED = "$21";
    private static final String KEY_HOMING_ENABLED = "$22";
    private static final String KEY_HOMING_INVERT_DIRECTION = "$23";
    private static final String KEY_INVERT_DIRECTION = "$3";
    private static final String KEY_INVERT_LIMIT_PINS = "$5";
    private static final String KEY_STEPS_PER_MM_X = "$100";
    private static final String KEY_STEPS_PER_MM_Y = "$101";
    private static final String KEY_STEPS_PER_MM_Z = "$102";
    private static final String KEY_SOFT_LIMIT_X = "$130";
    private static final String KEY_SOFT_LIMIT_Y = "$131";
    private static final String KEY_SOFT_LIMIT_Z = "$132";
    private static final String KEY_MAXIMUM_RATE_X = "$110";
    private static final String KEY_MAXIMUM_RATE_Y = "$111";
    private static final String KEY_MAXIMUM_RATE_Z = "$112";

    /**
     * A GRBL settings description lookups
     */
    private final Map<String, FirmwareSetting> settings = new ConcurrentHashMap<>();

    /**
     * A delegate for all serial communication handling
     */
    private final GrblFirmwareSettingsCommunicatorListener serialCommunicatorDelegate;

    public GrblFirmwareSettings(IController controller) {
        this.serialCommunicatorDelegate = new GrblFirmwareSettingsCommunicatorListener(controller);
        this.serialCommunicatorDelegate.addListener(this);
    }

    /**
     * Sets a property value on the controller. It will wait until the setting has been stored,
     * if this fails a {@link FirmwareSettingsException} will be thrown.
     *
     * @param key   the name of the setting to update
     * @param value the value of the setting
     * @return the value stored on the controller
     * @throws FirmwareSettingsException if the value couldn't be persisted on the controller.
     */
    @Override
    synchronized public FirmwareSetting setValue(final String key, final String value) throws FirmwareSettingsException {

        final FirmwareSetting oldSetting = getSetting(key)
                .orElseThrow(() -> new FirmwareSettingsException("Couldn't find setting with key " + key + " to update."));

        // The setting already contains the value so we do not update
        if (oldSetting.getValue().equals(value)) {
            return oldSetting;
        }

        // Make a copy of existing property and send it to our controller
        final FirmwareSetting newSetting = new FirmwareSetting(oldSetting.getKey(), value, oldSetting.getUnits(), oldSetting.getDescription(), oldSetting.getShortDescription());
        return serialCommunicatorDelegate
                .updateSettingOnController(newSetting)
                .orElse(oldSetting);
    }

    /**
     * Sets a property value on the controller. It will wait until the setting has been stored,
     * if this fails a {@link FirmwareSettingsException} will be thrown.
     *
     * @param key   the name of the setting to update
     * @param value the value of the setting
     * @return the value stored on the controller
     * @throws FirmwareSettingsException if the value couldn't be persisted on the controller.
     */
    public FirmwareSetting setValue(final String key, final double value) throws FirmwareSettingsException {

        final FirmwareSetting oldSetting = getSetting(key)
                .orElseThrow(() -> new FirmwareSettingsException("Couldn't find setting with key " + key + " to update."));

        // The setting already contains the value so we do not update
        if (getValueAsDouble(key) == value) {
            return oldSetting;
        }

        DecimalFormat decimalFormat = new DecimalFormat("0.0##", Localization.dfs);
        return setValue(key, decimalFormat.format(value));
    }

    @Override
    public void addListener(IFirmwareSettingsListener listener) {
        serialCommunicatorDelegate.addListener(listener);
    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {
        serialCommunicatorDelegate.removeListener(listener);
    }

    @Override
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return new ArrayList<>(settings.values());
    }

    @Override
    public boolean isHardLimitsEnabled() throws FirmwareSettingsException {
        return getValueAsBoolean(KEY_HARD_LIMITS_ENABLED);
    }

    @Override
    public void setHardLimitsEnabled(boolean enabled) throws FirmwareSettingsException {
        setValue(KEY_HARD_LIMITS_ENABLED, enabled ? "1" : "0");
    }

    @Override
    public boolean isSoftLimitsEnabled() throws FirmwareSettingsException {
        return getValueAsBoolean(KEY_SOFT_LIMITS_ENABLED);
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) throws FirmwareSettingsException {
        setValue(KEY_SOFT_LIMITS_ENABLED, enabled ? "1" : "0");
    }

    @Override
    public boolean isInvertDirection(Axis axis) throws FirmwareSettingsException {
        switch (axis) {
            case X:
                return (getInvertDirectionMask() & 1) == 1;
            case Y:
                return (getInvertDirectionMask() & 2) == 2;
            case Z:
                return (getInvertDirectionMask() & 4) == 4;
            default:
                throw new FirmwareSettingsException("Couldn't get invert direction setting for axis " + axis + ", it's not supported by the controller");
        }
    }

    @Override
    public void setInvertDirection(Axis axis, boolean inverted) throws FirmwareSettingsException {
        Integer directionMask = getInvertDirectionMask();

        switch (axis) {
            case X:
                if (inverted) {
                    directionMask |= 0b1; // set first bit from LSB
                } else {
                    directionMask &= ~0b1; // unset first bit from LSB
                }
                break;
            case Y:
                if (inverted) {
                    directionMask |= 0b10; // set second bit from LSB
                } else {
                    directionMask &= ~0b10; // unset second bit from LSB
                }
                break;
            case Z:
                if (inverted) {
                    directionMask |= 0b100; // set third bit from LSB
                } else {
                    directionMask &= ~0b100; // unset third bit from LSB
                }
                break;
            default:
                throw new FirmwareSettingsException("Couldn't set the invert direction for axis " + axis + ", it's not supported by the hardware");
        }

        setValue(KEY_INVERT_DIRECTION, String.valueOf(directionMask));
    }

    @Override
    public void setStepsPerMillimeter(Axis axis, double stepsPerMillimeter) throws FirmwareSettingsException {
        switch (axis) {
            case X:
                setValue(KEY_STEPS_PER_MM_X, stepsPerMillimeter);
                break;
            case Y:
                setValue(KEY_STEPS_PER_MM_Y, stepsPerMillimeter);
                break;
            case Z:
                setValue(KEY_STEPS_PER_MM_Z, stepsPerMillimeter);
                break;
            default:
                throw new FirmwareSettingsException("Couldn't set the steps per millimeter for axis " + axis + ", it's not supported by the hardware");
        }
    }

    @Override
    public double getStepsPerMillimeter(Axis axis) throws FirmwareSettingsException {
        switch (axis) {
            case X:
                return getValueAsDouble(KEY_STEPS_PER_MM_X);
            case Y:
                return getValueAsDouble(KEY_STEPS_PER_MM_Y);
            case Z:
                return getValueAsDouble(KEY_STEPS_PER_MM_Z);
            default:
                return 0;
        }
    }

    @Override
    public void setSoftLimit(Axis axis, double limit) throws FirmwareSettingsException {
        switch (axis) {
            case X:
                setValue(KEY_SOFT_LIMIT_X, limit);
                break;
            case Y:
                setValue(KEY_SOFT_LIMIT_Y, limit);
                break;
            case Z:
                setValue(KEY_SOFT_LIMIT_Z, limit);
                break;
            default:
                throw new FirmwareSettingsException("Couldn't set the soft limits for axis " + axis + ", it's not supported by the hardware");
        }
    }

    @Override
    public double getSoftLimit(Axis axis) throws FirmwareSettingsException {
        switch (axis) {
            case X:
                return getValueAsDouble(KEY_SOFT_LIMIT_X);
            case Y:
                return getValueAsDouble(KEY_SOFT_LIMIT_Y);
            case Z:
                return getValueAsDouble(KEY_SOFT_LIMIT_Z);
            default:
                return 0;
        }
    }

    @Override
    public boolean isHomingDirectionInverted(Axis axis) {
        switch (axis) {
            case X:
                return (getHomingInvertDirectionMask() & 1) == 1;
            case Y:
                return (getHomingInvertDirectionMask() & 2) == 2;
            case Z:
                return (getHomingInvertDirectionMask() & 4) == 4;
            default:
                return false;
        }
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) throws FirmwareSettingsException {
        Integer directionMask = getHomingInvertDirectionMask();

        switch (axis) {
            case X:
                if (inverted) {
                    directionMask |= 0b1; // set first bit from LSB
                } else {
                    directionMask &= ~0b1; // unset first bit from LSB
                }
                break;

            case Y:
                if (inverted) {
                    directionMask |= 0b10; // set first bit from LSB
                } else {
                    directionMask &= ~0b10; // unset first bit from LSB
                }
                break;

            case Z:
                if (inverted) {
                    directionMask |= 0b100; // set first bit from LSB
                } else {
                    directionMask &= ~0b100; // unset first bit from LSB
                }
                break;

            default:
                break;
        }

        setValue(KEY_HOMING_INVERT_DIRECTION, String.valueOf(directionMask));
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException {
        setValue(KEY_INVERT_LIMIT_PINS, inverted ? "1" : "0");
    }

    @Override
    public boolean isHardLimitsInverted() throws FirmwareSettingsException {
        return getValueAsBoolean(KEY_INVERT_LIMIT_PINS);
    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException {
        settings.forEach(setting -> {
            try {
                setValue(setting.getKey(), setting.getValue());
            } catch (FirmwareSettingsException e) {
                LOGGER.warning("Couldn't set the firmware setting " + setting.getKey() + " to value " + setting.getValue() + ". Error message: " + e.getMessage());
            }
        });
    }

    @Override
    public double getMaximumRate(Axis axis) throws FirmwareSettingsException {
        switch (axis) {
            case X:
                return getValueAsDouble(KEY_MAXIMUM_RATE_X);
            case Y:
                return getValueAsDouble(KEY_MAXIMUM_RATE_Y);
            case Z:
                return getValueAsDouble(KEY_MAXIMUM_RATE_Z);
            default:
                throw new FirmwareSettingsException("Couldn't get maximum rate setting for axis " + axis + ", it's not supported by the controller");
        }
    }

    private int getInvertDirectionMask() {
        return getSetting(KEY_INVERT_DIRECTION)
                .map(FirmwareSetting::getValue)
                .map(Integer::valueOf)
                .orElse(0);
    }

    private int getHomingInvertDirectionMask() {
        return getSetting(KEY_HOMING_INVERT_DIRECTION)
                .map(FirmwareSetting::getValue)
                .map(Integer::valueOf)
                .orElse(0);
    }

    @Override
    public boolean isHomingEnabled() throws FirmwareSettingsException {
        return getValueAsBoolean(KEY_HOMING_ENABLED);
    }

    @Override
    public void setHomingEnabled(boolean enabled) throws FirmwareSettingsException {
        setValue(KEY_HOMING_ENABLED, enabled ? "1" : "0");
    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        return getSetting(KEY_REPORTING_UNITS_IN_INCHES)
                .map(FirmwareSetting::getValue)
                .map(value -> {
                    if ("0".equals(value)) {
                        return UnitUtils.Units.MM;
                    } else if ("1".equals(value)) {
                        return UnitUtils.Units.INCH;
                    } else {
                        return UnitUtils.Units.UNKNOWN;
                    }
                })
                .orElse(UnitUtils.Units.UNKNOWN);
    }

    /*
     * SerialCommunicatorListener
     */
    @Override
    public void rawResponseListener(String response) {
        serialCommunicatorDelegate.rawResponseListener(response);
    }

    @Override
    public void commandSent(GcodeCommand command) {
        serialCommunicatorDelegate.commandSent(command);
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        serialCommunicatorDelegate.commandSkipped(command);
    }

    @Override
    public void communicatorPausedOnError() {
        serialCommunicatorDelegate.communicatorPausedOnError();
    }

    /*
     * IFirmwareSettingsListener
     */
    @Override
    public void onUpdatedFirmwareSetting(FirmwareSetting setting) {
        LOGGER.log(Level.FINE, "Updating setting " + setting.getKey() + " = " + setting.getValue());
        settings.put(setting.getKey(), setting);
    }

    /*
     * Helpers
     */
    private int getValueAsInteger(String key) throws FirmwareSettingsException {
        FirmwareSetting firmwareSetting = getSetting(key).orElseThrow(() -> new FirmwareSettingsException("Couldn't find setting with key: " + key));
        if (!NumberUtils.isNumber(firmwareSetting.getValue())) {
            throw new FirmwareSettingsException("Expected the key " + key + " to contain a numeric value but was " + firmwareSetting.getValue());
        }

        return NumberUtils.createNumber(firmwareSetting.getValue()).intValue();
    }

    private double getValueAsDouble(String key) throws FirmwareSettingsException {
        FirmwareSetting firmwareSetting = getSetting(key).orElseThrow(() -> new FirmwareSettingsException("Couldn't find setting with key: " + key));
        if (!NumberUtils.isNumber(firmwareSetting.getValue())) {
            throw new FirmwareSettingsException("Expected the key " + key + " to contain a numeric value but was " + firmwareSetting.getValue());
        }

        return NumberUtils.createNumber(firmwareSetting.getValue()).doubleValue();
    }

    private boolean getValueAsBoolean(String key) throws FirmwareSettingsException {
        FirmwareSetting firmwareSetting = getSetting(key).orElseThrow(() -> new FirmwareSettingsException("Couldn't find setting with key: " + key));
        return "1".equalsIgnoreCase(firmwareSetting.getValue());
    }
}
