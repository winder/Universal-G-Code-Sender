/*
    Copyright 2016-2023 Will Winder

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

import java.awt.HeadlessException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Component;

/**
 * @author wwinder
 */
public class NarrowOptionPane extends JOptionPane {
    public static String pattern =
            "<html><body><p style='width: %dpx;'>%s</p></body></html>";

    public static void showNarrowDialog(Component parentComponent, int textWidthInPixels,
                                        String message, String title, int messageType)
            throws HeadlessException {
        JOptionPane.showMessageDialog(parentComponent,
                String.format(pattern, textWidthInPixels, message.replaceAll("\n", "<br>")),
                title, messageType);
    }

    public static int showNarrowConfirmDialog(int textWidthInPixels,
                                              String message, String title, int optionType, int messageType)
            throws HeadlessException {
        return JOptionPane.showConfirmDialog(
                new JPanel(),
                String.format(pattern, textWidthInPixels, message.replaceAll("\n", "<br>")),
                title,
                optionType,
                messageType);
    }

    public static int showNarrowDialog(int textWidthInPixels, String message, Object[] params, String title, int optionType, int messageType) {
        List<Object> paramsList = new ArrayList<>();
        paramsList.add(String.format(pattern, textWidthInPixels, message.replaceAll("\n", "<br>")));
        paramsList.addAll(Arrays.asList(params));

        return JOptionPane.showConfirmDialog(
                new JPanel(),
                paramsList.toArray(),
                title,
                optionType,
                messageType);
    }
}
