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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class MacroPanel extends JPanel implements UGSEventListener {

    private final BackendAPI backend;
    private final java.util.List<JButton> customGcodeButtons = new ArrayList<>();
    private final java.util.List<JTextField> customGcodeTextFields = new ArrayList<>();
    private final java.util.List<JTextField> customGcodeNameFields = new ArrayList<>();
    private final java.util.List<JTextField> customGcodeDescriptionFields = new ArrayList<>();

    public MacroPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }
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
            customGcodeButtons.add(createMacroButton(i));
            JTextField textField = createMacroTextField(i);
            JTextField nameField = createMacroNameField(i);
            JTextField descriptionField = createMacroDescriptionField(i);

            Macro macro = backend.getSettings().getMacro(i);
            if (macro != null) {
                textField.setText(macro.getGcode());
                if (macro.getName() != null) {
                    nameField.setText(macro.getName());
                }
                if (macro.getDescription() != null) {
                    descriptionField.setText(macro.getDescription());
                }
            }
        }

        MigLayout layout = new MigLayout("fill, wrap 4", "[fill, sg 1]r[fill]r[fill, grow 50]r[fill, grow 50]");
        setLayout(layout);

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            add(customGcodeButtons.get(i), "sg 1");
            add(customGcodeNameFields.get(i), "w 75!");
            add(customGcodeTextFields.get(i));
            add(customGcodeDescriptionFields.get(i));
        }

        super.doLayout();
    }

    private JTextField createMacroTextField(int index) {
        JTextField textField = new MacroTextField(index, backend.getSettings());

        customGcodeTextFields.add(textField);
        return textField;
    }

    private JTextField createMacroNameField(int index) {
        JTextField textField = new MacroNameField(index, backend.getSettings());

        customGcodeNameFields.add(textField);
        return textField;
    }

    private JTextField createMacroDescriptionField(int index) {
        JTextField textField = new MacroDescriptionField(index, backend.getSettings());

        customGcodeDescriptionFields.add(textField);
        return textField;
    }

    private JButton createMacroButton(int i) {
        JButton button = new JButton(i+"");

        button.setEnabled(false);
        this.setToolTipText(Localization.getString("macroPanel.button"));

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customGcodeButtonActionPerformed(i);
            }
        });
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
