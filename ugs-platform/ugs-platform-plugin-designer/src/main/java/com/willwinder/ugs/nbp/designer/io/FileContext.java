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
package com.willwinder.ugs.nbp.designer.io;

import java.beans.PropertyChangeListener;

/**
 * A file context interface that can be used to track changes in the file state.
 */
public interface FileContext {

    /**
     * Returns if there a is loaded design file
     *
     * @return true if a file is loaded
     */
    boolean isFileLoaded();

    /**
     * Adds a change listener for the file context
     *
     * @param change the change event
     */
    void addChangeListener(PropertyChangeListener change);
}
