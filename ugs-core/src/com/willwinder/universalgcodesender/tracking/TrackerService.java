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

import com.willwinder.universalgcodesender.model.BackendAPI;

import java.util.logging.Logger;

public class TrackerService {
    private static final Logger LOGGER = Logger.getLogger(MatomoTracker.class.getName());
    private static ITracker tracker;
    private static BackendAPI backendAPI;

    public static void initService(BackendAPI backendAPI, Client client) {
        if (tracker == null) {
            tracker = new MatomoTracker(backendAPI, client);
            TrackerService.backendAPI = backendAPI;
        }
    }

    public static void report(Event event) {
        /*if (!backendAPI.getSettings().useTracking()) {
            return;
        }*/

        if (tracker != null) {
            tracker.report(event);
        } else {
            LOGGER.warning("Tracker is not initialized, please use TrackerService.initService first");
        }
    }

    public static void report(Event event, String resourceName, int resourceValue) {
        /*if (!backendAPI.getSettings().useTracking()) {
            return;
        }*/

        if (tracker != null) {
            tracker.report(event, resourceName, resourceValue);
        } else {
            LOGGER.warning("Tracker is not initialized, please use TrackerService.initService first");
        }
    }
}
