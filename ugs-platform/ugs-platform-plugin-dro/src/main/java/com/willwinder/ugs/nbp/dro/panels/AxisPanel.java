/*
    Copyright 2020-2024 Will Winder

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

import com.willwinder.ugs.nbp.core.actions.ResetACoordinateToZeroAction;
import com.willwinder.ugs.nbp.core.actions.ResetBCoordinateToZeroAction;
import com.willwinder.ugs.nbp.core.actions.ResetCCoordinateToZeroAction;
import com.willwinder.ugs.nbp.core.actions.ResetXCoordinateToZeroAction;
import com.willwinder.ugs.nbp.core.actions.ResetYCoordinateToZeroAction;
import com.willwinder.ugs.nbp.core.actions.ResetZCoordinateToZeroAction;
import com.willwinder.ugs.nbp.dro.FontManager;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.MouseClickListener;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Insets;
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
    private final Set<AxisPanelListener> axisPanelListenerList = new HashSet<>();
    private transient ScheduledFuture<?> highlightLabelsFuture;

    public AxisPanel(Axis axis, FontManager fontManager) {
        super(new MigLayout("fill, inset 0", "[50]5[grow, fill]"));
        RoundedPanel axisPanel = new RoundedPanel(RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fillx, inset 4 6 4 6, gap 0", "[grow, right]"));

        work.addMouseListener(new MouseClickListener() {
            @Override
            public void onClick(MouseEvent e) {
                axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onWorkPositionClick(work, axis));
            }
        });

        axisPanel.add(work, "grow, gapleft 5, wrap");
        axisPanel.add(machine, "span 2");

        fontManager.addWorkCoordinateLabel(work);
        fontManager.addMachineCoordinateLabel(machine);

        JButton resetButton = new JButton(createAction(axis));
        resetButton.setMargin(new Insets(0, 0, 0, 0));
        resetButton.setText("");
        add(resetButton, "grow");
        add(axisPanel);
    }

    private static Action createAction(Axis axis) {
        return switch (axis) {
            case X:
                yield new ResetXCoordinateToZeroAction();
            case Y:
                yield new ResetYCoordinateToZeroAction();
            case Z:
                yield new ResetZCoordinateToZeroAction();
            case A:
                yield new ResetACoordinateToZeroAction();
            case B:
                yield new ResetBCoordinateToZeroAction();
            case C:
                yield new ResetCCoordinateToZeroAction();
        };
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
        work.setEnabled(enabled);
        machine.setEnabled(enabled);
    }
}
