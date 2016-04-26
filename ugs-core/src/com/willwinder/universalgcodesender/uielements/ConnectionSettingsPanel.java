/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.AvailableLanguages;
import com.willwinder.universalgcodesender.i18n.Language;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.Settings;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ConnectionSettingsPanel extends JPanel {
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

    public ConnectionSettingsPanel(Settings settings) {
        this.settings = settings;
        initComponents();
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
        settings.setSmallArcThreshold((double)smallArcThreshold.getValue());
        settings.setAutoConnectEnabled(autoConnect.getValue());
        settings.setAutoReconnect(autoReconnect.getValue());
        settings.setLanguage(((Language)languageCombo.getSelectedItem()).getLanguageCode());
    }

    private void initComponents() {
        setLayout(new MigLayout("wrap 1, debug", "grow, fill", "grow, fill"));

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

        smallArcThreshold = new Spinner(
                Localization.getString("sender.arcs.threshold"),
                new SpinnerNumberModel(settings.getSmallArcThreshold(), 1., null, .1));
        add(smallArcThreshold);

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

        languageCombo = new JComboBox(AvailableLanguages.getAvailableLanguages());
        add(languageCombo);
    }

    /**
     * Helper object to simplify layout.
     */
    private class Spinner extends JPanel {
        JLabel label;
        JSpinner spinner;
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
        JCheckBox box;
        public Checkbox(String text, boolean selected) {
            box = new JCheckBox(text, selected);
            setLayout(new MigLayout("insets 0"));
            add(box, "gapleft 50, w 100");
        }

        boolean getValue() { return box.isSelected(); }
    }

}
