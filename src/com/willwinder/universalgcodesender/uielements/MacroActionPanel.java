package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.MainWindow;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MacroActionPanel extends JPanel implements UGSEventListener {

    private BackendAPI backend;
    private Settings settings;
    private java.util.List<JButton> customGcodeButtons = new ArrayList<JButton>();

    public MacroActionPanel() {

    }

    public MacroActionPanel(Settings settings, BackendAPI backend) {
        this.settings = settings;
        this.backend = backend;
        backend.addUGSEventListener(this);
    }

    @Override
    public void updateUI() {
        super.updateUI();
    }

    @Override
    public void doLayout() {
        initMacroButtons();
        super.doLayout();
    }

    private void initMacroButtons() {
        if (settings == null) {
            //I suppose this should be in a text field.
            System.err.println("settings is null!  Cannot init buttons!");
            return;
        }
        Integer lastMacroIndex = settings.getLastMacroIndex()+1;

        for (int i = customGcodeButtons.size(); i <= lastMacroIndex; i++) {
            JButton button = createMacroButton(i);
        }

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            JButton button = customGcodeButtons.get(i);
            Macro macro = settings.getMacro(i);
            if (macro != null) {
                if (macro.getName() != null) {
                    button.setText(macro.getName());
                }
                if (macro.getDescription() != null) {
                    button.setToolTipText(macro.getDescription());
                }
            }
        }

        org.jdesktop.layout.GroupLayout macroPanelLayout = new org.jdesktop.layout.GroupLayout(this);

        org.jdesktop.layout.GroupLayout.ParallelGroup parallelGroup = macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING);

        org.jdesktop.layout.GroupLayout.SequentialGroup sequentialGroup = macroPanelLayout.createSequentialGroup();
        parallelGroup.add(sequentialGroup);

        sequentialGroup.addContainerGap();
        org.jdesktop.layout.GroupLayout.ParallelGroup parallelGroup1 = macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING);
        sequentialGroup.add(parallelGroup1);

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            org.jdesktop.layout.GroupLayout.SequentialGroup group = macroPanelLayout.createSequentialGroup();
            group.add(customGcodeButtons.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, getWidth())
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED);
            parallelGroup1.add(group);
        }

        macroPanelLayout.setHorizontalGroup( parallelGroup );
        org.jdesktop.layout.GroupLayout.SequentialGroup sequentialGroup1 = macroPanelLayout.createSequentialGroup();
        macroPanelLayout.setVerticalGroup(
                macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(sequentialGroup1
                                        .add(8, 8, 8)
                        ));

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            sequentialGroup1.add(macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(customGcodeButtons.get(i)));
        }

        setLayout(macroPanelLayout);
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
        Macro macro = settings.getMacro(i);

        //Poor coupling here.  We should probably pull the executeCustomGcode method out into the backend.
        if (backend == null) {
            System.err.println("MacroPanel not properly initialized.  Cannot execute custom gcode");
        } else {
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
