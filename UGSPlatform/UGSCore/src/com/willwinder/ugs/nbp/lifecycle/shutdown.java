/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.lifecycle;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import org.openide.modules.OnStop;

/**
 *
 * @author wwinder
 */
@OnStop
public class shutdown implements Runnable {
    @Override
    public void run() {
        // Save settings.
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        SettingsFactory.saveSettings(settings);
    }
    
}
