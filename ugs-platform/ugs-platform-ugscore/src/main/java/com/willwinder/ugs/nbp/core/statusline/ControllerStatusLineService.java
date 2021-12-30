/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.core.statusline;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.uielements.components.RoundedBorder;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;

/**
 * A service that provides a status line field with the current controller status.
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 2)
public class ControllerStatusLineService implements StatusLineElementProvider, UGSEventListener {
    public static final int BORDER_RADIUS = 4;
    private JLabel label;
    private BackendAPI backend;

    @Override
    public Component getStatusLineElement() {
        label = new JLabel();
        label.setOpaque(true);
        label.setBorder(new RoundedBorder(BORDER_RADIUS));
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        updateLabel();
        return new SeparatorPanel(label);
    }

    @Override
    public void UGSEvent(UGSEvent ugsEvent) {
        if (ugsEvent instanceof ControllerStateEvent) {
            updateLabel();
        }
    }

    private void updateLabel() {
        label.setText(Utils.getControllerStateText(backend.getControllerState()));
        label.setBackground(Utils.getControllerStateBackgroundColor(backend.getControllerState()));
    }
}
