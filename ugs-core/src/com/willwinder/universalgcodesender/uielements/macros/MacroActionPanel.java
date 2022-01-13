/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.universalgcodesender.uielements.macros;

import com.google.common.base.Strings;
import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class MacroActionPanel extends JPanel implements UGSEventListener {

    private static final int INSET = 10;
    private static final int PADDING = 10;

    private final BackendAPI backend;
    private final List<JButton> customGcodeButtons = new ArrayList<>();
    private List<Macro> macros = new ArrayList<>();
    private JPanel macroPanel = new JPanel();

    public MacroActionPanel(BackendAPI backend) {
        if (backend == null) {
            throw new RuntimeException("BackendAPI must be provided.");
        }
        setMinimumSize(new Dimension(50,0));
        this.backend = backend;
        backend.addUGSEventListener(this);

        macros = backend.getSettings().getMacros();

        // Insert a scrollpane in case the buttons wont fit.
        JScrollPane scrollPane = new JScrollPane(macroPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        doLayout();
    }

    @Override
    public void doLayout() {
        macroPanel.removeAll();
        customGcodeButtons.clear();

        // Cache the largest width amongst the buttons.
        int maxWidth = 0;
        int maxHeight = 0;
        int idx = 0;
        // Create buttons.
        for (Macro macro : macros) {
            idx++;
            JButton button = new JButton(macro.getName());
            button.setEnabled(backend.isIdle());
            customGcodeButtons.add(button);

            // Add action listener
            button.addActionListener((ActionEvent evt) -> customGcodeButtonActionPerformed(macro));

            // set full name or otherwise use the index as text
            if (Strings.isNullOrEmpty(macro.getNameAndDescription())) {
                button.setText(Integer.toString(idx));
            } else {
                button.setText(macro.getNameAndDescription());
            }

            if (!StringUtils.isEmpty(macro.getDescription())) {
                button.setToolTipText(macro.getDescription());
            }

            if (button.getPreferredSize().width > maxWidth) {
                maxWidth = button.getPreferredSize().width;
            }

            if (button.getPreferredSize().height > maxHeight) {
                maxHeight = button.getPreferredSize().height;
            }
        }

        // Calculate columns/rows which can fit in the space we have.
        int columns = (getWidth() - (2 * INSET)) / (maxWidth + PADDING);
        int rows = (getHeight() - (2 * INSET)) / (maxHeight + PADDING);

        // At least one column.
        columns = Math.max(columns, 1);

        // Update number of rows if more are needed.
        if (columns * rows < customGcodeButtons.size()) {
            rows = customGcodeButtons.size() / columns;
            if (customGcodeButtons.size() % columns != 0) {
                rows++;
            }
        }

        // Layout for buttons.
        StringBuilder columnConstraint = new StringBuilder();
        for (int i = 0; i < columns; i++) {
            if (i > 0) {
                columnConstraint.append("unrelated");
            }
            columnConstraint.append("[fill, sg 1]");
        }
        MigLayout layout = new MigLayout("fillx, wrap " + columns + ", inset " + INSET, columnConstraint.toString());
        macroPanel.setLayout(layout);

        // Put buttons in grid.
        int x = 0;
        int y = 0;
        for (JButton button : customGcodeButtons) {
            macroPanel.add(button, "cell " + x +  " " + y);
            y++;
            if (y == rows) {
                x++;
                y = 0;
            }
        }
        revalidate();
        super.doLayout();
        updateEnabledState();
    }

    private void customGcodeButtonActionPerformed(Macro macro) {
        EventQueue.invokeLater(() -> {
            try {
                MacroHelper.executeCustomGcode(macro.getGcode(), backend);
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getMessage());
            }
        });
    }

    private void updateCustomGcodeControls(boolean enabled) {
        customGcodeButtons.forEach((button -> button.setEnabled(enabled)));
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            ThreadHelper.invokeLater(() -> {
                macros = backend.getSettings().getMacros();
                doLayout();
            });
        } else if (evt instanceof ControllerStateEvent){
            updateEnabledState();
        }
    }

    private void updateEnabledState() {
        updateCustomGcodeControls(backend.isIdle());
    }
}
