/**
 * Static helper class for executing custom macros.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.awt.EventQueue;
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
 *
 * @author wwinder
 */
public class MacroHelper {

    /**
     * Process and send a custom gcode string.
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
    public static void executeCustomGcode(final String str, BackendAPI backend) {
        if (str == null) {
            return;
        }
        String command = MacroHelper.substituteValues(str, backend);
        command = command.replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", "");
        final String[] parts = command.split(";");
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String cmd : parts) {
                        backend.sendGcodeCommand(cmd);
                    }
                } catch (Exception ex) {
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                }
            }
        });
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
    static private final Pattern PROMPT_REGEX = Pattern.compile("\\{prompt\\|([^\\}]+)\\}");
    protected static String substituteValues(String str, BackendAPI backend) {
        SystemStateBean bean = new SystemStateBean();
        backend.updateSystemState(bean);

        // Do simple substitutions
        String command = str;
        command = command.replaceAll("\\{machine_x\\}", bean.getMachineX());
        command = command.replaceAll("\\{machine_y\\}", bean.getMachineY());
        command = command.replaceAll("\\{machine_z\\}", bean.getMachineZ());
        command = command.replaceAll("\\{work_x\\}", bean.getWorkX());
        command = command.replaceAll("\\{work_y\\}", bean.getWorkY());
        command = command.replaceAll("\\{work_z\\}", bean.getWorkZ());

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
