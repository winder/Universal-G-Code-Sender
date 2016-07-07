/**
 * 
 */
package com.willwinder.universalgcodesender.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.GrblController;
import com.willwinder.universalgcodesender.LoopBackCommunicator;
import com.willwinder.universalgcodesender.TinyGController;
import com.willwinder.universalgcodesender.XLCDCommunicator;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class ControllerSettings {
    private class ControllerConfig {
        public String name;
        public JsonElement args;
    }
    
    static public class ProcessorConfig {
        public String name;
        public Boolean enabled = true;
        public Boolean optional = true;
        public JsonObject args = null;
        public ProcessorConfig(String name, Boolean enabled, Boolean optional, JsonObject args) {
            this.name = name;
            this.enabled = enabled;
            this.optional = optional;
            this.args = args;
        }
    }

    public class ProcessorGroups {
        ArrayList<ProcessorConfig> Front;
        ArrayList<ProcessorConfig> Custom;
        ArrayList<ProcessorConfig> End;
    }


    String Name;
    ControllerSettings Controller;
    ProcessorGroups GcodeProcessors;

    public enum CONTROLLER {
        GRBL("GRBL"),
        SMOOTHIE("SmoothieBoard"),
        TINYG("TinyG"),
        XLCD("XLCD"),
        LOOPBACK("Loopback"),
        LOOPBACK_SLOW("Loopback_Slow");

        final String name;
        CONTROLLER(String name) {
            this.name = name;
        }

        public static CONTROLLER fromString(String name) {
            for (CONTROLLER c : values()) {
                if (c.name.equalsIgnoreCase(name)) {
                    return c;
                }
            }
            return null;
        }
    }

    public String getName() {
        return Name;
    }

    /**
     * Parse the "Controller" object in the firmware config json.
     * 
     * "Controller": {
     *     "name": "GRBL",
     *     "args": null
     * }
     */
    public AbstractController getController() {
        //String controllerName = controllerConfig.get("name").getAsString();
        String controllerName = this.Controller.Name;
        CONTROLLER controller = CONTROLLER.fromString(controllerName);
        switch (controller) {
            case GRBL:
                return new GrblController();
            case SMOOTHIE:
                return new GrblController();
            case TINYG:
                return new TinyGController();
            case XLCD:
                return new GrblController(new XLCDCommunicator());
            case LOOPBACK:
                return new GrblController(new LoopBackCommunicator());
            case LOOPBACK_SLOW:
                return new GrblController(new LoopBackCommunicator(100));
            default:
                throw new AssertionError(controller.name());
        }
    }
    
    public List<ICommandProcessor> getProcessors(Settings settings) {
        //return CommandProcessorLoader.initializeWithProcessors(commandProcessorConfig.toString(), settings);
        return null;
    }
}