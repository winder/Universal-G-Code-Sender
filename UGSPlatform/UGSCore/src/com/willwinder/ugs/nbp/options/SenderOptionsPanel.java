/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.options;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.ugs.nbp.options.OptionTable.BoolOption;
import com.willwinder.ugs.nbp.options.OptionTable.DoubleOption;
import com.willwinder.ugs.nbp.options.OptionTable.IntOption;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.Settings;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wwinder
 */
public class SenderOptionsPanel extends AbstractOptionsPanel {
    SenderOptionsPanelController controller;
    public SenderOptionsPanel(SenderOptionsPanelController controller) {
        this.controller = controller;
    }

    private String localize(String s) {
        return Localization.getString(s);
    }

    void load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(SenderPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(SenderPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);

        // isOverrideSpeedSelected, setOverrideSpeedSelected, bool
        add(new BoolOption(localize("sender.speed.override"), "", settings.isOverrideSpeedSelected()));
        // setOverrideSpeedValue, getOverrideSpeedValue, double
        add(new DoubleOption(localize("sender.speed.percent"), "", settings.getOverrideSpeedValue()));
        // isSingleStepMode, setSingleStepMode, bool
        add(new BoolOption(localize("sender.singlestep"), "", settings.isSingleStepMode()));
        // getMaxCommandLength, setMaxCommandLength, int
        add(new IntOption(localize("sender.command.length"), "", settings.getMaxCommandLength()));
        // getTruncateDecimalLength, setTruncateDecimalLength, int
        add(new IntOption(localize("sender.truncate"), "", settings.getTruncateDecimalLength()));
        // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
        add(new BoolOption(localize("sender.whitespace"), "", settings.isRemoveAllWhitespace()));
        // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
        add(new BoolOption(localize("sender.status"), "", settings.isStatusUpdatesEnabled()));
        // getStatusUpdateRate, setStatusUpdateRate, int
        add(new IntOption(localize("sender.status.rate"), "", settings.getStatusUpdateRate()));
        // isDisplayStateColor, setDisplayStateColor, bool
        add(new BoolOption(localize("sender.state"), "", settings.isDisplayStateColor()));
        // isConvertArcsToLines, setConvertArcsToLines, bool
        add(new BoolOption(localize("sender.arcs"), "", settings.isConvertArcsToLines()));
        // getSmallArcThreshold, setSmallArcThreshold, double
        add(new DoubleOption(localize("sender.arcs.threshold"), "", settings.getSmallArcThreshold()));
        // getSmallArcSegmentLength, setSmallArcSegmentLength, double
        add(new DoubleOption(localize("sender.arcs.length"), "", settings.getSmallArcSegmentLength()));
        // isAutoConnectEnabled, setAutoConnect, bool
        add(new BoolOption(localize("sender.autoconnect"), "", settings.isAutoConnectEnabled()));
        // isAutoReconnect, setAutoReconnect, bool
        add(new BoolOption(localize("sender.autoreconnect"), "", settings.isAutoReconnect()));
    }

    void store() {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(SenderPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(SenderPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
