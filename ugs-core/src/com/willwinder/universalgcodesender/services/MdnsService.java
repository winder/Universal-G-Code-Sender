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

import javax.jmdns.JmDNS;
import javax.jmdns.NetworkTopologyDiscovery;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A small service that can be used to listen for mDNS entries
 *
 * @author Joacim Breiler
 */
public class MdnsService {
    private static MdnsService instance;
    private final List<JmDNS> jmdnsList;

    private final List<MdnsEntry> mdnsEntries = new ArrayList<>();

    MdnsService() {
        jmdnsList = Arrays.stream(NetworkTopologyDiscovery.Factory.getInstance().getInetAddresses())
                .filter(Inet4Address.class::isInstance)
                .map(inetAddress -> {
                    try {
                        return JmDNS.create(inetAddress);
                    } catch (IOException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static MdnsService getInstance() {
        if (instance == null) {
            instance = new MdnsService();
        }

        return instance;
    }

    public void registerListener(String mdnsName) {
        jmdnsList.forEach(jmDNS -> jmDNS.addServiceListener(mdnsName, new ServiceListener() {
            @Override
            public void serviceAdded(ServiceEvent event) {
                Optional<MdnsEntry> entry = mdnsEntries.stream().filter(mdnsEntry -> mdnsEntry.getType().equals(mdnsName) && mdnsEntry.getName().equals(event.getName())).findFirst();
                if (!entry.isPresent()) {
                    entry = Optional.of(new MdnsEntry(mdnsName, event.getName()));
                    mdnsEntries.add(entry.get());
                }

                entry.get().setPort(event.getInfo().getPort());
                if (event.getInfo().getInet4Addresses().length > 0) {
                    entry.get().setHost(event.getInfo().getInet4Addresses()[0].getHostAddress());
                }
            }

            @Override
            public void serviceRemoved(ServiceEvent event) {
                Optional<MdnsEntry> entry = getServices(mdnsName).stream().filter(mdnsEntry -> mdnsEntry.getName().equals(event.getName())).findFirst();
                entry.ifPresent(mdnsEntries::remove);
            }

            @Override
            public void serviceResolved(ServiceEvent event) {
                serviceAdded(event);
            }
        }));
    }

    public List<MdnsEntry> getServices(String mdnsName) {
        return mdnsEntries.stream().filter(mdnsEntry -> mdnsEntry.getType().equals(mdnsName)).collect(Collectors.toList());
    }
}
