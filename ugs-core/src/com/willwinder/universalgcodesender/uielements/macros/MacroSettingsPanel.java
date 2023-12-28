/*
    Copyright 2016 Will Winder

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
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.SerializationUtils;

import javax.swing.Timer;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MacroSettingsPanel extends JPanel implements UGSEventListener {
    private static final Logger logger = Logger.getLogger(MacroSettingsPanel.class.getName());
    private static final String MIN_WIDTH = "width 100:100:, growx";

    private final transient BackendAPI backend;
    private final List<JButton> moveUpButtons = new ArrayList<>();
    private final List<JButton> moveDownButtons = new ArrayList<>();
    private final List<JButton> tryButton = new ArrayList<>();
    private final List<JButton> deleteButtons = new ArrayList<>();
    private final List<JTextField> macroNameFields = new ArrayList<>();
    private final List<JTextArea> macroGcodeFields = new ArrayList<>();
    private final List<JTextField> macroDescriptionFields = new ArrayList<>();

    private final Icon removeIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/remove.png")));
    private final Icon addIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/add.png")));
    private final Icon exportIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/upload.png")));
    private final Icon importIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/download.png")));
    private final Icon helpIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/information.png")));
    private final Icon runIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/bug.png")));
    private final Icon upIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/up.png")));
    private final Icon downIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resources/icons/down.png")));


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
    private final List<Macro> macros;

    private final Timer layoutTimer;
    private boolean shouldDoLayout = true;

    public void save() {
        backend.getSettings().setMacros(macros);
    }

    /**
     * Helper for updating macros and creating the different fields.
     */
    private enum MacroFieldEnum {
        NAME, CODE, DESCRIPTION, DELETE_BUTTON
    }

    public MacroSettingsPanel(BackendAPI backend) {
        super(new MigLayout("fillx, wrap 7", "[fill, grow 10, sg 1]r[fill, grow 10]r[fill, grow 10]r[fill, grow 60]r[fill, grow 30]r[fill]r[fill]"));

        this.backend = backend;
        this.backend.addUGSEventListener(this);

        this.macros = backend.getSettings().getMacros().stream()
                .map(SerializationUtils::clone)
                .collect(Collectors.toList());

        addListeners();

        buttonPanel.add(helpButton, "grow");
        buttonPanel.add(importButton, "grow");
        buttonPanel.add(exportButton, "grow");


        // Initialize the timer with a delay of 500ms
        layoutTimer = new Timer(500, e -> shouldDoLayout = true);
        layoutTimer.setRepeats(false);
        layoutTimer.stop();
    }

    @Override
    public void doLayout() {
        if (!shouldDoLayout) {
            return;
        }

        clearForm();

        macros.forEach(macro -> {
            moveUpButtons.add(createMoveUpButton(macro));
            moveDownButtons.add(createMoveDownButton(macro));
            tryButton.add(createMacroButton(macro));
            String gcode = macro.getGcodeString();
            macroGcodeFields.add(createMacroGcodeField(macro, gcode));
            macroNameFields.add(createMacroField(macro, MacroFieldEnum.NAME, macro.getName()));
            macroDescriptionFields.add(createMacroField(macro, MacroFieldEnum.DESCRIPTION, macro.getDescription()));
            deleteButtons.add(createDeleteMacroButton(macro));
        });

        add(buttonPanel, "grow, spanx, wrap");
        add(new JPanel(), "span 2, sg 1");
        add(nameHeader);
        add(gcodeHeader);
        add(descriptionHeader);
        add(buttonHeader);
        add(deleteHeader);

        for (int i = 0; i < tryButton.size(); i++) {
            add(moveUpButtons.get(i), "sg 1, aligny top");
            add(moveDownButtons.get(i), "sg 1, aligny top");

            add(macroNameFields.get(i), MIN_WIDTH);
            add(macroGcodeFields.get(i), MIN_WIDTH);
            add(macroDescriptionFields.get(i), MIN_WIDTH);
            add(tryButton.get(i));
            add(deleteButtons.get(i));
        }

        add(new JPanel(), "span 5");
        add(addButton, "span 2");

        updateCustomGcodeControls(backend.isIdle());
        super.doLayout();
        revalidate();
    }

    private JButton createMoveUpButton(Macro macro) {
        JButton button = new JButton(upIcon);
        button.addActionListener((ActionEvent evt) -> {
            int index = macros.indexOf(macro);
            if (index > 0) {
                Collections.swap(macros, index, index - 1);
                doLayout();
            }
        });
        return button;
    }

    private JButton createMoveDownButton(Macro macro) {
        JButton button = new JButton(downIcon);
        button.addActionListener((ActionEvent evt) -> {
            int index = macros.indexOf(macro);
            if (index < macros.size() - 1) {
                Collections.swap(macros, index, index + 1);
                doLayout();
            }
        });
        return button;
    }

    private void clearForm() {
        moveUpButtons.forEach(this::remove);
        moveUpButtons.clear();

        moveDownButtons.forEach(this::remove);
        moveDownButtons.clear();

        tryButton.forEach(this::remove);
        tryButton.clear();

        macroGcodeFields.forEach(this::remove);
        macroGcodeFields.clear();

        macroNameFields.forEach(this::remove);
        macroNameFields.clear();

        macroDescriptionFields.forEach(this::remove);
        macroDescriptionFields.clear();

        deleteButtons.forEach(this::remove);
        deleteButtons.clear();

        removeAll();
    }

    private JButton createDeleteMacroButton(Macro macro) {
        JButton button = new JButton(Localization.getString("delete"), removeIcon);
        button.addActionListener((ActionEvent evt) -> {
            macros.remove(macro);
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
        switch (field) {
            case NAME:
                macro.setName(text);
                break;
            case DESCRIPTION:
                macro.setDescription(text);
                break;
            default:
        }

        // Add it if it doesn't exists
        if (!macros.contains(macro)) {
            macros.add(macro);
        }
    }

    private void updateGcode(Macro macro, String[] gcode) {
        macro.setGcode(gcode);

        // Add it if it doesn't exists
        if (!macros.contains(macro)) {
            macros.add(macro);
        }
    }

    private JTextArea createMacroGcodeField(Macro macro, String text) {
        JTextArea textField = new JTextArea(text) {
            @Override
            protected void processKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    handleEnterAction(macro, this);

                    shouldDoLayout = true;
                    setRows(getRows() + 1);
                    repaint();
                    shouldDoLayout = false;
                } else {
                    super.processKeyEvent(e);
                }
            }
        };

        textField.getDocument().addDocumentListener(new DocumentListener() {
            void update() {
                if (layoutTimer.isRunning()) {
                    layoutTimer.restart();
                } else {
                    layoutTimer.start();
                }
                shouldDoLayout = false; // Prevent doLayout while typing
                updateGcode(macro, textField.getText().split("\n"));
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Do nothing
            }
        });


        textField.setEditable(true);
        textField.setEnabled(true);
        textField.setLineWrap(true);
        textField.setWrapStyleWord(true);
        textField.setAutoscrolls(true);
        textField.setMargin(new Insets(5, 5, 5, 5));
        textField.setFont(new Font("Monospaced", Font.PLAIN, 12));

        return textField;
    }

    private void handleEnterAction(Macro macro, JTextArea textField) {
        // Handle what should happen when Enter is pressed
        // For example, you might want to insert a new line at the cursor position:
        try {
            int insertPosition = textField.getCaretPosition();
            textField.getDocument().insertString(insertPosition, "\n", null);
        } catch (BadLocationException ex) {
            // Do nothing as this should never happen
        }

        updateGcode(macro, textField.getText().split("\n"));
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
        for (JButton button : tryButton) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        updateCustomGcodeControls(backend.isIdle());
    }

    private void addListeners() {

        this.addButton.addActionListener(l -> {
            String macroName = findUniqueMacroName(macros.size());
            macros.add(new Macro(UUID.randomUUID().toString(), macroName, null, new String[]{""}));
            doLayout();
        });

        this.helpButton.addActionListener(l -> GUIHelpers.displayHelpDialog(helpText));

        this.exportButton.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser(backend.getSettings().getLastOpenedFilename());
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    Collection<Macro> macroList = backend.getSettings().getMacros();

                    try (OutputStream fileOutputStream = new FileOutputStream(fileChooser.getSelectedFile())) {
                        Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Macro.class, new Macro.MacroSerializer())
                                .setPrettyPrinting()
                                .create();
                        writer.write(gson.toJson(macroList));
                        writer.flush();
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Problem while saving macros.", ex);
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                }
            }
        });

        this.importButton.addActionListener(l -> {
            JFileChooser fileChooser = new JFileChooser(backend.getSettings().getLastOpenedFilename());
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    File importFile = fileChooser.getSelectedFile();

                    try (InputStream reader = new FileInputStream(importFile)) {
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Macro.class, new Macro.MacroSerializer())
                                .registerTypeAdapter(Macro.class, new Macro.MacroDeserializer())
                                .create();
                        Type macroListType = new TypeToken<List<Macro>>() {}.getType(); // Assuming Macro now reflects the correct structure
                        List<Macro> macroList = gson.fromJson(new InputStreamReader(reader, StandardCharsets.UTF_8), macroListType);

                        this.macros.addAll(macroList);

                        // Update the window.
                        SwingUtilities.invokeLater(() -> {
                            this.repaint();
                            this.revalidate();
                        });
                    }
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Problem while importing.", ex);
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                }
            }
        });
    }

    private String findUniqueMacroName(int index) {
        final String macroName = "Macro #" + (index + 1);
        if (macros.stream().noneMatch(m ->
                {
                    if (m.getName() != null) {
                        return m.getName().equalsIgnoreCase(macroName);
                    }
                    return false;
                }
            ))
        {
            return macroName;
        }
        return findUniqueMacroName(index + 1);
    }
}
