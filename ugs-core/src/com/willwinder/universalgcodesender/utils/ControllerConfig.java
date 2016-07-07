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
public class ControllerConfig {
    private class ControllerConfig {
        public String name;
        public JsonElement args;
    }
    
    public class ProcessorConfig {
        public String name;
        public Boolean enabled = true;
        public Boolean optional = true;
        public JsonObject args = null;
    }

    public class ProcessorGroups {
        ArrayList<ProcessorConfig> Front;
        ArrayList<ProcessorConfig> Custom;
        ArrayList<ProcessorConfig> End;
    }


    String Name;
    ControllerConfig Controller;
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

    /*
    ConfigLoader2(File configFile) throws FileNotFoundException {
        this.configFile = configFile;

        BufferedReader br = new BufferedReader(new FileReader(configFile));
        JsonObject object = new JsonParser().parse(br).getAsJsonObject();
        this.name = object.get("Name").getAsString();

        controllerConfig = object.get("Controller").getAsJsonObject();
        commandProcessorConfig = object.get("GcodeParser").getAsJsonObject();
    }
    */

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
        String controllerName = this.Controller.name;
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