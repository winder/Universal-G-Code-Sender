/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MachineStatusFontManager {

    private List<Font> workCoordinateFont = new ArrayList<>(3);
    private List<Font> machineCoordinateFont = new ArrayList<>(3);
    private List<Font> axisResetFont = new ArrayList<>(3);
    private List<Font> axisResetZeroFont = new ArrayList<>(3);
    private List<Font> propertyLabelFont = new ArrayList<>(3);
    private List<Font> speedValueFont = new ArrayList<>(3);
    private List<Font> activeStateFont = new ArrayList<>(3);

    private List<JComponent> workCoordinateComponents = new ArrayList<>();
    private List<JComponent> machineCoordinateComponents = new ArrayList<>();
    private List<JComponent> axisResetComponents = new ArrayList<>();
    private List<JComponent> axisResetZeroComponents = new ArrayList<>();
    private List<JComponent> propertyLabelComponents = new ArrayList<>();
    private List<JComponent> speedValueComponents = new ArrayList<>();
    private List<JComponent> activeStateLabelComponents = new ArrayList<>();

    public void init() {
        String fontPath = "/resources/";
        String fontName = "LED.ttf";
        InputStream is = getClass().getResourceAsStream(fontPath + fontName);

        Font font = createFont(is, fontName);
        workCoordinateFont.add(font.deriveFont(Font.PLAIN,28));
        workCoordinateFont.add(font.deriveFont(Font.PLAIN,30));
        workCoordinateFont.add(font.deriveFont(Font.PLAIN,38));
        workCoordinateFont.add(font.deriveFont(Font.PLAIN,46));
        machineCoordinateFont.add(font.deriveFont(Font.PLAIN,19));
        machineCoordinateFont.add(font.deriveFont(Font.PLAIN,19));
        machineCoordinateFont.add(font.deriveFont(Font.PLAIN,27));
        machineCoordinateFont.add(font.deriveFont(Font.PLAIN,32));
        speedValueFont.add(font.deriveFont(Font.PLAIN,20));
        speedValueFont.add(font.deriveFont(Font.PLAIN,24));
        speedValueFont.add(font.deriveFont(Font.PLAIN,32));
        speedValueFont.add(font.deriveFont(Font.PLAIN,40));

        // https://www.fontsquirrel.com
        fontName = "OpenSans-CondBold.ttf";
        is = getClass().getResourceAsStream(fontPath + fontName);
        font = createFont(is, fontName);
        activeStateFont.add(font.deriveFont(Font.PLAIN, 26));
        activeStateFont.add(font.deriveFont(Font.PLAIN, 30));
        activeStateFont.add(font.deriveFont(Font.PLAIN, 38));
        activeStateFont.add(font.deriveFont(Font.PLAIN, 46));

        fontName = "OpenSans-Regular.ttf";
        is = getClass().getResourceAsStream(fontPath + fontName);
        font = createFont(is, fontName);
        axisResetFont.add(font.deriveFont(Font.PLAIN, 26));
        axisResetFont.add(font.deriveFont(Font.PLAIN, 30));
        axisResetFont.add(font.deriveFont(Font.PLAIN, 38));
        axisResetFont.add(font.deriveFont(Font.PLAIN, 46));

        Font zeroFont = font.deriveFont(Font.PLAIN, 18);
        axisResetZeroFont.add(zeroFont);
        axisResetZeroFont.add(zeroFont);
        axisResetZeroFont.add(zeroFont);

        propertyLabelFont.add(font.deriveFont(Font.PLAIN, 12));
        propertyLabelFont.add(font.deriveFont(Font.PLAIN, 14));
        propertyLabelFont.add(font.deriveFont(Font.PLAIN, 17));
        propertyLabelFont.add(font.deriveFont(Font.PLAIN, 24));
    }

    public void applyFonts(int size) {
        int index = Math.max(0, Math.min(2, size));
        workCoordinateComponents.forEach(c -> c.setFont(workCoordinateFont.get(index)));
        machineCoordinateComponents.forEach(c -> c.setFont(machineCoordinateFont.get(index)));
        axisResetComponents.forEach(c -> c.setFont(axisResetFont.get(index)));
        axisResetZeroComponents.forEach(c -> c.setFont(axisResetZeroFont.get(index)));
        propertyLabelComponents.forEach(c -> c.setFont(propertyLabelFont.get(index)));
        speedValueComponents.forEach(c -> c.setFont(speedValueFont.get(index)));
        activeStateLabelComponents.forEach(c -> c.setFont(activeStateFont.get(index)));
    }

    public void addWorkCoordinateLabel(JComponent... label) {
        workCoordinateComponents.addAll(Arrays.asList(label));
    }

    public void addMachineCoordinateLabel(JComponent... label) {
        machineCoordinateComponents.addAll(Arrays.asList(label));
    }

    public void addAxisResetLabel(JLabel... label) {
        axisResetComponents.addAll(Arrays.asList(label));
    }

    public void addAxisResetZeroLabel(JLabel... label) {
        axisResetZeroComponents.addAll(Arrays.asList(label));
    }

    public void addPropertyLabel(JLabel... label) {
        propertyLabelComponents.addAll(Arrays.asList(label));
    }

    public void addSpeedLabel(JLabel... label) {
        speedValueComponents.addAll(Arrays.asList(label));
    }

    public void addActiveStateLabel(JLabel... labels) {
        activeStateLabelComponents.addAll(Arrays.asList(labels));
    }

    public void registerFonts(GraphicsEnvironment ge) {
        List<Font> all = new ArrayList<>();
        all.addAll(workCoordinateFont);
        all.addAll(machineCoordinateFont);
        all.addAll(axisResetFont);
        all.addAll(axisResetZeroFont);
        all.addAll(propertyLabelFont);
        all.addAll(speedValueFont);
        all.forEach(ge::registerFont);
    }

    public static Font createFont(InputStream is, String fontName) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            is.close();
            return font;
        } catch (Exception exc) {
            exc.printStackTrace();
            System.err.println(fontName + " not loaded.  Using serif font.");
            return new Font("sans-serif", Font.PLAIN, 24);
        }
    }
}
