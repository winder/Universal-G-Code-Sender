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
package com.willwinder.universalgcodesender.uielements.components;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ProcessorConfigCheckbox extends JPanel {
    final public JCheckBox box;
    final private ProcessorConfig pc;

    private final static Gson GSON;
    private final static JsonParser PARSER = new JsonParser();

    static {
        GSON = new GsonBuilder().setPrettyPrinting().create();
    }

    public ProcessorConfigCheckbox(ProcessorConfig pc, String helpMessage) {
        this.pc = pc;

        if (helpMessage == null)
            throw new RuntimeException("Help message was not provided.");

        JButton help = new JButton("?");
        help.addActionListener((ActionEvent e) -> {
            GUIHelpers.displayHelpDialog(helpMessage);
        });
        add(help, "w 25");

        box = new JCheckBox(Localization.getString(pc.name));
        box.setSelected(pc.enabled);
        box.addActionListener((ActionEvent e) -> {
                pc.enabled = box.isSelected();
            });
        if (!pc.optional) {
            box.setEnabled(false);
        }
        JButton edit = new JButton("edit");
        edit.addActionListener(evt -> editArgs());

        setLayout(new MigLayout("insets 0 50 0 0", "[][grow]20[]25"));
        add(box, "growx");

        if (pc.args != null) {
            add(edit, "w 45");
        }
    }

    public void editArgs() {
        JTextArea ta = new JTextArea(20, 20);
        ta.setText(GSON.toJson(pc.args));
        switch (JOptionPane.showConfirmDialog(null, new JScrollPane(ta))) {
            case JOptionPane.OK_OPTION:
                pc.args = PARSER.parse(ta.getText()).getAsJsonObject();
                System.out.println(ta.getText());
                break;
        }

    }

    public void setSelected(Boolean s) {box.setSelected(s); }
    public boolean getValue() { return box.isSelected(); }
}