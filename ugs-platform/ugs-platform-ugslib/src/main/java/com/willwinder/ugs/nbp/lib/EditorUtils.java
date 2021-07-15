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
package com.willwinder.ugs.nbp.lib;

import com.willwinder.ugs.nbp.lib.lookup.EditorCookie;
import org.apache.commons.lang3.ArrayUtils;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class EditorUtils {
    /**
     * Close all open editors that has the {@link EditorCookie} registered.
     *
     * @return true if all editors could be closed
     */
    public static boolean closeOpenEditors() {
        List<TopComponent> topComponents = getOpenEditors();

        boolean closed = true;
        for (TopComponent topComponent : topComponents) {
            if (!topComponent.close()) {
                closed = false;
                Closeable closeable = topComponent.getLookup().lookup(Closeable.class);
                if (closeable != null) {
                    try {
                        closeable.close();
                        closed = true;
                    } catch (IOException e) {
                        throw new RuntimeException("Couldn't close window", e);
                    }
                }
            }
        }
        return closed;
    }

    /**
     * Finds all editors that has the {@link EditorCookie} registered.
     *
     * @return a list with opened editors
     */
    public static List<TopComponent> getOpenEditors() {
        return TopComponent.getRegistry().getOpened()
                .stream()
                .filter(topComponent ->
                        Arrays.stream(ArrayUtils.nullToEmpty(topComponent.getActivatedNodes(), Node[].class))
                                .anyMatch(node -> node.getCookie(EditorCookie.class) != null))
                .collect(Collectors.toList());
    }
}
