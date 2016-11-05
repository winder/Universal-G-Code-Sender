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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractConnectionPanel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ConnectionPanel extends AbstractConnectionPanel {

    public ConnectionPanel(BackendAPI backend) {
        super(backend);
    }

    @Override
    protected void layoutComponents() {
        JPanel top = new JPanel(new MigLayout("inset 0, fill, wrap 1"));

        JPanel port = new JPanel(new MigLayout("inset 0, fillx"));
        port.add(super.portLabel);
        port.add(super.portCombo, "grow");
        top.add(port);

        JPanel baud = new JPanel(new MigLayout("inset 0, fillx"));
        baud.add(super.baudLabel);
        baud.add(super.baudCombo, "grow");
        baud.add(super.refreshButton);
        baud.add(super.connectDisconnectButton);
        top.add(baud);

        JPanel firmware = new JPanel(new MigLayout("inset 0, fillx"));
        firmware.add(super.firmwareLabel);
        firmware.add(super.firmwareCombo, "grow");
        top.add(firmware);

        add(top);
    }
}
