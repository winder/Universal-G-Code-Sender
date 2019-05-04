/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.miginfocom.swing.MigLayout;

/**
 * Static helper class for executing custom macros.
 *
 * @author wwinder
 */
public class MacroHelper {
    static private final Pattern MACHINE_X = Pattern.compile("\\{machine_x\\}");
    static private final Pattern MACHINE_Y = Pattern.compile("\\{machine_y\\}");
    static private final Pattern MACHINE_Z = Pattern.compile("\\{machine_z\\}");
    static private final Pattern WORK_X = Pattern.compile("\\{work_x\\}");
    static private final Pattern WORK_Y = Pattern.compile("\\{work_y\\}");
    static private final Pattern WORK_Z = Pattern.compile("\\{work_z\\}");
    static private final Pattern PROMPT_REGEX = Pattern.compile("\\{prompt\\|([^\\}]+)\\}");

    /**
     * Process and send a custom gcode string.
     * 
     * Not safe to run in the AWT Event Dispatching thread, be sure to use
     * EventQueue.invokeLater() if running in response to a GUI event.
     * 
     * Interactive substitutions can be made with special characters:
     *   %machine_x% - The machine X location
     *   %machine_y% - The machine Y location
     *   %machine_z% - The machine Z location
     *   %work_x% - The work X location
     *   %work_y% - The work Y location
     *   %work_z% - The work Z location
     *   %prompt|name% - Prompt the user for a value named 'name'.
     * 
     * @param str 
     * @param backend 
     */
    public static void executeCustomGcode(final String str, BackendAPI backend) throws Exception {
        if (str == null) {
            return;
        }
        String command = MacroHelper.substituteValues(str, backend);
        command = command.replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", "");
        final String[] parts = command.split(";");

        /* 
         * specifically NOT catching exceptions on gCode commands, let them pass to the invoking method
         * so the error handling is aligned to the UI that triggered it (i.e. GUI button press versus Pendant UI http request)
         */
        for (String cmd : parts) {
            backend.sendGcodeCommand(cmd);
        }
    }
    
    /**
     * Interactive substitutions can be made with special characters:
     *   {machine_x} - The machine X location
     *   {machine_y} - The machine Y location
     *   {machine_z} - The machine Z location
     *   {work_x} - The work X location
     *   {work_y} - The work Y location
     *   {work_z} - The work Z location
     *   {prompt|name} - Prompt the user for a value named 'name'.
     * 
     * @param str
     * @param backend
     * @return 
     */
    protected static String substituteValues(String str, BackendAPI backend) {
        // Early exit if there is nothing to match.
        if (!str.contains("{")) {
            return str;
        }

        // Do simple substitutions
        String command = str;
        Position machinePosition = backend.getMachinePosition();
        command = MACHINE_X.matcher(command).replaceAll(Utils.formatter.format(machinePosition.getX()));
        command = MACHINE_Y.matcher(command).replaceAll(Utils.formatter.format(machinePosition.getY()));
        command = MACHINE_Z.matcher(command).replaceAll(Utils.formatter.format(machinePosition.getZ()));

        Position workPosition = backend.getWorkPosition();
        command = WORK_X.matcher(command).replaceAll(Utils.formatter.format(workPosition.getX()));
        command = WORK_Y.matcher(command).replaceAll(Utils.formatter.format(workPosition.getY()));
        command = WORK_Z.matcher(command).replaceAll(Utils.formatter.format(workPosition.getZ()));

        // Prompt for additional substitutions
        Matcher m = PROMPT_REGEX.matcher(command);
        List<String> prompts = new ArrayList<>();
        while (m.find()) {
            prompts.add(m.group(1));
        }

        if (prompts.size() > 0) {
            List<JTextField> fields = new ArrayList<>();
            JPanel myPanel = new JPanel();
            myPanel.setLayout(new MigLayout("wrap 2, width 200"));
            for (String s : prompts) {
                JTextField field = new JTextField();
                myPanel.add(new JLabel(s + ":"));
                myPanel.add(field, "growx, pushx");
                fields.add(field);
            }

            int result = JOptionPane.showConfirmDialog(null, myPanel, 
                     Localization.getString("macro.substitution"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                for(int i = 0; i < prompts.size(); i++) {
                    command = command.replace("{prompt|" + prompts.get(i) + "}", fields.get(i).getText());
                }
            } else {
                command = "";
            }
        }

        return command;
    }
}
