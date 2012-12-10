/*
 * An object representing a single GcodeCommand. The only tricky part about this
 * class is the commandNum, which is used in the GUI for indexing a table of
 * these objects. 
 * 
 * TODO: Get rid of the commandNum member.
 */

/*
    Copywrite 2012 Will Winder

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

/**
 *
 * @author wwinder
 */
public class GcodeCommand {
    private String command;
    private String response;
    private String responseType;
    private Boolean sent = false;
    private Boolean done = false;
    private Boolean isOk = false;
    private Boolean isError = false;
    private Integer commandNum = -1;
    
    public GcodeCommand(String command) {
        this.command = command;
    }
    
    public GcodeCommand(String command, int num) {
        this.command = command;
        this.commandNum = num;
    }
    
    /** Setters. */
    public void setCommand(String command) {
        this.command = command;
    }
    
    public void setCommandNumber(int i) {
        this.commandNum = i;
    }
    
    public void setResponse(String response) {
        this.response = response;
        this.parseResponse();
        this.done = this.isDone();
    }
    
    public void setSent(Boolean sent) {
        this.sent = sent;
    }
    
    /** Getters. */
    @Override
    public String toString() {
        return getCommandString()  + "("+commandNum+")";
    }
    
    public String getCommandString() {
        return this.command;
    }
    
    public int getCommandNumber() {
        return this.commandNum;
    }
    
    public String getResponse() {
        return this.response;
    }
    
    public Boolean isSent() {
        return this.sent;
    }
    
    public Boolean isOk() {
        return this.isOk;
    }
    
    public Boolean isError() {
        return this.isError;
    }

    public Boolean parseResponse() {
        // No response? Set it to false.
        if (response.length() < 0) {
            this.isOk = false;
            this.isError = false;
        }
        
        // Command complete, can be 'ok' or 'error'.
        if (response.toLowerCase().equals("ok")) {
            this.isOk = true;
            this.isError = false;
        } else if (response.toLowerCase().startsWith("error")) {
            this.isOk = false;
            this.isError = true;
        }
        
        return this.isOk;
    }
    
    public String responseString() {
        String returnString = "";
        String number = "";
        
        if (this.commandNum != -1) {
            number = this.commandNum.toString();
        }
        if (this.isOk) {
            returnString = "ok" + number;
        }
        else if (this.isError) {
            returnString = "error"+number+"["+response.substring("error: ".length()) + "]";
        }
        
        return returnString;
    }
    
    public Boolean isDone() {
        return (this.response != null);
    }
    
    public static Boolean isOkErrorResponse(String response) {
        if (response.toLowerCase().equals("ok")) {
            return true;
        } else if (response.toLowerCase().startsWith("error")) {
            return true;
        }
        return false;
    }
}
