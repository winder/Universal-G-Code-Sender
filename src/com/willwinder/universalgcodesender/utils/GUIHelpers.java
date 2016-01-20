/*
    Copywrite 2016 Will Winder

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
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author wwinder
 */
public class GUIHelpers {
    // Set with reflection in unit tests.
    private static boolean unitTestMode = false;

    public static void invokeLater(Runnable r) {
        if (unitTestMode) {
            r.run();
        } else {
            java.awt.EventQueue.invokeLater(r);
        }
    }

    public static void displayErrorDialog(final String errorMessage) {
        java.awt.EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(new JFrame(), errorMessage, 
                    Localization.getString("error"), JOptionPane.ERROR_MESSAGE);
        });
    }

}
