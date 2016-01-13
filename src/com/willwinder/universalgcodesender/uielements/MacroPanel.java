package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.Settings;
import org.jdesktop.layout.*;

import javax.swing.*;
import javax.swing.GroupLayout;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class MacroPanel extends JPanel {

    private Settings settings;
    private java.util.List<JButton> customGcodeButtons = new ArrayList<JButton>();
    private java.util.List<JTextField> customGcodeTextFields = new ArrayList<JTextField>();

    public MacroPanel() {

    }

    public MacroPanel(Settings settings) {
        this.settings = settings;
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
//        logger.info((lastMacroIndex) + " macros");

        for (int i = customGcodeButtons.size(); i <= lastMacroIndex; i++) {
            JButton button = createMacroButton(i);
            JTextField textField = createMacroTextField(i);

            Macro macro = settings.getMacro(i);
            if (macro != null) {
                textField.setText(macro.getGcode());
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
            group.add(customGcodeButtons.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(customGcodeTextFields.get(i));
            parallelGroup1.add(group);
        }
//        sequentialGroup.addContainerGap();

        macroPanelLayout.setHorizontalGroup( parallelGroup );
        org.jdesktop.layout.GroupLayout.SequentialGroup sequentialGroup1 = macroPanelLayout.createSequentialGroup();
        macroPanelLayout.setVerticalGroup(
                macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(sequentialGroup1
                                        .add(8, 8, 8)
//                                        .add(macroInstructions)
                        ));


        for (int i = 0; i < customGcodeButtons.size(); i++) {
//            sequentialGroup1.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            sequentialGroup1.add(macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(customGcodeButtons.get(i))
                    .add(customGcodeTextFields.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
        }
//        sequentialGroup1.addContainerGap(76, Short.MAX_VALUE);

        setLayout(macroPanelLayout);
    }

    private JTextField createMacroTextField(int index) {
        JTextField textField = new MacroTextField(index, settings);

        customGcodeTextFields.add(textField);
        return textField;
    }

    private JButton createMacroButton(int i) {
        JButton button = new JButton(i+"");

        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customGcodeButtonActionPerformed(evt);
            }
        });
        customGcodeButtons.add(button);
        return button;
    }

    private void customGcodeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //This is probably totally wrong.  Need to get the button out of the event, and from there figure out the macro.
        Macro macro = settings.getMacro(Integer.parseInt(evt.getActionCommand()));
//        executeCustomGcode(macro.getGcode());
    }

    public void updateCustomGcodeControls(boolean enabled) {

    }
}
