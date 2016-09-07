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
package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author wwinder
 */
public class VisualizerPopupMenu extends JPopupMenu {
    private final JogToHereAction jogToHereAction;
    private final JMenuItem jogToHere = new JMenuItem();

    public VisualizerPopupMenu(BackendAPI backend) {
        jogToHereAction = new JogToHereAction(backend);

        jogToHere.setText(String.format(Localization.getString("platform.visualizer.jogToHere"), 0, 0));

        jogToHere.setAction(jogToHereAction);
        add(jogToHere);
    }

    public void setJogLocation(double x, double y) {
        jogToHereAction.setJogLocation(x, y);
        jogToHere.setText(String.format(Localization.getString("platform.visualizer.popup.jogToHere"), x, y));
    }

    private static class JogToHereAction extends AbstractAction {
        private double x = 0;
        private double y = 0;
        private final BackendAPI backend;

        JogToHereAction(BackendAPI backend) {
            this.backend = backend;
        }

        public void setJogLocation(double x, double y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
