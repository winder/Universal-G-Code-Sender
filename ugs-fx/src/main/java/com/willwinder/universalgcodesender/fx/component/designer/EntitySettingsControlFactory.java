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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * Builds the controls used by {@link EntitySettingsPanel}, each bound to the whole selection.
 * Numeric fields preview edits live in the visualizer and record a single undoable action per edit;
 * combos and switches apply immediately. Writes back into the controls are wrapped in a shared
 * {@link EditGuard} so the panel's own updates don't loop back through these listeners.
 */
class EntitySettingsControlFactory {
    private static final String MIXED_VALUE_PROMPT = "—";

    private final EditGuard guard;
    private final Consumer<Runnable> valueRefresherRegistry;

    EntitySettingsControlFactory(EditGuard guard, Consumer<Runnable> valueRefresherRegistry) {
        this.guard = guard;
        this.valueRefresherRegistry = valueRefresherRegistry;
    }

    UnitTextField doubleField(ToDoubleFunction<Cuttable> getter,
                              CuttableDoubleSetter setter,
                              List<Cuttable> entities, Unit units) {
        UnitTextField field = numericField(entities, getter, units);
        bindLivePreviewWithUndo(field, getter, setter, entities);
        valueRefresherRegistry.accept(() -> refreshDoubleField(field, getter, entities));
        return field;
    }

    TextField intField(ToIntFunction<Cuttable> getter,
                       CuttableIntSetter setter,
                       List<Cuttable> entities, Unit units) {
        boolean mixed = entities.stream().mapToInt(getter).distinct().count() > 1;
        UnitTextField field = new UnitTextField(new UnitValue(units, mixed ? 0 : getter.applyAsInt(entities.get(0))), units);
        field.setPromptText(mixed ? MIXED_VALUE_PROMPT : "");
        commitOnEditInt(field, parsed -> applyToAll(entities, e -> setter.set(e, parsed)));
        return field;
    }

    ComboBox<CutType> cutTypeCombo(List<Cuttable> entities) {
        // Show the intersection of available cut types so we never set an invalid value.
        Set<CutType> available = new LinkedHashSet<>(entities.get(0).getAvailableCutTypes());
        for (int i = 1; i < entities.size(); i++) {
            available.retainAll(entities.get(i).getAvailableCutTypes());
        }
        return enumCombo(available.toArray(new CutType[0]), CutType::getName,
                Cuttable::getCutType, Cuttable::setCutType, entities);
    }

    <E> ComboBox<E> enumCombo(E[] values,
                              Function<E, String> labelFn,
                              Function<Cuttable, E> getter,
                              BiConsumer<Cuttable, E> setter,
                              List<Cuttable> entities) {
        ComboBox<E> combo = new ComboBox<>(FXCollections.observableArrayList(values));
        boolean mixed = entities.stream().map(getter).distinct().count() > 1;
        combo.setValue(mixed ? null : getter.apply(entities.get(0)));
        combo.setPromptText(mixed ? MIXED_VALUE_PROMPT : "");
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(E value) {
                return value == null ? "" : labelFn.apply(value);
            }

            @Override
            public E fromString(String s) {
                return null;
            }
        });
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != oldVal) {
                applyToAll(entities, e -> setter.accept(e, newVal));
            }
        });
        return combo;
    }

    SwitchButton switchControl(Function<Cuttable, Boolean> getter,
                               BiConsumer<Cuttable, Boolean> setter,
                               List<Cuttable> entities) {
        SwitchButton sw = new SwitchButton();
        boolean mixed = entities.stream().map(getter).distinct().count() > 1;
        // SwitchButton has no tri-state; show as off when mixed.
        sw.selectedProperty().set(!mixed && getter.apply(entities.get(0)));
        sw.selectedProperty().addListener((obs, oldVal, newVal) ->
                applyToAll(entities, e -> setter.accept(e, newVal)));
        return sw;
    }

    private UnitTextField numericField(List<Cuttable> entities, ToDoubleFunction<Cuttable> getter, Unit units) {
        boolean mixed = entities.stream().mapToDouble(getter).distinct().count() > 1;
        UnitTextField field = new UnitTextField(new UnitValue(units, mixed ? 0 : getter.applyAsDouble(entities.get(0))), units);
        field.setPromptText(mixed ? MIXED_VALUE_PROMPT : "");
        return field;
    }

    /**
     * Applies a numeric field's value to the whole selection on every valid keystroke so the
     * change is shown live in the visualizer, but only records a single undoable action when the
     * edit is committed (the field is blurred, or Enter is pressed). This keeps the undo stack to
     * one entry per edit instead of one per keystroke.
     */
    private void bindLivePreviewWithUndo(UnitTextField field,
                                         ToDoubleFunction<Cuttable> getter,
                                         CuttableDoubleSetter setter,
                                         List<Cuttable> entities) {
        // The per-entity values captured when the edit session begins; used to build the undo action.
        Map<Cuttable, Double> originalValues = new HashMap<>();

        // Live preview: push every valid value straight to the entities (no undo entry yet).
        field.unitValueProperty().addListener((obs, oldVal, newVal) -> {
            if (guard.isActive() || newVal == null) return;
            applyToAll(entities, e -> setter.set(e, newVal.value().doubleValue()));
        });

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (Boolean.TRUE.equals(isFocused)) {
                captureOriginalValues(getter, entities, originalValues);
            } else {
                commitEdit(setter, entities, originalValues, field.getValue());
            }
        });

        // Enter commits without waiting for the field to lose focus, then starts a fresh session.
        field.setOnAction(e -> {
            commitEdit(setter, entities, originalValues, field.getValue());
            captureOriginalValues(getter, entities, originalValues);
        });
    }

    private void captureOriginalValues(ToDoubleFunction<Cuttable> getter,
                                       List<Cuttable> entities,
                                       Map<Cuttable, Double> originalValues) {
        originalValues.clear();
        entities.forEach(e -> originalValues.put(e, getter.applyAsDouble(e)));
    }

    private void commitEdit(CuttableDoubleSetter setter,
                            List<Cuttable> entities,
                            Map<Cuttable, Double> originalValues,
                            double finalValue) {
        if (originalValues.isEmpty()) {
            return;
        }

        Map<Cuttable, Double> before = new HashMap<>(originalValues);
        originalValues.clear();

        boolean changed = before.values().stream().anyMatch(original -> original != finalValue);
        if (!changed) {
            return;
        }

        // The live preview has already applied finalValue, so the action only needs to record how
        // to redo it and how to revert each entity to the value it had before the edit started.
        UndoableAction action = new UndoableAction() {
            @Override
            public void redo() {
                entities.forEach(e -> setter.set(e, finalValue));
            }

            @Override
            public void undo() {
                entities.forEach(e -> setter.set(e, before.get(e)));
            }
        };
        ControllerFactory.getController().getUndoManager().addAction(action);
    }

    private void commitOnEditInt(TextField field, IntConsumer commit) {
        Runnable doCommit = () -> {
            String text = field.getText();
            if (text == null || text.isBlank()) return;
            try {
                commit.accept(Integer.parseInt(text.trim()));
            } catch (NumberFormatException ignore) {
            }
        };
        field.setOnAction(e -> doCommit.run());
        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (Boolean.FALSE.equals(newVal)) doCommit.run();
        });
    }

    private void refreshDoubleField(UnitTextField field, ToDoubleFunction<Cuttable> getter, List<Cuttable> entities) {
        // Don't stomp on a value the user is currently editing.
        if (field.isFocused()) {
            return;
        }
        boolean mixed = entities.stream().mapToDouble(getter).distinct().count() > 1;
        guard.run(() -> {
            field.setValue(mixed ? 0 : getter.applyAsDouble(entities.get(0)));
            field.setPromptText(mixed ? MIXED_VALUE_PROMPT : "");
        });
    }

    private void applyToAll(List<Cuttable> entities, Consumer<Cuttable> action) {
        guard.run(() -> entities.forEach(action));
    }

    @FunctionalInterface
    interface CuttableDoubleSetter {
        void set(Cuttable entity, double value);
    }

    @FunctionalInterface
    interface CuttableIntSetter {
        void set(Cuttable entity, int value);
    }
}
