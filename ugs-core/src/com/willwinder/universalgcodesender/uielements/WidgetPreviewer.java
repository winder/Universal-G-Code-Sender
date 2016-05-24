/**
 * Opens all the widgets in their own little frame with the widget name as its
 * title.
 */
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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.uielements.action.ActionButtonPanel;
import com.willwinder.universalgcodesender.uielements.action.ActionPanel;
import com.willwinder.universalgcodesender.uielements.command.CommandPanel;
import com.willwinder.universalgcodesender.uielements.connection.ConnectionPanel;
import com.willwinder.universalgcodesender.uielements.jog.JogPanel;
import com.willwinder.universalgcodesender.uielements.machinestatus.MachineStatusPanel;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class WidgetPreviewer {
    public static void main(String[] args) throws Exception {
        BackendAPI backend = new GUIBackend();
        backend.applySettings(SettingsFactory.loadSettings());
        JPanel panel = new JPanel();

        // Create the main frame.
        JFrame frame = new JFrame("Widget Previewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        // Button panel...
        panel.setLayout(new MigLayout("wrap 1"));

        panel.add(frameLauncherButton("CommandTextArea", new CommandTextArea(backend)));
        //panel.add(frameLauncherButton("ConnectionSettingsDialog", new ConnectionSettingsDialog(backend.getSettings()), null, false));
        panel.add(frameLauncherButton("ConnectionSettingsPanel", new ConnectionSettingsPanel(backend.getSettings())));
        panel.add(frameLauncherButton("MacroActionPanel", new MacroActionPanel(backend)));
        panel.add(frameLauncherButton("MacroPanel", new MacroPanel(backend)));
        panel.add(frameLauncherButton("OverridesPanel", new OverridesPanel(backend)));
        panel.add(frameLauncherButton("SendStatusLine", new SendStatusLine(backend)));
        panel.add(frameLauncherButton("SendStatusPanel", new SendStatusPanel(backend)));
        panel.add(frameLauncherButton("ActionButtonPanel", new ActionButtonPanel(backend)));
        panel.add(frameLauncherButton("ActionPanel", new ActionPanel(backend)));
        panel.add(frameLauncherButton("CommandPanel", new CommandPanel(backend)));
        panel.add(frameLauncherButton("ConnectionPanel", new ConnectionPanel(backend)));
        panel.add(frameLauncherButton("JogPanel", new JogPanel(backend)));
        panel.add(frameLauncherButton("MachineStatusPanel", new MachineStatusPanel(backend)));

        // Display the main frame.
        frame.pack();
        frame.setVisible(true);
    }

    private static JButton frameLauncherButton(String title, Component c) {
        JButton button = new JButton(title);
        button.addActionListener((ActionEvent e) -> {
            frameLauncher(title, c);
        });
        return button;
    }

    private static void frameLauncher(String title, Component c) {
        JFrame frame = new JFrame(title);

        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(c, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }
}
