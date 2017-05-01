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
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;

public class MacroPanel extends JPanel implements UGSEventListener {

    private final BackendAPI backend;
    private final List<JButton> customGcodeButtons = new ArrayList<>();
    private final List<JTextField> macroNameFields = new ArrayList<>();
    private final List<JTextField> macroGcodeFields = new ArrayList<>();
    private final List<JTextField> macroDescriptionFields = new ArrayList<>();

    private final String helpText = Localization.getString("mainWindow.swing.macroInstructions");
    private final JButton helpButton = new JButton(Localization.getString("help"));
    private final JLabel buttonHeader = new JLabel("");
    private final JLabel nameHeader = new JLabel(Localization.getString("macroPanel.name"));
    private final JLabel gcodeHeader = new JLabel(Localization.getString("macroPanel.text"));
    private final JLabel descriptionHeader = new JLabel(Localization.getString("macroPanel.description"));

    /**
     * Helper for updating macros and creating the different fields.
     */
    private enum MACRO_FIELD {
        NAME, CODE, DESCRIPTION
    }

    public MacroPanel(BackendAPI backend) {
        if (backend == null) {
            throw new RuntimeException();
        }
        this.backend = backend;
        backend.addUGSEventListener(this);
        this.helpButton.addActionListener(l -> {
            GUIHelpers.displayHelpDialog(helpText);
        });
    }

    @Override
    public void doLayout() {
        Integer lastMacroIndex = backend.getSettings().getLastMacroIndex()+1;

        // Create components if needed
        for (int i = customGcodeButtons.size(); i <= lastMacroIndex; i++) {
            Macro macro = backend.getSettings().getMacro(i);
            customGcodeButtons.add(createMacroButton(i));
            macroGcodeFields.add(createMacroField(i, MACRO_FIELD.CODE, macro.getGcode()));
            macroNameFields.add(createMacroField(i, MACRO_FIELD.NAME, macro.getName()));
            macroDescriptionFields.add(createMacroField(i, MACRO_FIELD.DESCRIPTION, macro.getDescription()));
        }

        MigLayout layout = new MigLayout("fill, wrap 4", "[fill, sg 1]r[fill]r[fill, grow 50]r[fill, grow 50]");
        setLayout(layout);

        add(this.helpButton, "span 4");
        add(buttonHeader, "sg 1");
        add(nameHeader, "w 75!");
        add(gcodeHeader);
        add(descriptionHeader);

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            add(customGcodeButtons.get(i), "sg 1");
            add(macroNameFields.get(i), "w 75!");
            add(macroGcodeFields.get(i));
            add(macroDescriptionFields.get(i));
        }

        updateCustomGcodeControls(backend.isIdle());
        super.doLayout();
    }

    /**
     * Updates a macro and saves it in the settings.
     * @param index macro index
     * @param field field to update
     * @param text updated text
     */
    private void update(int index, MACRO_FIELD field, String text) {
        Settings s = backend.getSettings();
        Macro macro = s.getMacro(index);
        switch (field) {
            case NAME:
                macro.setName(text);
                break;
            case CODE:
                macro.setGcode(text);
                break;
            case DESCRIPTION:
                macro.setDescription(text);
                break;
        }
        s.updateMacro(index, macro);
    }

    private JTextField createMacroField(int index, MACRO_FIELD f, String text) {
        JTextField textField = new JTextField(text);
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                update(index, f, textField.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                update(index, f, textField.getText());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                update(index, f, textField.getText());
            }
        });

        return textField;
    }

    private JButton createMacroButton(int i) {
        JButton button = new JButton(i+"");

        button.setEnabled(false);
        this.setToolTipText(Localization.getString("macroPanel.button"));

        button.addActionListener((ActionEvent evt) -> {
            customGcodeButtonActionPerformed(i);
        });
        return button;
    }

    private void customGcodeButtonActionPerformed(int i) {
        Macro macro = backend.getSettings().getMacro(i);
        MacroHelper.executeCustomGcode(macro.getGcode(), backend);
    }

    private void updateCustomGcodeControls(boolean enabled) {
        for (JButton button : customGcodeButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        updateCustomGcodeControls(backend.isIdle());
    }
}
