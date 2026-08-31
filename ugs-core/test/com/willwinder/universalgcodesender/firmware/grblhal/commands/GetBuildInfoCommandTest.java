package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOption;
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalOption;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

public class GetBuildInfoCommandTest {
    private GetBuildInfoCommand command;

    @Before
    public void setUp() {
        command = new GetBuildInfoCommand();
        command.appendResponse("[VER:1.1f.20230312:]");
        command.appendResponse("[OPT:VSHM,35,1024,3,0]");
        command.appendResponse("[NEWOPT:ENUMS,RT+,HOME,TC,SED]");
        command.appendResponse("[FIRMWARE:grblHAL]");
        command.appendResponse("[PLUGIN:SDCARD v1.07]");
        command.appendResponse("ok");
    }

    @Test
    public void getVersion_shouldReturnTheReportedVersion() {
        Optional<GrblVersion> result = command.getVersion();

        Assertions.assertThat(result).isPresent();
        Assertions.assertThat(result.get().getVersionNumber()).isEqualTo(1.1);
        Assertions.assertThat(result.get().getVersionLetter()).isEqualTo('f');
    }

    @Test
    public void getBuildOptions_shouldReturnTheReportedGrblOptions() {
        boolean result = command.getBuildOptions().isEnabled(GrblBuildOption.VARIABLE_SPINDLE_ENABLED);

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void getExtendedOptions_shouldReturnTheReportedGrblHalOptions() {
        boolean result = command.getExtendedOptions().isEnabled(GrblHalOption.TOOL_CHANGE);

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void getFirmwareName_shouldReturnTheReportedFirmware() {
        Optional<String> result = command.getFirmwareName();

        Assertions.assertThat(result).contains("grblHAL");
    }

    @Test
    public void getPlugins_shouldReturnTheLoadedPlugins() {
        List<String> result = command.getPlugins();

        Assertions.assertThat(result).containsExactly("SDCARD v1.07");
    }

    @Test
    public void isGrblHal_shouldReturnTrueWhenTheFirmwareIdentifiesItself() {
        boolean result = command.isGrblHal();

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void isGrblHal_shouldReturnFalseWhenTheFirmwareIsNotReported() {
        GetBuildInfoCommand grblCommand = new GetBuildInfoCommand();
        grblCommand.appendResponse("[VER:1.1f.20170801:]");
        grblCommand.appendResponse("[OPT:V,15,128]");
        grblCommand.appendResponse("ok");

        boolean result = grblCommand.isGrblHal();

        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void getVersion_shouldReturnEmptyWhenNoVersionWasReported() {
        GetBuildInfoCommand emptyCommand = new GetBuildInfoCommand();
        emptyCommand.appendResponse("ok");

        Optional<GrblVersion> result = emptyCommand.getVersion();

        Assertions.assertThat(result).isEmpty();
    }
}
