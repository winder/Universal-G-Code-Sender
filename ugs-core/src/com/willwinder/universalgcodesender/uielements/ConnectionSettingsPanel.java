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
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import javax.swing.JComboBox;
import javax.swing.SpinnerNumberModel;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class ConnectionSettingsPanel extends AbstractUGSSettings {

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
    final Checkbox statusPollingEnabled = new Checkbox(
                Localization.getString("sender.status"));
    final Spinner statusPollRate = new Spinner(
                Localization.getString("sender.status.rate"),
                new SpinnerNumberModel((int)1, 1, null, 100));
    final Checkbox stateColorDisplayEnabled = new Checkbox(
                Localization.getString("sender.state"));
    final Spinner smallArcLength = new Spinner(
                Localization.getString("sender.arcs.length"),
                new SpinnerNumberModel(1., .0001, null, .1));
    final Checkbox autoConnect = new Checkbox(
                Localization.getString("sender.autoconnect"));
    final Checkbox autoReconnect = new Checkbox(
                Localization.getString("sender.autoreconnect"));
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
                .append(Localization.getString("sender.help.speed.override")).append("\n\n")
                .append(Localization.getString("sender.help.speed.percent")).append("\n\n")
                .append(Localization.getString("sender.help.command.length")).append("\n\n")
                .append(Localization.getString("sender.help.truncate")).append("\n\n")
                .append(Localization.getString("sender.help.singlestep")).append("\n\n")
                .append(Localization.getString("sender.help.status")).append("\n\n")
                .append(Localization.getString("sender.help.status.rate")).append("\n\n")
                .append(Localization.getString("sender.help.state")).append("\n\n")
                .append(Localization.getString("sender.help.arcs.length")).append("\n\n")
                .append(Localization.getString("sender.help.autoconnect"))
                //.append(Localization.getString("sender.help.autoreconnect"))
                ;
        return message.toString();
    }

    @Override
    public void save() {
        settings.setOverrideSpeedValue(new Double((int)overrideSpeedPercent.getValue()));
        settings.setMaxCommandLength((int)maxCommandLength.getValue());
        settings.setTruncateDecimalLength((int)truncateDecimalDigits.getValue());
        settings.setSingleStepMode(singleStepMode.getValue());
        settings.setStatusUpdatesEnabled(statusPollingEnabled.getValue());
        settings.setStatusUpdateRate((int)statusPollRate.getValue());
        settings.setDisplayStateColor(stateColorDisplayEnabled.getValue());
        settings.setSmallArcSegmentLength((double)smallArcLength.getValue());
        settings.setAutoConnectEnabled(autoConnect.getValue());
        settings.setAutoReconnect(autoReconnect.getValue());
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

        overrideSpeedPercent.setValue((int)s.getOverrideSpeedValue());
        add(overrideSpeedPercent);

        maxCommandLength.setValue(s.getMaxCommandLength());
        add(maxCommandLength);

        truncateDecimalDigits.setValue((int)s.getTruncateDecimalLength());
        add(truncateDecimalDigits);

        singleStepMode.setSelected(s.isSingleStepMode());
        add(singleStepMode);

        statusPollingEnabled.setSelected(s.isStatusUpdatesEnabled());
        add(statusPollingEnabled);

        statusPollRate.setValue((int)s.getStatusUpdateRate());
        add(statusPollRate);

        stateColorDisplayEnabled.setSelected(s.isDisplayStateColor());
        add(stateColorDisplayEnabled);

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
    }
}
