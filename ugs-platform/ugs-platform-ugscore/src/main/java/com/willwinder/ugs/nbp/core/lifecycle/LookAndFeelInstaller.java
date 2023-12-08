/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.core.lifecycle;

import org.apache.commons.lang3.SystemUtils;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;

/**
 * Sets the look and feel (if not already set)
 */
public class LookAndFeelInstaller extends ModuleInstall {

    @Override
    public void validate() {
        if (SystemUtils.IS_OS_MAC_OSX) {
            // FlatlLaf look and feel doesn't work on MacOSXs
            setMacOSLaF();
        } else {
            setLaF();
        }
    }

    private void setLaF() {
        Preferences prefs = NbPreferences.root().node("laf");
        if (prefs.get("laf", "").isBlank()) {
            prefs.put("laf", "com.formdev.flatlaf.FlatLightLaf");
        }
    }

    private void setMacOSLaF() {
        Preferences prefs = NbPreferences.root().node("laf");
        String currentLaF = prefs.get("laf", "");
        if (currentLaF.isBlank() || currentLaF.equals("com.formdev.flatlaf.FlatLightLaf")) {
            prefs.put("laf", "com.apple.laf.AquaLookAndFeel");
        }
    }
}
