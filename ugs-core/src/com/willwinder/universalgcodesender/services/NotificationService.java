/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.services;

/**
 * A service for displaying textual notifications to the user
 *
 * @author Joacim Breiler
 */
public interface NotificationService {

    /**
     * Show text in the status line. Can be called at any time.
     * <p/>
     * Default implementation of status line in displays the text in status line and clears it after a while.
     * Also there is no guarantee how long the text will be displayed as it can be replaced with new call to
     * this method at any time.
     *
     * @param text the text to display
     */
    void setStatusText(String text);
}
