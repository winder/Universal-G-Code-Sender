/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.clipart.sources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.willwinder.ugs.nbp.designer.gui.clipart.*;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class RemixIconSource implements ClipartSource {
    private final Font font;

    private final Map<Category, List<? extends Clipart>> cliparts;

    public RemixIconSource() {
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, InsertClipartDialog.class.getResourceAsStream("/fonts/remixicon/remixicon.ttf"))
                    .deriveFont(FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new RuntimeException("Could not load font", e);
        }

        Gson gson = new GsonBuilder().create();
        java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> icons = gson.fromJson(new InputStreamReader(InsertClipartDialog.class.getResourceAsStream("/fonts/remixicon/icons.json")), type);

        gson = new GsonBuilder().create();
        type = new TypeToken<Map<String, Map<String, String[]>>>() {
        }.getType();
        Map<String, Map<String, String[]>> categories = gson.fromJson(new InputStreamReader(InsertClipartDialog.class.getResourceAsStream("/fonts/remixicon/tags.json")), type);

        Map<String, Category> categoryMap = new HashMap<>();
        categoryMap.put("Buildings", Category.BUILDINGS);
        categoryMap.put("Business", Category.OFFICE);
        categoryMap.put("Communication", Category.SCIENCE);
        categoryMap.put("Design", Category.SPECIAL);
        categoryMap.put("Development", Category.SPECIAL);
        categoryMap.put("Device", Category.ELECTRONICS);
        categoryMap.put("Document", Category.SPECIAL);
        categoryMap.put("Editor", Category.SPECIAL);
        categoryMap.put("Finance", Category.OFFICE);
        categoryMap.put("Health & Medical", Category.SCIENCE);
        categoryMap.put("Logos", Category.LOGOS);
        categoryMap.put("Map", Category.GEOGRAPHY);
        categoryMap.put("Media", Category.COMPUTER);
        categoryMap.put("System", Category.COMPUTER);
        categoryMap.put("User & Faces", Category.PEOPLE);
        categoryMap.put("Weather", Category.WEATHER);
        categoryMap.put("Others", Category.UNSORTED);

        cliparts = new HashMap<>();
        categories.keySet().forEach(categoryName -> {
            Set<String> iconNames = categories.get(categoryName).keySet();
            Category category = categoryMap.get(categoryName);
            List<FontClipart> categoryIcons = iconNames.stream()
                    .map(iconName -> new FontClipart(iconName, category, font, icons.get(iconName)))
                    .collect(Collectors.toList());
            cliparts.put(category, categoryIcons);
        });
    }

    @Override
    public String getName() {
        return "Remix Icon 2.5.0";
    }

    @Override
    public String getCredits() {
        return "Jimmy Cheung & Wendy Gao";
    }

    @Override
    public String getUrl() {
        return "https://remixicon.com/";
    }

    @Override
    public List<? extends Clipart> getCliparts(Category category) {
        return cliparts.getOrDefault(category, Collections.emptyList());
    }
}
