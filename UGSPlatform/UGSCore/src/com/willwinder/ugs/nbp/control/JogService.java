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
package com.willwinder.ugs.nbp.control;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils.Units;
import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
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
        SwingUtilities.invokeLater(() -> {
            FileObject menuFolder = FileUtil.getConfigFile("Menu/Machine/Jog");
            try {
                registerAction("JogService.xPlus" , menuFolder, 1, 0, 0);
                registerAction("JogService.xMinus", menuFolder,-1, 0, 0);
                registerAction("JogService.yPlus" , menuFolder, 0, 1, 0);
                registerAction("JogService.yMinus", menuFolder, 0,-1, 0);
                registerAction("JogService.zPlus" , menuFolder, 0, 0, 1);
                registerAction("JogService.zMinus", menuFolder, 0, 0,-1);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    private void registerAction(String nameId, FileObject in, int x, int y, int z) throws IOException {
        String name = NbBundle.getMessage(JogService.class, nameId);

        // Create if missing.
        FileObject menu = in.getFileObject(name, "instance");
        if (menu == null) {
            menu = in.createData(name, "instance");
        }

        AbstractAction action = new JogAction(name, this, x, y, z);
        menu.setAttribute("instanceCreate", action);
        menu.setAttribute("instanceClass", action.getClass().getName());
    }

    protected class JogAction extends AbstractAction {
        JogService js;
        int x,y,z;
        public JogAction(String name, JogService service, int x, int y, int z) {
            super(name);
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
