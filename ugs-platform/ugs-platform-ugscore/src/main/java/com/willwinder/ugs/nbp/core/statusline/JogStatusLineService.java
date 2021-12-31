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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import javax.swing.JLabel;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service = StatusLineElementProvider.class, position=0)
public class JogStatusLineService implements StatusLineElementProvider {
    @Override
    public Component getStatusLineElement() {
        JogStatusLine jogStatusLine = new JogStatusLine(CentralLookup.getDefault().lookup(BackendAPI.class));
        return new SeparatorPanel(jogStatusLine);
    }

    private class JogStatusLine extends JLabel implements UGSEventListener {
        private static final String FORMAT = "Step size: %s%s";
        private final BackendAPI backend;
        public JogStatusLine(BackendAPI backend) {
            this.backend = backend;
            this.backend.addUGSEventListener(this);
            setText();
        }

        private void setText() {
            Settings s = backend.getSettings();
            Units u = s.getPreferredUnits();
            setText(String.format(FORMAT,
                    s.getManualModeStepSize(),
                    u.abbreviation));
        }

        @Override
        public void UGSEvent(UGSEvent evt) {
            if (evt instanceof SettingChangedEvent) {
                setText();
            }
        }
    }
}
