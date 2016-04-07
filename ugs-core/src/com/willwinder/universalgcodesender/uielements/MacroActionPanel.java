package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

public class MacroActionPanel extends JPanel implements UGSEventListener {

    private static final int BUTTON_WIDTH = 75;
    private static final int PADDING = 10;

    private final BackendAPI backend;
    private java.util.List<JButton> customGcodeButtons = new ArrayList<JButton>();

    public MacroActionPanel() {
        this(null, null);
    }

    public MacroActionPanel(Settings settings, BackendAPI backend) {
        setMinimumSize(new Dimension(50,0));
        this.backend = backend;
        if (this.backend != null) {
            backend.addUGSEventListener(this);
        }
//        initMacroButtons();
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
        }

        int columns = getWidth() / (BUTTON_WIDTH + PADDING);

        StringBuilder columnConstraint = new StringBuilder();
        for (int i = 0; i < columns; i++) {
            if (i > 0) {
                columnConstraint.append("unrelated");
            }
            columnConstraint.append("[fill, sg 1]");
        }

        MigLayout layout = new MigLayout("fill, wrap "+columns, columnConstraint.toString());
        setLayout(layout);
        for (JButton button : customGcodeButtons) {
            add(button, "sg 1");
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
            MacroPanel.executeCustomGcode(macro.getGcode(), backend);
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
