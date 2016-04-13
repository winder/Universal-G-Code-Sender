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
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils.Units;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=JogService.class) 
public class JogService {
    private double stepSize;
    private Units units;

    private BackendAPI backend;

    public JogService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        initActions();
    }

    private void changed() {
        NbPreferences.forModule(JogService.class).put("changed", UUID.randomUUID().toString());
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
            String jogMenu = "Jog";
            String category = "Machine";
            String menuPath = "Menu/" + category + "/" + jogMenu;
            
            ars.registerAction(getMessage(this.getClass(), "JogService.xPlus") ,
                    category, "M-RIGHT" , menuPath, new JogAction(this, 1, 0, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.xMinus"),
                    category, "M-LEFT"  , menuPath, new JogAction(this,-1, 0, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.yPlus") ,
                    category, "M-UP"    , menuPath, new JogAction(this, 0, 1, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.yMinus"),
                    category, "M-DOWN"  , menuPath, new JogAction(this, 0,-1, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.zPlus") ,
                    category, "SM-UP"   , menuPath, new JogAction(this, 0, 0, 1));
            ars.registerAction(getMessage(this.getClass(), "JogService.zMinus"),
                    category, "SM-DOWN" , menuPath, new JogAction(this, 0, 0,-1));

            String jogSizeMenu = "Step Size";
            menuPath = menuPath + "/" + jogSizeMenu;
            ars.registerAction("10",
                    category, "" , menuPath, new JogSizeAction(this, 10));
            ars.registerAction("1",
                    category, "" , menuPath, new JogSizeAction(this, 1));
            ars.registerAction("0.1",
                    category, "" , menuPath, new JogSizeAction(this, 0.1));
            ars.registerAction("0.01",
                    category, "" , menuPath, new JogSizeAction(this, 0.01));
            ars.registerAction("0.001",
                    category, "" , menuPath, new JogSizeAction(this, 0.001));

            ars.registerAction(getMessage(this.getClass(), "JogService.stepSize.divide"),
                    category, "" , menuPath, new JogSizeAction(this, '/'));
            ars.registerAction(getMessage(this.getClass(), "JogService.stepSize.multiply"),
                    category, "" , menuPath, new JogSizeAction(this, '*'));
            ars.registerAction(getMessage(this.getClass(), "JogService.stepSize.decrease"),
                    category, "" , menuPath, new JogSizeAction(this, '-'));
            ars.registerAction(getMessage(this.getClass(), "JogService.stepSize.increase"),
                    category, "" , menuPath, new JogSizeAction(this, '+'));

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected class JogSizeAction extends AbstractAction {
        JogService js;
        double size = 0;
        char operation;
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
            if (size != 0)
                js.setStepSize(size);
            else {
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
