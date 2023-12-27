/*
    Copyright 2018-2024 Will Winder

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
import com.willwinder.universalgcodesender.services.KeyboardService;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static helper class for executing custom macros.
 *
 * @author wwinder
 */
public class MacroHelper {
    private static final Pattern MACHINE_X = Pattern.compile("\\{machine_x}");
    private static final Pattern MACHINE_Y = Pattern.compile("\\{machine_y}");
    private static final Pattern MACHINE_Z = Pattern.compile("\\{machine_z}");
    private static final Pattern WORK_X = Pattern.compile("\\{work_x}");
    private static final Pattern WORK_Y = Pattern.compile("\\{work_y}");
    private static final Pattern WORK_Z = Pattern.compile("\\{work_z}");
    private static final Pattern PROMPT_REGEX = Pattern.compile("\\{prompt\\|([^}]+)}");
    private static final Pattern KEYPRESS_REGEX = Pattern.compile("\\{keypress\\|([^}]+)}");

    // GCODE variables are #101, #102, etc. // #([\d]+)=value
    private static final Pattern GCODE_VAR_REGEX = Pattern.compile("#(\\d+)=((([-+]+|)\\d+\\.?\\d*)|\\[(.+?)])");

    /**
     * GCODE operations are AXIS[1+2], AXIS[#VAR-4], AXIS[1+2-3], AXIS[1/2-#VAR], etc.
     * Groups:
     * - Group 1: The first number
     * - Group 4: The operator
     * - Group 6: The second number
     * Note: The spaces are optional, but the operator must be present.
     */
    private static final Pattern GCODE_OP_REGEX = Pattern.compile("\\[([#\\d.]+)(|(|\\s+)([+\\-*/])(|\\s+)([#\\d.]+))]");



    /**
     * Process and send a custom gcode string.
     * <p>
     * Not safe to run in the AWT Event Dispatching thread, be sure to use
     * EventQueue.invokeLater() if running in response to a GUI event.
     * <p>
     * Interactive substitutions can be made with special characters:
     * %machine_x% - The machine X location
     * %machine_y% - The machine Y location
     * %machine_z% - The machine Z location
     * %work_x% - The work X location
     * %work_y% - The work Y location
     * %work_z% - The work Z location
     * %prompt|name% - Prompt the user for a value named 'name'.
     *
     * @param str
     * @param backend
     */
    public static void executeCustomGcode(final String[] str, BackendAPI backend) throws Exception {
        if (str == null) {
            return;
        }
        String gcode = String.join("\n", str);
        String command = MacroHelper.substituteValues(gcode, backend);
        command = MacroHelper.compileGcode(command);
        final String[] parts = command.split("(\\r\\n|\\n\\r|\\r|\\n)");

        /*
         * specifically NOT catching exceptions on gCode commands, let them pass to the invoking method
         * so the error handling is aligned to the UI that triggered it (i.e. GUI button press versus Pendant UI http request)
         */
        for (String cmd : parts) {
            if (StringUtils.isNotEmpty(cmd)) {
                backend.sendGcodeCommand(cmd);
            }
        }
    }

    /**
     * Interactive substitutions can be made with special characters:
     * {machine_x} - The machine X location
     * {machine_y} - The machine Y location
     * {machine_z} - The machine Z location
     * {work_x} - The work X location
     * {work_y} - The work Y location
     * {work_z} - The work Z location
     * {prompt|name} - Prompt the user for a value named 'name'.
     * {keypress|keys} - Dispatch keyboard press events on the host system. Keys are defined using AWT format, see {@link KeyStroke#getKeyStroke(String)}
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

        command = parsePrompts(command);
        command = parseKeyPress(command);

        return command;
    }

    private static String parseKeyPress(String command) {
        Matcher m = KEYPRESS_REGEX.matcher(command);
        List<String> keyPressList = new ArrayList<>();
        while (m.find()) {
            keyPressList.add(m.group(1));
        }
        command = RegExUtils.removeAll(command, m.pattern());

        if (!keyPressList.isEmpty()) {
            KeyboardService.getInstance().sendKeys(keyPressList);
        }

        return command;
    }



    private static String parsePrompts(String command) {
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
                for (int i = 0; i < prompts.size(); i++) {
                    command = command.replace("{prompt|" + prompts.get(i) + "}", fields.get(i).getText());
                    command = command.replace("{" + prompts.get(i) + "}", fields.get(i).getText()); // for reusing values
                }
            } else {
                command = "";
            }
        }
        return command;
    }

    private static Map<Integer, String> parseGcodeVars(String command) {
        Matcher m = GCODE_VAR_REGEX.matcher(command);
        Map<Integer, String> gcodeVars = new HashMap<>();
        while (m.find()) {
            Integer varNum = Integer.valueOf(m.group(1));
            String value = m.group(3);

            if (value.startsWith("[")) {
                Matcher opMatcher = GCODE_OP_REGEX.matcher(value);
                if (!opMatcher.matches()) {
                    throw new RuntimeException("Invalid GCODE operation in variable: " + value);
                }

                String firstOperand = getOperand(opMatcher.group(1), gcodeVars);
                String operator = opMatcher.group(4);
                String secondOperand = getOperand(opMatcher.group(6), gcodeVars);
                double result = getOperationResult(operator, firstOperand, secondOperand);
                value = String.valueOf(result);
            }

            gcodeVars.put(varNum, value);
        }
        return gcodeVars;
    }

    private static Map<Integer, String> parseOperations(String command) {
        Matcher m = GCODE_OP_REGEX.matcher(command);
        Map<Integer, String> gcodeOps = new HashMap<>();

        while (m.find()) {
            Integer lineNum = command.substring(0, m.start()).split("\n").length;
            String originalOp = m.group(0);
            gcodeOps.put(lineNum, originalOp);
        }

        return gcodeOps;
    }

    private static String compileGcode(String command) {

        Map<Integer, String> gcodeVars = parseGcodeVars(command);
        Map<Integer, String> gcodeOps = parseOperations(command);

        for (Map.Entry<Integer, String> entry : gcodeOps.entrySet()) {
            String op = entry.getValue();
            Matcher opMatcher = GCODE_OP_REGEX.matcher(op);
            if (!opMatcher.matches()) {
                throw new RuntimeException("Invalid GCODE operation: " + op);
            }

            String firstOperand = opMatcher.group(1), operator = opMatcher.group(4), secondOperand = opMatcher.group(6);
            firstOperand = getOperand(firstOperand, gcodeVars);

            if (secondOperand == null) {
                Integer opId = -1 * entry.getKey(); // Negative line numbers are used for storing the result of the operation.
                gcodeVars.put(opId, firstOperand);
                continue;
            } else if (operator == null) {
                throw new RuntimeException("Invalid GCODE operation: " + op);
            }

            secondOperand = getOperand(secondOperand, gcodeVars);
            double result = getOperationResult(operator, firstOperand, secondOperand);
            String resultStr = Utils.formatter.format(result);
            Integer opId = -1 * entry.getKey();
            gcodeVars.put(opId, resultStr);
        }

        return replaceGcodeVarsAndOps(command, gcodeVars, gcodeOps);
    }

    private static String replaceGcodeVarsAndOps(String command, Map<Integer, String> gcodeVars, Map<Integer, String> gcodeOps) {
        // Replace the operations:
        for (Map.Entry<Integer, String> entry : gcodeOps.entrySet()) {
            String op = entry.getValue();
            Integer lineNum = entry.getKey();
            Integer varId = -1 * lineNum;
            command = command.replace(op, gcodeVars.get(varId));
        }

        // Remove the variables lines:
        String[] lines = command.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!line.matches("^#\\d+=.*$")) {
                sb.append(line).append("\n");
            }
        }

        return sb.toString();
    }

    private static String getOperand(String operand, Map<Integer, String> gcodeVars) {
        if (operand.startsWith("#")) {
            Integer varId = Integer.valueOf(operand.substring(1));
            if (!gcodeVars.containsKey(varId)) {
                throw new RuntimeException("GCODE variable not found: " + operand);
            }
            operand = gcodeVars.get(varId);
        }

        return operand;
    }

    private static double getOperationResult(String operator, String firstOperand, String secondOperand) {
        return switch (operator) {
            case "+" -> Double.parseDouble(firstOperand) + Double.parseDouble(secondOperand);
            case "-" -> Double.parseDouble(firstOperand) - Double.parseDouble(secondOperand);
            case "*" -> Double.parseDouble(firstOperand) * Double.parseDouble(secondOperand);
            case "/" -> Double.parseDouble(firstOperand) / Double.parseDouble(secondOperand);
            default -> 0;
        };
    }
}
