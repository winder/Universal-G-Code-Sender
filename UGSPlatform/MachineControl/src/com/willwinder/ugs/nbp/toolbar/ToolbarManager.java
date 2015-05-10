/*
    Copywrite 2015 Will Winder

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

package com.willwinder.ugs.nbp.toolbar;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BackendAPIReadOnly;
import com.willwinder.universalgcodesender.model.ControlStateEvent;
import java.awt.Component;
import org.openide.awt.*;
import org.openide.modules.OnStart;
import org.openide.modules.OnStop;

@OnStart
public final class ToolbarManager implements Runnable, ControlStateListener {
    BackendAPIReadOnly backend;
    
    @Override
    public void run() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addControlStateListener(this);
    }

    @OnStop
    public static final class Down implements Runnable {
        @Override
        public void run() {
        }
    }
    
    @Override
    public void ControlStateEvent(com.willwinder.universalgcodesender.model.ControlStateEvent cse) {
        if (!ToolbarPool.getDefault().getConfiguration().equals("FileOpened")) {
            ToolbarPool.getDefault().setConfiguration("FileOpened");
        }        

        Component[] components = ToolbarPool.getDefault().findToolbar("StartPauseStop").getComponents();

        setPlayEnabled(components, backend.canSend() || backend.isPaused());
        setPauseEnabled(components, backend.canPause());
        setStopEnabled(components, backend.canCancel());
    }
    
    private void setPlayEnabled(Component[] components, boolean enabled) {
        components[1].setEnabled(enabled);
    }
    
    private void setPauseEnabled(Component[] components, boolean enabled) {
        components[2].setEnabled(enabled);
    }
       
    private void setStopEnabled(Component[] components, boolean enabled) {
        components[3].setEnabled(enabled);
    }
}