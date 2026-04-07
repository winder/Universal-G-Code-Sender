/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.designer.io.FileContext;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

/**
 * Listens to if there is a loaded design file and updates the file context accordingly.
 *
 * @author Joacim Breiler
 */
public class PlatformFileContext implements FileContext, LookupListener {

    private final List<PropertyChangeListener> listeners = new LinkedList<>();

    public PlatformFileContext() {
        Lookup.Result<UgsDataObject> lookupResult = Utilities.actionsGlobalContext().lookupResult(UgsDataObject.class);
        lookupResult.addLookupListener(this);
        DataObject.getRegistry().addChangeListener((e) -> resultChanged(null));
    }

    @Override
    public boolean isFileLoaded() {
        return TopComponent.getRegistry().getActivatedNodes().length > 0;
    }

    @Override
    public void addChangeListener(PropertyChangeListener changeListener) {
        listeners.add(changeListener);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        listeners.forEach(listener -> listener.propertyChange(new PropertyChangeEvent(this, "fileLoaded", false, isFileLoaded())));
    }
}
