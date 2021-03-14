package com.willwinder.universalgcodesender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

public class MarlinUtils {
	public static boolean isOkErrorAlarmResponse(String response) {
		return isOkResponse(response); // || isErrorResponse(response) || isAlarmResponse(response);
	}

	public static boolean isOkResponse(String response) {
		return StringUtils.equalsIgnoreCase(response, "ok");
	}

	/**
	 * Check if a string contains a Marlin position string.
	 */
	private static final String STATUS_REGEX = "^X\\:";
	private static final Pattern STATUS_PATTERN = Pattern.compile(STATUS_REGEX);
	private static final String GCODE_SET_COORDINATE = "G92";
	public static final String GCODE_ABS_COORDS = "G90";
	public static final String GCODE_REL_COORDS = "G91";
	public static final String GCODE_RETURN_TO_XY_ZERO_LOCATION = "G0 X0 Y0";
	public static final String GCODE_RETURN_TO_Z_ZERO_LOCATION = "G0 Z0";

	static protected Boolean isMarlinStatusString(final String response) {
		return STATUS_PATTERN.matcher(response).find();
	}

	/*
	 * M114 response...
	 *
	 * X:0.00 Y:0.00 Z:0.00 E:0.00 Count X:0 Y:0 Z:0
	 */

	static protected ControllerStatus getStatusFromStatusString(
			ControllerStatus lastStatus, final String status,
			final Capabilities version, Units reportingUnits) {
		final Pattern splitterPattern = Pattern.compile("^X\\:([^ ]+) Y\\:([^ ]+) Z\\:([^ ]+) E");
		Matcher matcher = splitterPattern.matcher(status);
		if (matcher.find()) {
			Double xpos = getCoord(matcher, 1);
			Double ypos = getCoord(matcher, 2);
			Double zpos = getCoord(matcher, 3);
			Position pos = new Position(xpos, ypos, zpos, Units.MM);
			return new ControllerStatus(lastStatus.getState(), pos, pos);
		}

		return lastStatus;
	}

	private static Double getCoord(Matcher matcher, int idx) {
		String str = matcher.group(idx);
		Double pos = Double.parseDouble(str);
		return pos;
	}

	public static boolean isMarlinEchoMessage(String response) {
		return StringUtils.startsWith(response, "echo:");
	}

	public static boolean isBusyResponse(String response) {
		return StringUtils.startsWith(response, "echo:busy:");
	}

	public static boolean isPausedResponse(String response) {
		return StringUtils.startsWith(response, "echo:busy: paused for user");
	}

	/**
	 * Generate a command to set the work coordinate position for multiple axis.
	 *
	 * @param offsets
	 *            the new work position to use (one ore more axis)
	 * @return a string with the gcode command
	 */
	protected static String getSetCoordCommand(PartialPosition offsets) {
		String coordsString = offsets.getFormattedGCode();
		return MarlinUtils.GCODE_SET_COORDINATE + " " + coordsString;
	}

}
