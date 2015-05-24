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
package com.willwinder.ugs.nbp.connection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author wwinder
 */
public class ConnectionProperty  {
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private String address;
    private String baud;
/*
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
*/
    
    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String newValue) {
        String oldValue = this.address;
        this.address = newValue;
        this.pcs.firePropertyChange("address", oldValue, newValue);
    }

    /**
     * @return the baud
     */
    public String getBaud() {
        return baud;
    }

    /**
     * @param baud the baud to set
     */
    public void setBaud(String newValue) {
        String oldValue = this.baud;
        this.baud = newValue;
        this.pcs.firePropertyChange("baud", oldValue, newValue);
    }
}
