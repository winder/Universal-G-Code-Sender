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
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.FeedOverrideProcessor;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.SpindleOnDweller;
import com.willwinder.universalgcodesender.gcode.processors.PatternRemover;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import java.util.ArrayList;
import java.util.List;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;

/**
 *
 * @author wwinder
 */
public class CommandProcessorLoader {
    /**
     * Add any ICommandProcessors specified in a JSON string. Processors are
     * initialized using the application settings if they are enabled.
     * 
     * JSON Format:
     * [
     *     {
     *         "name":"ArcExpander",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     },{
     *         "name": "CommandLenghtProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     },{
     *         "name": "CommentProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     },{
     *         "name": "DecimalProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     },{
     *         "name": "FeedOverrideProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     },{
     *         "name": "M30Processor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     },{
     *         name: "WhitespaceProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
         "args": {}
     },{
         name: "SpindleOnDweller",
         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {}
     *     }
     *  ]
     */
    static private List<ProcessorConfig> getConfigFrom(String jsonConfig) {
        List<ProcessorConfig> list = new ArrayList<>();
        JsonArray json = new JsonParser().parse(jsonConfig).getAsJsonArray();
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
     * 
     * JSON Format:
     * [   {
     *         "name":"ArcExpander",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {
     *             "segmentLengthMM": <double>
     *         }
     *     },{
     *         "name": "CommandLenghtProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {
     *             "commandLength": <double>
     *         }
     *     },{
     *         "name": "CommentProcessor",
     *         "enabled": <enabled>
     *         "optional": <optional>,
     *     },{
     *         "name": "DecimalProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {
     *             "decimals": <double>
     *         }
     *     },{
     *         "name": "FeedOverrideProcessor",
     *         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {
     *             "speed": <double>
     *         }
     *     },{
     *         "name": "M30Processor",
     *         "enabled": <enabled>
     *         "optional": <optional>,
     *     },{
     *         name: "WhitespaceProcessor",
     *         "enabled": <enabled>
     *         "optional": <optional>,
     },{
         name: "SpindleOnDweller",
         "enabled": <enabled>,
     *         "optional": <optional>,
     *         "args": {
     *             "duraion": <double>
     *         }
     *     }
     *  ]
     */
    static public List<CommandProcessor> initializeWithProcessors(String jsonConfig) {
        return initializeWithProcessors(getConfigFrom(jsonConfig));
    }

    static public List<CommandProcessor> initializeWithProcessors(List<ProcessorConfig> config) {
        List<CommandProcessor> list = new ArrayList<>();
        for (ProcessorConfig pc : config) {
            CommandProcessor p = null;

            // Check if the processor is enabled.
            if (pc.optional && !pc.enabled) {
                continue;
            }

            switch (pc.name) {
                case "ArcExpander":
                    double length = pc.args.get("segmentLengthMM").getAsDouble();
                    p = new ArcExpander(true, length);
                    break;
                case "CommandLengthProcessor":
                    int commandLength = pc.args.get("commandLength").getAsInt();
                    p = new CommandLengthProcessor(commandLength);
                    break;
                case "CommentProcessor":
                    p = new CommentProcessor();
                    break;
                case "DecimalProcessor":
                    int decimals = pc.args.get("decimals").getAsInt();
                    p = new DecimalProcessor(decimals);
                    break;
                case "FeedOverrideProcessor":
                    double override = pc.args.get("speedOverridePercent").getAsDouble();
                    p = new FeedOverrideProcessor(override);
                    break;
                case "M30Processor":
                    p = new M30Processor();
                    break;
                case "PatternRemover":
                    String pattern = pc.args.get("pattern").getAsString();
                    p = new PatternRemover(pattern);
                    break;
                case "WhitespaceProcessor":
                    p = new WhitespaceProcessor();
                    break;
                case "SpindleOnDweller":
                    double duration = pc.args.get("duration").getAsDouble();
                    p = new SpindleOnDweller(duration);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown processor: " + pc.name);
            }

            list.add(p);
        }

        return list;
    }

    /**
     * Helper to instantiate a processor by name and call the getHelp method.
     * @param pc
     * @return 
     */
    static public String getHelpForConfig(ProcessorConfig pc) {
        CommandProcessor p;
        try {
            switch (pc.name) {
                case "ArcExpander":
                    double length = pc.args.get("segmentLengthMM").getAsDouble();
                    p = new ArcExpander(true, length);
                    break;
                case "CommandLengthProcessor":
                    int commandLength = pc.args.get("commandLength").getAsInt();
                    p = new CommandLengthProcessor(commandLength);
                    break;
                case "CommentProcessor":
                    p = new CommentProcessor();
                    break;
                case "DecimalProcessor":
                    int decimals = pc.args.get("decimals").getAsInt();
                    p = new DecimalProcessor(decimals);
                    break;
                case "FeedOverrideProcessor":
                    double override = pc.args.get("speedOverridePercent").getAsDouble();
                    p = new FeedOverrideProcessor(override);
                    break;
                case "M30Processor":
                    p = new M30Processor();
                    break;
                case "PatternRemover":
                    String pattern = pc.args.get("pattern").getAsString();
                    p = new PatternRemover(pattern);
                    break;
                case "WhitespaceProcessor":
                    p = new WhitespaceProcessor();
                    break;
                case "SpindleOnDweller":
                    int duration = pc.args.get("duration").getAsInt();
                    p = new SpindleOnDweller(duration);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown processor: " + pc.name);
            }
            return p.getHelp();
        } catch (Exception e) {
            return Localization.getString("settings.processors.loadError")
                    + ": " + Localization.getString(pc.name);
        }
    }
}
