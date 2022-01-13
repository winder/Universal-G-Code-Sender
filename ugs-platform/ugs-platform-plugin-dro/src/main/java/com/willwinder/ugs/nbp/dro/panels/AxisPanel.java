/*
    Copyright 2020 Will Winder

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * A panel that shows the machine and work position for an axis together with a reset button.
 */
public class AxisPanel extends JPanel {

    private static final int COMMON_RADIUS = 7;
    public static final int HIGHLIGHT_TIME = 300;

    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000");
    private final JLabel work = new JLabel("0.000");
    private final JLabel machine = new JLabel("0.000");
    private final RoundedPanel resetPanel;
    private final Set<AxisPanelListener> axisPanelListenerList = new HashSet<>();
    private transient ScheduledFuture<?> highlightLabelsFuture;

    public AxisPanel(Axis axis, FontManager fontManager) {
        super(new BorderLayout());
        RoundedPanel axisPanel = new RoundedPanel(COMMON_RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fillx, wrap 2, inset 4 6 4 6, gap 0", "[left][grow, right]"));

        resetPanel = new RoundedPanel(COMMON_RADIUS);
        resetPanel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.setBackground(ThemeColors.DARK_BLUE_GREY);
        resetPanel.setBackgroundDisabled(ThemeColors.VERY_DARK_GREY);
        resetPanel.setHoverBackground(ThemeColors.MED_BLUE_GREY);
        resetPanel.setLayout(new MigLayout("inset 4 10 4 12, gap 0"));
        JLabel axisLabel = new JLabel(String.valueOf(axis));
        axisLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(axisLabel, "al center, dock center, id axis");
        JLabel zeroLabel = new JLabel("0");
        zeroLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(zeroLabel, "id zero, pos (axis.x + axis.w - 4) (axis.y + axis.h - zero.h)");
        resetPanel.addClickListener(() -> axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onResetClick(resetPanel, axis)));

        work.setHorizontalAlignment(SwingConstants.RIGHT);
        work.setForeground(ThemeColors.LIGHT_BLUE);

        work.addMouseListener(new MouseClickListener() {
            @Override
            public void onClick(MouseEvent e) {
                axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onWorkPositionClick(work, axis));
            }
        });

        machine.setHorizontalAlignment(SwingConstants.RIGHT);
        machine.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.add(resetPanel, "sy 2");
        axisPanel.add(work, "grow, gapleft 5");
        axisPanel.add(machine, "span 2");

        fontManager.addAxisResetLabel(axisLabel);
        fontManager.addAxisResetZeroLabel(zeroLabel);
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

    private void setLabelValue(JLabel label, double value) {
        String newValue = decimalFormatter.format(value);
        if (!label.getText().equals(newValue)) {
            highlightLabels();
            label.setText(newValue);
        }
    }

    private void highlightLabels() {
        work.setForeground(ThemeColors.RED);
        machine.setForeground(ThemeColors.RED);

        // Disable any old future
        if (highlightLabelsFuture != null && !highlightLabelsFuture.isDone()) {
            highlightLabelsFuture.cancel(false);
        }

        // Start new future
        highlightLabelsFuture = ThreadHelper.invokeLater(() -> {
            work.setForeground(ThemeColors.LIGHT_BLUE);
            machine.setForeground(ThemeColors.LIGHT_BLUE);
        }, HIGHLIGHT_TIME);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        resetPanel.setEnabled(enabled);
        machine.setEnabled(enabled);
        work.setEnabled(enabled);
    }

    public interface AxisPanelListener {
        /**
         * When the reset click button is pressed
         *
         * @param component - the button component being pressed
         * @param axis      - the axis that should be reset
         */
        void onResetClick(JComponent component, Axis axis);

        /**
         * When the work position is being clicked
         *
         * @param component - the label being clicked
         * @param axis      - the axis that the label is showing
         */
        void onWorkPositionClick(JComponent component, Axis axis);
    }
}
