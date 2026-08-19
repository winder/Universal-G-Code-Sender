/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.willwinder.universalgcodesender.gcode.processors.ArcExpander;
import com.willwinder.universalgcodesender.gcode.processors.BacklashCompensator;
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.EmptyLineRemoverProcessor;
import com.willwinder.universalgcodesender.gcode.processors.FeedOverrideProcessor;
import com.willwinder.universalgcodesender.gcode.processors.LineSplitter;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.PatternRemover;
import com.willwinder.universalgcodesender.gcode.processors.SpindleOnDweller;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * @author wwinder
 */
public class CommandProcessorLoader {
    private static final Logger LOGGER = Logger.getLogger(CommandProcessorLoader.class.getSimpleName());

    /**
     * Add any ICommandProcessors specified in a JSON string. Processors are
     * initialized using the application settings if they are enabled.
     * <p>
     * JSON Format:
     * [
     * {
     * "name":"ArcExpander",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * "name": "CommandLenghtProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * "name": "CommentProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * "name": "DecimalProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * "name": "FeedOverrideProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * "name": "M30Processor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * name: "WhitespaceProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * },{
     * name: "SpindleOnDweller",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {}
     * }
     * ]
     */
    static private List<ProcessorConfig> getConfigFrom(String jsonConfig) {
        List<ProcessorConfig> list = new ArrayList<>();
        JsonArray json = JsonParser.parseString(jsonConfig).getAsJsonArray();
        for (JsonElement entry : json) {
            JsonObject object = entry.getAsJsonObject();

            boolean optional = true;
            boolean enabled = true;
            JsonObject args = null;

            if (object.has("optional") && !object.get("optional").isJsonNull()) {
                optional = object.get("optional").getAsBoolean();
            }

            if (object.has("enabled") && !object.get("enabled").isJsonNull()) {
                enabled = object.get("enabled").getAsBoolean();
            }

            if (object.has("args") && !object.get("args").isJsonNull()) {
                args = object.get("args").getAsJsonObject();
            }

            String name = object.get("name").getAsString();

            list.add(new ProcessorConfig(name, enabled, optional, args));
        }

        return list;
    }

    /**
     * Add any ICommandProcessors specified in a JSON string. Processors are
     * configured by properties in the JSON file.
     * <p>
     * JSON Format:
     * [   {
     * "name":"ArcExpander",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {
     * "segmentLengthMM": <double>
     * }
     * },{
     * "name": "CommandLenghtProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {
     * "commandLength": <double>
     * }
     * },{
     * "name": "CommentProcessor",
     * "enabled": <enabled>
     * "optional": <optional>,
     * },{
     * "name": "DecimalProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {
     * "decimals": <double>
     * }
     * },{
     * "name": "FeedOverrideProcessor",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {
     * "speed": <double>
     * }
     * },{
     * "name": "M30Processor",
     * "enabled": <enabled>
     * "optional": <optional>,
     * },{
     * "name": "WhitespaceProcessor",
     * "enabled": <enabled>
     * "optional": <optional>,
     * },{
     * "name": "SpindleOnDweller",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {
     * "duraion": <double>
     * }
     * },{
     * "name":"LineSplitter",
     * "enabled": <enabled>,
     * "optional": <optional>,
     * "args": {
     * "segmentLengthMM": <double>
     * }
     * }
     * ]
     */
    static public List<CommandProcessor> initializeWithProcessors(String jsonConfig) {
        return initializeWithProcessors(getConfigFrom(jsonConfig));
    }

    static public List<CommandProcessor> initializeWithProcessors(List<ProcessorConfig> config) {
        List<CommandProcessor> list = new ArrayList<>();
        for (ProcessorConfig pc : config) {
            // Check if the processor is enabled.
            if (pc.optional && !pc.enabled) {
                continue;
            }

            getProcessor(pc).ifPresent(list::add);
        }

        return list;
    }

    /**
     * Helper to instantiate a processor by name and call the getHelp method.
     *
     * @param pc processor config
     * @return a help text for the given processor
     */
    static public String getHelpForConfig(ProcessorConfig pc) {
        return getProcessor(pc).map(CommandProcessor::getHelp)
                .orElse(Localization.getString("settings.processors.loadError")
                        + ": " + Localization.getString(pc.name));
    }

    private static double getDouble(JsonObject args, String name, double defaultValue) {
        if (args == null || !args.has(name) || args.get(name).isJsonNull()) {
            return defaultValue;
        }
        return args.get(name).getAsDouble();
    }

    private static Optional<CommandProcessor> getProcessor(ProcessorConfig pc) {
        return switch (pc.name) {
            case "ArcExpander" -> {
                double length = pc.args.get("segmentLengthMM").getAsDouble();
                yield Optional.of(new ArcExpander(true, length));
            }
            case "CommandLengthProcessor" -> {
                int commandLength = pc.args.get("commandLength").getAsInt();
                yield Optional.of(new CommandLengthProcessor(commandLength));
            }
            case "CommentProcessor" -> Optional.of(new CommentProcessor());
            case "DecimalProcessor" -> {
                int decimals = pc.args.get("decimals").getAsInt();
                yield Optional.of(new DecimalProcessor(decimals));
            }
            case "FeedOverrideProcessor" -> {
                double override = pc.args.get("speedOverridePercent").getAsDouble();
                yield Optional.of(new FeedOverrideProcessor(override));
            }
            case "M30Processor" -> Optional.of(new M30Processor());
            case "PatternRemover" -> {
                String pattern = pc.args.get("pattern").getAsString();
                yield Optional.of(new PatternRemover(pattern));
            }
            case "WhitespaceProcessor" -> Optional.of(new WhitespaceProcessor());
            case "SpindleOnDweller" -> {
                double duration = pc.args.get("duration").getAsDouble();
                yield Optional.of(new SpindleOnDweller(duration));
            }
            case "LineSplitter" -> Optional.of(new LineSplitter(pc.args.get("segmentLengthMM").getAsDouble()));
            case "BacklashCompensator" -> {
                Position backlash = new Position(
                        getDouble(pc.args, "backlashXMM", 0),
                        getDouble(pc.args, "backlashYMM", 0),
                        getDouble(pc.args, "backlashZMM", 0),
                        UnitUtils.Units.MM);
                yield Optional.of(new BacklashCompensator(backlash,
                        getDouble(pc.args, "arcSegmentLengthMM", 0.1)));
            }
            case "EmptyLineRemoverProcessor" -> Optional.of(new EmptyLineRemoverProcessor());
            default -> {
                LOGGER.severe("Unknown processor: " + pc.name);
                yield Optional.empty();
            }
        };
    }
}
