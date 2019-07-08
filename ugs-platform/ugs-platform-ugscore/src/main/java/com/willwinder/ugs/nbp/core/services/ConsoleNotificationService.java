/*
    Copyright 2019 Will Winder

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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * A console notification service that will display notifications for all error messages.
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = ConsoleNotificationService.class)
public class ConsoleNotificationService implements MessageListener {

    public static final String ERROR_ICON_PATH = "/org/netbeans/core/windows/resources/error.png";

    /**
     * Number of milliseconds to show the notification
     */
    private static final int TIME_TO_LIVE = 10000;

    public ConsoleNotificationService() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addMessageListener(this);
    }

    @Override
    public void onMessage(MessageType messageType, String message) {
        if (messageType.equals(MessageType.ERROR)) {
            Notification notify = NotificationDisplayer.getDefault()
                    .notify(Localization.getString("controller.log.error.reported"),
                            ImageUtilities.loadImageIcon(ERROR_ICON_PATH, false),
                            message,
                            null,
                            NotificationDisplayer.Priority.LOW,
                            NotificationDisplayer.Category.ERROR);

            // Clears the notification automatically
            ThreadHelper.invokeLater(notify::clear, TIME_TO_LIVE);
        }
    }
}
