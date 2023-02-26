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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.model.MdnsEntry;

import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * A service listener that will add or remove entries keeping the {@link MdnsService} updated
 *
 * @author Joacim Breiler
 */
public class MdnsServiceListener implements ServiceListener {
    private final MdnsService service;
    private final String serviceType;

    MdnsServiceListener(MdnsService service, String serviceType) {
        this.service = service;
        this.serviceType = serviceType;
    }

    @Override
    public void serviceAdded(ServiceEvent event) {
        MdnsEntry entry = service.getServices(serviceType).stream()
                .filter(mdnsEntry -> mdnsEntry.getName().equals(event.getName()))
                .findFirst().orElse(createAndRegisterEntry(event));

        // Update entry information
        entry.setPort(event.getInfo().getPort());
        if (event.getInfo().getInet4Addresses().length > 0) {
            entry.setHost(event.getInfo().getInet4Addresses()[0].getHostAddress());
        }
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        service.getServices(serviceType).stream()
                .filter(mdnsEntry -> mdnsEntry.getName().equals(event.getName()))
                .findFirst()
                .ifPresent(service::remove);
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        serviceAdded(event);
    }

    private MdnsEntry createAndRegisterEntry(ServiceEvent event) {
        MdnsEntry entry = new MdnsEntry(serviceType, event.getName());
        service.add(entry);
        return entry;
    }
}
