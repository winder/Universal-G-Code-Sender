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

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.uielements.IChanged;
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
        return new JogStatusLine(CentralLookup.getDefault().lookup(JogService.class));
    }

    private class JogStatusLine extends JLabel implements IChanged {
        private static final String format = "Step size: %s%s ";
        private JogService jogService;
        public JogStatusLine(JogService js) {
            jogService = js;
            jogService.addChangeListener(this);
            setText();
        }

        private void setText() {
            setText(String.format(format,
                    jogService.getStepSize(),
                    jogService.getUnits().abbreviation));
        }

        @Override
        public void changed() {
            setText();
        }
    }
}
