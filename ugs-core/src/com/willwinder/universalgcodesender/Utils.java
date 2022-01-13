/*
    Copyright 2012-2018 Will Winder

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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Version;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * A collection of utilities that don't relate to anything in particular.
 *
 * @author wwinder
 */
public class Utils {

    public static NumberFormat formatter = new DecimalFormat("#.###", Localization.dfs);

    public static String formattedMillis(long millis) {
        String format = String.format("%%0%dd", 2);  
        long elapsedTime = millis / 1000;  
        String hours = String.format(format, elapsedTime / 3600);
        elapsedTime %= 3600;
        
        String minutes = String.format(format, elapsedTime / 60);
        elapsedTime %= 60;
        
        String seconds = String.format(format, elapsedTime);


        return hours + ":" + minutes + ":" + seconds;
    }

    /**
     * Converts the controller state to a localized string to be shown to the user
     *
     * @param state the state to translate
     * @return a translated string of the state
     */
    public static String getControllerStateText(ControllerState state) {
        String text = state.name();
        if (state == ControllerState.ALARM) {
            text = Localization.getString("mainWindow.status.alarm");
        } else if (state == ControllerState.HOLD) {
            text = Localization.getString("mainWindow.status.hold");
        } else if (state == ControllerState.DOOR) {
            text = Localization.getString("mainWindow.status.door");
        } else if (state == ControllerState.RUN) {
            text = Localization.getString("mainWindow.status.run");
        } else if (state == ControllerState.JOG) {
            text = Localization.getString("mainWindow.status.jog");
        } else if (state == ControllerState.CHECK) {
            text = Localization.getString("mainWindow.status.check");
        } else if (state == ControllerState.IDLE) {
            text = Localization.getString("mainWindow.status.idle");
        }
        return text;
    }

    /**
     * Returns a background color suited for a specific state
     *
     * @param state the state
     * @return a background color
     */
    public static Color getControllerStateBackgroundColor(ControllerState state) {
        if (state == ControllerState.ALARM) {
            return ThemeColors.RED;
        } else if (state == ControllerState.HOLD) {
            return ThemeColors.ORANGE;
        } else if (state == ControllerState.DOOR) {
            return ThemeColors.ORANGE;
        } else if (state == ControllerState.RUN) {
            return ThemeColors.GREEN;
        } else if (state == ControllerState.JOG) {
            return ThemeColors.GREEN;
        } else if (state == ControllerState.HOME) {
            return ThemeColors.GREEN;
        } else if (state == ControllerState.CHECK) {
            return ThemeColors.LIGHT_BLUE;
        }
        return ThemeColors.GREY;
    }


    public static void checkNightlyBuild(Settings settings) {
        if (settings.isShowNightlyWarning() && Version.isNightlyBuild()) {
            EventQueue.invokeLater(() -> {
                String title = Localization.getString("sender.notification.nightlyBuild.title");
                String message = Localization.getString("sender.notification.nightlyBuild.message") + "\n\n";
                String doNotShowAgainText = Localization.getString("sender.notification.nightlyBuild.doNotShowAgain");

                JCheckBox checkbox = new JCheckBox(doNotShowAgainText);
                Object[] params = {message, checkbox};
                JOptionPane.showMessageDialog(new JFrame(), params,
                        title, JOptionPane.INFORMATION_MESSAGE);

                boolean showNightlyWarning = !checkbox.isSelected();
                settings.setShowNightlyWarning(showNightlyWarning);
            });
        }
    }

}
