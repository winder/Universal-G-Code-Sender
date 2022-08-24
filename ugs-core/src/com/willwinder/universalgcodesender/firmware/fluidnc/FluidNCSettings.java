package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.FluidNCCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetFirmwareSettingsCommand;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.ControllerUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class FluidNCSettings implements IFirmwareSettings {
    private static final Logger LOGGER = Logger.getLogger(FluidNCSettings.class.getName());

    private final Map<String, FirmwareSetting> settings = new ConcurrentHashMap<>();
    private final IController controller;
    private final Set<IFirmwareSettingsListener> listeners = Collections.synchronizedSet(new HashSet<>());

    public FluidNCSettings(IController controller) {
        this.controller = controller;
    }

    public void refresh() throws FirmwareSettingsException {
        try {
            GetFirmwareSettingsCommand firmwareSettingsCommand = new GetFirmwareSettingsCommand();
            ControllerUtils.sendAndWaitForCompletion(controller, firmwareSettingsCommand);

            if (firmwareSettingsCommand.isOk()) {
                firmwareSettingsCommand.getSettings().keySet().forEach(key -> {
                    String value = firmwareSettingsCommand.getSettings().get(key);
                    FirmwareSetting firmwareSetting = new FirmwareSetting(key, value, "", "", "");
                    settings.put(key, firmwareSetting);
                    listeners.forEach(l -> l.onUpdatedFirmwareSetting(firmwareSetting));
                });
            }
        } catch (Exception e) {
            throw new FirmwareSettingsException("Couldn't fetch settings", e);
        }
    }

    @Override
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    @Override
    public FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException {
        try {
            if (!settings.containsKey(key) || !settings.get(key).getValue().equals(value)) {
                FluidNCCommand systemCommand = new FluidNCCommand("$/" + key + "=" + value);
                ControllerUtils.sendAndWaitForCompletion(controller, systemCommand);
                if (systemCommand.isOk()) {
                    FirmwareSetting firmwareSetting = new FirmwareSetting(key, value, "", "", "");
                    settings.put(key, firmwareSetting);
                    listeners.forEach(l -> l.onUpdatedFirmwareSetting(firmwareSetting));
                }
            }
        } catch (Exception e) {
            throw new FirmwareSettingsException("Couldn't store setting", e);
        }

        return getSetting(key).orElse(null);
    }

    @Override
    public void addListener(IFirmwareSettingsListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public boolean isHomingEnabled() throws FirmwareSettingsException {
        return true;
    }

    @Override
    public void setHomingEnabled(boolean enabled) throws FirmwareSettingsException {

    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        FirmwareSetting firmwareSetting = getSetting("report_inches").orElse(new FirmwareSetting("report_inches", "false"));
        if (firmwareSetting.getValue().equalsIgnoreCase("false")) {
            return UnitUtils.Units.MM;
        } else {
            return UnitUtils.Units.INCH;
        }
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return new ArrayList<>(settings.values());
    }

    @Override
    public boolean isHardLimitsEnabled() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHardLimitsEnabled(boolean enabled) throws FirmwareSettingsException {

    }

    @Override
    public boolean isSoftLimitsEnabled() throws FirmwareSettingsException {
        return getSetting("axes/x/soft_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/y/soft_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/z/soft_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent();
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) throws FirmwareSettingsException {

    }

    @Override
    public boolean isInvertDirection(Axis axis) throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setInvertDirection(Axis axis, boolean inverted) throws FirmwareSettingsException {

    }

    @Override
    public void setStepsPerMillimeter(Axis axis, double stepsPerMillimeter) throws FirmwareSettingsException {

    }

    @Override
    public double getStepsPerMillimeter(Axis axis) throws FirmwareSettingsException {
        return 0;
    }

    @Override
    public void setSoftLimit(Axis axis, double limit) throws FirmwareSettingsException {
        setValue("axes/" + axis.name().toLowerCase() + "/max_travel_mm", Utils.formatter.format(limit));
    }

    @Override
    public double getSoftLimit(Axis axis) throws FirmwareSettingsException {
        return getSetting("axes/" + axis.name().toLowerCase() + "/max_travel_mm")
                .map(s -> {
                    try {
                        return Utils.formatter.parse(s.getValue()).doubleValue();
                    } catch (ParseException e) {
                        return 0d;
                    }
                })
                .orElse(0d);
    }

    @Override
    public boolean isHomingDirectionInverted(Axis axis) {
        return false;
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) throws FirmwareSettingsException {

    }

    @Override
    public boolean isHardLimitsInverted() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException {

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
        return 0;
    }
}
