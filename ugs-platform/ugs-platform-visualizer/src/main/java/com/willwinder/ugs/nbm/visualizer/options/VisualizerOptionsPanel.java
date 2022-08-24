/*
    Copyright 2016-2022 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.options;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.ugs.nbp.lib.options.Option;
import com.willwinder.universalgcodesender.uielements.IChanged;

import java.awt.Color;

final public class VisualizerOptionsPanel extends AbstractOptionsPanel {
    VisualizerOptions vo = new VisualizerOptions();

    public VisualizerOptionsPanel(IChanged changer) {
        super(changer);
    }

    @Override
    public void cancel() {
    }

    @Override
    public void load() {
        vo = new VisualizerOptions();
        for (Option op : vo) {
            this.add(op);
        }
    }

    @Override
    public void store() {
        for (int i = 0; i < optionTable.getModel().getRowCount(); i++) {
            Option<?> op = optionTable.getOption(i);
            if (op.value instanceof Color) {
                VisualizerOptions.setColorOption(op.option, (Color) op.getValue());
            } else if (op.value instanceof Boolean) {
                VisualizerOptions.setStringOption(op.option, op.getValue().toString());
            } else {
                VisualizerOptions.setStringOption(op.option, (String) op.getValue());
            }
        }
    }

    @Override
    public boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
