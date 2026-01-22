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
package com.willwinder.ugs.nbp.designer.io.gerber;

import java.util.Arrays;

public record Aperture(String name, Code code, double[] params, Macro macro) {

    public Aperture {
        params = params == null ? new double[0] : Arrays.copyOf(params, params.length);

        if (macro != null) {
            code = Code.MACRO;
        }
    }

    public Aperture(String name, Code code, double[] params) {
        this(name, code, params, null);
    }

    public Aperture(String name, Macro macro, double[] params) {
        this(name, Code.MACRO, params, macro);
    }

    @Override
    public double[] params() {
        return Arrays.copyOf(params, params.length);
    }

    boolean isMacro() {
        return macro != null;
    }
}
