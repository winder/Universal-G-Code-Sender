/*
 * A collection of utilities that don't relate to anything in particular.
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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.tracking.Client;
import com.willwinder.universalgcodesender.tracking.TrackerService;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.TrackingSetting;
import com.willwinder.universalgcodesender.utils.Version;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.EventQueue;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
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

    public static void checkNightlyBuild(Settings settings) {
        if (settings.isShowNightlyWarning() && Version.isNightlyBuild()) {
            EventQueue.invokeLater(() -> {
                String message =
                        "This version of Universal Gcode Sender is a nightly build.\n"
                                + "It contains all of the latest features and improvements, \n"
                                + "but may also have bugs that still need to be fixed.\n"
                                + "\n"
                                + "If you encounter any problems, please report them on github.\n\n";

                JCheckBox checkbox = new JCheckBox("Do not show this message again.");
                Object[] params = {message, checkbox};
                JOptionPane.showMessageDialog(new JFrame(), params,
                        "", JOptionPane.INFORMATION_MESSAGE);

                boolean showNightlyWarning = !checkbox.isSelected();
                settings.setShowNightlyWarning(showNightlyWarning);
            });
        }
    }

    public static void setupTrackerService(BackendAPI backend, Settings settings, Class clazz, Client client) {
        EventQueue.invokeLater(() -> {
            // Only start the tracker when the UI components are fully loaded
            if (settings.getTrackingSetting() == TrackingSetting.NOT_ANSWERED) {
                String message = "May we collect anonymous usage statistics to help improve Universal G-code Sender?\n\n" +
                        "Read more about our privacy policy and the data we collect:\n" +
                        "http://winder.github.io/ugs_website/";

                int result = JOptionPane.showConfirmDialog(new JFrame(), message,
                        "Help us improve Universal G-code Sender", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    settings.setTrackingSetting(TrackingSetting.ENABLE_TRACKING);
                } else if (result == JOptionPane.NO_OPTION) {
                    settings.setTrackingSetting(TrackingSetting.DISABLE_TRACKING);
                }
            }

            TrackerService.initService(backend, client);
            TrackerService.report(clazz, "Startup");
        });
    }
}
