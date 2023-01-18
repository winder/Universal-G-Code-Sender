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
package com.willwinder.ugs.nbp.core.services;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

import java.util.logging.Logger;

/**
 * This class proxies the original ContextGlobalProvider and ensures that the current data object and its
 * cookies remains in the GlobalContext regardless of the TopComponent selection.
 *
 * Based on the project lookup: https://netbeans.apache.org/wiki/DevFaqAddGlobalContext.html
 *
 * @author Joacim Breiler
 * @author Bruce Schubert
 * @see ContextGlobalProvider
 * @see GlobalActionContextImpl
 */
@ServiceProvider(service = ContextGlobalProvider.class,
        supersedes = "org.netbeans.modules.openide.windows.GlobalActionContextImpl")
public class GlobalActionContextProxy implements ContextGlobalProvider {

    private static final Logger LOGGER = Logger.getLogger(GlobalActionContextProxy.class.getName());

    /**
     * Additional content for our proxy lookup
     */
    private final InstanceContent content;

    /**
     * The content lookup
     */
    private Lookup instanceContentLookup;

    /**
     * Data objects listener results
     */
    private final Result<DataObject> dataObjects;

    /**
     * The primary lookup managed by the platform
     */
    private final Lookup globalContextLookup;

    /**
     * The actual proxyLookup returned by this class
     */
    private Lookup proxyLookup;


    public GlobalActionContextProxy() {
        this.content = new InstanceContent();

        // The default GlobalContextProvider

        GlobalActionContextImpl globalContextProvider = new GlobalActionContextImpl();
        globalContextLookup = globalContextProvider.createGlobalContext();

        // Monitor the existance of a data objects
        LookupListener resultListener = new LookupListenerImpl();
        dataObjects = globalContextLookup.lookupResult(DataObject.class);
        dataObjects.addLookupListener(resultListener);
    }

    /**
     * Returns a ProxyLookup that adds the current data object instance and its cookies to the
     * global selection returned by Utilities.actionsGlobalContext().
     *
     * @return a ProxyLookup that includes the original global context lookup.
     */
    @Override
    public Lookup createGlobalContext() {
        if (proxyLookup == null) {
            LOGGER.config("Creating a proxy for Utilities.actionsGlobalContext()");
            instanceContentLookup = new AbstractLookup(content);
            proxyLookup = new ProxyLookup(globalContextLookup, instanceContentLookup);
        }
        return proxyLookup;
    }

    /**
     * Unconditionally clears the project lookup.
     */
    private void clearDataObjects() {
        instanceContentLookup.lookupAll(DataObject.class).forEach(content::remove);
        instanceContentLookup.lookupAll(Node.Cookie.class).forEach(content::remove);
    }

    /**
     * Replaces the data object in the content.
     *
     * @param dataObject to place in the content lookup.
     */
    private void updateCurrentDataObject(DataObject dataObject) {
        clearDataObjects();
        if (dataObject == null) {
            return;
        }

        content.add(dataObject);
        dataObject.getLookup().lookupAll(Node.Cookie.class).forEach(content::add);
    }

    /**
     * This class listens for changes in the about dataobjects, and ensures that the latest dataobject
     * remains in the Utilities.actionsGlobalContext() if it is still open.
     */
    private class LookupListenerImpl implements LookupListener {
        /**
         * The last dataobject that was selected
         */
        private DataObject currentDataObject;

        @Override
        public void resultChanged(LookupEvent event) {
            if (!dataObjects.allInstances().isEmpty()) {
                // There are active data objects
                // Clear our local proxy but save the data object for later
                clearDataObjects();
                currentDataObject = dataObjects.allInstances().iterator().next();
            } else if (currentDataObject != null) {
                // We lost the data object, attempt to reload it using our cached instance
                updateCurrentDataObject(currentDataObject);
            }
        }
    }
}
