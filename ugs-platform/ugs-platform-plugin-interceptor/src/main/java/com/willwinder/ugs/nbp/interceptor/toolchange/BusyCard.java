/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.nbp.interceptor.toolchange;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;

/**
 * The card shown while the machine is busy, e.g. finishing the current commands or resuming the job. It
 * shows an indeterminate progress bar.
 *
 * @author Joacim Breiler
 */
class BusyCard extends JPanel {
    BusyCard() {
        super(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        add(progressBar, BorderLayout.NORTH);
    }
}
