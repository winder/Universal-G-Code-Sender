/*
    Copyright 2016-2020 Will Winder

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
package com.willwinder.ugs.nbp.dro;

import com.willwinder.universalgcodesender.utils.FontUtils;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import javax.swing.JComponent;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FontManager {

    private final List<Font> workCoordinateFont = new ArrayList<>(3);
    private final List<Font> machineCoordinateFont = new ArrayList<>(3);
    private final List<Font> propertyLabelFont = new ArrayList<>(3);
    private final List<Font> activeStateFont = new ArrayList<>(3);

    private final List<JComponent> workCoordinateComponents = new ArrayList<>();
    private final List<JComponent> machineCoordinateComponents = new ArrayList<>();
    private final List<JComponent> propertyLabelComponents = new ArrayList<>();
    private final List<JComponent> speedValueComponents = new ArrayList<>();
    private final List<JComponent> activeStateLabelComponents = new ArrayList<>();

    public void init() {
        Font font = FontUtils.getLcdFont();
        Font boldFont = FontUtils.getSansBoldFont();
        Font regularFont = FontUtils.getSansFont();

        workCoordinateFont.add(font.deriveFont(Font.PLAIN, 20));
        workCoordinateFont.add(font.deriveFont(Font.PLAIN, 26));
        workCoordinateFont.add(font.deriveFont(Font.PLAIN, 34));

        machineCoordinateFont.add(font.deriveFont(Font.PLAIN, 14));
        machineCoordinateFont.add(font.deriveFont(Font.PLAIN, 18));
        machineCoordinateFont.add(font.deriveFont(Font.PLAIN, 24));

        activeStateFont.add(boldFont.deriveFont(Font.PLAIN, 20));
        activeStateFont.add(boldFont.deriveFont(Font.PLAIN, 26));
        activeStateFont.add(boldFont.deriveFont(Font.PLAIN, 34));

        propertyLabelFont.add(regularFont.deriveFont(Font.PLAIN, 12));
        propertyLabelFont.add(regularFont.deriveFont(Font.PLAIN, 14));
        propertyLabelFont.add(regularFont.deriveFont(Font.PLAIN, 17));
    }

    public void applyFonts(int size) {
        ThreadHelper.invokeLater(() -> {
            int index = Math.max(0, Math.min(2, size));
            workCoordinateComponents.forEach(c -> c.setFont(workCoordinateFont.get(index)));
            machineCoordinateComponents.forEach(c -> c.setFont(machineCoordinateFont.get(index)));
            propertyLabelComponents.forEach(c -> c.setFont(propertyLabelFont.get(index)));
            speedValueComponents.forEach(c -> c.setFont(machineCoordinateFont.get(index)));
            activeStateLabelComponents.forEach(c -> c.setFont(activeStateFont.get(index)));
        });
    }

    public void addWorkCoordinateLabel(JComponent... label) {
        workCoordinateComponents.addAll(Arrays.asList(label));
    }

    public void addMachineCoordinateLabel(JComponent... label) {
        machineCoordinateComponents.addAll(Arrays.asList(label));
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
        all.addAll(propertyLabelFont);
        all.forEach(ge::registerFont);
    }

}
