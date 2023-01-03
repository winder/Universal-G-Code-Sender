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

import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.JLabel;
import java.awt.Component;
import java.util.concurrent.Executors;

/**
 * A status line panel that displays the file send status
 *
 * @author wwinder
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 0)
public class PerformanceLineService implements StatusLineElementProvider, Runnable {
    private JLabel performanceLabel = new JLabel("        ");
    private Thread thread;

    @Override
    public Component getStatusLineElement() {
        if (thread == null) {
            thread = Executors.defaultThreadFactory().newThread(this);
            thread.setName(PerformanceLineService.class.getSimpleName());
            thread.start();
        }

        return new SeparatorPanel(performanceLabel);
    }

    @Override
    public void run() {
        while (true) {
            try {
                performanceLabel.setText("Memory:" + formatSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " / " + formatSize(Runtime.getRuntime().maxMemory()) + " Threads: " + Thread.activeCount());
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private String formatSize(long memory) {
        if (memory > 1000000000) {
            return Math.round(memory / 10000000.0) / 100.0 + " Gb";
        } else if (memory > 1000000) {
            return Math.round(memory / 1000000.0) + " Mb";
        } else if (memory > 1000) {
            return Math.round(memory / 1000.0) + " Kb";
        }
        return memory + " bytes";
    }
}
