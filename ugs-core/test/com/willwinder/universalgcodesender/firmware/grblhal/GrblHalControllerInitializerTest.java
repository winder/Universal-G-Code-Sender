package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.ControllerException;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOption;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetParserStateCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetSettingsCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetStatusCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetAlarmCodesCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetBuildInfoCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetErrorCodesCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetSettingDetailsCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetSettingGroupsCommand;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.assertj.core.api.Assertions;

import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrblHalControllerInitializerTest {
    /**
     * The controller is mocked and does not need time to boot
     */
    private static final Duration NO_STARTUP_DELAY = Duration.ZERO;

    private GrblHalController controller;
    private GrblHalFirmwareSettings firmwareSettings;
    private GrblHalCodes codes;
    private GrblHalControllerInitializer initializer;

    @Before
    public void setUp() throws Exception {
        controller = mock(GrblHalController.class);
        firmwareSettings = new GrblHalFirmwareSettings(controller);
        codes = new GrblHalCodes();

        when(controller.isCommOpen()).thenReturn(true);
        when(controller.getMessageService()).thenReturn(new MessageService());
        when(controller.getFirmwareSettings()).thenReturn(firmwareSettings);
        when(controller.getCodes()).thenReturn(codes);

        // Any command that a test does not care about completes with an ok
        respondTo(GcodeCommand.class, "ok");

        initializer = new GrblHalControllerInitializer(controller, NO_STARTUP_DELAY);
    }

    /**
     * Completes any command of the given type with the given response lines. Stubbings are matched
     * last to first, so a specific command type has to be registered after the general ones.
     */
    private void respondTo(Class<? extends GcodeCommand> type, String... responseLines) {
        doAnswer(invocation -> {
            GcodeCommand command = invocation.getArgument(0);
            for (String line : responseLines) {
                command.appendResponse(line);
            }
            command.setDone(true);
            return null;
        }).when(controller).sendCommandImmediately(any(type));
    }

    private void respondToStatusWith(ControllerState state) throws Exception {
        respondTo(GetStatusCommand.class, "<" + state.name() + "|MPos:0.000,0.000,0.000|FS:0,0>");
    }

    private void respondToEverything() throws Exception {
        respondToStatusWith(ControllerState.IDLE);
        respondTo(GetBuildInfoCommand.class,
                "[VER:1.1f.20230312:]",
                "[OPT:VSHM,35,1024]",
                "[NEWOPT:ENUMS,TC]",
                "[FIRMWARE:grblHAL]",
                "ok");
        respondTo(GetSettingDetailsCommand.class,
                "[SETTING:14|2|Invert control inputs||1|N/A,Feed hold,Cycle start|||0|0]",
                "ok");
        respondTo(GetSettingGroupsCommand.class, "[SETTINGGROUP:2|0|Control signals]", "ok");
        respondTo(GetErrorCodesCommand.class, "[ERRORCODE:1|Expected command letter|A description.]", "ok");
        respondTo(GetAlarmCodesCommand.class, "[ALARMCODE:1|Hard limit|Another description.]", "ok");
        respondTo(GetSettingsCommand.class, "$13=0", "ok");
        respondTo(GetParserStateCommand.class, "[GC:G0 G54 G17 G21 G90]", "ok");
    }

    @Test
    public void isInitialized_shouldBeFalseBeforeInitializing() {
        Assertions.assertThat(initializer.isInitialized()).isFalse();
        Assertions.assertThat(initializer.isInitializing()).isFalse();
    }

    @Test
    public void getVersionString_shouldOnlyReturnTheFirmwareNameWhenTheVersionIsUnknown() {
        Assertions.assertThat(initializer.getVersionString()).isEqualTo("grblHAL");
    }

    @Test
    public void getBuildOptions_shouldBeEmptyBeforeInitializing() {
        Assertions.assertThat(initializer.getBuildOptions().isEnabled(GrblBuildOption.VARIABLE_SPINDLE_ENABLED)).isFalse();
        Assertions.assertThat(initializer.getExtendedOptions().getOptions()).isEmpty();
    }

    @Test
    public void initialize_shouldDetectTheVersionAndOptions() throws Exception {
        respondToEverything();

        boolean result = initializer.initialize();

        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(initializer.isInitialized()).isTrue();
        Assertions.assertThat(initializer.isInitializing()).isFalse();
        Assertions.assertThat(initializer.getVersion().getVersionNumber()).isEqualTo(1.1);
        Assertions.assertThat(initializer.getVersion().getVersionLetter()).isEqualTo('f');
        Assertions.assertThat(initializer.getVersionString()).isEqualTo("grblHAL 1.1f");
        Assertions.assertThat(initializer.getBuildOptions().isEnabled(GrblBuildOption.VARIABLE_SPINDLE_ENABLED)).isTrue();
        Assertions.assertThat(initializer.getExtendedOptions().isEnabled(GrblHalOption.TOOL_CHANGE)).isTrue();
        verify(controller, never()).issueSoftReset();
    }

    @Test
    public void initialize_shouldStoreTheSettingDetailsGroupsAndCodes() throws Exception {
        respondToEverything();

        initializer.initialize();

        Assertions.assertThat(firmwareSettings.getSettingDetail("$14"))
                .map(GrblHalSettingDetail::name)
                .contains("Invert control inputs");
        Assertions.assertThat(firmwareSettings.getGroupNames()).containsExactly("Control signals");
        Assertions.assertThat(codes.getErrorCode("1")).map(GrblHalCode::message).contains("Expected command letter");
        Assertions.assertThat(codes.getAlarmCode("1")).map(GrblHalCode::message).contains("Hard limit");
    }

    @Test
    public void initialize_shouldFetchTheSettingsAndParserStateLast() throws Exception {
        respondToEverything();

        initializer.initialize();

        verify(controller, times(1)).sendCommandImmediately(any(GetSettingsCommand.class));
        verify(controller, times(1)).sendCommandImmediately(any(GetParserStateCommand.class));
    }

    @Test
    public void initialize_shouldOnlyInitializeOnce() throws Exception {
        respondToEverything();

        Assertions.assertThat(initializer.initialize()).isTrue();
        Assertions.assertThat(initializer.initialize()).isFalse();
        verify(controller, times(1)).sendCommandImmediately(any(GetBuildInfoCommand.class));
    }

    @Test
    public void initialize_shouldStillSucceedWhenTheControllerCanNotEnumerateItsSettingsAndCodes() throws Exception {
        respondToEverything();
        respondTo(GetSettingDetailsCommand.class, "error:2");
        respondTo(GetSettingGroupsCommand.class, "error:2");
        respondTo(GetErrorCodesCommand.class, "error:2");
        respondTo(GetAlarmCodesCommand.class, "error:2");

        boolean result = initializer.initialize();

        Assertions.assertThat(result).isTrue();
        Assertions.assertThat(firmwareSettings.getSettingDetail("$14")).isEmpty();
        Assertions.assertThat(firmwareSettings.getGroupNames()).isEmpty();
        Assertions.assertThat(codes.getErrorCode("1")).isEmpty();
        Assertions.assertThat(codes.getAlarmCode("1")).isEmpty();
    }

    @Test
    public void initialize_shouldThrowWhenTheVersionCanNotBeDetected() throws Exception {
        respondToEverything();
        respondTo(GetBuildInfoCommand.class, "ok");

        Assertions.assertThatThrownBy(() -> initializer.initialize())
                .isInstanceOf(ControllerException.class)
                .hasMessageContaining("Could not detect the grblHAL version");
        Assertions.assertThat(initializer.isInitialized()).isFalse();
        Assertions.assertThat(initializer.isInitializing()).isFalse();
        verify(controller).closeCommPort();
    }

    @Test
    public void initialize_shouldNotInitializeInCheckMode() throws Exception {
        respondToEverything();
        respondToStatusWith(ControllerState.CHECK);

        boolean result = initializer.initialize();

        Assertions.assertThat(result).isFalse();
        Assertions.assertThat(initializer.isInitialized()).isFalse();
        verify(controller, times(1)).issueSoftReset();
        verify(controller, never()).sendCommandImmediately(any(GetBuildInfoCommand.class));
    }

    @Test
    public void reset_shouldClearTheInitializedStateAndTheReportedInformation() throws Exception {
        respondToEverything();
        initializer.initialize();

        initializer.reset();

        Assertions.assertThat(initializer.isInitialized()).isFalse();
        Assertions.assertThat(initializer.isInitializing()).isFalse();
        Assertions.assertThat(initializer.getVersionString()).isEqualTo("grblHAL");
        Assertions.assertThat(initializer.getExtendedOptions().getOptions()).isEmpty();
        Assertions.assertThat(initializer.getBuildOptions().isEnabled(GrblBuildOption.VARIABLE_SPINDLE_ENABLED)).isFalse();
    }

    @Test
    public void reset_shouldAllowTheControllerToBeInitializedAgain() throws Exception {
        respondToEverything();
        initializer.initialize();

        initializer.reset();

        Assertions.assertThat(initializer.initialize()).isTrue();
        verify(controller, times(2)).sendCommandImmediately(any(GetBuildInfoCommand.class));
    }
}
