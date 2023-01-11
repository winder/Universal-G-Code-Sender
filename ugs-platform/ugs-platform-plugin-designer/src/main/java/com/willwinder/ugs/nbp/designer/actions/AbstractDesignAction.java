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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.platform.UgsDataObject;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

import javax.swing.AbstractAction;

/**
 * An abstract action class for design actions. This will listen if a connected machine is idle or
 * that a file has been loaded
 *
 * @author Joacim Breiler
 */
public abstract class AbstractDesignAction extends AbstractAction implements LookupListener, UGSEventListener {
    private final transient BackendAPI backendAPI;
    private final transient Lookup context;

    protected AbstractDesignAction() {
        this(Utilities.actionsGlobalContext());
    }

    protected AbstractDesignAction(Lookup context) {
        super();
        this.context = context;
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this);

        Lookup.Result<UgsDataObject> lookupResult = context.lookupResult(UgsDataObject.class);
        lookupResult.addLookupListener(this);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        boolean isIdleOrDisconnected = (backendAPI.isConnected() && backendAPI.isIdle()) || !backendAPI.isConnected();
        boolean isFileLoaded = Utils.isStandalone() || (!context.lookupAll(UgsDataObject.class).isEmpty());
        setEnabled(isIdleOrDisconnected && isFileLoaded);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            resultChanged(null);
        }
    }
}
