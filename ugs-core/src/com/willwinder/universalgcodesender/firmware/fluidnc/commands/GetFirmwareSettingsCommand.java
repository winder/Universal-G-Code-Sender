/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.types.CommandException;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.resolver.JsonScalarResolver;
import org.snakeyaml.engine.v2.resolver.ScalarResolver;
import org.snakeyaml.engine.v2.schema.JsonSchema;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetFirmwareSettingsCommand extends SystemCommand {

    static class InternalSchema extends JsonSchema {
        @Override
        public ScalarResolver getScalarResolver() {
            return new InternalResolver();
        }
    }
    static class InternalResolver extends JsonScalarResolver {
        public static final Pattern SIMPLE_FLOAT =
                Pattern.compile("^(-?(0|[1-9][0-9]*)(\\.[0-9]*)?)$");
        @Override
        protected void addImplicitResolvers() {
            addImplicitResolver(Tag.NULL, EMPTY, null);
            addImplicitResolver(Tag.BOOL, BOOL, "tf");
            addImplicitResolver(Tag.INT, INT, "-0123456789");
            addImplicitResolver(Tag.NULL, NULL, "n\u0000");
            addImplicitResolver(Tag.ENV_TAG, ENV_FORMAT, "$");
            addImplicitResolver(Tag.FLOAT, SIMPLE_FLOAT, "-0123456789.");
        }
    }
    public GetFirmwareSettingsCommand() {
        super("$Config/Dump");
    }

    public Map<String, String> getSettings() {
        if (!isOk()) {
            return new HashMap<>();
        }

        String response = Arrays
                .stream(getResponse().split("\\r?\\n"))
                .filter(line -> !line.startsWith("[MSG:"))
                .filter(line -> !line.equals("ok"))
                .collect(Collectors.joining("\n"));

        try {
            Load load = new Load(LoadSettings.builder()
                    .setAllowDuplicateKeys(false)
                    .setAllowNonScalarKeys(false)
                    .setAllowRecursiveKeys(false)
                    .setCodePointLimit(1_000_000) // ~1 MB
                    .setMaxAliasesForCollections(10)
                    .setSchema(new InternalSchema())
                    .build());
            Map<String, Object> settingsTree = (Map<String, Object>) load.loadFromString(response);
            return flatten(settingsTree);
        } catch (YamlEngineException e) {
            throw new CommandException(e);
        }
    }

    private Map<String, String> flatten(Map<String, Object> mapToFlatten) {
        return mapToFlatten.entrySet()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(this::flatten)
                .collect(LinkedHashMap::new, (map, entry) ->
                        map.put(entry.getKey().toLowerCase(), entry.getValue().toString()), LinkedHashMap::putAll);
    }

    private Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {
        if (entry == null || entry.getValue() == null) {
            return Stream.empty();
        }

        Object value = entry.getValue();
        if (value instanceof Map<?, ?> properties) {
            return properties.entrySet().stream()
                    .flatMap(e -> flatten(new AbstractMap.SimpleEntry<>(entry.getKey().toLowerCase() + "/" + e.getKey().toString().toLowerCase(), e.getValue())));
        }

        return Stream.of(entry);
    }
}
