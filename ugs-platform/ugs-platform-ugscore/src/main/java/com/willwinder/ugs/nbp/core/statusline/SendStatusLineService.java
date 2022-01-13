/*
    Copyright 2018 Will Winder

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
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.toolbars.SendStatusLine;

import java.awt.Component;

import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

/**
 * A status line panel that displays the file send status
 *
 * @author wwinder
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 3)
public class SendStatusLineService implements StatusLineElementProvider {

    @Override
    public Component getStatusLineElement() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        JPanel panel = new JPanel();
        panel.add(new SendStatusLine(backend));
        return panel;
    }
}
