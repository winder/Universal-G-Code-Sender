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
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
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
    Collection<Component> components = new ArrayList<>();

    Checkbox overrideSpeedEnabled;
    Spinner overrideSpeedPercent;
    Spinner maxCommandLength;
    Spinner truncateDecimalDigits;
    Checkbox singleStepMode;
    Checkbox removeAllWhitespace;
    Checkbox statusPollingEnabled;
    Spinner statusPollRate;
    Checkbox stateColorDisplayEnabled;
    Checkbox convertArcsToLines;
    Spinner smallArcThreshold;
    Spinner smallArcLength;
    Checkbox autoConnect;
    Checkbox autoReconnect;
    JComboBox languageCombo;

    private final Settings settings;
    private final IChanged changer;

    public ConnectionSettingsPanel(Settings settings, IChanged changer) {
        this.changer = changer;
        this.settings = settings;
        initComponents();
        initActions();
        change();
    }
    public ConnectionSettingsPanel(Settings settings) {
        this(settings, null);
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

    private void initComponents() {
        setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        overrideSpeedEnabled = new Checkbox(
                Localization.getString("sender.speed.override"),
                settings.isOverrideSpeedSelected());
        add(overrideSpeedEnabled);

        overrideSpeedPercent = new Spinner(
                Localization.getString("sender.speed.percent"),
                new SpinnerNumberModel((int)settings.getOverrideSpeedValue(), 1, null, 1));
        add(overrideSpeedPercent);

        maxCommandLength = new Spinner(
                Localization.getString("sender.command.length"),
                new SpinnerNumberModel((int)settings.getMaxCommandLength(), 1, null, 1));
        add(maxCommandLength);

        truncateDecimalDigits = new Spinner(
                Localization.getString("sender.truncate"),
                new SpinnerNumberModel((int)settings.getTruncateDecimalLength(), 1, null, 1));
        add(truncateDecimalDigits);

        singleStepMode = new Checkbox(
                Localization.getString("sender.singlestep"),
                settings.isSingleStepMode());
        add(singleStepMode);

        removeAllWhitespace = new Checkbox(
                Localization.getString("sender.whitespace"),
                settings.isRemoveAllWhitespace());
        add(removeAllWhitespace);

        statusPollingEnabled = new Checkbox(
                Localization.getString("sender.status"),
                settings.isStatusUpdatesEnabled());
        add(statusPollingEnabled);

        statusPollRate = new Spinner(
                Localization.getString("sender.status.rate"),
                new SpinnerNumberModel((int)settings.getStatusUpdateRate(), 1, null, 100));
        add(statusPollRate);

        stateColorDisplayEnabled = new Checkbox(
                Localization.getString("sender.state"),
                settings.isDisplayStateColor());
        add(stateColorDisplayEnabled);

        convertArcsToLines = new Checkbox(
                Localization.getString("sender.arcs"),
                settings.isConvertArcsToLines());
        add(convertArcsToLines);

        smallArcLength = new Spinner(
                Localization.getString("sender.arcs.length"),
                new SpinnerNumberModel(settings.getSmallArcSegmentLength(), 1., null, .1));
        add(smallArcLength);

        autoConnect = new Checkbox(
                Localization.getString("sender.autoconnect"),
                settings.isAutoConnectEnabled());
        add(autoConnect);

        autoReconnect = new Checkbox(
                Localization.getString("sender.autoreconnect"),
                settings.isAutoReconnect());
        add(autoReconnect);

        languageCombo = new JComboBox(AvailableLanguages.getAvailableLanguages().toArray());
        for (int i = 0; i < languageCombo.getItemCount(); i++) {
            Language l = (Language)languageCombo.getItemAt(i);
            if (l.getLanguageCode().equals(settings.getLanguage())) {
                languageCombo.setSelectedIndex(i);
                break;
            }
        }
        add(languageCombo);
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

        Object getValue() { return spinner.getValue(); }
    }

    private class Checkbox extends JPanel {
        public JCheckBox box;
        public Checkbox(String text, boolean selected) {
            box = new JCheckBox(text, selected);
            setLayout(new MigLayout("insets 0"));
            add(box, "gapleft 50, w 100");
        }

        boolean getValue() { return box.isSelected(); }
    }

}
