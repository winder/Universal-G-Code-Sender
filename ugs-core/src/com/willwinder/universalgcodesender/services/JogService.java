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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.uielements.IChanged;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author wwinder
 */
public class JogService {
    private double stepSizeXY = 1;
    private double stepSizeZ = 1;
    private double feedRate = 1;
    private Units units;

    private BackendAPI backend;
    private final Collection<IChanged> changeListeners = new ArrayList<>();

    public JogService(BackendAPI backend) {
        this.backend = backend;

        stepSizeXY = backend.getSettings().getManualModeStepSize();
        stepSizeZ = backend.getSettings().getzJogStepSize();
        setUnits(Units.getUnit(backend.getSettings().getDefaultUnits()));
    }

    public void addChangeListener(IChanged changed) {
        changeListeners.add(changed);
    }

    private void changed() {
        for (IChanged changed : changeListeners) {
            changed.changed();
        }
    }

    public void increaseStepSize() {
        if (stepSizeXY >= 1) {
            stepSizeXY++;
        } else if (stepSizeXY >= 0.1) {
            stepSizeXY = stepSizeXY + 0.1;
        } else if (stepSizeXY >= 0.01) {
            stepSizeXY = stepSizeXY + 0.01;
        } else {
            stepSizeXY = 0.01;
        }
        changed();
    }

    public void decreaseStepSize() {
        if (stepSizeXY > 1) {
            stepSizeXY--;
        } else if (stepSizeXY > 0.1) {
            stepSizeXY = stepSizeXY - 0.1;
        } else if (stepSizeXY > 0.01) {
            stepSizeXY = stepSizeXY - 0.01;
        }
        changed();
    }

    public void divideStepSize() {
        if (stepSizeXY > 100) {
            stepSizeXY = 100;
        } else if (stepSizeXY <= 100 && stepSizeXY > 10) {
            stepSizeXY = 10;
        } else if (stepSizeXY <= 10 && stepSizeXY > 1) {
            stepSizeXY = 1;
        } else if (stepSizeXY <= 1 && stepSizeXY > 0.1) {
            stepSizeXY = 0.1;
        } else if (stepSizeXY <= 0.1 ) {
            stepSizeXY = 0.01;
        }
        changed();
    }

    public void multiplyStepSize() {
        if (stepSizeXY < 0.01) {
            stepSizeXY = 0.01;
        } else if (stepSizeXY >= 0.01 && stepSizeXY < 0.1) {
            stepSizeXY = 0.1;
        }  else if (stepSizeXY >= 0.1 && stepSizeXY < 1) {
            stepSizeXY = 1;
        }  else if (stepSizeXY >= 1 && stepSizeXY < 10) {
            stepSizeXY = 10;
        }  else if (stepSizeXY >= 10) {
            stepSizeXY = 100;
        }
        changed();
    }

    public void setStepSize(double size) {
        this.stepSizeXY = size;
        backend.getSettings().setManualModeStepSize(getStepSize());
        changed();
    }

    public double getStepSize() {
        return this.stepSizeXY;
    }

    public void setStepSizeZ(double size) {
        this.stepSizeZ = size;
        backend.getSettings().setzJogStepSize(getStepSizeZ());
        changed();
    }

    public double getStepSizeZ() {
        return this.stepSizeZ;
    }

    public void setFeedRate(double rate) {
        this.feedRate = rate;
        backend.getSettings().setJogFeedRate(getFeedRate());
        changed();
    }

    public double getFeedRate() {
        return this.feedRate;
    }

    public void setUnits(Units units) {
        this.units = units;
        if (units != null) {
            backend.getSettings().setDefaultUnits(units.abbreviation);
        }
        changed();
    }
    
    public Units getUnits() {
        return this.units;
    }
    
    public void adjustManualLocation(int x, int y, int z) {
        try {
            this.backend.adjustManualLocation(x, y, z, stepSizeXY, feedRate, units);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean canJog() {
        return backend.getControlState() == UGSEvent.ControlState.COMM_IDLE;
    }

    /**
     * Create the actions, this makes them available for keymapping and makes
     * them usable from the drop down menu's.
     */
    /*
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
*/
}

