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
package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.ugs.designer.gui.expression.MathExpressionParser;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.function.UnaryOperator;

/**
 * A TextField that renders a unit "suffix" on the right side when NOT editing (i.e. not focused),
 * and accepts numbers or arithmetic expressions such as {@code 25.4 * 2} or {@code (10 + 4) / 2}.
 * <p>
 * Numeric behavior:
 * - Filters input to the characters of an expression: digits, '.', '+', '-', '*', '/', parentheses and spaces
 * - Exposes a doubleValueProperty() kept in sync with the text, evaluating it whenever it is a complete expression
 * - On Enter and focus lost: replaces the text with its value, or repairs incomplete text such as "" / "-" / "2 *"
 * <p>
 * Styling:
 * - Adds style class: "unit-text-field"
 * - The unit label adds style class: "unit-label"
 * - Adds pseudo-class ":showing-unit" when the unit is visible
 */
public class UnitTextField extends TextField {

    private final SimpleObjectProperty<Unit> unit = new SimpleObjectProperty<>(this, "unit", Unit.MM);
    private final DoubleProperty doubleValue = new SimpleDoubleProperty(this, "doubleValue", 0.0);

    private final DecimalFormat displayFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US));

    private boolean updatingTextFromValue = false;
    private boolean updatingValueFromText = false;

    public UnitTextField() {
        this(new UnitValue(Unit.MM, 0), Unit.MM);
    }

    public UnitTextField(UnitValue value, Unit displayUnits) {
        super();
        getStyleClass().add("unit-text-field");
        setUnit(displayUnits);

        installNumericTextFormatter();
        installSyncBehavior();

        setValue(value.convertTo(displayUnits).doubleValue());
        setText(format(getValue()));
    }

    public final Unit getUnit() {
        return unit.get();
    }

    public final void setUnit(Unit unit) {
        UnitValue currentValue = getUnitValue();
        this.doubleValue.set(currentValue.convertTo(unit).doubleValue());
        this.unit.set(unit);
    }

    private UnitValue getUnitValue() {
        return new UnitValue(this.unit.get(), getValue());
    }

    public final ObservableValue<Unit> unitProperty() {
        return unit;
    }

    public final double getValue() {
        return doubleValue.get();
    }

    public final void setValue(double value) {
        doubleValue.set(value);
    }

    protected final DoubleProperty valueProperty() {
        return doubleValue;
    }

    public ObservableValue<UnitValue> unitValueProperty() {
        return doubleValue.map(v -> new UnitValue(this.unit.get(), v));
    }


    private void installNumericTextFormatter() {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String next = change.getControlNewText();
            if (next == null || next.isEmpty()) return change; // allow clearing while editing

            return MathExpressionParser.isExpressionText(next) ? change : null;
        };

        TextFormatter<?> current = getTextFormatter();
        if (current == null || current.getFilter() != filter) {
            setTextFormatter(new TextFormatter<>(filter));
        }
    }

    private void installSyncBehavior() {
        // Text -> value (when user edits)
        textProperty().addListener((obs, oldV, newV) -> {
            if (updatingTextFromValue) return;

            OptionalDouble parsed = NumericExpression.evaluate(newV);
            if (parsed.isEmpty()) return; // incomplete expressions such as "-" or "2 *"

            try {
                updatingValueFromText = true;
                setValue(parsed.getAsDouble());
            } finally {
                updatingValueFromText = false;
            }
        });

        // Value -> text (when programmatically set / bound)
        valueProperty().addListener((obs, oldV, newV) -> {
            if (updatingValueFromText) return;

            String formatted = format(newV == null ? 0.0 : newV.doubleValue());
            if (formatted.equals(getText())) return;

            try {
                updatingTextFromValue = true;
                setText(formatted);
            } finally {
                updatingTextFromValue = false;
            }
        });

        focusedProperty().addListener((_, _, isNowFocused) -> {
            if (!Boolean.TRUE.equals(isNowFocused)) {
                normalizeText();
            }
        });
        addEventHandler(ActionEvent.ACTION, _ -> normalizeText());
    }

    private void normalizeText() {
        setText(format(NumericExpression.evaluate(getText()).orElse(getValue())));
    }

    private String format(double v) {
        return displayFormat.format(v);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new UnitTextFieldSkin(this);
    }
}