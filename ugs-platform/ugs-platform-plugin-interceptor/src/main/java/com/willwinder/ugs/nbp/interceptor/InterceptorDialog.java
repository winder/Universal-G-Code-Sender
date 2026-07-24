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
package com.willwinder.ugs.nbp.interceptor;

import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;

/**
 * A dialog that presents the progress of a command interceptor to the operator. Implementations render the
 * screens for the different interceptor states. All methods are called on the event dispatch thread.
 *
 * @author Joacim Breiler
 */
public interface InterceptorDialog {

    /**
     * Handle an event from the {@link InterceptorStateEvent} bus.
     *
     * @param event the interceptor event
     */
    void handleEvent(InterceptorStateEvent event);

    /**
     * Handle the controller state
     */
    void handleControllerState(ControllerStateEvent controllerStateEvent);
}
