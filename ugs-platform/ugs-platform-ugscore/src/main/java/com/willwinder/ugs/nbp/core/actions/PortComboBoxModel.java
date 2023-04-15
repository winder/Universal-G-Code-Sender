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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.connection.DefaultConnectionDevice;
import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import org.apache.commons.lang3.StringUtils;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * A combo box model for storing connection devices
 *
 * @author Joacim Breiler
 */
public class PortComboBoxModel extends AbstractListModel<IConnectionDevice> implements MutableComboBoxModel<IConnectionDevice>, Serializable {
    private final List<IConnectionDevice> devices = new CopyOnWriteArrayList<>();
    private transient IConnectionDevice selectedDevice;

    @Override
    public void addElement(IConnectionDevice device) {
        boolean exists = devices.stream().anyMatch(d -> StringUtils.equals(d.getAddress(), device.getAddress()));
        if (!exists && device.getAddress() != null) {
            devices.add(device);
            fireIntervalAdded(this, devices.size() - 1, devices.size() - 1);
        }
    }

    @Override
    public void removeElement(Object obj) {
        if (obj instanceof IConnectionDevice) {
            int index = devices.indexOf(obj);
            removeElementAt(index);
        } else {
            throw new IllegalArgumentException("Yo!");
        }
    }

    @Override
    public void insertElementAt(IConnectionDevice device, int index) {
        devices.add(index, device);
        fireIntervalAdded(this, index, index);
    }

    @Override
    public void removeElementAt(int index) {
        IConnectionDevice device = devices.get(index);
        if (selectedDevice == device) {
            selectedDevice = null;
            fireContentsChanged(this, -1, -1);
        }

        devices.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public Object getSelectedItem() {
        return selectedDevice;
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof IConnectionDevice) {
            selectedDevice = (IConnectionDevice) anItem;
        } else {
            selectedDevice = devices.stream()
                    .filter(d -> d.getAddress().equals(anItem))
                    .findFirst()
                    .orElse(new DefaultConnectionDevice(anItem.toString()));
        }

        fireContentsChanged(this, -1, -1);
    }

    @Override
    public int getSize() {
        return devices.size();
    }

    @Override
    public IConnectionDevice getElementAt(int index) {
        if (index > devices.size() - 1) {
            return null;
        }
        return devices.get(index);
    }

    public void setElements(List<IConnectionDevice> newDevices) {
        // Remove devices not in supplied list
        devices.stream().filter(d -> !newDevices.contains(d)).forEach(this::removeElement);

        // Add devices that was not already added
        newDevices.stream().filter(d -> !devices.contains(d)).forEach(this::addElement);
    }
}
