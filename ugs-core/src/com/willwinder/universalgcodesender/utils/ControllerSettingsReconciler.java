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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfigGroups;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * Merges the firmware configuration bundled with this version of UGS with the configuration the
 * user already has in their settings directory.
 * <p>
 * The bundled configuration owns the structure - which processors exist, in which order they are
 * applied and if they are optional. The user owns their preferences, which are transferred to the
 * bundled configuration:
 * <ul>
 *     <li>if an optional processor is enabled or not</li>
 *     <li>the values of any processor arguments that still exist in the bundled configuration</li>
 *     <li>the custom processors, which are entirely user defined</li>
 * </ul>
 * Processors that were added to the bundled configuration are introduced using their bundled
 * defaults and processors that were removed from it are dropped. Arguments are matched by their
 * name, so renaming an argument is the way to force a new default value on existing installations.
 *
 * @author Joacim Breiler
 */
public class ControllerSettingsReconciler {
    private static final Logger LOGGER = Logger.getLogger(ControllerSettingsReconciler.class.getName());

    private ControllerSettingsReconciler() {
    }

    /**
     * Applies the user preferences found in the given user settings onto the bundled settings.
     *
     * @param bundledSettings the settings bundled with this version of UGS, these will be modified
     * @param userSettings    the settings currently stored in the users settings directory
     * @return the bundled settings with the user preferences applied
     */
    public static ControllerSettings reconcile(ControllerSettings bundledSettings, ControllerSettings userSettings) {
        ProcessorConfigGroups bundledProcessors = bundledSettings.getProcessorConfigs();
        ProcessorConfigGroups userProcessors = userSettings.getProcessorConfigs();
        if (bundledProcessors == null || userProcessors == null) {
            return bundledSettings;
        }

        if (userProcessors.Custom != null) {
            bundledProcessors.Custom = new ArrayList<>(userProcessors.Custom);
        }

        Map<String, Deque<ProcessorConfig>> availableUserProcessors = indexByName(userProcessors);
        List<String> addedProcessors = new ArrayList<>();
        applyUserPreferences(bundledProcessors.Front, availableUserProcessors, addedProcessors);
        applyUserPreferences(bundledProcessors.End, availableUserProcessors, addedProcessors);

        logChanges(bundledSettings, userSettings, addedProcessors, remainingProcessorNames(availableUserProcessors));
        return bundledSettings;
    }

    private static void applyUserPreferences(List<ProcessorConfig> bundledProcessors, Map<String, Deque<ProcessorConfig>> availableUserProcessors, List<String> addedProcessors) {
        if (bundledProcessors == null) {
            return;
        }

        for (ProcessorConfig bundledProcessor : bundledProcessors) {
            ProcessorConfig userProcessor = takeProcessor(availableUserProcessors, bundledProcessor.name);
            if (userProcessor == null) {
                addedProcessors.add(bundledProcessor.name);
                continue;
            }

            if (Boolean.TRUE.equals(bundledProcessor.optional) && userProcessor.enabled != null) {
                bundledProcessor.enabled = userProcessor.enabled;
            }
            bundledProcessor.args = mergeArguments(bundledProcessor.args, userProcessor.args);
        }
    }

    private static JsonObject mergeArguments(JsonObject bundledArgs, JsonObject userArgs) {
        if (bundledArgs == null || userArgs == null) {
            return bundledArgs;
        }

        JsonObject mergedArgs = new JsonObject();
        for (Map.Entry<String, JsonElement> bundledArg : bundledArgs.entrySet()) {
            JsonElement userValue = userArgs.get(bundledArg.getKey());
            boolean hasUserValue = userValue != null && !userValue.isJsonNull();
            mergedArgs.add(bundledArg.getKey(), (hasUserValue ? userValue : bundledArg.getValue()).deepCopy());
        }
        return mergedArgs;
    }

    /**
     * Indexes the processors by name, keeping the ones with the same name in the order they were
     * defined. A processor is consumed when it has been matched so that a configuration with
     * multiple processors of the same name is reconciled one by one.
     */
    private static Map<String, Deque<ProcessorConfig>> indexByName(ProcessorConfigGroups processors) {
        Map<String, Deque<ProcessorConfig>> index = new LinkedHashMap<>();
        Stream.of(processors.Front, processors.End)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .forEach(processor -> index.computeIfAbsent(processor.name, name -> new ArrayDeque<>()).add(processor));
        return index;
    }

    private static ProcessorConfig takeProcessor(Map<String, Deque<ProcessorConfig>> availableUserProcessors, String name) {
        Deque<ProcessorConfig> processors = availableUserProcessors.get(name);
        if (processors == null) {
            return null;
        }
        return processors.poll();
    }

    private static List<String> remainingProcessorNames(Map<String, Deque<ProcessorConfig>> availableUserProcessors) {
        return availableUserProcessors.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
    }

    private static void logChanges(ControllerSettings bundledSettings, ControllerSettings userSettings, List<String> addedProcessors, List<String> removedProcessors) {
        LOGGER.info(() -> String.format("Updated the firmware configuration \"%s\" from version %s to %s, keeping the current settings. Added processors: %s. Removed processors: %s",
                bundledSettings.getName(),
                userSettings.getVersion(),
                bundledSettings.getVersion(),
                describe(addedProcessors),
                describe(removedProcessors)));
    }

    private static String describe(List<String> processorNames) {
        return processorNames.isEmpty() ? "none" : String.join(", ", processorNames);
    }
}
