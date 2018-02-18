/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.shared;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * A checkbox for selecting / deselecting if a renderable should be shown or not.
 *
 * @author wwinder
 */
public class RenderableCheckBox extends JCheckBoxMenuItem implements ItemListener {
    private final Renderable r;

    public RenderableCheckBox(Renderable r) {
        super(r.getTitle(), r.isEnabled());
        this.r = r;

        this.addItemListener(this);
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        if (ie.getStateChange() == ItemEvent.SELECTED) {
            r.setEnabled(true);
        } else if (ie.getStateChange() == ItemEvent.DESELECTED) {
            r.setEnabled(false);
        }
    }
}