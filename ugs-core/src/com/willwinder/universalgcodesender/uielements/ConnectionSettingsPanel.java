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

import com.willwinder.universalgcodesender.i18n.AvailableLanguages;
import com.willwinder.universalgcodesender.i18n.Language;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfigGroups;
import com.willwinder.universalgcodesender.utils.FirmwareUtils.ConfigTuple;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ConnectionSettingsPanel extends JPanel {
    final Collection<Component> components = new ArrayList<>();

    final Checkbox overrideSpeedEnabled = new Checkbox(
                Localization.getString("sender.speed.override"));
    final Spinner overrideSpeedPercent = new Spinner(
                Localization.getString("sender.speed.percent"),
                new SpinnerNumberModel((int)1, 1, null, 1));
    final Spinner maxCommandLength = new Spinner(
                Localization.getString("sender.command.length"),
                new SpinnerNumberModel((int)1, 1, null, 1));
    final Spinner truncateDecimalDigits = new Spinner(
                Localization.getString("sender.truncate"),
                new SpinnerNumberModel((int)1, 1, null, 1));
    final Checkbox singleStepMode = new Checkbox(
                Localization.getString("sender.singlestep"));
    final Checkbox removeAllWhitespace = new Checkbox(
                Localization.getString("sender.whitespace"));
    final Checkbox statusPollingEnabled = new Checkbox(
                Localization.getString("sender.status"));
    final Spinner statusPollRate = new Spinner(
                Localization.getString("sender.status.rate"),
                new SpinnerNumberModel((int)1, 1, null, 100));
    final Checkbox stateColorDisplayEnabled = new Checkbox(
                Localization.getString("sender.state"));
    final Checkbox convertArcsToLines = new Checkbox(
                Localization.getString("sender.arcs"));
    final Spinner smallArcLength = new Spinner(
                Localization.getString("sender.arcs.length"),
                new SpinnerNumberModel(1., 1., null, .1));
    final Checkbox autoConnect = new Checkbox(
                Localization.getString("sender.autoconnect"));
    final Checkbox autoReconnect = new Checkbox(
                Localization.getString("sender.autoreconnect"));
    final JComboBox languageCombo = new JComboBox(AvailableLanguages.getAvailableLanguages().toArray());
    final JComboBox controllerConfigs;
    final JPanel controllerConfigPanel = new JPanel();

    private final Settings settings;
    private final Map<String,ConfigTuple> configFiles;
    private final IChanged changer;

    public ConnectionSettingsPanel(Settings settings, IChanged changer, Map<String,ConfigTuple> configFiles) {
        this.changer = changer;
        this.settings = settings;
        this.configFiles = configFiles;
        controllerConfigs = new JComboBox(configFiles.keySet().toArray());
        updateComponents();
    }
    public ConnectionSettingsPanel(Settings settings, Map<String,ConfigTuple> configFiles) {
        this(settings, null, configFiles);
    }

    private void change() {
        if (changer != null) changer.changed();
    }

    private void initActions() {
        for (Component c : components) {
            Class clazz = c.getClass();
            if (clazz == Spinner.class) {
                ((Spinner)c).spinner.addChangeListener((ChangeEvent e) -> {
                    change();
                });
            }
            else if (clazz == Checkbox.class) {
                ((Checkbox)c).box.addActionListener((ActionEvent e) -> {
                    change();
                });
            }
            else if (clazz == JComboBox.class) {
                ((JComboBox)c).addActionListener((ActionEvent e) -> {
                    change();
                });
            }
        }
    }

    public Component add(Component comp) {
        Component ret = super.add(comp);
        components.add(comp);
        return ret;
    }

    public Component add(JPanel panel, Component comp) {
        Component ret = panel.add(comp);
        components.add(comp);
        return ret;
    }

    public void updateSettingsObject() {
        settings.setOverrideSpeedSelected(overrideSpeedEnabled.getValue());
        settings.setOverrideSpeedValue(new Double((int)overrideSpeedPercent.getValue()));
        settings.setMaxCommandLength((int)maxCommandLength.getValue());
        settings.setTruncateDecimalLength((int)truncateDecimalDigits.getValue());
        settings.setSingleStepMode(singleStepMode.getValue());
        settings.setRemoveAllWhitespace(removeAllWhitespace.getValue());
        settings.setStatusUpdatesEnabled(statusPollingEnabled.getValue());
        settings.setStatusUpdateRate((int)statusPollRate.getValue());
        settings.setDisplayStateColor(stateColorDisplayEnabled.getValue());
        settings.setConvertArcsToLines(convertArcsToLines.getValue());
        settings.setSmallArcSegmentLength((double)smallArcLength.getValue());
        settings.setAutoConnectEnabled(autoConnect.getValue());
        settings.setAutoReconnect(autoReconnect.getValue());
        settings.setLanguage(((Language)languageCombo.getSelectedItem()).getLanguageCode());
    }

    private void updateComponents() {
        updateComponents(settings);
    }

    public void updateComponents(Settings s) {
        components.clear();
        this.removeAll();

        setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        overrideSpeedEnabled.setSelected(s.isOverrideSpeedSelected());
        add(overrideSpeedEnabled);

        overrideSpeedPercent.setValue((int)s.getOverrideSpeedValue());
        add(overrideSpeedPercent);

        maxCommandLength.setValue(s.getMaxCommandLength());
        add(maxCommandLength);

        truncateDecimalDigits.setValue((int)s.getTruncateDecimalLength());
        add(truncateDecimalDigits);

        singleStepMode.setSelected(s.isSingleStepMode());
        add(singleStepMode);

        removeAllWhitespace.setSelected(s.isRemoveAllWhitespace());
        add(removeAllWhitespace);

        statusPollingEnabled.setSelected(s.isStatusUpdatesEnabled());
        add(statusPollingEnabled);

        statusPollRate.setValue((int)s.getStatusUpdateRate());
        add(statusPollRate);

        stateColorDisplayEnabled.setSelected(s.isDisplayStateColor());
        add(stateColorDisplayEnabled);

        convertArcsToLines.setSelected(s.isConvertArcsToLines());
        add(convertArcsToLines);

        smallArcLength.setValue(s.getSmallArcSegmentLength());
        add(smallArcLength);

        autoConnect.setSelected(s.isAutoConnectEnabled());
        add(autoConnect);

        autoReconnect.setSelected(s.isAutoReconnect());
        add(autoReconnect);

        for (int i = 0; i < languageCombo.getItemCount(); i++) {
            Language l = (Language)languageCombo.getItemAt(i);
            if (l.getLanguageCode().equals(s.getLanguage())) {
                languageCombo.setSelectedIndex(i);
                break;
            }
        }
        add(languageCombo);

        super.add(controllerConfigs);
        super.add(controllerConfigPanel);
        updateControllerConfigPanel();

        initActions();
    }

    void updateControllerConfigPanel() {
        controllerConfigPanel.removeAll();
        controllerConfigPanel.setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        ConfigTuple ct = configFiles.get(controllerConfigs.getSelectedItem());
        ProcessorConfigGroups pcg = ct.loader.getProcessorConfigs();
        System.out.println(ct.file);

        for (ProcessorConfig pc : pcg.Front) {
            add(controllerConfigPanel, new ProcessorConfigCheckbox(pc));
        }
        for (ProcessorConfig pc : pcg.Custom) {
            add(controllerConfigPanel, new ProcessorConfigCheckbox(pc));
        }
        for (ProcessorConfig pc : pcg.End) {
            add(controllerConfigPanel, new ProcessorConfigCheckbox(pc));
        }
    }

    /**
     * Helper object to simplify layout.
     */
    private class Spinner extends JPanel {
        JLabel label;
        public JSpinner spinner;
        public Spinner(String text, SpinnerModel model) {
            label = new JLabel(text);
            spinner = new JSpinner(model);
            setLayout(new MigLayout("insets 0, wrap 2"));
            add(spinner, "w 70");
            add(label);
        }

        void setValue(Object v) { spinner.setValue(v); }
        Object getValue() { return spinner.getValue(); }
    }

    private class Checkbox extends JPanel {
        public JCheckBox box;
        public Checkbox(String text) {
            box = new JCheckBox(text);
            setLayout(new MigLayout("insets 0"));
            add(box, "gapleft 50, w 100");
        }

        void setSelected(Boolean s) {box.setSelected(s); }
        boolean getValue() { return box.isSelected(); }
    }

    private class ProcessorConfigCheckbox extends JPanel {
        public JCheckBox box;
        private ProcessorConfig pc;

        public ProcessorConfigCheckbox(ProcessorConfig pc) {
            this.pc = pc;
            box = new JCheckBox(pc.name);
            box.setSelected(pc.enabled);
            if (!pc.optional) {
                box.setEnabled(false);
            }
            setLayout(new MigLayout("insets 0"));
            add(box, "gapleft 50, w 100");
        }

        void setSelected(Boolean s) {box.setSelected(s); }
        boolean getValue() { return box.isSelected(); }
    }
}
