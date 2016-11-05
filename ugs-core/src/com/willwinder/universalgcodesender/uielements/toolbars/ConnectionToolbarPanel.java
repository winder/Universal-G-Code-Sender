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
package com.willwinder.universalgcodesender.uielements.toolbars;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractConnectionPanel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public final class ConnectionToolbarPanel extends AbstractConnectionPanel {

    public ConnectionToolbarPanel(BackendAPI backend) {
        super(backend);
    }

    @Override
    protected void layoutComponents() {
        // Layout components
        JPanel p = new JPanel();
        p.setLayout(new MigLayout("insets 0 0 0 0"));
        p.add(firmwareLabel, "gapleft 5");
        p.add(firmwareCombo, "gapright 20");
        p.add(portLabel);
        p.add(refreshButton);
        p.add(portCombo, "gapright 20");
        p.add(baudLabel);
        p.add(baudCombo, "gapright 20");
        p.add(connectDisconnectButton);

        add(p);
    }
}