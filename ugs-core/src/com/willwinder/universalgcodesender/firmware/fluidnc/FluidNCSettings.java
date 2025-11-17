/*
    Copyright 2022-2024 Will Winder

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
import com.willwinder.universalgcodesender.types.CommandException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
public class FluidNCSettings implements IFirmwareSettings {
    private static final Logger LOGGER = Logger.getLogger(FluidNCSettings.class.getName());

    private final Map<String, FirmwareSetting> settings = new ConcurrentHashMap<>();
    private final IController controller;
    private final Set<IFirmwareSettingsListener> listeners = Collections.synchronizedSet(new HashSet<>());

    public FluidNCSettings(IController controller) {
        this.controller = controller;
    }

    public void refresh() throws FirmwareSettingsException, CommandException {
        GetFirmwareSettingsCommand firmwareSettingsCommand = new GetFirmwareSettingsCommand();

        try {
            ControllerUtils.sendAndWaitForCompletion(controller, firmwareSettingsCommand,6000*3);
        } catch (InterruptedException e) {
            throw new FirmwareSettingsException("Timed out waiting for the controller settings", e);
        }

         LOGGER.info("refresh() Starting");
        if (firmwareSettingsCommand.isOk()) {
            Map<String, String> responseSettings = firmwareSettingsCommand.getSettings();
            responseSettings.keySet().forEach(key -> {
                String value = responseSettings.get(key);
                FirmwareSetting firmwareSetting = new FirmwareSetting(key, value, "", "", "");
                settings.put(key.toLowerCase(), firmwareSetting);                   
                
         LOGGER.info("GOT DATA " + key.toLowerCase() + "="+ firmwareSetting);
                listeners.forEach(l -> l.onUpdatedFirmwareSetting(firmwareSetting));
            });
        }
    }

    @Override
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.ofNullable(settings.get(key));
    }
    
    @Override
    public void saveFirmwareSettings() throws FirmwareSettingsException {
        FluidNCCommand cmdPersist = new FluidNCCommand("$CD=config.yaml");
        try{
            ControllerUtils.sendAndWaitForCompletion(controller, cmdPersist);  
        } catch (Exception e) {
            throw new FirmwareSettingsException("Couldn't save settings to the controller", e);
        }
                
    }
    @Override
    public FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException {
        try {
            key = key.toLowerCase();
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
    public void setHomingEnabled(boolean enabled) {

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
    public boolean isHardLimitsEnabled() {
        return getSetting("axes/x/motor0/hard_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/x/motor1/hard_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/y/motor0/hard_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/y/motor1/hard_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/z/motor0/hard_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/z/motor1/hard_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent();
    }
    
    private boolean checkMotorHasEndstop( String aAxis, String aMotor) {
        return getSetting("axes/"+aAxis+"/"+aMotor+"/limit_neg_pin").filter(s -> !StringUtils.equalsIgnoreCase("NO_PIN", s.getValue())).isPresent() || 
                getSetting("axes/"+aAxis+"/"+aMotor+"/limit_pos_pin").filter(s -> !StringUtils.equalsIgnoreCase("NO_PIN", s.getValue())).isPresent() || 
                getSetting("axes/"+aAxis+"/"+aMotor+"/limit_all_pin").filter(s -> !StringUtils.equalsIgnoreCase("NO_PIN", s.getValue())).isPresent();
    }
    
    @Override
    public boolean hasX0() {
        return checkMotorHasEndstop("x","motor0");
    }
    
    @Override
    public boolean hasX1() {
        return checkMotorHasEndstop("x","motor1");
    }
    
    @Override
    public boolean hasY0() {
        return checkMotorHasEndstop("y","motor0");
    }
    
    @Override
    public boolean hasY1() {
        return checkMotorHasEndstop("y","motor1");
    }
    
    @Override
    public boolean hasZ0() {
        return checkMotorHasEndstop("z","motor0");
    }
    
    @Override
    public boolean hasZ1() {
        return checkMotorHasEndstop("z","motor1");
    }    
    private void setHardLimitEnabled(String aAxis,String aMotor,boolean enabled) throws FirmwareSettingsException{
            if (checkMotorHasEndstop(aAxis, aMotor)) {
                setValue("axes/"+aAxis+"/"+aMotor+"/hard_limits", enabled ? "true" : "false");
            }
    }
    
    @Override
    public void setHardLimitsEnabled(boolean enabled) {
        // Only set Hard limits on 
        try {
            setHardLimitEnabled("x","motor0",enabled);
            setHardLimitEnabled("x","motor1",enabled);
            setHardLimitEnabled("y","motor0",enabled);
            setHardLimitEnabled("y","motor1",enabled);
            setHardLimitEnabled("z","motor0",enabled);
            setHardLimitEnabled("z","motor1",enabled);         
        } catch (FirmwareSettingsException ex) {
            Logger.getLogger(FluidNCSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isSoftLimitsEnabled() throws FirmwareSettingsException {
        return getSetting("axes/x/soft_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/y/soft_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent() ||
                getSetting("axes/z/soft_limits").filter(s -> StringUtils.equalsIgnoreCase("true", s.getValue())).isPresent();
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) {
        try {
            setValue("axes/x/soft_limits", enabled ? "true" : "false");
            setValue("axes/y/soft_limits", enabled ? "true" : "false");
            setValue("axes/z/soft_limits", enabled ? "true" : "false");
        } catch (FirmwareSettingsException ex) {
            Logger.getLogger(FluidNCSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    public void setMposMM(Axis axis, double limit) throws FirmwareSettingsException {
        String keyName = "axes/" + axis.name().toLowerCase() + "/homing/mpos_mm";
        
        setValue(keyName, Utils.formatter.format(limit));
    }

    @Override
    public double getMposMM(Axis axis) throws FirmwareSettingsException {
        String keyName = "axes/" + axis.name().toLowerCase() + "/homing/mpos_mm";
        
        return getSetting(keyName)
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
    public void setPulloffMM(Axis axis, String aMotor, double limit) throws FirmwareSettingsException {
        setValue("axes/" + axis.name().toLowerCase() + "/"+aMotor+"/pulloff_mm", Utils.formatter.format(limit));
    }

    @Override
    public double getPulloffMM(Axis axis, String aMotor) throws FirmwareSettingsException {
        return getSetting("axes/" + axis.name().toLowerCase() + "/"+aMotor+"/pulloff_mm")
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
        String key = "axes/" + axis.name().toLowerCase() + "/homing/positive_direction";
        FirmwareSetting firmwareSetting = getSetting(key).orElse(new FirmwareSetting(key, "false"));
        return firmwareSetting.getValue().equalsIgnoreCase("false");
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) {
        String key = "axes/" + axis.name().toLowerCase() + "/homing/positive_direction";
        try {
            setValue(key, inverted ? "false" : "true");
        } catch (FirmwareSettingsException ex) {
            Logger.getLogger(FluidNCSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isHardLimitsInverted() {
        return false;
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) {

    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException {
        settings.forEach(setting -> {
            try {
                setValue(setting.getKey().toLowerCase(), setting.getValue());
            } catch (FirmwareSettingsException e) {
                LOGGER.warning("Couldn't set the firmware setting " + setting.getKey() + " to value " + setting.getValue() + ". Error message: " + e.getMessage());
            }
        });
    }

    @Override
    public double getMaximumRate(Axis axis) {
        return 0;
    }

    private Optional<SpeedMap> getSpeedMap(String speedMapSetting) {
        FirmwareSetting value = settings.get(speedMapSetting);
        return Optional.ofNullable(value)
                .map(FirmwareSetting::getValue)
                .map(SpeedMap::new);
    }

    @Override
    public int getMaxSpindleSpeed() throws FirmwareSettingsException {
        return Stream.of(getSpeedMap("laser/speed_map"),
                        getSpeedMap("10V/speed_map"),
                        getSpeedMap("pwm/speed_map"),
                        getSpeedMap("besc/speed_map"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(SpeedMap::getMax)
                .findFirst()
                .orElseThrow(() -> new FirmwareSettingsException("Could not find setting for max speed"));
    }
}
