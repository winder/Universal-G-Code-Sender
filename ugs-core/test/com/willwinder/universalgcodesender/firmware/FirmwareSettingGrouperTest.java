package com.willwinder.universalgcodesender.firmware;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class FirmwareSettingGrouperTest {

    private static FirmwareSetting setting(String key, String groupName) {
        return new FirmwareSetting(key, "0", "", "", "", groupName);
    }

    @Test
    public void group_shouldKeepTheGroupOrderReportedByTheController() {
        List<FirmwareSetting> settings = List.of(
                setting("$0", "Stepper"),
                setting("$10", "General"),
                setting("$14", "Control signals"));

        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(settings,
                List.of("General", "Control signals", "Stepper"));

        Assertions.assertThat(result)
                .extracting(FirmwareSettingGroup::name)
                .containsExactly("General", "Control signals", "Stepper");
    }

    @Test
    public void group_shouldSortSettingsWithinAGroupByTheirNumber() {
        List<FirmwareSetting> settings = List.of(
                setting("$100", "Stepper"),
                setting("$2", "Stepper"),
                setting("$29", "Stepper"));

        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(settings, List.of("Stepper"));

        Assertions.assertThat(result.get(0).settings())
                .extracting(FirmwareSetting::getKey)
                .containsExactly("$2", "$29", "$100");
    }

    @Test
    public void group_shouldOmitGroupsWithoutAnySettings() {
        List<FirmwareSetting> settings = List.of(setting("$14", "Control signals"));

        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(settings,
                List.of("Root", "General", "Control signals", "Axis"));

        Assertions.assertThat(result).extracting(FirmwareSettingGroup::name).containsExactly("Control signals");
    }

    @Test
    public void group_shouldCollectSettingsWithoutAGroupInANamelessGroupLast() {
        List<FirmwareSetting> settings = List.of(
                setting("$999", ""),
                setting("$14", "Control signals"));

        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(settings, List.of("Control signals"));

        Assertions.assertThat(result).hasSize(2);
        Assertions.assertThat(result.get(0).hasName()).isTrue();
        Assertions.assertThat(result.get(1).hasName()).isFalse();
        Assertions.assertThat(result.get(1).settings()).extracting(FirmwareSetting::getKey).containsExactly("$999");
    }

    @Test
    public void group_shouldCollectSettingsWhoseGroupWasNotReported() {
        List<FirmwareSetting> settings = List.of(setting("$14", "Some plugin group"));

        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(settings, List.of("General"));

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).hasName()).isFalse();
        Assertions.assertThat(result.get(0).settings()).extracting(FirmwareSetting::getKey).containsExactly("$14");
    }

    @Test
    public void group_shouldReturnASingleNamelessGroupForControllersWithoutGroups() {
        List<FirmwareSetting> settings = List.of(setting("$10", ""), setting("$1", ""));

        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(settings, List.of());

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).hasName()).isFalse();
        Assertions.assertThat(result.get(0).settings())
                .extracting(FirmwareSetting::getKey)
                .containsExactly("$1", "$10");
    }

    @Test
    public void group_shouldReturnNoGroupsWhenThereAreNoSettings() {
        List<FirmwareSettingGroup> result = FirmwareSettingGrouper.group(List.of(), List.of("General"));

        Assertions.assertThat(result).isEmpty();
    }

    @Test
    public void groupedOrder_shouldFlattenTheGroupsIntoOneList() {
        List<FirmwareSetting> settings = List.of(
                setting("$0", "Stepper"),
                setting("$999", ""),
                setting("$10", "General"));

        List<FirmwareSetting> result = FirmwareSettingGrouper.groupedOrder(settings, List.of("General", "Stepper"));

        Assertions.assertThat(result)
                .extracting(FirmwareSetting::getKey)
                .containsExactly("$10", "$0", "$999");
    }
}
