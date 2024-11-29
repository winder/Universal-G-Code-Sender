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
import javax.swing.SwingConstants;
import java.awt.Dimension;
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
    private final HighlightableLabel axisLabel = new HighlightableLabel();
    private final CoordinateLabel workLabel = new CoordinateLabel(0.0);
    private final CoordinateLabel machineLabel = new CoordinateLabel(0.0);
    private final Set<AxisPanelListener> axisPanelListenerList = new HashSet<>();
    private transient ScheduledFuture<?> highlightLabelsFuture;

    public AxisPanel(Axis axis, FontManager fontManager) {
        super(new MigLayout("fill, inset 0", "[grow, fill]5[50]"));
        RoundedPanel axisPanel = new RoundedPanel(RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fill, inset 6 6 6 6, gap 0", "[left][grow, right, fill]"));

        axisLabel.setText(axis.toString());
        workLabel.addMouseListener(new MouseClickListener() {
            @Override
            public void onClick(MouseEvent e) {
                axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onWorkPositionClick(workLabel, axis));
            }
        });

        fontManager.addWorkCoordinateLabel(axisLabel);
        fontManager.addWorkCoordinateLabel(workLabel);
        fontManager.addMachineCoordinateLabel(machineLabel);

        JPanel coordinatesPanel = new JPanel(new MigLayout("fill, inset 0, wrap 1, gap 0", "[grow,  align right]"));
        coordinatesPanel.setOpaque(false);
        coordinatesPanel.add(workLabel);
        coordinatesPanel.add(machineLabel, "hidemode 3");

        axisPanel.add(axisLabel, "spany 2, growy");
        axisPanel.add(coordinatesPanel, "growy");
        add(axisPanel, "growy");

        Dimension minimumSize = new Dimension(50, 18);
        JButton resetButton = new JButton(createAction(axis));
        resetButton.setMinimumSize(minimumSize);
        resetButton.setVerticalTextPosition(SwingConstants.TOP);
        resetButton.setHorizontalTextPosition(SwingConstants.CENTER);

        resetButton.setMargin(new Insets(0, 0, 0, 0));
        add(resetButton, "grow");
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
        setLabelValue(machineLabel, value);
    }

    public void setWorkPosition(double value) {
        setLabelValue(workLabel, value);
    }

    private void setLabelValue(CoordinateLabel label, double value) {
        if (label.getValue() != value) {
            highlightLabels();
            label.setValue(value);
        }
    }

    private void highlightLabels() {
        axisLabel.setHighlighted(true);
        workLabel.setHighlighted(true);
        machineLabel.setHighlighted(true);

        // Disable any old future
        if (highlightLabelsFuture != null && !highlightLabelsFuture.isDone()) {
            highlightLabelsFuture.cancel(false);
        }

        // Start new future
        highlightLabelsFuture = ThreadHelper.invokeLater(() -> {
            workLabel.setHighlighted(false);
            machineLabel.setHighlighted(false);
            axisLabel.setHighlighted(false);
        }, HIGHLIGHT_TIME);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        workLabel.setEnabled(enabled);
        machineLabel.setEnabled(enabled);
        axisLabel.setEnabled(enabled);
    }

    public void setShowMachinePosition(boolean showMachinePosition) {
        machineLabel.setVisible(showMachinePosition);
    }
}
