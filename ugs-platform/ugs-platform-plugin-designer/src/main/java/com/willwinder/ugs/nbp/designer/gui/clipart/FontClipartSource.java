/*
    Copyright 2022-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import com.google.gson.Gson;
import com.willwinder.ugs.nbp.designer.gui.clipart.model.FontMapping;
import org.apache.commons.io.IOUtils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Joacim Breiler
 */
public class FontClipartSource extends AbstractClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();
    private final FontMapping fontMapping;

    public FontClipartSource(String mappingFile) {
        fontMapping = loadFontMapping(mappingFile);
        Font font = loadFont();
        fontMapping.getCliparts()
                .forEach(clipart -> clipart.getCategories().forEach(category ->
                        cliparts.add(new FontClipart(clipart.getName(), category, font, clipart.getText(), this))));
    }

    private FontMapping loadFontMapping(String mappingFile) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(IOUtils.toString(Objects.requireNonNull(FontClipartSource.class.getResourceAsStream(mappingFile)), StandardCharsets.UTF_8), FontMapping.class);
        } catch (IOException e) {
            throw new ClipartSourceException("Could not load clipart font mapping file", e);
        }
    }

    private Font loadFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontClipartSource.class.getResourceAsStream(fontMapping.getFont()))).deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }
    }

    @Override
    public String getName() {
        return fontMapping.getName();
    }

    @Override
    public String getCredits() {
        return fontMapping.getCredits();
    }

    @Override
    public String getUrl() {
        return fontMapping.getUrl();
    }

    @Override
    public List<? extends Clipart> getCliparts() {
        return cliparts;
    }

    @Override
    public String getLicense() {
        return fontMapping.getLicense();
    }
}
