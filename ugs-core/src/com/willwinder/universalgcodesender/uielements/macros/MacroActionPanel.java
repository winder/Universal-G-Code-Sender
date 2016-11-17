/*
    Copywrite 2016 Will Winder

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

import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.BorderLayout;
import java.awt.Dimension;
import net.miginfocom.swing.MigLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import org.apache.commons.lang3.StringUtils;

public class MacroActionPanel extends JPanel implements UGSEventListener {

    private static final int INSET = 10;
    private static final int PADDING = 10;

    private final BackendAPI backend;
    private final List<JButton> customGcodeButtons = new ArrayList<>();
    private final ArrayList<Macro> macros = new ArrayList<>();
    JPanel macroPanel = new JPanel();

    // Indicates that the macro list needs to be refreshed.
    private boolean macrosDirty = true;

    public MacroActionPanel(BackendAPI backend) {
        if (backend == null) {
            throw new RuntimeException("BackendAPI must be provided.");
        }
        setMinimumSize(new Dimension(50,0));
        this.backend = backend;
        backend.addUGSEventListener(this);

        // Insert a scrollpane in case the buttons wont fit.
        JScrollPane scrollPane = new JScrollPane(macroPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void doLayout() {
        Settings s = backend.getSettings();

        // Lookup macros.
        if (macrosDirty) {
            Integer lastMacroIndex = s.getLastMacroIndex()+1;
            macros.clear();
            for (int i = 0 ; i < lastMacroIndex; i++) {
                Macro m = s.getMacro(i);
                if (StringUtils.isNotEmpty(m.getGcode())) {
                    macros.add(s.getMacro(i));
                }
            }
        }

        // Cache the largest width amongst the buttons.
        int maxWidth = 0;
        int maxHeight = 0;

        // Create buttons.
        for (int i = 0; i < macros.size() ; i++) {
            final int index = i;
            Macro macro = macros.get(i);
            JButton button;
            if (customGcodeButtons.size() <= i) {
                button = new JButton(i+"");
                button.setEnabled(false);
                customGcodeButtons.add(button);
                // Add action listener
                button.addActionListener((ActionEvent evt) -> {
                    customGcodeButtonActionPerformed(index);
                });
            } else {
                button = customGcodeButtons.get(i);
            }


            if (!StringUtils.isEmpty(macro.getName())) {
                button.setText(macro.getName());
            } else if (!StringUtils.isEmpty(macro.getDescription())) {
                button.setText(macro.getDescription());
            }

            if (!StringUtils.isEmpty(macro.getDescription())) {
                button.setToolTipText(macro.getDescription());
            }

            if (button.getPreferredSize().width > maxWidth) maxWidth = button.getPreferredSize().width;
            if (button.getPreferredSize().height > maxHeight) maxHeight = button.getPreferredSize().height;
        }

        // If button count was reduced, clear out any extras.
        if (customGcodeButtons.size() > macros.size()) {
            this.macroPanel.removeAll();
            this.macroPanel.repaint();
            for (int i = customGcodeButtons.size(); i > macros.size(); i--) {
                JButton b = customGcodeButtons.remove(i-1);
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
            if (customGcodeButtons.size() % columns != 0) rows++;
        }

        // Layout for buttons.
        StringBuilder columnConstraint = new StringBuilder();
        for (int i = 0; i < columns; i++) {
            if (i > 0) {
                columnConstraint.append("unrelated");
            }
            columnConstraint.append("[fill, sg 1]");
        }
        MigLayout layout = new MigLayout("fill, wrap "+columns + ", inset " + INSET, columnConstraint.toString());
        macroPanel.setLayout(layout);
        
        // Put buttons in grid.
        int x = 0; int y = 0;
        for (JButton button : customGcodeButtons) {
            macroPanel.add(button, "cell " + x +  " " + y);
            y++;
            if (y == rows) {
                x++;
                y = 0;
            }             
        }

        super.doLayout();
    }

    private void customGcodeButtonActionPerformed(int macroIndex) {
        Macro m = backend.getSettings().getMacro(macroIndex);
        MacroHelper.executeCustomGcode(m.getGcode(), backend);
    }

    private void updateCustomGcodeControls(boolean enabled) {
        for (JButton button : customGcodeButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        if (evt.isSettingChangeEvent()) {
            macrosDirty = true;
            doLayout();
        }
        else {
            updateCustomGcodeControls(backend.isIdle());
        }
    }
}
