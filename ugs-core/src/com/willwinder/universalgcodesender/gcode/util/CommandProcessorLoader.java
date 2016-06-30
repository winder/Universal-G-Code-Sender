/*
    Copywrite 2016 Will Winder

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
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.ArcExpander;
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommandSplitter;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.FeedOverrideProcessor;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.PatternRemover;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.utils.Settings;
import java.util.Map.Entry;

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
     *         "enabled": <enabled>
     *     },{
     *         "name": "CommandLenghtProcessor",
     *         "enabled": <enabled>
     *     },{
     *         "name": "CommandSplitter",
     *         "enabled": <enabled>
     *     },{
     *         "name": "CommentProcessor",
     *         "enabled": <enabled>
     *     },{
     *         "name": "DecimalProcessor",
     *         "enabled": <enabled>
     *     },{
     *         "name": "FeedOverrideProcessor",
     *         "enabled": <enabled>
     *     },{
     *         "name": "M30Processor",
     *         "enabled": <enabled>
     *     },{
     *         name: "WhitespaceProcessor",
     *         "enabled": <enabled>
     *     }
     *  ]
     */
    static GcodeParser initializeWithProcessors(GcodeParser gcp, String jsonConfig, Settings settings) {
        JsonArray json = new JsonParser().parse(jsonConfig).getAsJsonArray();
        for (JsonElement entry : json) {
            ICommandProcessor p = null;

            JsonObject object = entry.getAsJsonObject();
            // Check if the processor is enabled.
            if (!object.get("enabled").getAsBoolean()) {
                continue;
            }

            String name = object.get("name").getAsString();
            switch (name) {
                case "ArcExpander": {
                    double length = settings.getSmallArcSegmentLength();
                    p = new ArcExpander(true, length);
                    break;
                }
                case "CommandLengthProcessor":
                    int commandLength = settings.getMaxCommandLength();
                    p = new CommandLengthProcessor(commandLength);
                    break;
                case "CommandSplitter":
                    p = new CommandSplitter();
                    break;
                case "CommentProcessor":
                    p = new CommentProcessor();
                    break;
                case "DecimalProcessor": {
                    int decimalPlaces = settings.getTruncateDecimalLength();
                    p = new DecimalProcessor(decimalPlaces);
                    break;
                }
                case "FeedOverrideProcessor":
                    double override = settings.getOverrideSpeedValue();
                    p = new FeedOverrideProcessor(override);
                    break;
                case "M30Processor":
                    p = new M30Processor();
                    break;
                /*
                case "PatternRemover":
                    String pattern = entry.getValue().getAsJsonObject().get("pattern").getAsString();
                    p = new PatternRemover(pattern);
                    break;
                */
                case "WhitespaceProcessor":
                    p = new WhitespaceProcessor();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown processor: " + name);
            }
            if (p != null)
                gcp.addCommandProcessor(p);
        }

        return gcp;
    }

    /**
     * Add any ICommandProcessors specified in a JSON string. Processors are
     * configured by properties in the JSON file.
     * 
     * JSON Format:
     * [   {
     *         "name":"ArcExpander",
     *         "enabled": <enabled>,
     *         "args": {
     *             "length": <double>
     *         }
     *     },{
     *         "name": "CommandLenghtProcessor",
     *         "enabled": <enabled>,
     *         "args": {
     *             "commandLength": <double>
     *         }
     *     },{
     *         "name": "CommandSplitter",
     *         "enabled": <enabled>
     *     },{
     *         "name": "CommentProcessor",
     *         "enabled": <enabled>
     *     },{
     *         "name": "DecimalProcessor",
     *         "enabled": <enabled>,
     *         "args": {
     *             "decimals": <double>
     *         }
     *     },{
     *         "name": "FeedOverrideProcessor",
     *         "enabled": <enabled>,
     *         "args": {
     *             "speed": <double>
     *         }
     *     },{
     *         "name": "M30Processor",
     *         "enabled": <enabled>
     *     },{
     *         name: "WhitespaceProcessor",
     *         "enabled": <enabled>
     *     }
     *  ]
     */
    static GcodeParser initializeWithProcessors(GcodeParser gcp, String jsonConfig) {
        JsonArray json = new JsonParser().parse(jsonConfig).getAsJsonArray();
        for (JsonElement entry : json) {
            ICommandProcessor p = null;
            JsonObject object = entry.getAsJsonObject();
            JsonObject args = null;
            if (object.has("args") && !object.get("args").isJsonNull()) {
                args = object.get("args").getAsJsonObject();
            }
            String name = object.get("name").getAsString();
            switch (name) {
                case "ArcExpander":
                    double length = args.get("length").getAsDouble();
                    p = new ArcExpander(true, length);
                    break;
                case "CommandLengthProcessor":
                    int commandLength = args.get("commandLength").getAsInt();
                    p = new CommandLengthProcessor(commandLength);
                    break;
                case "CommandSplitter":
                    p = new CommandSplitter();
                    break;
                case "CommentProcessor":
                    p = new CommentProcessor();
                    break;
                case "DecimalProcessor":
                    int decimals = args.get("decimals").getAsInt();
                    p = new DecimalProcessor(decimals);
                    break;
                case "FeedOverrideProcessor":
                    double override = args.get("speed").getAsDouble();
                    p = new FeedOverrideProcessor(override);
                    break;
                case "M30Processor":
                    p = new M30Processor();
                    break;
                case "PatternRemover":
                    String pattern = args.get("pattern").getAsString();
                    p = new PatternRemover(pattern);
                    break;
                case "WhitespaceProcessor":
                    p = new WhitespaceProcessor();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown processor: " + name);
            }
            if (p != null)
                gcp.addCommandProcessor(p);
        }

        return gcp;
    }
}
