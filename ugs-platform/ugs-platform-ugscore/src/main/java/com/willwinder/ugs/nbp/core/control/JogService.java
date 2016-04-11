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
import javax.swing.AbstractAction;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import static org.openide.util.NbBundle.getMessage;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author wwinder
 */
@ServiceProvider(service=JogService.class) 
public class JogService {
    private double stepSize;
    private Units units;

    BackendAPI backend;

    public JogService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        initActions();
    }

    public void setStepSize(double size) {
        this.stepSize = size;
    }

    public void setUnits(Units units) {
        this.units = units;
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
     * Create the jog actions.
     */
    private void initActions() {
        ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);

        try {
            String menuPath = "Menu/Machine/Jog";
            
            ars.registerAction(getMessage(this.getClass(), "JogService.xPlus") ,
                    "Machine", "M-RIGHT" , menuPath, new JogAction(this, 1, 0, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.xMinus"),
                    "Machine", "M-LEFT"  , menuPath, new JogAction(this,-1, 0, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.yPlus") ,
                    "Machine", "M-UP"    , menuPath, new JogAction(this, 0, 1, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.yMinus"),
                    "Machine", "M-DOWN"  , menuPath, new JogAction(this, 0,-1, 0));
            ars.registerAction(getMessage(this.getClass(), "JogService.zPlus") ,
                    "Machine", "SM-UP"   , menuPath, new JogAction(this, 0, 0, 1));
            ars.registerAction(getMessage(this.getClass(), "JogService.zMinus"),
                    "Machine", "SM-DOWN" , menuPath, new JogAction(this, 0, 0,-1));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected class JogAction extends AbstractAction {
        JogService js;
        int x,y,z;
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
