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
package com.willwinder.ugs.nbp.lib.services;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.ServiceProvider;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * A service that can be used to find actions using their shortcut key mapping
 * in the format defined in {@link org.openide.util.Utilities#stringToKey(String)}
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = ShortcutService.class)
public class ShortcutService {

    /**
     * Cache the found shortcuts to speed things up using the shortcut as key and the actionId as value.
     */
    private final Cache<String, String> shortcutCache = CacheBuilder.newBuilder()
            .expireAfterWrite(20, TimeUnit.SECONDS)
            .build();

    /**
     * Finds a shortcut in format defined in {@link org.openide.util.Utilities#stringToKey(String)}
     *
     * @param keyAsString the keymap shortcut
     * @return a shortcut reference
     */
    public Optional<String> getActionIdForShortcut(String keyAsString) {
        String actionId = shortcutCache.getIfPresent(keyAsString);
        if (StringUtils.isNotEmpty(actionId)) {
            return Optional.of(actionId);
        }

        FileObject rootFileObject = FileUtil.getConfigFile("Shortcuts/" + keyAsString + ".shadow");
        if (rootFileObject == null) {
            return Optional.empty();
        }

        String name = rootFileObject.getName();
        actionId = rootFileObject.getAttribute("originalFile").toString();
        shortcutCache.put(name, actionId);
        return Optional.of(actionId);
    }
}
