package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetBuildInfoCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetStatusCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.SystemCommand;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.MessageService;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletion;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletionWithRetry;
import com.willwinder.universalgcodesender.utils.SemanticVersion;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FluidNCUtils {
    public static final double GRBL_COMPABILITY_VERSION = 1.1d;
    public static final SemanticVersion MINIMUM_VERSION = new SemanticVersion(3, 3, 0);
    public static final byte DISABLE_ECHO_COMMAND = (byte)0x0c;

    private static final String MESSAGE_REGEX = "\\[MSG:.*]";
    private static final Pattern MESSAGE_PATTERN = Pattern.compile(MESSAGE_REGEX);
    private static final String PROBE_REGEX = "\\[PRB:.*]";
    private static final Pattern PROBE_PATTERN = Pattern.compile(PROBE_REGEX);
    private static final String WELCOME_REGEX = "(?<protocolvendor>.*)\\s(?<protocolversion>[0-9a-z.]*)\\s\\[((?<fncvariant>[a-zA-Z]*)?\\s(v(?<fncversion>[0-9.]*))?)+.*]";
    private static final Pattern WELCOME_PATTERN = Pattern.compile(WELCOME_REGEX, Pattern.CASE_INSENSITIVE);
    private static final Pattern PROBE_POSITION_PATTERN = Pattern.compile("\\[PRB:(-?\\d*\\.\\d*),(-?\\d*\\.\\d*),(-?\\d*\\.\\d*)(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?:\\d?]");

    public static boolean isMessageResponse(String response) {
        return MESSAGE_PATTERN.matcher(response).find();
    }

    protected static Optional<String> parseMessageResponse(final String response) {
        if (!isMessageResponse(response)) {
            return Optional.empty();
        }

        return Optional.of(response.substring(5, response.length() - 1));
    }

    public static boolean isProbeMessage(String response) {
        return PROBE_PATTERN.matcher(response).find();
    }

    protected static Position parseProbePosition(final String response, final UnitUtils.Units units) {
        // Don't parse failed probe response.
        if (response.endsWith(":0]")) {
            return Position.INVALID;
        }

        return GrblUtils.getPositionFromStatusString(response, PROBE_POSITION_PATTERN, units);
    }

    public static boolean isWelcomeResponse(String response) {
        return WELCOME_PATTERN.matcher(response).find();
    }

    public static Optional<SemanticVersion> parseSemanticVersion(String response) {
        Matcher matcher = WELCOME_PATTERN.matcher(response);
        if (matcher.find()) {
            String versionString = matcher.group("fncversion");
            try {
                return Optional.of(new SemanticVersion(versionString));
            } catch (ParseException e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public static ControllerStatus getStatusFromStatusResponse(ControllerStatus lastStatus, String status, UnitUtils.Units reportingUnits) {
        return GrblUtils.getStatusFromStatusStringV1(lastStatus, status, reportingUnits);
    }

    public static GetStatusCommand queryForStatusReport(IController controller, MessageService messageService) throws InterruptedException {
        return sendAndWaitForCompletionWithRetry(GetStatusCommand::new, controller, 4000, 10, executionNumber -> {
            if (executionNumber == 1) {
                messageService.dispatchMessage(MessageType.INFO, "*** Fetching device status\n");
            } else {
                messageService.dispatchMessage(MessageType.INFO, "*** Fetching device status (" + executionNumber + " of 10)...\n");
            }
        });
    }

    public static void addCapabilities(Capabilities capabilities, SemanticVersion version, IFirmwareSettings firmwareSettings) {
        capabilities.addCapability(CapabilitiesConstants.JOGGING);
        capabilities.addCapability(CapabilitiesConstants.RETURN_TO_ZERO);
        capabilities.addCapability(CapabilitiesConstants.CONTINUOUS_JOGGING);
        capabilities.addCapability(CapabilitiesConstants.HOMING);
        capabilities.addCapability(CapabilitiesConstants.FIRMWARE_SETTINGS);
        capabilities.addCapability(CapabilitiesConstants.OVERRIDES);
        capabilities.addCapability(CapabilitiesConstants.OPEN_DOOR);

        try {
            if (firmwareSettings.isSoftLimitsEnabled()) {
                capabilities.addCapability(CapabilitiesConstants.SOFT_LIMITS);
            }

            firmwareSettings.getAllSettings().forEach(setting -> {
                addCapabilityIfSettingStartsWith(capabilities, setting, "axes/a", CapabilitiesConstants.A_AXIS);
                addCapabilityIfSettingStartsWith(capabilities, setting, "axes/b", CapabilitiesConstants.B_AXIS);
                addCapabilityIfSettingStartsWith(capabilities, setting, "axes/c", CapabilitiesConstants.C_AXIS);
                addCapabilityIfSettingStartsWith(capabilities, setting, "axes/x", CapabilitiesConstants.X_AXIS);
                addCapabilityIfSettingStartsWith(capabilities, setting, "axes/y", CapabilitiesConstants.Y_AXIS);
                addCapabilityIfSettingStartsWith(capabilities, setting, "axes/z", CapabilitiesConstants.Z_AXIS);
            });
        } catch (FirmwareSettingsException e) {
            // Never mind
        }

        if (version.compareTo(new SemanticVersion(3, 5, 2)) >= 0) {
            capabilities.addCapability(CapabilitiesConstants.FILE_SYSTEM);
        }
    }

    /**
     * Adds a capability if there is a setting which key starts with the given setting key.
     *
     * @param setting              the setting to check
     * @param settingKey           the key which the setting key should start with
     * @param capabilitiesConstant the capabilities constant to add
     */
    private static void addCapabilityIfSettingStartsWith(Capabilities capabilities, FirmwareSetting setting, String settingKey, String capabilitiesConstant) {
        if (StringUtils.startsWith(setting.getKey().toLowerCase(), settingKey.toLowerCase())) {
            capabilities.addCapability(capabilitiesConstant);
        }
    }

    /**
     * Queries the controller for the firmware version
     *
     * @param controller     the current controller that should send the command
     * @param messageService the message service
     * @return the command that was executed by the controller containing the parsed version
     * @throws Exception             if the command couldn't be sent
     * @throws IllegalStateException if the parsed version is not for a FluidNC controller or the version is too old
     */
    public static GetBuildInfoCommand queryBuildInformation(IController controller, MessageService messageService) throws Exception {
        messageService.dispatchMessage(MessageType.INFO, "*** Fetching device firmware version\n");
        GetBuildInfoCommand getBuildInfoCommand = sendAndWaitForCompletion(controller, new GetBuildInfoCommand());
        String firmwareVariant = getBuildInfoCommand.getFirmware();
        SemanticVersion semanticVersion = getBuildInfoCommand.getVersion();

        if (!firmwareVariant.equalsIgnoreCase("FluidNC") || semanticVersion.compareTo(MINIMUM_VERSION) < 0) {
            messageService.dispatchMessage(MessageType.INFO, String.format("*** Expected a 'FluidNC %s' or later but got '%s %s'\n", MINIMUM_VERSION, firmwareVariant, semanticVersion));
            throw new IllegalStateException("Unknown controller version: " + semanticVersion.toString());
        }

        return getBuildInfoCommand;
    }

    /**
     * Checks if the controller is responsive and not in a locked alarm state.
     *
     * @return true if responsive
     * @throws Exception if we couldn't query for status
     */
    public static boolean isControllerResponsive(IController controller, MessageService messageService) throws Exception {
        GetStatusCommand statusCommand = FluidNCUtils.queryForStatusReport(controller, messageService);
        if (!statusCommand.isDone() || statusCommand.isError()) {
            throw new IllegalStateException("Could not query the device status");
        }

        // Some commands are not available in check mode
        if (statusCommand.getControllerStatus().getState() == ControllerState.CHECK) {
            return false;
        }

        // The controller is not up and running properly
        if (statusCommand.getControllerStatus().getState() == ControllerState.SLEEP || statusCommand.getControllerStatus().getState() == ControllerState.HOLD || statusCommand.getControllerStatus().getState() == ControllerState.DOOR || statusCommand.getControllerStatus().getState() == ControllerState.ALARM) {
            try {
                // Figure out if it is still responsive even if it is in HOLD or ALARM state
                // We can do this
                SystemCommand systemCommand = sendAndWaitForCompletion(controller, new SystemCommand("$I"));
                return systemCommand.isOk();
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }
}
