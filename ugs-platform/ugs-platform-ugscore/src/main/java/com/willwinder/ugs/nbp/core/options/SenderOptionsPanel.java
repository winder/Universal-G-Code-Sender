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
    private static final String SENDER_SPEED_OVERRIDE = "sender.speed.override";
    private static final String SENDER_SPEED_PERCENT = "sender.speed.percent";
    private static final String SENDER_SINGLESTEP = "sender.singlestep";
    private static final String SENDER_COMMAND_LENGTH = "sender.command.length";
    private static final String SENDER_TRUNCATE = "sender.truncate";
    private static final String SENDER_WHITESPACE = "sender.whitespace";
    private static final String SENDER_STATUS = "sender.status";
    private static final String SENDER_STATUS_RATE = "sender.status.rate";
    private static final String SENDER_STATE = "sender.state";
    private static final String SENDER_ARCS = "sender.arcs";
    private static final String SENDER_ARCS_THRESHOLD = "sender.arcs.threshold";
    private static final String SENDER_ARCS_LENGTH = "sender.arcs.length";
    private static final String SENDER_AUTOCONNECT = "sender.autoconnect";
    private static final String SENDER_AUTORECONNECT = "sender.autoreconnect";

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
        key = SENDER_SPEED_OVERRIDE;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // setOverrideSpeedValue, getOverrideSpeedValue, double
        key = SENDER_SPEED_PERCENT;
        op = new Option<Double>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isSingleStepMode, setSingleStepMode, bool
        key = SENDER_SINGLESTEP;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // getMaxCommandLength, setMaxCommandLength, int
        key = SENDER_COMMAND_LENGTH;
        op = new Option<Integer>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // getTruncateDecimalLength, setTruncateDecimalLength, int
        key = SENDER_TRUNCATE;
        op = new Option<Integer>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
        key = SENDER_WHITESPACE;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
        key = SENDER_STATUS;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // getStatusUpdateRate, setStatusUpdateRate, int
        key = SENDER_STATUS_RATE;
        op = new Option<Integer>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isDisplayStateColor, setDisplayStateColor, bool
        key = SENDER_STATE;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isConvertArcsToLines, setConvertArcsToLines, bool
        key = SENDER_ARCS;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // getSmallArcThreshold, setSmallArcThreshold, double
        key = SENDER_ARCS_THRESHOLD;
        op = new Option<Double>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // getSmallArcSegmentLength, setSmallArcSegmentLength, double
        key = SENDER_ARCS_LENGTH;
        op = new Option<Double>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isAutoConnectEnabled, setAutoConnect, bool
        key = SENDER_AUTOCONNECT;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);

        // isAutoReconnect, setAutoReconnect, bool
        key = SENDER_AUTORECONNECT;
        op = new Option<Boolean>(key, localize(key), "", null);
        loadMap.put(key, op);
        storeMap.put(op.localized, op);
    }

    private String localize(String s) {
        return Localization.getString(s);
    }

    @Override
    public void load() {
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);

        // Update value in options.
        for (Entry<String,Option> entry : loadMap.entrySet()) {
            switch (entry.getKey()) {
                // isOverrideSpeedSelected, setOverrideSpeedSelected, bool
                case SENDER_SPEED_OVERRIDE:
                    entry.getValue().setValue(settings.isOverrideSpeedSelected());
                    break;

                // setOverrideSpeedValue, getOverrideSpeedValue, double
                case SENDER_SPEED_PERCENT:
                    entry.getValue().setValue(settings.getOverrideSpeedValue());
                    break;

                // isSingleStepMode, setSingleStepMode, bool
                case SENDER_SINGLESTEP:
                    entry.getValue().setValue(settings.isSingleStepMode());
                    break;

                // getMaxCommandLength, setMaxCommandLength, int
                case SENDER_COMMAND_LENGTH:
                    entry.getValue().setValue(settings.getMaxCommandLength());
                    break;

                // getTruncateDecimalLength, setTruncateDecimalLength, int
                case SENDER_TRUNCATE:
                    entry.getValue().setValue(settings.getTruncateDecimalLength());
                    break;

                // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
                case SENDER_WHITESPACE:
                    entry.getValue().setValue(settings.isRemoveAllWhitespace());
                    break;

                // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
                case SENDER_STATUS:
                    entry.getValue().setValue(settings.isStatusUpdatesEnabled());
                    break;

                // getStatusUpdateRate, setStatusUpdateRate, int
                case SENDER_STATUS_RATE:
                    entry.getValue().setValue(settings.getStatusUpdateRate());
                    break;

                // isDisplayStateColor, setDisplayStateColor, bool
                case SENDER_STATE:
                    entry.getValue().setValue(settings.isDisplayStateColor());
                    break;

                // isConvertArcsToLines, setConvertArcsToLines, bool
                case SENDER_ARCS:
                    entry.getValue().setValue(settings.isConvertArcsToLines());
                    break;

                // getSmallArcThreshold, setSmallArcThreshold, double
                case SENDER_ARCS_THRESHOLD:
                    entry.getValue().setValue(settings.getSmallArcThreshold());
                    break;

                // getSmallArcSegmentLength, setSmallArcSegmentLength, double
                case SENDER_ARCS_LENGTH:
                    entry.getValue().setValue(settings.getSmallArcSegmentLength());
                    break;

                // isAutoConnectEnabled, setAutoConnect, bool
                case SENDER_AUTOCONNECT:
                    entry.getValue().setValue(settings.isAutoConnectEnabled());
                    break;

                // isAutoReconnect, setAutoReconnect, bool
                case SENDER_AUTORECONNECT:
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

        if (storeMap.size() != optionTable.getModel().getRowCount()) {
            return;
        }

        // Update options
        for (int i = 0; i < optionTable.getModel().getRowCount(); i++) {
            String preference = (String) optionTable.getModel().getValueAt(i, 0);
            Option op = storeMap.get(preference);
            Object val = optionTable.getModel().getValueAt(i,1);
            op.setValue(val);
        }

        // Update value in options.
        for (Entry<String,Option> entry : loadMap.entrySet()) {
            switch (entry.getKey()) {
                // isOverrideSpeedSelected, setOverrideSpeedSelected, bool
                case SENDER_SPEED_OVERRIDE:
                    Boolean value = (Boolean)entry.getValue().getValue();
                    settings.setOverrideSpeedSelected((Boolean)entry.getValue().getValue());
                    break;

                // setOverrideSpeedValue, getOverrideSpeedValue, double
                case SENDER_SPEED_PERCENT:
                    settings.setOverrideSpeedValue((Double)entry.getValue().getValue());
                    break;

                // isSingleStepMode, setSingleStepMode, bool
                case SENDER_SINGLESTEP:
                    settings.setSingleStepMode((Boolean)entry.getValue().getValue());
                    break;

                // getMaxCommandLength, setMaxCommandLength, int
                case SENDER_COMMAND_LENGTH:
                    settings.setMaxCommandLength((Integer)entry.getValue().getValue());
                    break;

                // getTruncateDecimalLength, setTruncateDecimalLength, int
                case SENDER_TRUNCATE:
                    settings.setTruncateDecimalLength((Integer)entry.getValue().getValue());
                    break;

                // isRemoveAllWhitespace, setRemoveAllWhitespace, bool
                case SENDER_WHITESPACE:
                    settings.setRemoveAllWhitespace((Boolean)entry.getValue().getValue());
                    break;

                // isStatusUpdatesEnabled, setStatusUpdatesEnabled, bool
                case SENDER_STATUS:
                    settings.setStatusUpdatesEnabled((Boolean)entry.getValue().getValue());
                    break;

                // getStatusUpdateRate, setStatusUpdateRate, int
                case SENDER_STATUS_RATE:
                    settings.setStatusUpdateRate((Integer)entry.getValue().getValue());
                    break;

                // isDisplayStateColor, setDisplayStateColor, bool
                case SENDER_STATE:
                    settings.setDisplayStateColor((Boolean)entry.getValue().getValue());
                    break;

                // isConvertArcsToLines, setConvertArcsToLines, bool
                case SENDER_ARCS:
                    settings.setConvertArcsToLines((Boolean)entry.getValue().getValue());
                    break;

                // getSmallArcThreshold, setSmallArcThreshold, double
                case SENDER_ARCS_THRESHOLD:
                    settings.setSmallArcThreshold((Double)entry.getValue().getValue());
                    break;

                // getSmallArcSegmentLength, setSmallArcSegmentLength, double
                case SENDER_ARCS_LENGTH:
                    settings.setSmallArcSegmentLength((Double)entry.getValue().getValue());
                    break;

                // isAutoConnectEnabled, setAutoConnect, bool
                case SENDER_AUTOCONNECT:
                    settings.setAutoConnectEnabled((Boolean)entry.getValue().getValue());
                    break;

                // isAutoReconnect, setAutoReconnect, bool
                case SENDER_AUTORECONNECT:
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
