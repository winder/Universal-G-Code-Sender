/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.tracking;

public enum Event {
    APPLICATION_STARTED("Application", "Started"),
    APPLICATION_CLOSED("Application", "Closed"),
    FILE_STREAM_BEGIN("Application", "File stream begin"),
    FILE_STREAM_COMPLETE("Application", "File stream complete"),
    FILE_STREAM_PAUSE("Application", "File stream paused"),
    FILE_STREAM_RESUME("Application", "File stream resumed"),
    FILE_STREAM_CANCEL("Application", "File stream canceled"),
    SETUP_WIZARD_STARTED("Setup wizard", "Started"),
    SETUP_WIZARD_FINISHED("Setup wizard", "Finished"),
    SETUP_WIZARD_CANCELED("Setup wizard", "Canceled"),
    COMMON_ACTIONS_HELP("Common actions", "Help"),
    COMMON_ACTIONS_SOFT_RESET("Common actions", "Soft reset"),
    COMMON_ACTIONS_PARSER_STATE("Common actions", "Parser state"),
    COMMON_ACTIONS_RETURN_TO_ZERO("Common actions", "Return to zero"),
    COMMON_ACTIONS_CHECK_MODE("Common actions", "Check mode"),
    COMMON_ACTIONS_RESET_COORDINATES_TO_ZERO("Common actions", "Reset coordinates to zero"),
    COMMON_ACTIONS_PERFORM_HOMING("Common actions", "Perform homing"),
    COMMON_ACTIONS_KILL_ALARM("Common actions", "Kill alarm"),
    APPLICATION_CONNECT("Application", "Connect"),
    APPLICATION_DISCONNECT("Application", "Disonnect");

    private final String category;
    private final String action;

    Event(String category, String action) {
        this.category = category;
        this.action = action;
    }

    public String getCategory() {
        return category;
    }

    public String getAction() {
        return action;
    }
}
