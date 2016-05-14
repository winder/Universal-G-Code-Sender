/*
 * TinyG Control layer, coordinates all aspects of control.
 */

/*
    Copywrite 2013-2015 Will Winder

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
package com.willwinder.universalgcodesender;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.willwinder.universalgcodesender.gcode.TinyGGcodeCommandCreator;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.types.TinyGGcodeCommand;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class TinyGController extends AbstractController {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";

    private boolean isReady = false;
    private Units units;

    private String state = "";
    private Position machineLocation = new Position();
    private Position workLocation = new Position();
    
    protected TinyGController(TinyGCommunicator comm) {
        super(comm);
        
        this.commandCreator = new TinyGGcodeCommandCreator();
        //this.positionPollTimer = createPositionPollTimer();
    }
    
    public TinyGController() {
        this(new TinyGCommunicator()); //f4grx: connection created at opencomm() time
    }

    @Override
    public long getJobLengthEstimate(File gcodeFile) {
        return 0;
    }

    @Override
    protected void closeCommBeforeEvent() {
        //throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void closeCommAfterEvent() {
        //throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }
    
    @Override
    protected void openCommAfterEvent() throws Exception {
        byte b = 0x18;
        this.comm.sendByteImmediately(b);
    }

    @Override
    protected void cancelSendBeforeEvent() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void cancelSendAfterEvent() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void pauseStreamingEvent() throws IOException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void resumeStreamingEvent() throws IOException {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }


    @Override
    protected void rawResponseHandler(String response) {
        JsonObject jo;
        
        try {
            jo = TinyGUtils.jsonToObject(response);
        } catch (Exception e) {
            // Some TinyG responses aren't JSON, those will end up here.
            //this.messageForConsole(response + "\n");
            return;
        }
        
        if (TinyGUtils.isRestartingResponse(jo)) {
            this.messageForConsole("[restarting] " + response + "\n");
            this.isReady = false;
        }
        else if (TinyGUtils.isReadyResponse(jo)) {  
            //this.messageForConsole("Got version: " + TinyGUtils.getVersion(jo) + "\n");
            this.messageForConsole("[ready] " + response + "\n");
            this.isReady = true;

        }
        else if (TinyGUtils.isStatusResponse(jo)) {
            TinyGUtils.StatusResult result = TinyGUtils.updateStatus(jo);
            state = result.state;
            machineLocation.set(result.machine);
            workLocation.set(result.work);
            
            dispatchStatusString(state, this.machineLocation, workLocation);
        }
        else if (TinyGGcodeCommand.isOkErrorResponse(response)) {
            try {
                this.commandComplete(response);
            } catch (Exception e) {
                this.errorMessageForConsole(Localization.getString("controller.error.response")
                        + " <" + response + ">: " + e.getMessage());
            }

            this.messageForConsole(response + "\n");
        }
        else {
            // Display any unhandled messages
            this.messageForConsole("[unhandled message] " + response + "\n");
        }
    }

    @Override
    public void performHomingCycle() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }
    
    @Override
    public void resetCoordinatesToZero() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }
    
    @Override
    public void returnToHome() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void killAlarmLock() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void toggleCheckMode() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void viewParserState() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void softReset() throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void isReadyToStreamCommandsEvent() throws Exception {
        //throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void isReadyToSendCommandsEvent() throws Exception {
        //throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void statusUpdatesEnabledValueChanged(boolean enabled) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    protected void statusUpdatesRateValueChanged(int rate) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    static class TinyGUtils {
        private static JsonParser parser = new JsonParser();
        
        private static JsonObject jsonToObject(String response) {            
            return parser.parse(response).getAsJsonObject();
        }
        
        private static  boolean isTinyGVersion(JsonObject response) {
            if (response.has("r")) {
                JsonObject jo = response.getAsJsonObject("r");
                if (jo.has("fv")) {
                    return true;
                }
            }
            return false;
        }
        
        private static String getVersion(JsonObject response) {
            if (response.has("r")) {
                JsonObject jo = response.getAsJsonObject("r");
                if (jo.has("fv")) {
                    return jo.get("fv").getAsString();
                }
            }
            return "";
        }
        
        private static boolean isRestartingResponse(JsonObject response) {
            if (response.has("r")) {
                JsonObject jo = response.getAsJsonObject("r");
                if (jo.has("msg")) {
                    String msg = jo.get("msg").getAsString();
                    return msg.equals("Loading configs from EEPROM");
                }
            }
            return false;
        }
        
        private static boolean isReadyResponse(JsonObject response) {
            if (response.has("r")) {
                JsonObject jo = response.getAsJsonObject("r");
                if (jo.has("msg")) {
                    String msg = jo.get("msg").getAsString();
                    return msg.equals("SYSTEM READY");
                }
            }
            return false;
        }

        private static boolean isStatusResponse(JsonObject response) {
            return response.has("sr");
        }
        
        private static class StatusResult {
            private Point3d machine = new Point3d();
            private Point3d work = new Point3d();
            private String state;
        }
        private static StatusResult updateStatus(JsonObject response) {
            StatusResult result = new StatusResult();
            
            if (response.has("sr")) {
                JsonObject jo = response.getAsJsonObject("sr");
                
                if (jo.has("posx")) {
                    result.machine.x = jo.get("posx").getAsDouble();
                }
                if (jo.has("posy")) {
                    result.machine.y = jo.get("posy").getAsDouble();
                }
                if (jo.has("posz")) {
                    result.machine.z = jo.get("posz").getAsDouble();
                }
                if (jo.has("stat")) {
                    result.state = getStateAsString(jo.get("stat").getAsInt());
                }
                
                result.work.set(result.machine);
            }
            return result;
        }
        
        private static String getStateAsString(int state) {
            switch (state) {
                case 0:
                    return "initializing";
                case 1:
                    return "ready";
                case 2:
                    return "shutdown";
                case 3:
                    return "stop";
                case 4:
                    return "end";
                case 5:
                    return "run";
                case 6:
                    return "hold";
                case 9:
                    return "homing";
                default:
                    return "unknown("+state+")";
            }
        }
    }
}
