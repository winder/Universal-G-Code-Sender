/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.actions;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that will jog to a coordinate
 */
public class JogToHereAction extends AbstractAction {
    private final BackendAPI backend;
    private String x = "0";
    private String y = "0";

    public JogToHereAction(BackendAPI backend) {
        this.backend = backend;
    }

    public void setJogLocation(String x, String y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            backend.sendGcodeCommand("G0 X" + x + " Y" + y);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}