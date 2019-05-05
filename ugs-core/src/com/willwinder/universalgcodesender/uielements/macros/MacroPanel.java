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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;

import java.lang.reflect.Type;
import net.miginfocom.swing.MigLayout;


import javax.swing.*;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacroPanel extends JPanel implements UGSEventListener {
    private static final Logger logger = Logger.getLogger(MacroPanel.class.getName());
    public static final String BUTTON_HEIGHT = "wmin 16";

    private final BackendAPI backend;
    private final List<JButton> customGcodeButtons = new ArrayList<>();
    private final List<JButton> deleteButtons = new ArrayList<>();
    private final List<JTextField> macroNameFields = new ArrayList<>();
    private final List<JTextField> macroGcodeFields = new ArrayList<>();
    private final List<JTextField> macroDescriptionFields = new ArrayList<>();

    private final Icon removeIcon = new ImageIcon(getClass().getResource("/resources/icons/remove.png"));
    private final Icon addIcon = new ImageIcon(getClass().getResource("/resources/icons/add.png"));
    private final Icon exportIcon = new ImageIcon(getClass().getResource("/resources/icons/upload.png"));
    private final Icon importIcon = new ImageIcon(getClass().getResource("/resources/icons/download.png"));
    private final Icon helpIcon = new ImageIcon(getClass().getResource("/resources/icons/information.png"));
    private final Icon runIcon = new ImageIcon(getClass().getResource("/resources/icons/bug.png"));

    private final String helpText = Localization.getString("mainWindow.swing.macroInstructions");
    private final JButton helpButton = new JButton(Localization.getString("help"), helpIcon);
    private final JButton importButton = new JButton(Localization.getString("import"), importIcon);
    private final JButton exportButton = new JButton(Localization.getString("export"), exportIcon);
    private final JButton addButton = new JButton(Localization.getString("add"), addIcon);

    private final JLabel buttonHeader = new JLabel("");
    private final JLabel nameHeader = new JLabel(Localization.getString("macroPanel.name"));
    private final JLabel gcodeHeader = new JLabel(Localization.getString("macroPanel.text"));
    private final JLabel descriptionHeader = new JLabel(Localization.getString("macroPanel.description"));
    private final JLabel deleteHeader = new JLabel("");

    private final JPanel buttonPanel = new JPanel(new MigLayout("fill, ins 0"));

    /**
     * Helper for updating macros and creating the different fields.
     */
    private enum MacroFieldEnum {
        NAME, CODE, DESCRIPTION, DELETE_BUTTON
    }

    public MacroPanel(BackendAPI backend) {
        super(new MigLayout("fillx, wrap 5", "[fill, grow 10, sg 1]r[fill, grow 45]r[fill, grow 45]r[fill]r[fill]"));

        if (backend == null) {
            throw new RuntimeException();
        }
        this.backend = backend;
        backend.addUGSEventListener(this);

        addListeners();

        buttonPanel.add(helpButton, "grow, " + BUTTON_HEIGHT);
        buttonPanel.add(importButton, "grow, " + BUTTON_HEIGHT);
        buttonPanel.add(exportButton, "grow, " + BUTTON_HEIGHT);
    }

    @Override
    public void doLayout() {
        clearForm();

        // Create components if needed
        backend.getSettings().getMacros().forEach(macro -> {
            customGcodeButtons.add(createMacroButton(macro));
            macroGcodeFields.add(createMacroField(macro, MacroFieldEnum.CODE, macro.getGcode()));
            macroNameFields.add(createMacroField(macro, MacroFieldEnum.NAME, macro.getName()));
            macroDescriptionFields.add(createMacroField(macro, MacroFieldEnum.DESCRIPTION, macro.getDescription()));
            deleteButtons.add(createDeleteMacroButton(macro));
        });

        add(buttonPanel, "grow, span 5");
        add(nameHeader, "sg 1");
        add(gcodeHeader);
        add(descriptionHeader);
        add(buttonHeader);
        add(deleteHeader);

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            add(macroNameFields.get(i), BUTTON_HEIGHT + ", sg 1");
            add(macroGcodeFields.get(i), BUTTON_HEIGHT);
            add(macroDescriptionFields.get(i), BUTTON_HEIGHT);
            add(customGcodeButtons.get(i), BUTTON_HEIGHT);
            add(deleteButtons.get(i), BUTTON_HEIGHT);
        }

        add(addButton, BUTTON_HEIGHT);

        updateCustomGcodeControls(backend.isIdle());
        super.doLayout();
    }

    private void clearForm() {
        customGcodeButtons.forEach(this::remove);
        customGcodeButtons.clear();

        macroGcodeFields.forEach(this::remove);
        macroGcodeFields.clear();

        macroNameFields.forEach(this::remove);
        macroNameFields.clear();

        macroDescriptionFields.forEach(this::remove);
        macroDescriptionFields.clear();

        deleteButtons.forEach(this::remove);
        deleteButtons.clear();
    }

    private JButton createDeleteMacroButton(Macro macro) {
        JButton button = new JButton(Localization.getString("delete"), removeIcon);
        button.addActionListener((ActionEvent evt) -> {
            backend.getSettings().deleteMacro(macro);
            doLayout();
        });
        return button;
    }

    /**
     * Updates a macro and saves it in the settings.
     * @param macro the macro to update
     * @param field field to update
     * @param text updated text
     */
    private void update(Macro macro, MacroFieldEnum field, String text) {
        Settings s = backend.getSettings();
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
        s.updateMacro(macro);
    }

    private JTextField createMacroField(Macro macro, MacroFieldEnum f, String text) {
        JTextField textField = new JTextField(text);
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                update(macro, f, textField.getText());
            }

            @Override
            public void keyPressed(KeyEvent e) {
                update(macro, f, textField.getText());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                update(macro, f, textField.getText());
            }
        });

        return textField;
    }

    private JButton createMacroButton(Macro macro) {
        JButton button = new JButton(Localization.getString("macroPanel.try"), runIcon);

        button.setEnabled(false);
        this.setToolTipText(Localization.getString("macroPanel.button"));

        button.addActionListener((ActionEvent evt) -> customGcodeButtonActionPerformed(macro));
        return button;
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
        for (JButton button : customGcodeButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        updateCustomGcodeControls(backend.isIdle());
    }

    private void addListeners() {

        this.addButton.addActionListener(l -> {
            int lastMacroIndex = backend.getSettings().getMacros().size() + 1;
            backend.getSettings().addMacro(new Macro("Macro #" + lastMacroIndex, null, ""));
            doLayout();
        });

        this.helpButton.addActionListener(l -> GUIHelpers.displayHelpDialog(helpText));

        this.exportButton.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser(backend.getSettings().getLastOpenedFilename());
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Collection<Macro> macros = backend.getSettings().getMacros();

                    try (FileWriter fileWriter = new FileWriter(fileChooser.getSelectedFile())) {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        fileWriter.write(gson.toJson(macros, Collection.class));
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Problem while browsing.", ex);
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                }
            }
        });

        this.importButton.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser(backend.getSettings().getLastOpenedFilename());
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File importFile = fileChooser.getSelectedFile();

                    try (FileReader reader = new FileReader(importFile)) {
                        Type type = new TypeToken<ArrayList<Macro>>(){}.getType();
                        List<Macro> macros = new Gson().fromJson(reader, type);

                        for (Macro m : macros) {
                            backend.getSettings().updateMacro(m);
                        }

                        // Update the window.
                        SwingUtilities.invokeLater(() -> {
                            this.repaint();
                            this.revalidate();
                        });
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Problem while browsing.", ex);
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                }
            }
        });
    }
}