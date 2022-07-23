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

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class GetFirmwareSettingsCommand extends SystemCommand {

    public GetFirmwareSettingsCommand() {
        super("$Config/Dump");
    }

    public Map<String, String> getSettings() {
        if (!isOk()) {
            return new HashMap<>();
        }
        
        String response = StringUtils.removeEnd(getResponse(), "ok");
        Yaml yaml = new Yaml();
        Map<String, Object> settingsTree = yaml.load(response);
        return flatten(settingsTree);
    }

    private Map<String, String> flatten(Map<String, Object> mapToFlatten) {
        return mapToFlatten.entrySet()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(this::flatten)
                .collect(LinkedHashMap::new, (map, entry) ->
                        map.put(entry.getKey(), entry.getValue().toString()), LinkedHashMap::putAll);
    }

    private Stream<Map.Entry<String, Object>> flatten(Map.Entry<String, Object> entry) {
        if (entry == null || entry.getValue() == null) {
            return Stream.empty();
        }

        Object value = entry.getValue();
        if (value instanceof Map<?, ?>) {
            Map<?, ?> properties = (Map<?, ?>) value;
            return properties.entrySet().stream()
                    .flatMap(e -> flatten(new AbstractMap.SimpleEntry<>(entry.getKey() + "/" + e.getKey(), e.getValue())));
        }

        return Stream.of(entry);
    }
}
