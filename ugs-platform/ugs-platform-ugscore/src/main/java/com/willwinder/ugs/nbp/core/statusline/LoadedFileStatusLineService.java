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
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * A service that provides a status line field with the currently loaded file name.
 * If no file is loaded the component becomes invisible.
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 1)
public class LoadedFileStatusLineService implements StatusLineElementProvider {

    private final JLabel label;
    private final JComponent panel;
    private final BackendAPI backend;

    public LoadedFileStatusLineService() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this::onEvent);

        this.label = new JLabel();
        this.panel = new SeparatorPanel(this.label);
        updateText();
    }

    @Override
    public Component getStatusLineElement() {
        return this.panel;
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            updateText();
        }
    }

    private void updateText() {
        File file = this.backend.getGcodeFile();
        if (file != null) {
            String text = file.getName();
            this.label.setText(text);
            this.panel.setVisible(true);
        } else {
            this.label.setText("");
            this.panel.setVisible(false);
        }
    }
}
