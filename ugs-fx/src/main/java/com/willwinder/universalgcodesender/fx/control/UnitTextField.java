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

import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * A TextField that renders a unit "suffix" on the right side when NOT editing (i.e. not focused),
 * and only accepts numeric input.
 * <p>
 * Numeric behavior:
 * - Filters input to a numeric pattern (optionally negative)
 * - Exposes a doubleValueProperty() kept in sync with the text
 * - On focus lost: normalizes/repairs invalid intermediate text ("" / "-" / "." / "-.")
 * <p>
 * Styling:
 * - Adds style class: "unit-text-field"
 * - The unit label adds style class: "unit-label"
 * - Adds pseudo-class ":showing-unit" when the unit is visible
 */
public class UnitTextField extends TextField {
    private static final Pattern NUMERIC = Pattern.compile("^-?\\d*(?:\\.\\d*)?$");
    private static final Pattern NUMERIC_NON_NEGATIVE = Pattern.compile("^\\d*(?:\\.\\d*)?$");

    private final SimpleObjectProperty<Unit> unit = new SimpleObjectProperty<>(this, "unit", Unit.MM);
    private final DoubleProperty doubleValue = new SimpleDoubleProperty(this, "doubleValue", 0.0);

    private final DecimalFormat displayFormat = new DecimalFormat("0.###", DecimalFormatSymbols.getInstance(Locale.US));

    private boolean allowNegative = true;
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

    protected final void setValue(double value) {
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

            Pattern p = allowNegative ? NUMERIC : NUMERIC_NON_NEGATIVE;
            return p.matcher(next).matches() ? change : null;
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

            Double parsed = parseOrNull(newV);
            if (parsed == null) return; // intermediate states ("-", ".", "-.") etc.

            try {
                updatingValueFromText = true;
                setValue(parsed);
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

        // Normalize on focus lost
        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (Boolean.TRUE.equals(isNowFocused)) return;

            Double parsed = parseOrNull(getText());
            if (parsed == null) {
                // revert to last known good value
                setText(format(getValue()));
            } else {
                // normalize formatting (e.g. "01.0" -> "1")
                setText(format(parsed));
            }
        });
    }

    private String format(double v) {
        return displayFormat.format(v);
    }

    private static Double parseOrNull(String text) {
        if (text == null) return null;
        String t = text.trim();
        if (t.isEmpty() || "-".equals(t) || ".".equals(t) || "-.".equals(t)) return null;

        try {
            // We only allow '.' as decimal separator in the filter; parse accordingly.
            return Double.parseDouble(t);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new UnitTextFieldSkin(this);
    }
}