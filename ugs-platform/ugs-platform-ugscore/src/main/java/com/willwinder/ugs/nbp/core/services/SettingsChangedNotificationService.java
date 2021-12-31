/*
    Copyright 2017 Will Winder

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
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import org.apache.commons.lang3.StringUtils;
import org.openide.LifecycleManager;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A service that will listen for setting changes that requires a restart and notifies the user.
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = SettingsChangedNotificationService.class)
public class SettingsChangedNotificationService {

    private static final String RESTART_ICON = "org/netbeans/core/windows/resources/restart.png";
    private final BackendAPI backend;
    private Notification restartNotification;
    private String lastSelectedLanguage;

    public SettingsChangedNotificationService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::checkForLanguageChangeAndAskForRestart);
        lastSelectedLanguage = backend.getSettings().getLanguage();
    }

    private void checkForLanguageChangeAndAskForRestart(UGSEvent ugsEvent) {
        if (ugsEvent instanceof SettingChangedEvent && !StringUtils.equalsIgnoreCase(lastSelectedLanguage, backend.getSettings().getLanguage())) {
            lastSelectedLanguage = backend.getSettings().getLanguage();
            Localization.initialize(backend.getSettings().getLanguage());
            notifyRestartRequired();
        }
    }

    private void notifyRestartRequired() {
        if (null != restartNotification) {
            restartNotification.clear();
        }
        restartNotification = NotificationDisplayer.getDefault().notify(Localization.getString("restart"),
                ImageUtilities.loadImageIcon(RESTART_ICON, false), //NOI18N
                createRestartNotificationDetails(), createRestartNotificationDetails(),
                NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.INFO);
    }

    private JComponent createRestartNotificationDetails() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel label = new JLabel(Localization.getString("platform.window.restart.changed.settings")); //NOI18N
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(label, BorderLayout.CENTER);

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (null != restartNotification) {
                    restartNotification.clear();
                    restartNotification = null;
                }

                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            }
        });
        return panel;
    }
}
