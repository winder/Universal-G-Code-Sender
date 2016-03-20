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
package com.willwinder.ugs.nbp.core.options;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.ugs.nbp.lib.options.IChanged;
import com.willwinder.ugs.nbp.lib.options.OptionTable.Option;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author wwinder
 */
public class SenderOptionsPanel extends AbstractOptionsPanel {
    // This is a compatibility thing, because options aren't accessed as a map
    // I need a way to call each of the getter / setter methods. I'm going to
    // store the option objects in here and use the setting name as a key.
    HashMap<String, Option> loadMap;
    HashMap<String, Option> storeMap;

    public SenderOptionsPanel(IChanged controller) {
        super(controller);

        // LinkedHashMap to preserve insertion order.
        loadMap = new LinkedHashMap<>();
        storeMap = new LinkedHashMap<>();
        String key;
        Option op;

        // isOverrideSpeedSelected, setOverrideSpeedSelected, bool
        key = "sender.speed.override";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // setOverrideSpeedValue, getOverrideSpeedValue, double
        key = "sender.speed.percent";
        op = new Option<Double>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isSingleStepMode, setSingleStepMode, bool
        key = "sender.singlestep";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // getMaxCommandLength, setMaxCommandLength, int
        key = "sender.command.length";
        op = new Option<Integer>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // getTruncateDecimalLength, setTruncateDecimalLength, int
        key = "sender.truncate";
        op = new Option<Integer>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
        key = "sender.whitespace";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
        key = "sender.status";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // getStatusUpdateRate, setStatusUpdateRate, int
        key = "sender.status.rate";
        op = new Option<Integer>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isDisplayStateColor, setDisplayStateColor, bool
        key = "sender.state";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isConvertArcsToLines, setConvertArcsToLines, bool
        key = "sender.arcs";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // getSmallArcThreshold, setSmallArcThreshold, double
        key = "sender.arcs.threshold";
        op = new Option<Double>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // getSmallArcSegmentLength, setSmallArcSegmentLength, double
        key = "sender.arcs.length";
        op = new Option<Double>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isAutoConnectEnabled, setAutoConnect, bool
        key = "sender.autoconnect";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);

        // isAutoReconnect, setAutoReconnect, bool
        key = "sender.autoreconnect";
        op = new Option<Boolean>(localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.option, op);
    }

    private String localize(String s) {
        return Localization.getString(s);
    }

    @Override
    public void load() {
        clear();
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);

        // Update value in options.
        for (Entry<String,Option> entry : loadMap.entrySet()) {
            switch (entry.getKey()) {
                // isOverrideSpeedSelected, setOverrideSpeedSelected, bool
                case "sender.speed.override":
                    entry.getValue().setValue(settings.isOverrideSpeedSelected());
                    break;

                // setOverrideSpeedValue, getOverrideSpeedValue, double
                case "sender.speed.percent":
                    entry.getValue().setValue(settings.getOverrideSpeedValue());
                    break;

                // isSingleStepMode, setSingleStepMode, bool
                case "sender.singlestep":
                    entry.getValue().setValue(settings.isSingleStepMode());
                    break;

                // getMaxCommandLength, setMaxCommandLength, int
                case "sender.command.length":
                    entry.getValue().setValue(settings.getMaxCommandLength());
                    break;

                // getTruncateDecimalLength, setTruncateDecimalLength, int
                case "sender.truncate":
                    entry.getValue().setValue(settings.getTruncateDecimalLength());
                    break;

                // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
                case "sender.whitespace":
                    entry.getValue().setValue(settings.isRemoveAllWhitespace());
                    break;

                // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
                case "sender.status":
                    entry.getValue().setValue(settings.isStatusUpdatesEnabled());
                    break;

                // getStatusUpdateRate, setStatusUpdateRate, int
                case "sender.status.rate":
                    entry.getValue().setValue(settings.getStatusUpdateRate());
                    break;

                // isDisplayStateColor, setDisplayStateColor, bool
                case "sender.state":
                    entry.getValue().setValue(settings.isDisplayStateColor());
                    break;

                // isConvertArcsToLines, setConvertArcsToLines, bool
                case "sender.arcs":
                    entry.getValue().setValue(settings.isConvertArcsToLines());
                    break;

                // getSmallArcThreshold, setSmallArcThreshold, double
                case "sender.arcs.threshold":
                    entry.getValue().setValue(settings.getSmallArcThreshold());
                    break;

                // getSmallArcSegmentLength, setSmallArcSegmentLength, double
                case "sender.arcs.length":
                    entry.getValue().setValue(settings.getSmallArcSegmentLength());
                    break;

                // isAutoConnectEnabled, setAutoConnect, bool
                case "sender.autoconnect":
                    entry.getValue().setValue(settings.isAutoConnectEnabled());
                    break;

                // isAutoReconnect, setAutoReconnect, bool
                case "sender.autoreconnect":
                    entry.getValue().setValue(settings.isAutoReconnect());
                    break;

                default:
                    throw new IllegalArgumentException("Unknown option in sender options panel.");
            }

            add(entry.getValue());
        }
    }

    @Override
    public void store() {
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);

        // Update options
        for (int i = 0; i < optionTable.getModel().getRowCount(); i++) {
            String preference = (String) optionTable.getModel().getValueAt(i, 0);
            Option op = storeMap.get(preference);
            op.setValue(optionTable.getModel().getValueAt(i,1));
        }

        // Update value in options.
        for (Entry<String,Option> entry : loadMap.entrySet()) {
            switch (entry.getKey()) {
                // isOverrideSpeedSelected, setOverrideSpeedSelected, bool
                case "sender.speed.override":
                    Boolean value = (Boolean)entry.getValue().getValue();
                    settings.setOverrideSpeedSelected((Boolean)entry.getValue().getValue());
                    break;

                // setOverrideSpeedValue, getOverrideSpeedValue, double
                case "sender.speed.percent":
                    settings.setOverrideSpeedValue((Double)entry.getValue().getValue());
                    break;

                // isSingleStepMode, setSingleStepMode, bool
                case "sender.singlestep":
                    settings.setSingleStepMode((Boolean)entry.getValue().getValue());
                    break;

                // getMaxCommandLength, setMaxCommandLength, int
                case "sender.command.length":
                    settings.setMaxCommandLength((Integer)entry.getValue().getValue());
                    break;

                // getTruncateDecimalLength, setTruncateDecimalLength, int
                case "sender.truncate":
                    settings.setTruncateDecimalLength((Integer)entry.getValue().getValue());
                    break;

                // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
                case "sender.whitespace":
                    settings.setRemoveAllWhitespace((Boolean)entry.getValue().getValue());
                    break;

                // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
                case "sender.status":
                    settings.setStatusUpdatesEnabled((Boolean)entry.getValue().getValue());
                    break;

                // getStatusUpdateRate, setStatusUpdateRate, int
                case "sender.status.rate":
                    settings.setStatusUpdateRate((Integer)entry.getValue().getValue());
                    break;

                // isDisplayStateColor, setDisplayStateColor, bool
                case "sender.state":
                    settings.setDisplayStateColor((Boolean)entry.getValue().getValue());
                    break;

                // isConvertArcsToLines, setConvertArcsToLines, bool
                case "sender.arcs":
                    settings.setConvertArcsToLines((Boolean)entry.getValue().getValue());
                    break;

                // getSmallArcThreshold, setSmallArcThreshold, double
                case "sender.arcs.threshold":
                    settings.setSmallArcThreshold((Double)entry.getValue().getValue());
                    break;

                // getSmallArcSegmentLength, setSmallArcSegmentLength, double
                case "sender.arcs.length":
                    settings.setSmallArcSegmentLength((Double)entry.getValue().getValue());
                    break;

                // isAutoConnectEnabled, setAutoConnect, bool
                case "sender.autoconnect":
                    settings.setAutoConnectEnabled((Boolean)entry.getValue().getValue());
                    break;

                // isAutoReconnect, setAutoReconnect, bool
                case "sender.autoreconnect":
                    settings.setAutoReconnect((Boolean)entry.getValue().getValue());
                    break;

                default:
                    throw new IllegalArgumentException("Unknown option in sender options panel.");
            }
        }
        SettingsFactory.saveSettings(settings);
    }

    @Override
    public boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
