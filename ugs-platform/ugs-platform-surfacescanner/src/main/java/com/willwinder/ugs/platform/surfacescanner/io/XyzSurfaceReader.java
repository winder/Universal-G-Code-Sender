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
package com.willwinder.ugs.platform.surfacescanner.io;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class XyzSurfaceReader implements SurfaceReader {

    @Override
    public List<Position> read(InputStream inputStream) throws IOException {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        UnitUtils.Units preferredUnits = backendAPI.getSettings().getPreferredUnits();

        String data = IOUtils.toString(inputStream, Charset.defaultCharset());
        List<Position> positions = Arrays.stream(StringUtils.split(data, "\n"))
                .map(line -> {
                    String[] s = StringUtils.split(line, " ");
                    return new Position(Double.parseDouble(s[0]), Double.parseDouble(s[1]), Double.parseDouble(s[2]), preferredUnits);
                })
                .collect(Collectors.toList());

        return positions;
    }
}
