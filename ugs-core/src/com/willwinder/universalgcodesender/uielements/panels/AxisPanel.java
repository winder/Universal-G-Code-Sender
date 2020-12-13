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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.MachineStatusFontManager;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * A panel that shows the machine and work position for a axis together with a reset button.
 */
public class AxisPanel extends JPanel {

    private static final int COMMON_RADIUS = 7;

    private final DecimalFormat decimalFormatter = new DecimalFormat("0.000");
    private final JLabel work = new JLabel("0.000");
    private final JLabel machine = new JLabel("0.000");
    private final RoundedPanel resetPanel;
    private final Set<AxisPanelListener> axisPanelListenerList = new HashSet<>();

    public AxisPanel(Axis axis, MachineStatusFontManager machineStatusFontManager) {
        super(new BorderLayout());
        RoundedPanel axisPanel = new RoundedPanel(COMMON_RADIUS);
        axisPanel.setBackground(ThemeColors.VERY_DARK_GREY);
        axisPanel.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.setLayout(new MigLayout("fillx, wrap 2, inset 7, gap 0", "[left][grow, right]"));

        resetPanel = new RoundedPanel(COMMON_RADIUS);
        resetPanel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.setBackground(ThemeColors.DARK_BLUE_GREY);
        resetPanel.setBackgroundDisabled(ThemeColors.VERY_DARK_GREY);
        resetPanel.setHoverBackground(ThemeColors.MED_BLUE_GREY);
        resetPanel.setLayout(new MigLayout("inset 5 15 5 15"));
        JLabel axisLabel = new JLabel(String.valueOf(axis));
        axisLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(axisLabel, "al center, dock center, id axis");
        JLabel zeroLabel = new JLabel("0");
        zeroLabel.setForeground(ThemeColors.LIGHT_BLUE);
        resetPanel.add(zeroLabel, "pos (axis.x + axis.w - 4) (axis.y + axis.h - 25)");
        resetPanel.addClickListener(() -> axisPanelListenerList.forEach(axisPanelListener -> axisPanelListener.onResetClick(axis)));

        work.setHorizontalAlignment(SwingConstants.RIGHT);
        work.setForeground(ThemeColors.LIGHT_BLUE);
        work.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if(work.isEnabled()) {
                    work.setBackground(ThemeColors.LIGHT_GREEN);
                    work.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (work.isEnabled()) {
                    work.setForeground(ThemeColors.LIGHT_BLUE);
                    work.repaint();
                }
            }
        });
        machine.setHorizontalAlignment(SwingConstants.RIGHT);
        machine.setForeground(ThemeColors.LIGHT_BLUE);
        axisPanel.add(resetPanel, "sy 2");
        axisPanel.add(work, "grow, gapleft 5");
        axisPanel.add(machine, "span 2");

        machineStatusFontManager.addAxisResetLabel(axisLabel);
        machineStatusFontManager.addAxisResetZeroLabel(zeroLabel);
        machineStatusFontManager.addWorkCoordinateLabel(work);
        machineStatusFontManager.addMachineCoordinateLabel(machine);

        add(axisPanel, BorderLayout.CENTER);
    }

    void addListener(AxisPanelListener axisPanelListener) {
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
            label.setForeground(ThemeColors.RED);
            label.setText(newValue);
        } else {
            label.setForeground(ThemeColors.LIGHT_BLUE);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        resetPanel.setEnabled(enabled);
        machine.setEnabled(enabled);
        work.setEnabled(enabled);
    }

    public interface AxisPanelListener {
        void onResetClick(Axis axis);
    }
}
