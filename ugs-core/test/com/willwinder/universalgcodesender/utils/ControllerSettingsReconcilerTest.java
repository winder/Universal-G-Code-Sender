/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.utils;

import com.google.gson.Gson;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

/**
 * @author Joacim Breiler
 */
public class ControllerSettingsReconcilerTest {

    @Test
    public void reconcile_shouldKeepUserEnabledFlagForOptionalProcessors() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [{"name": "ArcExpander", "enabled": false, "optional": true}],
                "Custom": [],
                "End": [{"name": "WhitespaceProcessor", "enabled": true, "optional": true}]
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [{"name": "ArcExpander", "enabled": true, "optional": true}],
                "Custom": [],
                "End": [{"name": "WhitespaceProcessor", "enabled": false, "optional": true}]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getProcessorConfigs().Front.get(0).enabled).isTrue();
        Assertions.assertThat(result.getProcessorConfigs().End.get(0).enabled).isFalse();
    }

    @Test
    public void reconcile_shouldIgnoreUserEnabledFlagForRequiredProcessors() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [{"name": "CommentProcessor", "enabled": true, "optional": false}],
                "Custom": [],
                "End": []
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [{"name": "CommentProcessor", "enabled": false, "optional": true}],
                "Custom": [],
                "End": []
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getProcessorConfigs().Front.get(0).enabled).isTrue();
    }

    @Test
    public void reconcile_shouldKeepUserArgumentValues() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "DecimalProcessor", "enabled": true, "optional": true, "args": {"decimals": 4}}]
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "DecimalProcessor", "enabled": true, "optional": true, "args": {"decimals": 2}}]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getProcessorConfigs().End.get(0).args.get("decimals").getAsInt()).isEqualTo(2);
    }

    @Test
    public void reconcile_shouldAddNewArgumentsWithBundledDefaults() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "SpindleOnDweller", "enabled": true, "optional": true, "args": {"duration": 2.5, "onlyOnFirstCommand": true}}]
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "SpindleOnDweller", "enabled": true, "optional": true, "args": {"duration": 5, "obsoleteArgument": 10}}]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        ProcessorConfig dweller = result.getProcessorConfigs().End.get(0);
        Assertions.assertThat(dweller.args.keySet()).containsExactly("duration", "onlyOnFirstCommand");
        Assertions.assertThat(dweller.args.get("duration").getAsDouble()).isEqualTo(5);
        Assertions.assertThat(dweller.args.get("onlyOnFirstCommand").getAsBoolean()).isTrue();
    }

    @Test
    public void reconcile_shouldAddNewProcessorsUsingBundledDefaults() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [],
                "Custom": [],
                "End": [
                    {"name": "BacklashCompensator", "enabled": false, "optional": true, "args": {"backlashXMM": 0}},
                    {"name": "WhitespaceProcessor", "enabled": true, "optional": true}
                ]
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "WhitespaceProcessor", "enabled": false, "optional": true}]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        List<ProcessorConfig> endProcessors = result.getProcessorConfigs().End;
        Assertions.assertThat(endProcessors).extracting(processor -> processor.name)
                .containsExactly("BacklashCompensator", "WhitespaceProcessor");
        Assertions.assertThat(endProcessors.get(0).enabled).isFalse();
        Assertions.assertThat(endProcessors.get(1).enabled).isFalse();
    }

    @Test
    public void reconcile_shouldRemoveProcessorsThatAreNoLongerBundled() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "WhitespaceProcessor", "enabled": true, "optional": true}]
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [],
                "End": [
                    {"name": "WhitespaceProcessor", "enabled": true, "optional": true},
                    {"name": "RemovedProcessor", "enabled": true, "optional": true}
                ]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getProcessorConfigs().End).extracting(processor -> processor.name)
                .containsExactly("WhitespaceProcessor");
    }

    @Test
    public void reconcile_shouldKeepUserSettingsForProcessorsThatMovedToAnotherGroup() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [{"name": "ArcExpander", "enabled": false, "optional": true, "args": {"segmentLengthMM": 1.3}}],
                "Custom": [],
                "End": []
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [],
                "End": [{"name": "ArcExpander", "enabled": true, "optional": true, "args": {"segmentLengthMM": 0.5}}]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        ProcessorConfig arcExpander = result.getProcessorConfigs().Front.get(0);
        Assertions.assertThat(arcExpander.enabled).isTrue();
        Assertions.assertThat(arcExpander.args.get("segmentLengthMM").getAsDouble()).isEqualTo(0.5);
    }

    @Test
    public void reconcile_shouldKeepCustomProcessors() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [],
                "Custom": [],
                "End": []
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [{"name": "PatternRemover", "enabled": true, "optional": true, "args": {"pattern": "T\\\\d+"}}],
                "End": []
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getProcessorConfigs().Custom).extracting(processor -> processor.name)
                .containsExactly("PatternRemover");
        Assertions.assertThat(result.getProcessorConfigs().Custom.get(0).args.get("pattern").getAsString()).isEqualTo("T\\d+");
    }

    @Test
    public void reconcile_shouldReconcileEachProcessorWithTheSameNameSeparately() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [],
                "Custom": [],
                "End": [
                    {"name": "PatternRemover", "enabled": true, "optional": true, "args": {"pattern": "a"}},
                    {"name": "PatternRemover", "enabled": true, "optional": true, "args": {"pattern": "b"}}
                ]
                """);
        ControllerSettings userSettings = settings(1, """
                "Front": [],
                "Custom": [],
                "End": [
                    {"name": "PatternRemover", "enabled": false, "optional": true, "args": {"pattern": "x"}},
                    {"name": "PatternRemover", "enabled": true, "optional": true, "args": {"pattern": "y"}}
                ]
                """);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getProcessorConfigs().End).extracting(processor -> processor.args.get("pattern").getAsString())
                .containsExactly("x", "y");
        Assertions.assertThat(result.getProcessorConfigs().End.get(0).enabled).isFalse();
    }

    @Test
    public void reconcile_shouldKeepBundledSettingsWhenUserHasNoProcessors() {
        ControllerSettings bundledSettings = settings(2, """
                "Front": [{"name": "CommentProcessor", "enabled": true, "optional": false}],
                "Custom": [],
                "End": []
                """);
        ControllerSettings userSettings = new Gson().fromJson("{\"Name\": \"GRBL\", \"Version\": 1}", ControllerSettings.class);

        ControllerSettings result = ControllerSettingsReconciler.reconcile(bundledSettings, userSettings);

        Assertions.assertThat(result.getVersion()).isEqualTo(2);
        Assertions.assertThat(result.getProcessorConfigs().Front).extracting(processor -> processor.name)
                .containsExactly("CommentProcessor");
    }

    private ControllerSettings settings(int version, String processors) {
        String json = String.format("""
                {
                    "Name": "GRBL",
                    "Version": %d,
                    "Controller": {"name": "GRBL", "args": null},
                    "GcodeProcessors": {%s}
                }
                """, version, processors);
        return new Gson().fromJson(json, ControllerSettings.class);
    }
}
