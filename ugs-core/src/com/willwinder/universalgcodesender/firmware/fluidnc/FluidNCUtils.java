package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.SemanticVersion;

import java.text.ParseException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.willwinder.universalgcodesender.GrblUtils.getControllerStateFromStateString;

public class FluidNCUtils {
    public static final double GRBL_COMPABILITY_VERSION = 1.1d;

    private static final String MESSAGE_REGEX = "\\[MSG:.*]";
    private static final Pattern MESSAGE_PATTERN = Pattern.compile(MESSAGE_REGEX);
    private static final String WELCOME_REGEX = "(?<protocolvendor>.*)\\s(?<protocolversion>[0-9a-z.]*)\\s\\[((?<fncvariant>[a-zA-Z]*)?\\s(v(?<fncversion>[0-9.]*))?)+.*]";
    private static final Pattern WELCOME_PATTERN = Pattern.compile(WELCOME_REGEX, Pattern.CASE_INSENSITIVE);
    private static final Pattern MACHINE_PATTERN = Pattern.compile("(?<=MPos:)(-?\\d*\\.?\\d*),(-?\\d*\\.?\\d*),(-?\\d*\\.?\\d*)(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?");
    private static final Pattern WORK_PATTERN = Pattern.compile("(?<=WPos:)(-?\\d*\\.?\\d*),(-?\\d*\\.?\\d*),(-?\\d*\\.?\\d*)(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?");
    private static final Pattern WCO_PATTERN = Pattern.compile("(?<=WCO:)(-?\\d*\\.?\\d*),(-?\\d*\\.?\\d*),(-?\\d*\\.?\\d*)(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?(?:,(-?\\d*\\.?\\d+))?");

    public static boolean isMessageResponse(String response) {
        return MESSAGE_PATTERN.matcher(response).find();
    }

    static protected Optional<String> parseMessageResponse(final String response) {
        if (!isMessageResponse(response)) {
            return Optional.empty();
        }

        return Optional.of(response.substring(5, response.length() - 1));
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

    public static String getVersionNumber(String response) {
        return response;
    }

    public static Optional<String> parseFirmwareVariant(String response) {
        Matcher matcher = WELCOME_PATTERN.matcher(response);
        if (matcher.find()) {
            String versionString = matcher.group("fncvariant");
            return Optional.ofNullable(versionString);
        }

        return Optional.empty();
    }

    public static ControllerStatus getStatusFromStatusResponse(ControllerStatus lastStatus, String status, UnitUtils.Units reportingUnits) {
        String stateString = "";
        String subStateString = "";
        Position MPos = null;
        Position WPos = null;
        Position WCO = null;

        ControllerStatus.OverridePercents overrides = null;
        ControllerStatus.EnabledPins pins = null;
        ControllerStatus.AccessoryStates accessoryStates = null;

        double feedSpeed = 0;
        double spindleSpeed = 0;
        if (lastStatus != null) {
            feedSpeed = lastStatus.getFeedSpeed();
            spindleSpeed = lastStatus.getSpindleSpeed();
        }
        boolean isOverrideReport = false;

        // Parse out the status messages.
        for (String part : status.substring(0, status.length() - 1).split("\\|")) {
            if (part.startsWith("<")) {
                int idx = part.indexOf(':');
                if (idx == -1) {
                    stateString = part.substring(1);
                } else {
                    stateString = part.substring(1, idx);
                    subStateString = part.substring(idx + 1);
                }
            } else if (part.startsWith("MPos:")) {
                MPos = GrblUtils.getPositionFromStatusString(status, MACHINE_PATTERN, reportingUnits);
            } else if (part.startsWith("WPos:")) {
                WPos = GrblUtils.getPositionFromStatusString(status, WORK_PATTERN, reportingUnits);
            } else if (part.startsWith("WCO:")) {
                WCO = GrblUtils.getPositionFromStatusString(status, WCO_PATTERN, reportingUnits);
            } else if (part.startsWith("Ov:")) {
                isOverrideReport = true;
                String[] overrideParts = part.substring(3).trim().split(",");
                if (overrideParts.length == 3) {
                    overrides = new ControllerStatus.OverridePercents(
                            Integer.parseInt(overrideParts[0]),
                            Integer.parseInt(overrideParts[1]),
                            Integer.parseInt(overrideParts[2]));
                }
            } else if (part.startsWith("F:")) {
                feedSpeed = GrblUtils.parseFeedSpeed(part);
            } else if (part.startsWith("FS:")) {
                String[] parts = part.substring(3).split(",");
                feedSpeed = Double.parseDouble(parts[0]);
                spindleSpeed = Double.parseDouble(parts[1]);
            } else if (part.startsWith("Pn:")) {
                String value = part.substring(part.indexOf(':') + 1);
                pins = new ControllerStatus.EnabledPins(value);
            } else if (part.startsWith("A:")) {
                String value = part.substring(part.indexOf(':') + 1);
                accessoryStates = new ControllerStatus.AccessoryStates(value);
            }
        }

        // Grab WCO from state information if necessary.
        if (WCO == null) {
            // Grab the work coordinate offset.
            if (lastStatus != null && lastStatus.getWorkCoordinateOffset() != null) {
                WCO = lastStatus.getWorkCoordinateOffset();
            } else {
                WCO = new Position(0, 0, 0, 0, 0, 0, reportingUnits);
            }
        }

        // Calculate missing coordinate with WCO
        if (WPos == null && MPos != null) {
            WPos = new Position(MPos.x - WCO.x, MPos.y - WCO.y, MPos.z - WCO.z, MPos.a - WCO.a, MPos.b - WCO.b, MPos.c - WCO.c, reportingUnits);
        } else if (MPos == null && WPos != null) {
            MPos = new Position(WPos.x + WCO.x, WPos.y + WCO.y, WPos.z + WCO.z, WPos.a + WCO.a, WPos.b + WCO.b, WPos.c + WCO.c, reportingUnits);
        }

        if (!isOverrideReport && lastStatus != null) {
            overrides = lastStatus.getOverrides();
            pins = lastStatus.getEnabledPins();
            accessoryStates = lastStatus.getAccessoryStates();
        } else if (isOverrideReport) {
            // If this is an override report and the 'Pn:' field wasn't sent
            // set all pins to a disabled state.
            if (pins == null) {
                pins = new ControllerStatus.EnabledPins("");
            }
            // Likewise for accessory states.
            if (accessoryStates == null) {
                accessoryStates = new ControllerStatus.AccessoryStates("");
            }
        }

        ControllerState state = getControllerStateFromStateString(stateString);

        return new ControllerStatus(state, subStateString, MPos, WPos, feedSpeed, reportingUnits, spindleSpeed, overrides, WCO, pins, accessoryStates);
    }
}
