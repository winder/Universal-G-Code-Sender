package com.willwinder.universalgcodesender.firmware.marlin;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.willwinder.universalgcodesender.MarlinController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;

public class MarlinFirmwareSettings implements CommunicatorListener, IFirmwareSettingsListener, IFirmwareSettings {
	private static final Logger LOGGER = Logger.getLogger(MarlinFirmwareSettings.class.getName());

	public MarlinFirmwareSettings(MarlinController marlinController) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public Optional<FirmwareSetting> getSetting(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addListener(IFirmwareSettingsListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeListener(IFirmwareSettingsListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isHomingEnabled() throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHomingEnabled(boolean enabled) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public Units getReportingUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FirmwareSetting> getAllSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isHardLimitsEnabled() throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHardLimitsEnabled(boolean enabled) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSoftLimitsEnabled() throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setSoftLimitsEnabled(boolean enabled) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isInvertDirection(Axis axis) throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setInvertDirection(Axis axis, boolean inverted) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStepsPerMillimeter(Axis axis, double stepsPerMillimeter) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getStepsPerMillimeter(Axis axis) throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSoftLimit(Axis axis, double limit) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getSoftLimit(Axis axis) throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isHomingDirectionInverted(Axis axis) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHomingDirectionInverted(Axis axis, boolean inverted) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isHardLimitsInverted() throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	public double getMaximumRate(Axis axis) throws FirmwareSettingsException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onUpdatedFirmwareSetting(FirmwareSetting setting) {
		// TODO Auto-generated method stub

	}

	@Override
	public void rawResponseListener(String response) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commandSent(GcodeCommand command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void commandSkipped(GcodeCommand command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void communicatorPausedOnError() {
		// TODO Auto-generated method stub

	}

}
