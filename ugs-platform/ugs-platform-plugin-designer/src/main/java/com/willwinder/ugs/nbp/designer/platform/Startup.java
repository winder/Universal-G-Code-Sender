/*
    Copyright 2021 Will Winder

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

import com.willwinder.ugs.nbp.core.services.FileFilterService;
import com.willwinder.ugs.nbp.designer.actions.OpenAction;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;

/**
 * @author Joacim Breiler
 */
@OnStart
public class Startup implements Runnable {

    @Override
    public void run() {
        // Registers the file types that can be opened in UGSs
        FileFilterService fileFilterService = Lookup.getDefault().lookup(FileFilterService.class);
        fileFilterService.registerFileFilter(OpenAction.DESIGN_FILE_FILTER);
    }
}
