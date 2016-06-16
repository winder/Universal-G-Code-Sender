package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MacroActionPanel extends JPanel implements UGSEventListener {

    private static final int INSET = 10;
    private static final int PADDING = 10;

    private final BackendAPI backend;
    private final java.util.List<JButton> customGcodeButtons = new ArrayList<>();
    JPanel macroPanel = new JPanel();

    public MacroActionPanel(BackendAPI backend) {
        setMinimumSize(new Dimension(50,0));
        this.backend = backend;
        if (this.backend != null) {
            backend.addUGSEventListener(this);
        }

        // Insert a scrollpane in case the buttons wont fit.
        JScrollPane scrollPane = new JScrollPane(macroPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void doLayout() {
        if (backend == null) {
            //I suppose this should be in a text field.
            System.err.println("settings is null!  Cannot init buttons!");
            return;
        }
        Integer lastMacroIndex = backend.getSettings().getLastMacroIndex()+1;

        for (int i = customGcodeButtons.size(); i <= lastMacroIndex; i++) {
            JButton button = createMacroButton(i);
        }

        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < customGcodeButtons.size(); i++) {
            JButton button = customGcodeButtons.get(i);
            Macro macro = backend.getSettings().getMacro(i);
            if (macro != null) {
                if (macro.getName() != null) {
                    button.setText(macro.getName());
                }
                if (macro.getDescription() != null) {
                    button.setToolTipText(macro.getDescription());
                }
            }
            if (button.getPreferredSize().width > maxWidth) maxWidth = button.getPreferredSize().width;
            if (button.getPreferredSize().height > maxHeight) maxHeight = button.getPreferredSize().height;
        }

        int columns = (getWidth() - (2 * INSET)) / (maxWidth + PADDING);
        int rows = (getHeight() - (2 * INSET)) / (maxHeight + PADDING);

        columns = Math.max(columns, 1);

        if (columns * rows < customGcodeButtons.size()) {
            rows = customGcodeButtons.size() / columns;
            if (customGcodeButtons.size() % columns != 0) rows++;
        }

        StringBuilder columnConstraint = new StringBuilder();
        for (int i = 0; i < columns; i++) {
            if (i > 0) {
                columnConstraint.append("unrelated");
            }
            columnConstraint.append("[fill, sg 1]");
        }

        MigLayout layout = new MigLayout("fill, wrap "+columns + ", inset " + INSET, columnConstraint.toString());
        macroPanel.setLayout(layout);
        
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

    private JButton createMacroButton(int i) {
        JButton button = new JButton(i+"");

        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customGcodeButtonActionPerformed(i);
            }
        });
        customGcodeButtons.add(button);
        return button;
    }

    private void customGcodeButtonActionPerformed(int i) {
        //Poor coupling here.  We should probably pull the executeCustomGcode method out into the backend.
        if (backend == null) {
            System.err.println("MacroPanel not properly initialized.  Cannot execute custom gcode");
        } else {
            Macro macro = backend.getSettings().getMacro(i);
            MacroHelper.executeCustomGcode(macro.getGcode(), backend);
        }
    }

    private void updateCustomGcodeControls(boolean enabled) {
        for (JButton button : customGcodeButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        updateCustomGcodeControls(backend.isIdle());
    }
}
