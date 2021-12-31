/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.model.BackendAPI;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static util that will schedule a timer which will prevent the computer
 * to go to sleep.
 *
 * @author wwinder
 */
public class KeepAwakeUtils {
    private static final Logger LOGGER = Logger.getLogger(KeepAwakeUtils.class.getName());

    private static Timer keepAliveTimer;

    private static void keepAwake() {
        LOGGER.log(Level.INFO, "Moving the mouse location slightly to keep the computer awake.");
        try {
            Point pObj = MouseInfo.getPointerInfo().getLocation();
            Robot hal = new Robot();
            hal.mouseMove(pObj.x + 1, pObj.y + 1);
            hal.mouseMove(pObj.x - 1, pObj.y - 1);
        } catch (AWTException | NullPointerException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    public static void start(BackendAPI backendAPI) {
        if (keepAliveTimer != null) {
            keepAliveTimer.cancel();
        }

        keepAliveTimer = new Timer("KeepAliveTimer", true);
        keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Move the mouse every 30 seconds to prevent sleeping.
                if (backendAPI.isPaused() || !backendAPI.isIdle()) {
                    keepAwake();
                }
            }
        }, 1000, 30000);
    }
}
