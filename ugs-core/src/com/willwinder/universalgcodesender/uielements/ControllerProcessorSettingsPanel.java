/**
 * Configure the controller settings.
 */
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfigGroups;
import com.willwinder.universalgcodesender.utils.FirmwareUtils.ConfigTuple;
import com.willwinder.universalgcodesender.utils.Settings;
import java.util.Arrays;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ControllerProcessorSettingsPanel extends AbstractUGSSettings {
    private final Map<String,ConfigTuple> configFiles;
    final JComboBox controllerConfigs;
    final JTable customRemoverTable;
    final JButton add = new JButton(Localization.getString("settings.processors.add"));
    final JButton remove = new JButton(Localization.getString("settings.processors.remove"));

    public ControllerProcessorSettingsPanel(Settings settings, IChanged changer, Map<String,ConfigTuple> configFiles) {
        super(settings, changer);
        this.configFiles = configFiles;
        this.controllerConfigs = new JComboBox(configFiles.keySet().toArray());
        this.customRemoverTable = getCustomRemoverTable();
        super.updateComponents();

        controllerConfigs.addActionListener(e -> super.updateComponents());
        add.addActionListener(e -> this.addNewPatternRemover());
        remove.addActionListener(e -> this.removeSelectedPatternRemover());
    }

    public ControllerProcessorSettingsPanel(Settings settings, Map<String,ConfigTuple> configFiles) {
        this(settings, null, configFiles);
    }

    private void addNewPatternRemover() {
        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        model.addRow(new Object[]{true, "asdf"});
    }

    private void removeSelectedPatternRemover() {
        int[] rows = customRemoverTable.getSelectedRows();
        Arrays.sort(rows);

        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        for (int i = rows.length; i > 0; i--) {
            int row = rows[i-1];
            model.removeRow(row);
        }
    }

    @Override
    public void save() {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public String getHelpMessage() {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     *  ------------------------------
     *  |  [      controller      ]  |
     *  | [ ] front processor 1      |
     *  | [ ] front processor 2      |
     *  |                            |
     *  | [+]                   [-]  |
     *  |  ________________________  |
     *  | | Enabled | Pattern      | |
     *  | |  [y]    | T\d+         | |
     *  | |  [n]    | M30          | |
     *  |  ------------------------  |
     *  |                            |
     *  | [ ] end processor 1        |
     *  | [ ] end processor 2        |
     *  |____________________________|
     */
    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();
        DefaultTableModel model = (DefaultTableModel) this.customRemoverTable.getModel();
        int rowCount = model.getRowCount();
        model.setRowCount(0);


        setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        super.addIgnoreChanges(controllerConfigs);

        ConfigTuple ct = configFiles.get(controllerConfigs.getSelectedItem());
        ProcessorConfigGroups pcg = ct.loader.getProcessorConfigs();
        System.out.println(ct.file);

        for (ProcessorConfig pc : pcg.Front) {
            add(new ProcessorConfigCheckbox(pc));
        }

        for (ProcessorConfig pc : pcg.End) {
            add(new ProcessorConfigCheckbox(pc));
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new MigLayout("wrap 3", "grow, fill", "grow, fill"));
        add(buttonPanel, add);
        add(buttonPanel, new JLabel());
        add(buttonPanel, remove);
        addIgnoreChanges(buttonPanel);

        for (ProcessorConfig pc : pcg.Custom) {
            Boolean enabled = pc.enabled;
            String pattern = "";
            if (pc.args != null && !pc.args.get("pattern").isJsonNull()) {
                pattern = pc.args.get("pattern").getAsString();
            }
            model.addRow(new Object[]{enabled, pattern});
            //add(new ProcessorConfigCheckbox(pc));
        }
        addIgnoreChanges(new JScrollPane(customRemoverTable));
    }

    private JTable getCustomRemoverTable() {
        final String[] columnNames = {
            Localization.getString("settings.processors.enabled"),
            Localization.getString("settings.processors.pattern")
        };
        final Class[] columnTypes =  {
            Boolean.class,
            String.class
        };

        JTable ret = new JTable();

        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public Class<?> getColumnClass(int idx) {
                return columnTypes[idx];
            }
        };

        ret.setModel(model);
        ret.getTableHeader().setReorderingAllowed(false);

        return ret;
    }
}
