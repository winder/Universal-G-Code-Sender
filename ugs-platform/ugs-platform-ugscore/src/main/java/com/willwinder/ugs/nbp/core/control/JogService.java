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
package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils.Units;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import static org.openide.util.NbBundle.getMessage;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=JogService.class, position=1) 
//@ServiceProvider(service = StatusLineElementProvider.class, position=1)
public class JogService implements StatusLineElementProvider {
    private double stepSize = 1;
    private Units units;

    private BackendAPI backend;

    public JogService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        String abbr = backend.getSettings().getDefaultUnits();
        this.setUnits(Units.getUnit(abbr));

        initActions();
    }

    private void changed() {
        NbPreferences.forModule(JogService.class).put("changed", UUID.randomUUID().toString());
    }

    @Override
    public Component getStatusLineElement() {
        return null;
    }

    public void increaseStepSize() {
        if (stepSize >= 1) {
            stepSize++;
        } else if (stepSize >= 0.1) {
            stepSize = stepSize + 0.1;
        } else if (stepSize >= 0.01) {
            stepSize = stepSize + 0.01;
        } else {
            stepSize = 0.01;
        }
        changed();
    }

    public void decreaseStepSize() {
        if (stepSize > 1) {
            stepSize--;
        } else if (stepSize > 0.1) {
            stepSize = stepSize - 0.1;
        } else if (stepSize > 0.01) {
            stepSize = stepSize - 0.01;
        }
        changed();
    }

    public void divideStepSize() {
        if (stepSize > 100) {
            stepSize = 100;
        } else if (stepSize <= 100 && stepSize > 10) {
            stepSize = 10;
        } else if (stepSize <= 10 && stepSize > 1) {
            stepSize = 1;
        } else if (stepSize <= 1 && stepSize > 0.1) {
            stepSize = 0.1;
        } else if (stepSize <= 0.1 ) {
            stepSize = 0.01;
        }
        changed();
    }

    public void multiplyStepSize() {
        if (stepSize < 0.01) {
            stepSize = 0.01;
        } else if (stepSize >= 0.01 && stepSize < 0.1) {
            stepSize = 0.1;
        }  else if (stepSize >= 0.1 && stepSize < 1) {
            stepSize = 1;
        }  else if (stepSize >= 1 && stepSize < 10) {
            stepSize = 10;
        }  else if (stepSize >= 10) {
            stepSize = 100;
        }
        changed();
    }

    public void setStepSize(double size) {
        this.stepSize = size;
        changed();
    }

    public double getStepSize() {
        return this.stepSize;
    }

    public void setUnits(Units units) {
        this.units = units;
        changed();
    }
    
    public Units getUnits() {
        return this.units;
    }
    
    public void adjustManualLocation(int x, int y, int z) {
        try {
            this.backend.adjustManualLocation(x, y, z, stepSize, units);
        } catch (Exception e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean canJog() {
        return backend.getControlState() == UGSEvent.ControlState.COMM_IDLE;
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
            String localCategory = Localization.getString("platform.menu.machine");
            String menuPath = "Menu/" + category + "/Jog";
            
            ars.registerAction(Localization.getString("jogging.xPlus") ,
                    category, localCategory, "M-RIGHT" , menuPath, localized, new JogAction(this, 1, 0, 0));
            ars.registerAction(Localization.getString("jogging.xMinus"),
                    category, localCategory, "M-LEFT"  , menuPath, localized, new JogAction(this,-1, 0, 0));
            ars.registerAction(Localization.getString("jogging.yPlus") ,
                    category, localCategory, "M-UP"    , menuPath, localized, new JogAction(this, 0, 1, 0));
            ars.registerAction(Localization.getString("jogging.yMinus"),
                    category, localCategory, "M-DOWN"  , menuPath, localized, new JogAction(this, 0,-1, 0));
            ars.registerAction(Localization.getString("jogging.zPlus") ,
                    category, localCategory, "SM-UP"   , menuPath, localized, new JogAction(this, 0, 0, 1));
            ars.registerAction(Localization.getString("jogging.zMinus"),
                    category, localCategory, "SM-DOWN" , menuPath, localized, new JogAction(this, 0, 0,-1));

            localized = String.format("Menu/%s/%s/%s",
                    Localization.getString("platform.menu.machine"),
                    Localization.getString("platform.menu.jog"),
                    Localization.getString("platform.menu.jog.size"));
            menuPath = menuPath + "/Step Size";
            ars.registerAction("10",
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, 10));
            ars.registerAction("1",
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, 1));
            ars.registerAction("0.1",
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, 0.1));
            ars.registerAction("0.01",
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, 0.01));
            ars.registerAction("0.001",
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, 0.001));

            ars.registerAction(Localization.getString("jogging.divide"),
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, '/'));
            ars.registerAction(Localization.getString("jogging.multiply"),
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, '*'));
            ars.registerAction(Localization.getString("jogging.decrease"),
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, '-'));
            ars.registerAction(Localization.getString("jogging.increase"),
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, '+'));

            ars.registerAction(Localization.getString("mainWindow.swing.inchRadioButton"),
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, Units.INCH));
            ars.registerAction(Localization.getString("mainWindow.swing.mmRadioButton"),
                    category, localCategory, "" , menuPath, localized, new JogSizeAction(this, Units.MM));

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected class JogSizeAction extends AbstractAction {
        JogService js;
        Double size = null;
        Character operation = null;
        Units unit = null;

        public JogSizeAction(JogService service, Units u) {
            js = service;
            unit = u;
        }
        public JogSizeAction(JogService service, char op) {
            js = service;
            operation = op;
        }

        public JogSizeAction(JogService service, double size) {
            js = service;
            this.size = size;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (size != null)
                js.setStepSize(size);
            else if (operation != null) {
                switch (operation) {
                    case '*':
                        js.multiplyStepSize();
                        break;
                    case '/':
                        js.divideStepSize();
                        break;
                    case '+':
                        js.increaseStepSize();
                        break;
                    case '-':
                        js.decreaseStepSize();
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
        private int x,y,z;
        public JogAction(JogService service, int x, int y, int z) {
            js = service;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            js.adjustManualLocation(x, y, z);
        }

        @Override
        public boolean isEnabled() {
            return js.canJog();
        }
    }
}
