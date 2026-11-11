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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.ugs.designer.gui.expression.ExpressionAwareTextFieldFormatter;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.text.DefaultFormatterFactory;
import java.awt.Component;
import java.text.ParseException;
import java.util.OptionalDouble;
import java.util.Set;

/**
 * Asks for the distance a constraint should keep, accepting math expressions such as
 * {@code 25.4 * 2}, and applies it as one undoable change.
 */
public final class ConstraintDistanceDialog {
    private static final int DECIMALS = 4;

    private ConstraintDistanceDialog() {
    }

    /**
     * Asks for the distance once the current mouse event has been handled, so the gesture that
     * asked for it finishes before the dialog takes the input.
     */
    public static void editDistanceLater(Controller controller, DistanceConstraint constraint) {
        SwingUtilities.invokeLater(() -> editDistance(controller, constraint));
    }

    private static void editDistance(Controller controller, DistanceConstraint constraint) {
        askForDistance(controller.getDrawing(), Math.abs(constraint.getDistance()))
                .ifPresent(value -> controller.changeConstraintDistance(constraint, value));
        controller.repaintDrawing();
    }

    // Empty if cancelled or left unchanged, so the exact distance is kept rather than the rounded one the field shows
    private static OptionalDouble askForDistance(Component parent, double currentDistance) {
        ExpressionAwareTextFieldFormatter editFormatter = new ExpressionAwareTextFieldFormatter(Unit.MM, DECIMALS, false);
        TextFieldWithUnit field = new TextFieldWithUnit(Unit.MM, DECIMALS, currentDistance);
        field.setFormatterFactory(new DefaultFormatterFactory(
                new ExpressionAwareTextFieldFormatter(Unit.MM, DECIMALS),
                new ExpressionAwareTextFieldFormatter(Unit.MM, DECIMALS),
                editFormatter));
        field.setValue(currentDistance);
        field.setColumns(10);
        field.addAncestorListener(new RequestFocusListener());
        Set<String> unchangedTexts = Set.of(field.getText().trim(), format(editFormatter, currentDistance));

        JPanel panel = new JPanel(new MigLayout("insets 0, gap 10", "[right][grow, fill]"));
        panel.add(new JLabel(Localization.getString("designer.constraint.distance")));
        panel.add(field);

        int result = JOptionPane.showConfirmDialog(parent, panel, Localization.getString("designer.constraint.distance.title"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION || unchangedTexts.contains(field.getText().trim())) {
            return OptionalDouble.empty();
        }
        try {
            field.commitEdit();
            return field.getValue() instanceof Number number ? OptionalDouble.of(number.doubleValue()) : OptionalDouble.empty();
        } catch (ParseException e) {
            return OptionalDouble.empty();
        }
    }

    private static String format(ExpressionAwareTextFieldFormatter formatter, double value) {
        try {
            return formatter.valueToString(value).trim();
        } catch (ParseException e) {
            return "";
        }
    }

    private static class RequestFocusListener implements AncestorListener {
        @Override
        public void ancestorAdded(AncestorEvent event) {
            JComponent component = event.getComponent();
            SwingUtilities.invokeLater(() -> {
                component.requestFocusInWindow();
                if (component instanceof TextFieldWithUnit textField) {
                    textField.selectAll();
                }
            });
            component.removeAncestorListener(this);
        }

        @Override
        public void ancestorRemoved(AncestorEvent event) {
            // Not used
        }

        @Override
        public void ancestorMoved(AncestorEvent event) {
            // Not used
        }
    }
}
