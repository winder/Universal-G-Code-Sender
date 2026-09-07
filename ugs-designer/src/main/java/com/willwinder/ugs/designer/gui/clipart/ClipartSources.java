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
package com.willwinder.ugs.designer.gui.clipart;

import java.util.Comparator;
import java.util.List;

/**
 * Registry of the bundled clipart sources. The sources are loaded lazily on first use and
 * then shared between all UI variants, since loading the fonts is relatively expensive.
 *
 * @author Joacim Breiler
 */
public final class ClipartSources {
    private static final List<String> FONT_SOURCES = List.of(
            "/fonts/christmas/mapping.json",
            "/fonts/your-sign/mapping.json",
            "/fonts/xmas/mapping.json",
            "/fonts/bu-dingbats/mapping.json",
            "/fonts/darrians-frames-font/mapping1.json",
            "/fonts/darrians-frames-font/mapping2.json",
            "/fonts/creepy-crawlies-font/mapping.json",
            "/fonts/house-icons/mapping.json",
            "/fonts/travelcons/mapping.json",
            "/fonts/tool/mapping.json",
            "/fonts/garden/mapping.json",
            "/fonts/sugar-coma-font/mapping.json",
            "/fonts/corners2/mapping.json",
            "/fonts/wwfreebie/mapping.json",
            "/fonts/destinys-borders/mapping.json",
            "/fonts/vintage-decorative-corners-23-font/mapping.json",
            "/fonts/vintage-decorative-signs-2-font/mapping.json",
            "/fonts/world-of-sci-fi-font/mapping.json",
            "/fonts/tropicana/mapping.json",
            "/fonts/transdings/mapping.json",
            "/fonts/sealife/mapping.json",
            "/fonts/logoskate-1/mapping.json",
            "/fonts/logoskate-2/mapping.json",
            "/fonts/mythical/mapping.json",
            "/fonts/komika-bubbles/mapping.json",
            "/fonts/fredoka-one/mapping.json",
            "/fonts/evilz/mapping.json",
            "/fonts/easterart/mapping.json",
            "/fonts/efon/mapping.json",
            "/fonts/eagle/mapping.json",
            "/fonts/black-white-banners/mapping.json",
            "/fonts/superhero/mapping.json",
            "/fonts/laurus-nobilis/mapping.json",
            "/fonts/vintage-monogram/mapping.json",
            "/fonts/berlin-monogram/mapping.json",
            "/fonts/auro-monogram/mapping.json"
    );

    private static List<ClipartSource> sources;

    private ClipartSources() {
    }

    public static synchronized List<ClipartSource> getSources() {
        if (sources == null) {
            sources = FONT_SOURCES.stream()
                    .map(ClipartSources::loadSource)
                    .toList();
        }
        return sources;
    }

    /**
     * Returns all cliparts in the given category from every source, sorted by name.
     */
    public static List<Clipart> getCliparts(Category category) {
        return getSources().stream()
                .flatMap(source -> source.getCliparts(category).stream())
                .map(Clipart.class::cast)
                .sorted(Comparator.comparing(clipart -> clipart.getName().toLowerCase()))
                .toList();
    }

    private static ClipartSource loadSource(String mappingFile) {
        try {
            return new FontClipartSource(mappingFile);
        } catch (Exception e) {
            throw new ClipartSourceException("Could not load source " + mappingFile, e);
        }
    }
}
