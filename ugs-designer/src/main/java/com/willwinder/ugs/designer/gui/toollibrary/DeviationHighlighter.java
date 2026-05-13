/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.UIManager;
import java.awt.Color;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Paints the background of editable fields amber whenever the live value deviates from a
 * library-provided reference value. Reference is looked up via a {@link Supplier} so the caller
 * can swap it whenever a new library tool is picked.
 */
public final class DeviationHighlighter {
    public static final Color DEVIATION_COLOR = new Color(0xFFF3CD);
    private static final double EPSILON = 1e-6;

    private DeviationHighlighter() {
    }

    public static void attachDouble(TextFieldWithUnit field, Supplier<Double> reference) {
        field.addPropertyChangeListener("value", evt -> updateDouble(field, reference));
        updateDouble(field, reference);
    }

    public static void attachText(JTextField field, Supplier<String> reference) {
        field.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { updateText(field, reference); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { updateText(field, reference); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { updateText(field, reference); }
        });
        updateText(field, reference);
    }

    public static <T> void attachCombo(JComboBox<T> combo, Supplier<T> reference) {
        combo.addItemListener(e -> updateCombo(combo, reference));
        updateCombo(combo, reference);
    }

    public static void updateDouble(TextFieldWithUnit field, Supplier<Double> reference) {
        Double ref = reference.get();
        double current = field.getDoubleValue();
        boolean deviates = ref != null && Math.abs(current - ref) > EPSILON;
        paint(field, deviates);
    }

    public static void updateText(JTextField field, Supplier<String> reference) {
        String ref = reference.get();
        String current = field.getText();
        boolean deviates = ref != null && !Objects.equals(ref, current);
        paint(field, deviates);
    }

    public static <T> void updateCombo(JComboBox<T> combo, Supplier<T> reference) {
        T ref = reference.get();
        Object current = combo.getSelectedItem();
        boolean deviates = ref != null && !Objects.equals(ref, current);
        paint(combo, deviates);
    }

    private static void paint(javax.swing.JComponent component, boolean deviates) {
        if (deviates) {
            component.setOpaque(true);
            component.setBackground(DEVIATION_COLOR);
        } else {
            Color fallback = UIManager.getColor(component instanceof JComboBox
                    ? "ComboBox.background" : "TextField.background");
            component.setBackground(fallback != null ? fallback : Color.WHITE);
        }
        component.repaint();
    }
}
