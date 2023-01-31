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

import com.willwinder.ugs.nbp.designer.gui.clipart.Category;
import com.willwinder.ugs.nbp.designer.gui.clipart.Clipart;
import com.willwinder.ugs.nbp.designer.gui.clipart.ClipartSource;
import com.willwinder.ugs.nbp.designer.gui.clipart.ClipartSourceException;
import com.willwinder.ugs.nbp.designer.gui.clipart.FontClipart;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class TravelconsSource implements ClipartSource {
    private final List<FontClipart> cliparts = new ArrayList<>();

    public TravelconsSource() {
        Font font;
        try {
            font = Font
                    .createFont(Font.TRUETYPE_FONT, TravelconsSource.class.getResourceAsStream("/fonts/travelcons/travelcons.ttf"))
                    .deriveFont(
                            FONT_SIZE);
        } catch (IOException | FontFormatException e) {
            throw new ClipartSourceException("Could not load font", e);
        }

        cliparts.add(new FontClipart("Ship", Category.TRANSPORTATION, font, "A", this));
        cliparts.add(new FontClipart("Drink", Category.FOOD, font, "B", this));
        cliparts.add(new FontClipart("Camping", Category.SIGNS_AND_SYMBOLS, font.deriveFont(font.getSize() * 0.8f), "C", this));
        cliparts.add(new FontClipart("Bus and Taxi", Category.TRANSPORTATION, font.deriveFont(font.getSize() * 0.7f), "D", this));
        cliparts.add(new FontClipart("Wheelchair", Category.SIGNS_AND_SYMBOLS, font, "E", this));
        cliparts.add(new FontClipart("Restaurant", Category.SIGNS_AND_SYMBOLS, font, "F", this));
        cliparts.add(new FontClipart("Gift shop", Category.SIGNS_AND_SYMBOLS, font, "G", this));
        cliparts.add(new FontClipart("Helicopter", Category.TRANSPORTATION, font, "H", this));
        cliparts.add(new FontClipart("Passport control", Category.SIGNS_AND_SYMBOLS, font, "I", this));
        cliparts.add(new FontClipart("Accessories", Category.SIGNS_AND_SYMBOLS, font, "J", this));
        cliparts.add(new FontClipart("Closet", Category.SIGNS_AND_SYMBOLS, font, "K", this));
        cliparts.add(new FontClipart("Reading", Category.SIGNS_AND_SYMBOLS, font, "L", this));
        cliparts.add(new FontClipart("Hospital", Category.SIGNS_AND_SYMBOLS, font, "M", this));
        cliparts.add(new FontClipart("Fire extinguisher", Category.SIGNS_AND_SYMBOLS, font, "N", this));
        cliparts.add(new FontClipart("Emergency", Category.SIGNS_AND_SYMBOLS, font, "O", this));
        cliparts.add(new FontClipart("Boat", Category.TRANSPORTATION, font, "P", this));
        cliparts.add(new FontClipart("Question", Category.SIGNS_AND_SYMBOLS, font, "Q", this));
        cliparts.add(new FontClipart("Car key", Category.SIGNS_AND_SYMBOLS, font, "R", this));
        cliparts.add(new FontClipart("Electrical", Category.SIGNS_AND_SYMBOLS, font, "S", this));
        cliparts.add(new FontClipart("Passport control", Category.SIGNS_AND_SYMBOLS, font, "T", this));
        cliparts.add(new FontClipart("Recycle", Category.SIGNS_AND_SYMBOLS, font, "U", this));
        cliparts.add(new FontClipart("Exit", Category.SIGNS_AND_SYMBOLS, font, "V", this));
        cliparts.add(new FontClipart("Water", Category.SIGNS_AND_SYMBOLS, font, "W", this));
        cliparts.add(new FontClipart("Restroom", Category.SIGNS_AND_SYMBOLS, font, "X", this));
        cliparts.add(new FontClipart("Hair salon", Category.SIGNS_AND_SYMBOLS, font, "Y", this));
        cliparts.add(new FontClipart("Baggage", Category.SIGNS_AND_SYMBOLS, font, "Z", this));
        cliparts.add(new FontClipart("Airplane", Category.TRANSPORTATION, font, "a", this));
        cliparts.add(new FontClipart("Bus", Category.TRANSPORTATION, font, "b", this));
        cliparts.add(new FontClipart("Coffey", Category.FOOD, font, "c", this));
        cliparts.add(new FontClipart("Doctor", Category.PEOPLE_AND_CHARACTERS, font, "d", this));
        cliparts.add(new FontClipart("Escalator", Category.SIGNS_AND_SYMBOLS, font, "e", this));
        cliparts.add(new FontClipart("Fast food", Category.FOOD, font, "f", this));
        cliparts.add(new FontClipart("Shopping cart", Category.SIGNS_AND_SYMBOLS, font, "g", this));
        cliparts.add(new FontClipart("Bed", Category.SIGNS_AND_SYMBOLS, font, "h", this));
        cliparts.add(new FontClipart("Ice cream", Category.FOOD, font, "i", this));
        cliparts.add(new FontClipart("Waiting hall", Category.SIGNS_AND_SYMBOLS, font, "j", this));
        cliparts.add(new FontClipart("Blind man", Category.SIGNS_AND_SYMBOLS, font, "k", this));
        cliparts.add(new FontClipart("Baggage storage", Category.SIGNS_AND_SYMBOLS, font, "l", this));
        cliparts.add(new FontClipart("Mail", Category.SIGNS_AND_SYMBOLS, font, "m", this));
        cliparts.add(new FontClipart("Meeting hall?", Category.SIGNS_AND_SYMBOLS, font, "n", this));
        cliparts.add(new FontClipart("Money exchange", Category.SIGNS_AND_SYMBOLS, font, "o", this));
        cliparts.add(new FontClipart("Telephone", Category.SIGNS_AND_SYMBOLS, font, "p", this));
        cliparts.add(new FontClipart("Restroom - men", Category.SIGNS_AND_SYMBOLS, font, "q", this));
        cliparts.add(new FontClipart("Train", Category.TRANSPORTATION, font, "r", this));
        cliparts.add(new FontClipart("Stairs", Category.SIGNS_AND_SYMBOLS, font, "s", this));
        cliparts.add(new FontClipart("Garbage", Category.SIGNS_AND_SYMBOLS, font, "t", this));
        cliparts.add(new FontClipart("Customs", Category.SIGNS_AND_SYMBOLS, font, "u", this));
        cliparts.add(new FontClipart("Elevator", Category.SIGNS_AND_SYMBOLS, font, "v", this));
        cliparts.add(new FontClipart("Restroom - woman", Category.SIGNS_AND_SYMBOLS, font, "w", this));
        cliparts.add(new FontClipart("Taxi", Category.TRANSPORTATION, font, "x", this));
        cliparts.add(new FontClipart("Ticket", Category.SIGNS_AND_SYMBOLS, font, "y", this));
        cliparts.add(new FontClipart("Bicycle", Category.TRANSPORTATION, font.deriveFont(font.getSize() * 0.8f), "z", this));
        cliparts.add(new FontClipart("?", Category.SIGNS_AND_SYMBOLS, font, "0", this));
        cliparts.add(new FontClipart("Direction", Category.SIGNS_AND_SYMBOLS, font, "1", this));
        cliparts.add(new FontClipart("No phones", Category.SIGNS_AND_SYMBOLS, font, "5", this));
        cliparts.add(new FontClipart("No entry", Category.SIGNS_AND_SYMBOLS, font, "6", this));
        cliparts.add(new FontClipart("No smoking", Category.SIGNS_AND_SYMBOLS, font, "7", this));
        cliparts.add(new FontClipart("No parking", Category.SIGNS_AND_SYMBOLS, font, "9", this));
    }

    @Override
    public String getName() {
        return "Travelcons";
    }

    @Override
    public String getCredits() {
        return "Daniel Zadorozny";
    }

    @Override
    public String getUrl() {
        return "https://www.dafont.com/travelcons.font";
    }

    @Override
    public List<Clipart> getCliparts(Category category) {
        return cliparts.stream().filter(clipart -> clipart.getCategory() == category).collect(Collectors.toList());
    }

    @Override
    public String getLicense() {
        return "Free for non-commercial use";
    }
}
