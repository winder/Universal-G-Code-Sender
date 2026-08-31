package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

public class GrblHalFirmwareSettingsInterceptorTest {
    private GrblHalFirmwareSettings firmwareSettings;
    private GrblHalFirmwareSettingsInterceptor target;

    @Before
    public void setUp() {
        IController controller = mock(IController.class);
        firmwareSettings = new GrblHalFirmwareSettings(controller);
        target = new GrblHalFirmwareSettingsInterceptor(controller, firmwareSettings);
    }

    @Test
    public void commandComplete_shouldUseTheDescriptionReportedByTheController() {
        firmwareSettings.updateSettingDetails(List.of(new GrblHalSettingDetail("$14", "2",
                "Invert control inputs", "mask", GrblHalDataType.BITFIELD,
                "N/A,Feed hold,Cycle start,N/A,N/A,N/A,EStop", "", "", false, false)));
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$14=6");
        command.appendResponse("ok");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$14");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getValue()).isEqualTo("6");
        Assertions.assertThat(result.get().getShortDescription()).isEqualTo("Invert control inputs");
        Assertions.assertThat(result.get().getUnits()).isEqualTo("mask");
    }

    @Test
    public void commandComplete_shouldUseTheGroupNameReportedByTheController() {
        firmwareSettings.updateSettingGroups(List.of(new GrblHalSettingGroup("2", "0", "Control signals")));
        firmwareSettings.updateSettingDetails(List.of(new GrblHalSettingDetail("$14", "2",
                "Invert control inputs", "mask", GrblHalDataType.BITFIELD, "", "", "", false, false)));
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$14=6");
        command.appendResponse("ok");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$14");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getGroupName()).isEqualTo("Control signals");
    }

    @Test
    public void commandComplete_shouldLeaveTheGroupNameEmptyWhenTheGroupIsUnknown() {
        firmwareSettings.updateSettingDetails(List.of(new GrblHalSettingDetail("$14", "2",
                "Invert control inputs", "mask", GrblHalDataType.BITFIELD, "", "", "", false, false)));
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$14=6");
        command.appendResponse("ok");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$14");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getGroupName()).isEmpty();
    }

    @Test
    public void commandComplete_shouldStoreTheValueOfSettingsTheControllerHasNotEnumerated() {
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$0=5.0");
        command.appendResponse("ok");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$0");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getValue()).isEqualTo("5.0");
        Assertions.assertThat(result.get().getShortDescription()).isEmpty();
    }

    @Test
    public void commandComplete_shouldUpdateASingleSettingAfterItHasBeenWritten() {
        firmwareSettings.updateSettingDetails(List.of(new GrblHalSettingDetail("$14", "2",
                "Invert control inputs", "mask", GrblHalDataType.BITFIELD, "", "", "", false, false)));
        GcodeCommand command = new GcodeCommand("$14=70");
        command.appendResponse("ok");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$14");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getValue()).isEqualTo("70");
        Assertions.assertThat(result.get().getShortDescription()).isEqualTo("Invert control inputs");
    }

    @Test
    public void commandComplete_shouldIgnoreSettingsFromCommandsThatFailed() {
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$14=6");
        command.appendResponse("error:2");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$14");
        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void commandComplete_shouldParseSettingsWithNonNumericValues() {
        GcodeCommand command = new GcodeCommand("$$");
        command.appendResponse("$302=192.168.1.10");
        command.appendResponse("ok");

        target.commandComplete(command);

        Optional<FirmwareSetting> result = firmwareSettings.getSetting("$302");
        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getValue()).isEqualTo("192.168.1.10");
    }
}
