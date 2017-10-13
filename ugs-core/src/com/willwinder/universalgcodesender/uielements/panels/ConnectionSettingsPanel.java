/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.i18n.AvailableLanguages;
import com.willwinder.universalgcodesender.i18n.Language;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import javax.swing.JComboBox;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ConnectionSettingsPanel extends AbstractUGSSettings {

    final Checkbox verboseConsoleOutput = new Checkbox(
                Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));
    final Checkbox useZStepSize = new Checkbox(
                Localization.getString("sender.step.separateZ"));
    final Checkbox singleStepMode = new Checkbox(
                Localization.getString("sender.singlestep"));
    final Checkbox statusPollingEnabled = new Checkbox(
                Localization.getString("sender.status"));
    final Spinner statusPollRate = new Spinner(
                Localization.getString("sender.status.rate"),
                new SpinnerNumberModel((int)1, 1, null, 100));
    final Checkbox stateColorDisplayEnabled = new Checkbox(
                Localization.getString("sender.state"));
    final Checkbox showNightlyWarning = new Checkbox(
                Localization.getString("sender.nightly-warning"));
    final JComboBox languageCombo = new JComboBox(AvailableLanguages.getAvailableLanguages().toArray());

    public ConnectionSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
        super.updateComponents();
    }

    public ConnectionSettingsPanel(Settings settings) {
        this(settings, null);
    }

    @Override
    public String getHelpMessage() {
        StringBuilder message = new StringBuilder()
                .append(Localization.getString("sender.help.verbose.console")).append("\n\n")
                .append(Localization.getString("sender.help.singlestep")).append("\n\n")
                .append(Localization.getString("sender.help.status")).append("\n\n")
                .append(Localization.getString("sender.help.status.rate")).append("\n\n")
                .append(Localization.getString("sender.help.state")).append("\n\n")
                ;
        return message.toString();
    }

    @Override
    public void save() {
        settings.setVerboseOutputEnabled(verboseConsoleOutput.getValue());
        settings.setUseZStepSize(useZStepSize.getValue());
        settings.setSingleStepMode(singleStepMode.getValue());
        settings.setStatusUpdatesEnabled(statusPollingEnabled.getValue());
        settings.setStatusUpdateRate((int)statusPollRate.getValue());
        settings.setDisplayStateColor(stateColorDisplayEnabled.getValue());
        //settings.setAutoConnectEnabled(autoConnect.getValue());
        settings.setShowNightlyWarning(showNightlyWarning.getValue());
        settings.setLanguage(((Language)languageCombo.getSelectedItem()).getLanguageCode());
    }

    @Override
    public void restoreDefaults() throws Exception {
        updateComponents(new Settings());
        SettingsFactory.saveSettings(settings);
        save();
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();

        setLayout(new MigLayout("wrap 1", "grow, fill", "grow, fill"));

        verboseConsoleOutput.setSelected(s.isVerboseOutputEnabled());
        add(verboseConsoleOutput);

        useZStepSize.setSelected(s.useZStepSize());
        add(useZStepSize);

        singleStepMode.setSelected(s.isSingleStepMode());
        add(singleStepMode);

        statusPollingEnabled.setSelected(s.isStatusUpdatesEnabled());
        add(statusPollingEnabled);

        statusPollRate.setValue((int)s.getStatusUpdateRate());
        add(statusPollRate);

        stateColorDisplayEnabled.setSelected(s.isDisplayStateColor());
        add(stateColorDisplayEnabled);

        showNightlyWarning.setSelected(s.isShowNightlyWarning());
        add(showNightlyWarning);

        for (int i = 0; i < languageCombo.getItemCount(); i++) {
            Language l = (Language)languageCombo.getItemAt(i);
            if (l.getLanguageCode().equals(s.getLanguage())) {
                languageCombo.setSelectedIndex(i);
                break;
            }
        }
        add(languageCombo);
    }
}
