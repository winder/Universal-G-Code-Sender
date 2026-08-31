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

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOption;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblCapabilitiesConstants;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * Helpers for detecting and controlling a grblHAL controller.
 * <p>
 * grblHAL speaks the GRBL 1.1 protocol, so parsing of status reports, feedback messages and
 * responses is delegated to {@link GrblUtils}. This class only contains the parts that differ.
 *
 * @author Joacim Breiler
 */
public class GrblHalUtils {

    /**
     * The firmware name that grblHAL reports in the build info command ($I)
     */
    public static final String FIRMWARE_NAME = "grblHAL";

    /**
     * Enumerates the settings together with their name, data type, unit and value range
     */
    public static final String GRBLHAL_SETTING_DETAILS_COMMAND = "$ES";

    /**
     * Lists the setting groups
     */
    public static final String GRBLHAL_SETTING_GROUPS_COMMAND = "$EG";

    /**
     * Lists the alarm codes supported by the controller
     */
    public static final String GRBLHAL_ALARM_CODES_COMMAND = "$EA";

    /**
     * Lists the error codes supported by the controller
     */
    public static final String GRBLHAL_ERROR_CODES_COMMAND = "$EE";

    /**
     * The welcome message is sent when the controller has been reset,
     * ie: {@code GrblHAL 1.1f ['$' or '$HELP' for help]}
     */
    private static final Pattern WELCOME_PATTERN = Pattern.compile("^GrblHAL\\s+\\d+\\.\\d+.*", Pattern.CASE_INSENSITIVE);

    private static final Pattern PROBE_PATTERN = Pattern.compile("\\[PRB:.*]");

    /**
     * Probe positions may contain up to six axes
     */
    private static final Pattern PROBE_POSITION_PATTERN = Pattern.compile("\\[PRB:(-?\\d*\\.\\d*),(-?\\d*\\.\\d*),(-?\\d*\\.\\d*)(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?:\\d?]");

    private GrblHalUtils() {
        // Can not be instanced
    }

    /**
     * Returns if the response is a welcome message from a grblHAL controller. Other firmwares, such
     * as FluidNC, also identify themselves using the name GrblHAL and are therefore not accepted.
     *
     * @param response a response from the controller
     * @return true if this is a grblHAL welcome message
     */
    public static boolean isWelcomeMessage(String response) {
        String trimmedResponse = StringUtils.trimToEmpty(response);
        return WELCOME_PATTERN.matcher(trimmedResponse).matches() &&
                !StringUtils.containsIgnoreCase(trimmedResponse, "FluidNC");
    }

    public static boolean isProbeMessage(String response) {
        return PROBE_PATTERN.matcher(response).find();
    }

    /**
     * Parses the position from a probe message, ie: {@code [PRB:0.000,0.000,-10.000:1]}
     *
     * @param response the probe message
     * @param units    the units that the controller reports its coordinates in
     * @return the probed position or null if the probe failed
     */
    public static Position parseProbePosition(String response, Units units) {
        if (response.endsWith(":0]")) {
            return null;
        }

        return GrblUtils.getPositionFromStatusString(response, PROBE_POSITION_PATTERN, units);
    }

    /**
     * Generates a command for setting the work coordinate position for one or more axes.
     *
     * @param offsets the new work position
     * @return a gcode command
     */
    public static String getSetCoordCommand(PartialPosition offsets) {
        return "G10 P0 L20 " + offsets.getFormattedGCode();
    }

    /**
     * Builds the capabilities for a connected grblHAL controller. As grblHAL implements the GRBL 1.1
     * protocol it will always support the capabilities added by GRBL 1.1.
     *
     * @param buildOptions the options reported in the [OPT:...] line
     * @return the capabilities of the controller
     */
    public static Capabilities getCapabilities(GrblBuildOptions buildOptions) {
        Capabilities capabilities = new Capabilities();
        capabilities.addCapability(CapabilitiesConstants.X_AXIS);
        capabilities.addCapability(CapabilitiesConstants.Y_AXIS);
        capabilities.addCapability(CapabilitiesConstants.Z_AXIS);
        capabilities.addCapability(CapabilitiesConstants.JOGGING);
        capabilities.addCapability(CapabilitiesConstants.CONTINUOUS_JOGGING);
        capabilities.addCapability(CapabilitiesConstants.CHECK_MODE);
        capabilities.addCapability(CapabilitiesConstants.FIRMWARE_SETTINGS);
        capabilities.addCapability(CapabilitiesConstants.RETURN_TO_ZERO);
        capabilities.addCapability(CapabilitiesConstants.HOMING);
        capabilities.addCapability(CapabilitiesConstants.HARD_LIMITS);
        capabilities.addCapability(CapabilitiesConstants.SOFT_LIMITS);
        capabilities.addCapability(CapabilitiesConstants.OVERRIDES);
        capabilities.addCapability(CapabilitiesConstants.OPEN_DOOR);
        capabilities.addCapability(CapabilitiesConstants.SETUP_WIZARD);
        capabilities.addCapability(CapabilitiesConstants.STEP_CALIBRATION);
        capabilities.addCapability(CapabilitiesConstants.MOTOR_WIRING);
        capabilities.addCapability(GrblCapabilitiesConstants.REAL_TIME);
        capabilities.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        capabilities.addCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING);

        if (buildOptions.isEnabled(GrblBuildOption.VARIABLE_SPINDLE_ENABLED)) {
            capabilities.addCapability(CapabilitiesConstants.VARIABLE_SPINDLE);
        }

        if (buildOptions.isEnabled(GrblBuildOption.HOMING_FORCE_ORIGIN_ENABLED)) {
            capabilities.addCapability(CapabilitiesConstants.HOMING_SETS_MACHINE_ZERO_POSITION);
        }

        return capabilities;
    }
}
