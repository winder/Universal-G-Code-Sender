/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;

/**
 *
 * @author wwinder
 */
public class AutoLevelerSettingsPanel extends AbstractUGSSettings {

    public AutoLevelerSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
    }

    @Override
    public void save() {
    }

    @Override
    public String getHelpMessage() {
        return "1234";
    }

    @Override
    public void restoreDefaults() throws Exception {
    }
}
