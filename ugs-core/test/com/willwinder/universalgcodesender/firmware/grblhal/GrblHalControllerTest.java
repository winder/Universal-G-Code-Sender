package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.ControllerException;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblProbeCommand;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.mockobjects.MockGrblCommunicator;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.CommunicatorState;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.types.ProbeGcodeCommand;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrblHalControllerTest {
    private MockGrblCommunicator communicator;
    private GrblHalControllerInitializer initializer;
    private GrblHalController controller;
    private ControllerListener listener;

    @Before
    public void setUp() {
        communicator = new MockGrblCommunicator();
        initializer = mock(GrblHalControllerInitializer.class);
        controller = new GrblHalController(communicator, initializer);
        listener = mock(ControllerListener.class);
        controller.addListener(listener);

        // The controller reports its coordinates in millimeters unless $13 says otherwise
        controller.getFirmwareSettings().updateFirmwareSetting(new FirmwareSetting("$13", "0"));
    }

    private void connect() {
        communicator.open = true;
    }

    private void initialized() {
        connect();
        when(initializer.isInitialized()).thenReturn(true);
    }

    @Test
    public void openCommPort_shouldThrowIfThePortIsAlreadyOpen() {
        connect();

        Assertions.assertThatThrownBy(() -> controller.openCommPort(ConnectionDriver.JSERIALCOMM, "/dev/ttyUSB0", 115200))
                .hasMessageContaining("already open");
    }

    @Test
    public void openCommPort_shouldConnectAndReportThatItIsConnecting() throws Exception {
        Boolean result = controller.openCommPort(ConnectionDriver.JSERIALCOMM, "/dev/ttyUSB0", 115200);

        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(communicator.numOpenCommPortCalls).isEqualTo(1);
        Assertions.assertThat(communicator.portName).isEqualTo("/dev/ttyUSB0");
        Assertions.assertThat(communicator.portRate).isEqualTo(115200);
        Assertions.assertThat(controller.getControllerStatus().getState()).isEqualTo(ControllerState.CONNECTING);
    }

    @Test
    public void rawResponseListener_shouldSetTheAlarmStateOnAnAlarmResponse() {
        initialized();

        controller.rawResponseListener("ALARM:1");

        Assertions.assertThat(controller.getControllerStatus().getState()).isEqualTo(ControllerState.ALARM);
        verify(listener).receivedAlarm(Alarm.HARD_LIMIT);
    }

    @Test
    public void rawResponseListener_shouldDispatchTheProbedPosition() {
        initialized();

        controller.rawResponseListener("[PRB:1.000,2.000,-3.000:1]");

        ArgumentCaptor<Position> position = ArgumentCaptor.forClass(Position.class);
        verify(listener).probeCoordinates(position.capture());
        Assertions.assertThat(position.getValue()).isEqualTo(new Position(1, 2, -3, Units.MM));
    }

    @Test
    public void rawResponseListener_shouldNotDispatchAFailedProbe() {
        initialized();

        controller.rawResponseListener("[PRB:1.000,2.000,-3.000:0]");

        verify(listener, never()).probeCoordinates(any());
    }

    @Test
    public void rawResponseListener_shouldUpdateTheStatusWhenInitialized() {
        initialized();

        controller.rawResponseListener("<Idle|MPos:1.000,2.000,3.000|FS:0,0>");

        Assertions.assertThat(controller.getControllerStatus().getState()).isEqualTo(ControllerState.IDLE);
        Assertions.assertThat(controller.getControllerStatus().getMachineCoord())
                .isEqualTo(new Position(1, 2, 3, Units.MM));
    }

    @Test
    public void rawResponseListener_shouldIgnoreStatusReportsUntilInitialized() {
        connect();

        controller.rawResponseListener("<Idle|MPos:1.000,2.000,3.000|FS:0,0>");

        Assertions.assertThat(controller.getControllerStatus().getState()).isEqualTo(ControllerState.DISCONNECTED);
    }

    @Test
    public void rawResponseListener_shouldStartConnectingOnAWelcomeMessage() {
        connect();

        controller.rawResponseListener("GrblHAL 1.1f ['$' or '$HELP' for help]");

        Assertions.assertThat(controller.getControllerStatus().getState()).isEqualTo(ControllerState.CONNECTING);
    }

    @Test
    public void rawResponseListener_shouldDetectAxesReportedInTheStatus() {
        initialized();

        controller.rawResponseListener("<Idle|MPos:1.000,2.000,3.000,4.000|FS:0,0>");

        Assertions.assertThat(controller.getCapabilities().hasAxis(Axis.A)).isTrue();
        Assertions.assertThat(controller.getCapabilities().hasAxis(Axis.B)).isFalse();
    }

    @Test
    public void performHomingCycle_shouldSendTheHomingCommandAndReportHoming() throws Exception {
        connect();

        controller.performHomingCycle();

        Assertions.assertThat(communicator.queuedString).isEqualTo(GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C);
        Assertions.assertThat(controller.getControllerStatus().getState()).isEqualTo(ControllerState.HOME);
    }

    @Test
    public void killAlarmLock_shouldSendTheKillAlarmLockCommand() throws Exception {
        connect();

        controller.killAlarmLock();

        Assertions.assertThat(communicator.queuedString).isEqualTo(GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND);
    }

    @Test
    public void toggleCheckMode_shouldSendTheCheckModeCommand() throws Exception {
        connect();

        controller.toggleCheckMode();

        Assertions.assertThat(communicator.queuedString).isEqualTo(GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND);
    }

    @Test
    public void viewParserState_shouldSendTheParserStateCommand() throws Exception {
        connect();

        controller.viewParserState();

        Assertions.assertThat(communicator.queuedString).isEqualTo(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND);
    }

    @Test
    public void resetCoordinatesToZero_shouldSendAWorkOffsetForEveryAxis() throws Exception {
        connect();

        controller.resetCoordinatesToZero();

        Assertions.assertThat(communicator.queuedString).isEqualTo("G10 P0 L20 X0 Y0 Z0");
    }

    @Test
    public void resetCoordinateToZero_shouldSendAWorkOffsetForTheGivenAxis() throws Exception {
        connect();

        controller.resetCoordinateToZero(Axis.Y);

        Assertions.assertThat(communicator.queuedString).isEqualTo("G10 P0 L20 Y0");
    }

    @Test
    public void setWorkPosition_shouldSendAWorkOffset() throws Exception {
        connect();

        controller.setWorkPosition(PartialPosition.builder(Units.MM).setX(10.0).build());

        Assertions.assertThat(communicator.queuedString).isEqualTo("G10 P0 L20 X10");
    }

    @Test
    public void setWorkPosition_shouldThrowWhenNotConnected() {
        Assertions.assertThatThrownBy(() -> controller.setWorkPosition(PartialPosition.builder(Units.MM).setX(10.0).build()))
                .hasMessageContaining("Must be connected");
    }

    @Test
    public void jogMachine_shouldSendAHardwareJogCommand() throws Exception {
        connect();

        controller.jogMachine(PartialPosition.builder(Units.MM).setX(10.0).build(), 1000);

        Assertions.assertThat(communicator.queuedString).startsWith("$J=");
        Assertions.assertThat(communicator.queuedString).contains("G91");
    }

    @Test
    public void jogMachineTo_shouldSendAnAbsoluteHardwareJogCommand() throws Exception {
        connect();

        controller.jogMachineTo(PartialPosition.builder(Units.MM).setX(10.0).build(), 1000);

        Assertions.assertThat(communicator.queuedString).startsWith("$J=");
        Assertions.assertThat(communicator.queuedString).contains("G90");
    }

    @Test
    public void cancelJog_shouldSendTheJogCancelByte() throws Exception {
        connect();

        controller.cancelJog();

        Assertions.assertThat(communicator.sentBytes).containsExactly(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
    }

    @Test
    public void requestStatusReport_shouldSendTheStatusByte() throws Exception {
        connect();

        controller.requestStatusReport();

        Assertions.assertThat(communicator.sentBytes).containsExactly(GrblUtils.GRBL_STATUS_COMMAND);
    }

    @Test
    public void requestStatusReport_shouldThrowWhenNotConnected() {
        Assertions.assertThatThrownBy(() -> controller.requestStatusReport())
                .isInstanceOf(ControllerException.class);
    }

    @Test
    public void issueSoftReset_shouldSendTheResetByteAndCancelTheSend() throws Exception {
        connect();

        controller.issueSoftReset();

        Assertions.assertThat(communicator.sentBytes).containsExactly(GrblUtils.GRBL_RESET_COMMAND);
        Assertions.assertThat(communicator.numCancelSendCalls).isEqualTo(1);
    }

    @Test
    public void openDoor_shouldPauseTheStreamAndSendTheDoorByte() throws Exception {
        connect();

        controller.openDoor();

        Assertions.assertThat(communicator.sentBytes)
                .containsExactly(GrblUtils.GRBL_PAUSE_COMMAND, GrblUtils.GRBL_DOOR_COMMAND);
    }

    @Test
    public void openDoor_shouldThrowWhenNotConnected() {
        Assertions.assertThatThrownBy(() -> controller.openDoor())
                .isInstanceOf(ControllerException.class);
    }

    @Test
    public void pauseStreaming_shouldSendThePauseByte() throws Exception {
        connect();

        controller.pauseStreaming();

        Assertions.assertThat(communicator.sentBytes).containsExactly(GrblUtils.GRBL_PAUSE_COMMAND);
    }

    @Test
    public void resumeStreaming_shouldSendTheResumeByte() throws Exception {
        connect();
        controller.pauseStreaming();

        controller.resumeStreaming();

        Assertions.assertThat(communicator.sentBytes)
                .containsExactly(GrblUtils.GRBL_PAUSE_COMMAND, GrblUtils.GRBL_RESUME_COMMAND);
    }

    @Test
    public void getFirmwareVersion_shouldReportNotConnectedWhenThePortIsClosed() {
        String result = controller.getFirmwareVersion();

        Assertions.assertThat(result)
                .isEqualTo("<" + Localization.getString("controller.log.notconnected") + ">");
    }

    @Test
    public void getFirmwareVersion_shouldReturnTheVersionReportedByTheController() {
        connect();
        when(initializer.getVersionString()).thenReturn("grblHAL 1.1f");

        String result = controller.getFirmwareVersion();

        Assertions.assertThat(result).isEqualTo("grblHAL 1.1f");
    }

    @Test
    public void createProbeCommand_shouldCreateAGrblProbeCommand() {
        ProbeGcodeCommand result = controller.createProbeCommand(
                PartialPosition.builder(Units.MM).setZ(-10.0).build(), new UnitValue(Unit.MM_PER_MINUTE, 100.0));

        Assertions.assertThat(result).isInstanceOf(GrblProbeCommand.class);
        Assertions.assertThat(result.getCommandString()).contains("G38.2");
    }

    @Test
    public void getCommunicatorState_shouldBeSendingWhileRunning() {
        initialized();

        controller.rawResponseListener("<Run|MPos:0.000,0.000,0.000|FS:0,0>");

        Assertions.assertThat(controller.getCommunicatorState()).isEqualTo(CommunicatorState.COMM_SENDING);
    }

    @Test
    public void getCommunicatorState_shouldBeIdleWhenIdle() {
        initialized();

        controller.rawResponseListener("<Idle|MPos:0.000,0.000,0.000|FS:0,0>");

        Assertions.assertThat(controller.getCommunicatorState()).isEqualTo(CommunicatorState.COMM_IDLE);
    }

    @Test
    public void isReadyToStreamFile_shouldThrowWhenTheControllerIsInAlarm() {
        initialized();
        controller.rawResponseListener("ALARM:1");

        Assertions.assertThatThrownBy(() -> controller.isReadyToStreamFile())
                .hasMessageContaining(Localization.getString("grbl.exception.Alarm"));
    }

    @Test
    public void closeCommPort_shouldResetTheInitializer() throws Exception {
        connect();

        controller.closeCommPort();

        verify(initializer).reset();
    }

    @Test
    public void getCapabilities_shouldBeEmptyBeforeTheControllerIsInitialized() {
        Assertions.assertThat(controller.getCapabilities().hasOverrides()).isFalse();
        Assertions.assertThat(controller.getCapabilities().hasJogging()).isFalse();
    }
}
