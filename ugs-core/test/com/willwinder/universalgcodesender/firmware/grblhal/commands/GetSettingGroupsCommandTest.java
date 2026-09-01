package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalSettingGroup;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class GetSettingGroupsCommandTest {
    private GetSettingGroupsCommand command;

    @Before
    public void setUp() {
        command = new GetSettingGroupsCommand();
        command.appendResponse("[SETTINGGROUP:0|0|Root]");
        command.appendResponse("[SETTINGGROUP:2|0|Control signals]");
        command.appendResponse("[SETTINGGROUP:13|0|Parking/Safety door]");
        command.appendResponse("[SETTINGGROUP:42|0|Axis]");
        command.appendResponse("[SETTINGGROUP:43|42|X-axis]");
        command.appendResponse("ok");
    }

    @Test
    public void getSettingGroups_shouldParseOneGroupPerLine() {
        List<GrblHalSettingGroup> result = command.getSettingGroups();

        Assertions.assertThat(result)
                .extracting(GrblHalSettingGroup::name)
                .containsExactly("Root", "Control signals", "Parking/Safety door", "Axis", "X-axis");
    }

    @Test
    public void getSettingGroups_shouldParseTheGroupHierarchy() {
        GrblHalSettingGroup result = command.getSettingGroups().get(4);

        Assertions.assertThat(result.id()).isEqualTo("43");
        Assertions.assertThat(result.parentId()).isEqualTo("42");
        Assertions.assertThat(result.name()).isEqualTo("X-axis");
    }

    @Test
    public void getSettingGroups_shouldIgnoreUnrelatedResponseLines() {
        GetSettingGroupsCommand mixedCommand = new GetSettingGroupsCommand();
        mixedCommand.appendResponse("[SETTING:0|35|Step pulse time|microseconds|6|#0.0|1.0||0|0]");
        mixedCommand.appendResponse("[SETTINGGROUP:1|0|General]");
        mixedCommand.appendResponse("ok");

        List<GrblHalSettingGroup> result = mixedCommand.getSettingGroups();

        Assertions.assertThat(result).extracting(GrblHalSettingGroup::name).containsExactly("General");
    }

    @Test
    public void getSettingGroups_shouldReturnEmptyListWhenTheCommandWasNotSupported() {
        GetSettingGroupsCommand unsupportedCommand = new GetSettingGroupsCommand();
        unsupportedCommand.appendResponse("error:2");

        List<GrblHalSettingGroup> result = unsupportedCommand.getSettingGroups();

        Assertions.assertThat(result).isEmpty();
    }
}
