package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.IController;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

import java.util.List;

public class GrblHalFirmwareSettingsTest {
    private GrblHalFirmwareSettings firmwareSettings;

    @Before
    public void setUp() {
        firmwareSettings = new GrblHalFirmwareSettings(mock(IController.class));
    }

    @Test
    public void getGroupNames_shouldOrderGroupsByTheirId() {
        firmwareSettings.updateSettingGroups(List.of(
                new GrblHalSettingGroup("35", "0", "Stepper"),
                new GrblHalSettingGroup("2", "0", "Control signals"),
                new GrblHalSettingGroup("1", "0", "General"),
                new GrblHalSettingGroup("11", "0", "Homing")));

        List<String> result = firmwareSettings.getGroupNames();

        Assertions.assertThat(result).containsExactly("General", "Control signals", "Homing", "Stepper");
    }

    @Test
    public void getGroupNames_shouldReturnEmptyListWhenNoGroupsWereReported() {
        List<String> result = firmwareSettings.getGroupNames();

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void getGroupNames_shouldPlaceGroupsWithNonNumericIdsLast() {
        firmwareSettings.updateSettingGroups(List.of(
                new GrblHalSettingGroup("x", "0", "Odd"),
                new GrblHalSettingGroup("1", "0", "General")));

        List<String> result = firmwareSettings.getGroupNames();

        Assertions.assertThat(result).containsExactly("General", "Odd");
    }

    @Test
    public void updateSettingGroups_shouldReplaceThePreviouslyReportedGroups() {
        firmwareSettings.updateSettingGroups(List.of(new GrblHalSettingGroup("1", "0", "General")));
        firmwareSettings.updateSettingGroups(List.of(new GrblHalSettingGroup("2", "0", "Control signals")));

        List<String> result = firmwareSettings.getGroupNames();

        Assertions.assertThat(result).containsExactly("Control signals");
    }
}
