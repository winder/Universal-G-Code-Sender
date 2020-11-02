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
package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbm.visualizer.actions.JogToHereAction;
import com.willwinder.ugs.nbm.visualizer.actions.SetWorkingCoordinatesHereAction;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.services.JogService;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

/**
 * @author dweigl
 */
public class CoordinatesSubMenu extends JMenu {

    public CoordinatesSubMenu(final BackendAPI backend, final Position position) {
        final DecimalFormat decimalFormatter = new DecimalFormat("#.#####", Localization.dfs);

        String strX = decimalFormatter.format(position.getX());
        String strY = decimalFormatter.format(position.getY());
        setText(String.format(Localization.getString("platform.visualizer.popup.coordinatesMenu"), strX, strY));

        // Jog
        JMenuItem jogToHere = new JMenuItem(
                new JogToHereAction(new JogService(backend), position)
        );

        // Set Offset
        JMenuItem setWorkOffsetToHere = new JMenuItem(
                new SetWorkingCoordinatesHereAction(backend, position)
        );

        // Copy
        JMenuItem copyCoords = new JMenuItem();
        copyCoords.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                PartialPosition xyPosition = new PartialPosition(position.getX(), position.getY(), position.getUnits());
                clipboard.setContents(new StringSelection(xyPosition.getFormattedGCode()), null);
            }
        });

        add(jogToHere);
        add(setWorkOffsetToHere);
        add(copyCoords);

        // set texts
        jogToHere.setText(Localization.getString("platform.visualizer.popup.submenu.jogToHere"));
        jogToHere.setToolTipText(Localization.getString("platform.visualizer.popup.submenu.jogToHereHint"));
        setWorkOffsetToHere.setText(Localization.getString("platform.visualizer.popup.submenu.setWorkOffsetToHere"));
        setWorkOffsetToHere.setToolTipText(Localization.getString("platform.visualizer.popup.submenu.setWorkOffsetToHereHint"));
        copyCoords.setText(Localization.getString("platform.visualizer.popup.submenu.copyCoordinates"));
    }


}
