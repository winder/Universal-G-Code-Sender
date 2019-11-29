package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrblUtilsControllerStatusTest {

    @Test
    public void getStatusFromStringVersion1WithCompleteStatusString() {
        String status = "<Idle|MPos:1.1,2.2,3.3|WPos:4.4,5.5,6.6|WCO:7.7,8.8,9.9|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getStateString()).isEqualTo("Idle");
        assertThat(controllerStatus.getState()).isEqualTo(ControllerState.IDLE);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(1.1, 2.2, 3.3, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(4.4, 5.5, 6.6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(7.7, 8.8, 9.9, UnitUtils.Units.MM));

        assertThat(controllerStatus.getOverrides().feed).isEqualTo(1);
        assertThat(controllerStatus.getOverrides().rapid).isEqualTo(2);
        assertThat(controllerStatus.getOverrides().spindle).isEqualTo(3);

        assertThat(controllerStatus.getFeedSpeed()).isEqualTo(Double.valueOf(12345.7));
        assertThat(controllerStatus.getSpindleSpeed()).isEqualTo(Double.valueOf(65432.1));

        assertThat(controllerStatus.getEnabledPins().CycleStart).isTrue();
        assertThat(controllerStatus.getEnabledPins().Door).isTrue();
        assertThat(controllerStatus.getEnabledPins().Hold).isTrue();
        assertThat(controllerStatus.getEnabledPins().SoftReset).isTrue();
        assertThat(controllerStatus.getEnabledPins().Probe).isTrue();
        assertThat(controllerStatus.getEnabledPins().X).isTrue();
        assertThat(controllerStatus.getEnabledPins().Y).isTrue();
        assertThat(controllerStatus.getEnabledPins().Z).isTrue();

        assertThat(controllerStatus.getAccessoryStates().Flood).isTrue();
        assertThat(controllerStatus.getAccessoryStates().Mist).isTrue();
        assertThat(controllerStatus.getAccessoryStates().SpindleCCW).isTrue();
        assertThat(controllerStatus.getAccessoryStates().SpindleCW).isTrue();
    }

    @Test
    public void getStatusFromStringVersion1WithoutWorkCoordinateOffsetStatusString() {
        String status = "<Idle|MPos:1.1,2.2,3.3|WPos:4.4,5.5,6.6|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(1.1, 2.2, 3.3, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(4.4, 5.5, 6.6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(0, 0, 0, UnitUtils.Units.MM));
    }

    @Test
    public void getStatusFromStringVersion1WithoutWorkCoordinateStatusString() {
        String status = "<Idle|MPos:1.0,2.0,3.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(1, 2, 3, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(-6, -6, -6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(7, 8, 9, UnitUtils.Units.MM));
    }

    @Test
    public void getStatusFromStringVersion1WithoutMachineCoordinateStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(11, 13, 15, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(4, 5, 6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(7, 8, 9, UnitUtils.Units.MM));
    }

    @Test
    public void getStatusFromStringVersion1WhereFeedOverridesFeedSpindleStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getFeedSpeed()).isEqualTo(Double.valueOf(12345.6));
        assertThat(controllerStatus.getSpindleSpeed()).isEqualTo(Double.valueOf(65432.1));
    }

    @Test
    public void getStatusFromStringVersion1WithoutPinsStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getEnabledPins().CycleStart).isFalse();
        assertThat(controllerStatus.getEnabledPins().Door).isFalse();
        assertThat(controllerStatus.getEnabledPins().Hold).isFalse();
        assertThat(controllerStatus.getEnabledPins().SoftReset).isFalse();
        assertThat(controllerStatus.getEnabledPins().Probe).isFalse();
        assertThat(controllerStatus.getEnabledPins().X).isFalse();
        assertThat(controllerStatus.getEnabledPins().Y).isFalse();
        assertThat(controllerStatus.getEnabledPins().Z).isFalse();
    }

    @Test
    public void getStatusFromStringVersion1WithoutAccessoryStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getAccessoryStates().Flood).isFalse();
        assertThat(controllerStatus.getAccessoryStates().Mist).isFalse();
        assertThat(controllerStatus.getAccessoryStates().SpindleCCW).isFalse();
        assertThat(controllerStatus.getAccessoryStates().SpindleCW).isFalse();
    }

}