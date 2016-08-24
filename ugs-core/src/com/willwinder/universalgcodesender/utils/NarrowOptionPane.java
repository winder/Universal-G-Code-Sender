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

import java.awt.Component;
import java.awt.HeadlessException;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author wwinder
 */
public class NarrowOptionPane extends JOptionPane
{
    static String pattern = 
            "<html><body><p style='width: 200px;'>%s</p></body></html>";

    public static int showNarrowConfirmDialog(int textWidthInPixels,
            String message, String title, int optionType, int messageType)
            throws HeadlessException {
        return JOptionPane.showConfirmDialog(
                new JPanel(),
                String.format(pattern, message),
                title,
                optionType,
                messageType);
    }
}
