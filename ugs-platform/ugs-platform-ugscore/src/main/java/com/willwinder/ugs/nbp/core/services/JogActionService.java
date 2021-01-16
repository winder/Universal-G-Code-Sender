/*
    Copyright 2016-2019 Will Winder

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

import static com.willwinder.ugs.nbp.core.services.JogActionService.Operation.*;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.services.JogService;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * A service for registering jog actions such as menu items and shortcuts
 *
 * @author wwinder
 */
@ServiceProvider(service=JogActionService.class) 
public class JogActionService {
    private JogService jogService;

    public JogActionService() {
        jogService = CentralLookup.getDefault().lookup(JogService.class);
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
                    category, "M-RIGHT" , menuPath, 0, localized, new JogAction(jogService, 1, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xMinus", Localization.getString("jogging.xMinus"),
                    category, "M-LEFT"  , menuPath, 0, localized, new JogAction(jogService,-1, 0));
            // Jog plus/minus Y
            ars.registerAction(JogActionService.class.getCanonicalName() + ".yPlus", Localization.getString("jogging.yPlus") ,
                    category, "M-UP"    , menuPath, 0, localized, new JogAction(jogService, 0, 1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".yMinus", Localization.getString("jogging.yMinus"),
                    category, "M-DOWN"  , menuPath, 0, localized, new JogAction(jogService, 0,-1));
            // Jog plus/minus Z
            ars.registerAction(JogActionService.class.getCanonicalName() + ".zPlus", Localization.getString("jogging.zPlus") ,
                    category, "SM-UP"   , menuPath, 0, localized, new JogAction(jogService, 1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".zMinus", Localization.getString("jogging.zMinus"),
                    category, "SM-DOWN" , menuPath, 0, localized, new JogAction(jogService, -1));
            // Jog plus/minus A
            ars.registerAction(JogActionService.class.getCanonicalName() + ".aPlus", Localization.getString("jogging.aPlus") ,
                    category, null   , menuPath, 0, localized, new JogAction(jogService, 0, 0, 0, 1, 0, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".aMinus", Localization.getString("jogging.aMinus"),
                    category, null , menuPath, 0, localized, new JogAction(jogService, 0, 0, 0, -1, 0, 0));
            // Jog plus/minus B
            ars.registerAction(JogActionService.class.getCanonicalName() + ".bPlus", Localization.getString("jogging.bPlus") ,
                    category, null , menuPath, 0, localized, new JogAction(jogService, 0, 0, 0, 0, 1, 0));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".bMinus", Localization.getString("jogging.bMinus"),
                    category, null , menuPath, 0, localized, new JogAction(jogService, 0, 0, 0, 0, -1, 0));
            // Jog plus/minus C
            ars.registerAction(JogActionService.class.getCanonicalName() + ".cPlus", Localization.getString("jogging.cPlus") ,
                    category, null , menuPath, 0, localized, new JogAction(jogService, 0, 0, 0, 0, 0, 1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".cMinus", Localization.getString("jogging.cMinus"),
                    category, null , menuPath, 0, localized, new JogAction(jogService, 0, 0, 0, 0, 0, -1));

            // Jog Diagonals
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xPlus.yPlus", Localization.getString("jogging.xPlus.yPlus") ,
                    category, null , menuPath, 0, localized, new JogAction(jogService, 1, 1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xPlus.yMinus", Localization.getString("jogging.xPlus.yMinus") ,
                    category, null , menuPath, 0, localized, new JogAction(jogService, 1, -1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xMinus.yMinus", Localization.getString("jogging.xMinus.yMinus") ,
                    category, null , menuPath, 0, localized, new JogAction(jogService, -1, -1));
            ars.registerAction(JogActionService.class.getCanonicalName() + ".xMinus.yPlus", Localization.getString("jogging.xMinus.yPlus") ,
                    category, null , menuPath, 0, localized, new JogAction(jogService, -1, 1));

            localized = String.format("Menu/%s/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.jog"),
                    Localization.getString("platform.menu.jog.size"));
            menuPath = menuPath + "/Step Size";

            // Set Step Size XY
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.10", "XY 10",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 10, StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.1", "XY 1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 1, StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.01", "XY 0.1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.1, StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.001", "XY 0.01",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.01, StepType.XY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "xy.0001", "XY 0.001",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.001, StepType.XY));

            // Set Step Size Z
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.10", "Z 10",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 10, StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.1", "Z 1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 1, StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.01", "Z 0.1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.1, StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.001", "Z 0.01",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.01, StepType.Z));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "z.0001", "Z 0.001",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.001, StepType.Z));

            // Set Step Size ABC
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.10", "ABC 10",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 10, StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.1", "ABC 1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 1, StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.01", "ABC 0.1",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.1, StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.001", "ABC 0.01",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.01, StepType.ABC));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + "abc.0001", "ABC 0.001",
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, 0.001, StepType.ABC));

            // Modify Step Size XY
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide", Localization.getString("jogging.divide"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPXY_DIVIDE));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply", Localization.getString("jogging.multiply"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPXY_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease", Localization.getString("jogging.decrease"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPXY_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase", Localization.getString("jogging.increase"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPXY_PLUS));

            // Modify Step Size Z
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide.z", Localization.getString("jogging.divide.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPZ_DIVIDE));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply.z", Localization.getString("jogging.multiply.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPZ_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease.z", Localization.getString("jogging.decrease.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPZ_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase.z", Localization.getString("jogging.increase.z"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPZ_PLUS));

            // Modify Step Size ABC
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide.abc", Localization.getString("jogging.divide.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPABC_DIVIDE));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply.abc", Localization.getString("jogging.multiply.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPABC_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease.abc", Localization.getString("jogging.decrease.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPABC_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase.abc", Localization.getString("jogging.increase.abc"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, STEPABC_PLUS));

            // Feed Rate
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".decrease.feed", Localization.getString("jogging.decrease.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, FEED_MINUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".increase.feed", Localization.getString("jogging.increase.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, FEED_PLUS));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".multiply.feed", Localization.getString("jogging.multiply.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, FEED_MULTIPLY));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".divide.feed", Localization.getString("jogging.divide.feed"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, FEED_DIVIDE));

            // Units
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".inch", Localization.getString("jogging.units.inch"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, Units.INCH));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".mm", Localization.getString("jogging.units.mm"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, Units.MM));
            ars.registerAction(JogSizeAction.class.getCanonicalName() + ".toggle", Localization.getString("jogging.units.toggle"),
                    category, "" , menuPath, 0, localized, new JogSizeAction(jogService, UNITS_TOGGLE));

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected enum Operation {
      STEPXY_PLUS,
      STEPXY_MINUS,
      STEPXY_MULTIPLY,
      STEPXY_DIVIDE,
      STEPZ_PLUS,
      STEPZ_MINUS,
      STEPZ_MULTIPLY,
      STEPZ_DIVIDE,
      STEPABC_PLUS,
      STEPABC_MINUS,
      STEPABC_MULTIPLY,
      STEPABC_DIVIDE,
      FEED_PLUS,
      FEED_MINUS,
      FEED_MULTIPLY,
      FEED_DIVIDE,
      UNITS_TOGGLE
    }

    protected enum StepType {
        XY,
        Z,
        ABC
    }

    protected class JogSizeAction extends AbstractAction {
        private JogService js;
        private Double size = null;
        private Operation operation = null;
        private Units unit = null;
        private StepType type = null;

        public JogSizeAction(JogService service, Units u) {
            js = service;
            unit = u;
        }
        public JogSizeAction(JogService service, Operation op) {
            js = service;
            operation = op;
        }

        public JogSizeAction(JogService service, double size, StepType type) {
            js = service;
            this.size = size;
            this.type = type;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (size != null) {
                switch (type) {
                    case XY:
                        js.setStepSizeXY(size);
                        break;
                    case Z:
                        js.setStepSizeZ(size);
                        break;
                    case ABC:
                        js.setStepSizeABC(size);
                        break;
                }
            }
            else if (operation != null) {
                switch (operation) {
                    case STEPXY_MULTIPLY:
                        js.multiplyXYStepSize();
                        break;
                    case STEPXY_DIVIDE:
                        js.divideXYStepSize();
                        break;
                    case STEPXY_PLUS:
                        js.increaseXYStepSize();
                        break;
                    case STEPXY_MINUS:
                        js.decreaseXYStepSize();
                        break;
                    case STEPZ_MULTIPLY:
                        js.multiplyZStepSize();
                        break;
                    case STEPZ_DIVIDE:
                        js.divideZStepSize();
                        break;
                    case STEPZ_PLUS:
                        js.increaseZStepSize();
                        break;
                    case STEPZ_MINUS:
                        js.decreaseZStepSize();
                        break;
                    case STEPABC_MULTIPLY:
                        js.multiplyABCStepSize();
                        break;
                    case STEPABC_DIVIDE:
                        js.divideABCStepSize();
                        break;
                    case STEPABC_PLUS:
                        js.increaseABCStepSize();
                        break;
                    case STEPABC_MINUS:
                        js.decreaseABCStepSize();
                        break;
                    case FEED_PLUS:
                        js.increaseFeedRate();
                        break;
                    case FEED_MINUS:
                        js.decreaseFeedRate();
                        break;
                    case FEED_MULTIPLY:
                        js.multiplyFeedRate();
                        break;
                    case FEED_DIVIDE:
                        js.divideFeedRate();
                        break;
                    case UNITS_TOGGLE:
                        js.setUnits(js.getUnits() == Units.MM ? Units.INCH : Units.MM);
                        break;
                    default:
                        break;
                }
            } else if (unit != null) {
                js.setUnits(unit);
            }
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    protected class JogAction extends AbstractAction {
        private JogService js;
        private int x,y,z,a,b,c;

        public JogAction(JogService service, int x, int y, int z, int a, int b, int c) {
            js = service;
            this.x = x;
            this.y = y;
            this.z = z;
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public JogAction(JogService service, int x, int y) {
            this(service, x, y, 0, 0, 0, 0);
        }

        public JogAction(JogService service, int z) {
            this(service, 0, 0, z, 0, 0, 0);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (z != 0) {
                js.adjustManualLocationZ(z);
            } else if (x != 0 || y != 0) {
                js.adjustManualLocationXY(x, y);
            } else if (a != 0 || b != 0 || c != 0) {
                js.adjustManualLocationABC(a, b, c);
            }
        }

        @Override
        public boolean isEnabled() {
            return js.canJog();
        }
    }
}
