/*
    Copyright 2016-2021 Will Winder

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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;

import static com.willwinder.ugs.nbp.core.services.JogSizeAction.Operation.*;

/**
 * A service for registering jog actions such as menu items and shortcuts
 *
 * @author wwinder
 */
@ServiceProvider(service=JogActionService.class) 
public class JogActionService {

    public JogActionService() {
        initActions();
    }

    /**
     * Create the actions, this makes them available for keymapping and makes
     * them usable from the drop down menu's.
     */
    private void initActions() {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);

        try {
            String localized = String.format("Menu/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.jog"));
            String category = "Machine";
            String menuPath = "Menu/" + category + "/Jog";

            // Jog plus/minus X
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xPlus", Localization.getString("jogging.xPlus") ,
                    category, "M-RIGHT" , menuPath, 0, localized, new JogAction(1, 0, 0, 0, 0, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xMinus", Localization.getString("jogging.xMinus"),
                    category, "M-LEFT"  , menuPath, 0, localized, new JogAction(-1, 0, 0, 0, 0, 0));
            // Jog plus/minus Y
            ars.registerAction(JogActionService.class.getCanonicalName() + ".yPlus", Localization.getString("jogging.yPlus") ,
                    category, "M-UP"    , menuPath, 0, localized, new JogAction(0, 1, 0, 0, 0,0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".yMinus", Localization.getString("jogging.yMinus"),
                    category, "M-DOWN"  , menuPath, 0, localized, new JogAction( 0,-1, 0,0 ,0 ,0));
            // Jog plus/minus Z
            ars.registerAction(JogActionService.class.getCanonicalName() + ".zPlus", Localization.getString("jogging.zPlus") ,
                    category, "SM-UP"   , menuPath, 0, localized, new JogAction(0, 0, 1, 0,0, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".zMinus", Localization.getString("jogging.zMinus"),
                    category, "SM-DOWN" , menuPath, 0, localized, new JogAction(0, 0,-1, 0,0,0));
            // Jog plus/minus A
            ars.registerAction(JogActionService.class.getCanonicalName() + ".aPlus", Localization.getString("jogging.aPlus") ,
                    category, null   , menuPath, 0, localized, new JogAction(0, 0, 0, 1, 0, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".aMinus", Localization.getString("jogging.aMinus"),
                    category, null , menuPath, 0, localized, new JogAction(0, 0, 0, -1, 0, 0));
            // Jog plus/minus B
            ars.registerAction(JogActionService.class.getCanonicalName() + ".bPlus", Localization.getString("jogging.bPlus") ,
                    category, null , menuPath, 0, localized, new JogAction(0, 0, 0, 0, 1, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".bMinus", Localization.getString("jogging.bMinus"),
                    category, null , menuPath, 0, localized, new JogAction(0, 0, 0, 0, -1, 0));
            // Jog plus/minus C
            ars.registerAction(JogActionService.class.getCanonicalName() + ".cPlus", Localization.getString("jogging.cPlus") ,
                    category, null , menuPath, 0, localized, new JogAction(0, 0, 0, 0, 0, 1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".cMinus", Localization.getString("jogging.cMinus"),
                    category, null , menuPath, 0, localized, new JogAction(0, 0, 0, 0, 0, -1));

            // Jog Diagonals
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xPlus.yPlus", Localization.getString("jogging.xPlus.yPlus") ,
                    category, null , menuPath, 0, localized, new JogAction( 1, 1, 0,0,0,0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xPlus.yMinus", Localization.getString("jogging.xPlus.yMinus") ,
                    category, null , menuPath, 0, localized, new JogAction( 1, -1,0,0,0,0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xMinus.yMinus", Localization.getString("jogging.xMinus.yMinus") ,
                    category, null , menuPath, 0, localized, new JogAction(-1, -1,0 ,0,0,0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xMinus.yPlus", Localization.getString("jogging.xMinus.yPlus") ,
                    category, null , menuPath, 0, localized, new JogAction( -1, 1, 0,0,0,0));

            localized = String.format("Menu/%s/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.jog"),
                    Localization.getString("platform.menu.jog.size"));
            menuPath = menuPath + "/Step Size";

            // Set Step Size XY
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.10", "XY 10",
                    category, "" , menuPath, 0, localized, new JogSizeAction(10, JogSizeAction.StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.1", "XY 1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(1, JogSizeAction.StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.01", "XY 0.1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.1, JogSizeAction.StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.001", "XY 0.01",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.01, JogSizeAction.StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.0001", "XY 0.001",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.001, JogSizeAction.StepType.XY));

            // Set Step Size Z
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.10", "Z 10",
                    category, "" , menuPath, 0, localized, new JogSizeAction(10, JogSizeAction.StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.1", "Z 1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(1, JogSizeAction.StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.01", "Z 0.1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.1, JogSizeAction.StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.001", "Z 0.01",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.01, JogSizeAction.StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.0001", "Z 0.001",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.001, JogSizeAction.StepType.Z));

            // Set Step Size ABC
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.10", "ABC 10",
                    category, "" , menuPath, 0, localized, new JogSizeAction(10, JogSizeAction.StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.1", "ABC 1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(1, JogSizeAction.StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.01", "ABC 0.1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.1, JogSizeAction.StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.001", "ABC 0.01",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.01, JogSizeAction.StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.0001", "ABC 0.001",
                    category, "" , menuPath, 0, localized, new JogSizeAction(0.001, JogSizeAction.StepType.ABC));

            // Modify Step Size XY
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide", Localization.getString("jogging.divide"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPXY_DIVIDE));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply", Localization.getString("jogging.multiply"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPXY_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease", Localization.getString("jogging.decrease"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPXY_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase", Localization.getString("jogging.increase"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPXY_PLUS));

            // Modify Step Size Z
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide.z", Localization.getString("jogging.divide.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPZ_DIVIDE));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply.z", Localization.getString("jogging.multiply.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPZ_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease.z", Localization.getString("jogging.decrease.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPZ_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase.z", Localization.getString("jogging.increase.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPZ_PLUS));

            // Modify Step Size ABC
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide.abc", Localization.getString("jogging.divide.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPABC_DIVIDE));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply.abc", Localization.getString("jogging.multiply.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPABC_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease.abc", Localization.getString("jogging.decrease.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPABC_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase.abc", Localization.getString("jogging.increase.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(STEPABC_PLUS));

            // Feed Rate
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease.feed", Localization.getString("jogging.decrease.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(FEED_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase.feed", Localization.getString("jogging.increase.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(FEED_PLUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply.feed", Localization.getString("jogging.multiply.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(FEED_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide.feed", Localization.getString("jogging.divide.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(FEED_DIVIDE));

            // Units
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".inch", Localization.getString("jogging.units.inch"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(Units.INCH));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".mm", Localization.getString("jogging.units.mm"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(Units.MM));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".toggle", Localization.getString("jogging.units.toggle"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(UNITS_TOGGLE));

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
