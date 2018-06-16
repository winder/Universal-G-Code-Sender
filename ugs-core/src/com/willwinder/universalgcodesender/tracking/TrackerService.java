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
import com.willwinder.universalgcodesender.utils.TrackingSetting;

import java.util.logging.Logger;

/**
 * A service that can be used to register usage statistics. This service will only register
 * usage data if the setting {@link com.willwinder.universalgcodesender.utils.Settings#tracking}
 * has been set to {@link TrackingSetting#ENABLE_TRACKING}.
 * <p>
 * The service needs to be initialized using the {@link #initService(BackendAPI, Client)} before using.
 *
 * @author Joacim Breiler
 */
public class TrackerService {
    private static final Logger LOGGER = Logger.getLogger(TrackerService.class.getName());
    private static ITracker tracker;
    private static BackendAPI backendAPI;

    public static void initService(BackendAPI backendAPI, Client client) {
        if (tracker == null) {
            tracker = new MatomoTracker(backendAPI, client);
            TrackerService.backendAPI = backendAPI;
        }
    }

    /**
     * Reports an event to the tracker server. It will only report the event if the user has agreed
     * to enabling tracking.
     *
     * @param module the module that this event occured in.
     * @param action the action that triggered the event. Ex. "Started", "File stream complete"
     */
    public static void report(Class module, String action) {
        if (backendAPI != null && backendAPI.getSettings().getTrackingSetting() != TrackingSetting.ENABLE_TRACKING) {
            return;
        }

        if (tracker != null) {
            tracker.report(module, action);
        } else {
            LOGGER.warning("Tracker is not initialized, please use TrackerService.initService first");
        }
    }

    /**
     * Reports an event to the tracker server. It will only report the event if the user has agreed
     * to enabling tracking.
     *
     * @param module   the module that this event occured in.
     * @param action   the action that triggered the event. Ex. "Started", "File stream complete"
     * @param newVisit if this is a new visit, ie the user restarted the program
     */
    public static void report(Class module, String action, boolean newVisit) {
        if (backendAPI != null && backendAPI.getSettings().getTrackingSetting() != TrackingSetting.ENABLE_TRACKING) {
            return;
        }

        if (tracker != null) {
            tracker.report(module, action, newVisit);
        } else {
            LOGGER.warning("Tracker is not initialized, please use TrackerService.initService first");
        }
    }

    /**
     * Reports a event with the given event type to a tracker server. It will only report the event if the user has agreed
     * to enabling tracking.
     *
     * @param module        the module that this event occured in.
     * @param action        the action that triggered the event. Ex. "Started", "File stream complete"
     * @param resourceName  the name of an extra resource we want to register for this event. Example "Rows sent" for the number of rows when a file completes.
     * @param resourceValue a optional number value for the resource. Example a number of rows sent when a file completes.
     */
    public static void report(Class module, String action, String resourceName, int resourceValue) {
        if (backendAPI != null && backendAPI.getSettings().getTrackingSetting() != TrackingSetting.ENABLE_TRACKING) {
            return;
        }

        if (tracker != null) {
            tracker.report(module, action, false, resourceName, resourceValue);
        } else {
            LOGGER.warning("Tracker is not initialized, please use TrackerService.initService first");
        }
    }
}
