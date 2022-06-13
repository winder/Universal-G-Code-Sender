package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.GrblUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GetFirmwareSettingsCommand extends SystemCommand {
    private Map<String, String> settings = new HashMap<>();

    public GetFirmwareSettingsCommand() {
        super("$Config/Dump");
    }

    @Override
    public void appendResponse(String response) {
        if (GrblUtils.isOkResponse(response)) {

            Yaml yaml = new Yaml();
            Map<String, Object> settingsTree = yaml.load(getResponse());
            settings = flatten(settingsTree);
        }

        super.appendResponse(response);
    }

    public Map<String, String> getSettings() {
        return settings;
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
