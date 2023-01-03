/*
    Copyright 2020-2023 Will Winder

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
package com.willwinder.ugs.nbp.dro.panels;

import com.willwinder.ugs.nbp.dro.FontManager;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.MouseClickListener;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * A panel that shows the machine and work position for an axis together with a reset button.
 */
public class AxisPanel extends JPanel {
    private static final int RADIUS = 7;
    public static final int HIGHLIGHT_TIME = 300;
    private final CoordinateLabel work = new CoordinateLabel(0.0);
    private final CoordinateLabel machine = new CoordinateLabel(0.0);
    private final RoundedPanel resetButton;
    private final Set<AxisPanelListener> axisPanelListenerList = new HashSet<>();
    private transient ScheduledFuture<?> highlightLabelsFuture;

    public AxisPanel(Axis axis, FontManager fontManager) {
        super(new BorderLayout());
        RoundedPanel axisPanel = new RoundedPanel(RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fillx, wrap 2, inset 4 6 4 6, gap 0", "[left][grow, right]"));

        resetButton = new AxisResetButton(axis, fontManager);
        resetButton.addClickListener(() -> axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onResetClick(resetButton, axis)));

        work.addMouseListener(new MouseClickListener() {
            @Override
            public void onClick(MouseEvent e) {
                axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onWorkPositionClick(work, axis));
            }
        });

        axisPanel.add(resetButton, "sy 2");
        axisPanel.add(work, "grow, gapleft 5");
        axisPanel.add(machine, "span 2");

        fontManager.addWorkCoordinateLabel(work);
        fontManager.addMachineCoordinateLabel(machine);

        add(axisPanel, BorderLayout.CENTER);
    }

    public void addListener(AxisPanelListener axisPanelListener) {
        axisPanelListenerList.add(axisPanelListener);
    }

    public void setMachinePosition(double value) {
        setLabelValue(machine, value);
    }

    public void setWorkPosition(double value) {
        setLabelValue(work, value);
    }

    private void setLabelValue(CoordinateLabel label, double value) {
        if (label.getValue() != value) {
            highlightLabels();
            label.setValue(value);
        }
    }

    private void highlightLabels() {
        work.setHighlighted(true);
        machine.setHighlighted(true);

        // Disable any old future
        if (highlightLabelsFuture != null && !highlightLabelsFuture.isDone()) {
            highlightLabelsFuture.cancel(false);
        }

        // Start new future
        highlightLabelsFuture = ThreadHelper.invokeLater(() -> {
            work.setHighlighted(false);
            machine.setHighlighted(false);
        }, HIGHLIGHT_TIME);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        work.setEnabled(enabled);
        machine.setEnabled(enabled);
    }
}
