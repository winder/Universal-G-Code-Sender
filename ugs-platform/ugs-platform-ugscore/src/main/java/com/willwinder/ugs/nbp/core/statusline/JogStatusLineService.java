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
package com.willwinder.ugs.nbp.core.statusline;

import com.willwinder.ugs.nbp.core.control.JogService;
import java.awt.Component;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.JLabel;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service = StatusLineElementProvider.class, position=0)
public class JogStatusLineService implements StatusLineElementProvider {
    @Override
    public Component getStatusLineElement() {
        return new JogStatusLine(Lookup.getDefault().lookup(JogService.class));
    }

    private class JogStatusLine extends JLabel implements PreferenceChangeListener {
        private static final String format = "Step size: %s%s ";
        private final JogService jogService;
        public JogStatusLine(JogService js) {
            jogService = js;
            NbPreferences.forModule(JogService.class).addPreferenceChangeListener(this);
            setText();
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            setText();
        }

        private void setText() {
            setText(String.format(format,
                    jogService.getStepSize(),
                    jogService.getUnits().abbreviation));
        }
    }
}
