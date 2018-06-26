/*
    Copyright 2016-2018 Will Winder

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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 *
 * @author wwinder
 */
public class GUIHelpers {
    private static final Logger LOGGER = Logger.getLogger(GUIHelpers.class.getName());

    // Set with reflection in unit tests.
    private static boolean unitTestMode = false;

    public static void invokeLater(Runnable r) {
        if (unitTestMode) {
            r.run();
        } else {
            java.awt.EventQueue.invokeLater(r);
        }
    }

    /**
     * Displays an error message to the user which will not block the current thread.
     * @param errorMessage message to display in the dialog.
     */
    public static void displayErrorDialog(final String errorMessage) {
        displayErrorDialog(errorMessage, false);
    }

    /**
     * Displays an error message to the user.
     * @param errorMessage message to display in the dialog.
     * @param modal toggle whether the message should block or fire and forget.
     */
    public static void displayErrorDialog(final String errorMessage, boolean modal) {
        if (StringUtils.isEmpty(errorMessage)) {
            LOGGER.warning("Something tried to display an error message with an empty message: " + ExceptionUtils.getStackTrace(new Throwable()));
            return;
        }

        Runnable r = () -> {
              //JOptionPane.showMessageDialog(new JFrame(), errorMessage, 
              //        Localization.getString("error"), JOptionPane.ERROR_MESSAGE);
              NarrowOptionPane.showNarrowDialog(250, errorMessage.replaceAll("\\.\\.", "\\."),
                      Localization.getString("error"),
                      JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
        };

        if (modal) {
            r.run();
        } else {
          java.awt.EventQueue.invokeLater(r);
        }
    }

    public static void displayHelpDialog(final String helpMessage) {
        java.awt.EventQueue.invokeLater(() -> {
            NarrowOptionPane.showNarrowConfirmDialog(250, helpMessage, Localization.getString("help"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
            //JOptionPane.showMessageDialog(new JFrame(), helpMessage, 
            //        Localization.getString("help"), JOptionPane.INFORMATION_MESSAGE);
        });
    }

    public static void openGcodeFile(File f, BackendAPI backend) {
        ThreadHelper.invokeLater(() -> {
            try {
              backend.setGcodeFile(f);
              Settings settings = backend.getSettings();
              settings.setLastOpenedFilename(f.getAbsolutePath());
              SettingsFactory.saveSettings(settings);
            } catch (Exception e) {
              LOGGER.log(Level.WARNING, "Couldn't set gcode-file" + e.getMessage(), e);
            }
        });
    }
}
