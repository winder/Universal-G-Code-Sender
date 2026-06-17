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
package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.entities.entities.EntityListener;
import com.willwinder.ugs.designer.entities.entities.EventType;
import com.willwinder.ugs.designer.entities.entities.EntitySetting;
import com.willwinder.ugs.designer.entities.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.entities.cuttable.ToolPathDirection;
import com.willwinder.ugs.designer.actions.UndoableAction;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.fx.component.CollapsibleTitledPane;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

/**
 * Floating panel that displays the entity settings common to all selected Cuttable
 * entities, and applies edits to the entire selection. Hidden when no Cuttable is
 * selected.
 */
public class EntitySettingsPanel extends VBox {
    private static final List<EntitySetting> TRANSFORM_KEYS = List.of(
            EntitySetting.POSITION_X,
            EntitySetting.POSITION_Y,
            EntitySetting.WIDTH,
            EntitySetting.HEIGHT,
            EntitySetting.ROTATION);

    private static final List<EntitySetting> CUT_KEYS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.START_DEPTH,
            EntitySetting.TARGET_DEPTH,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.FEED_RATE,
            EntitySetting.PASSES,
            EntitySetting.LEAD_IN_PERCENT,
            EntitySetting.TOOL_PATH_ANGLE,
            EntitySetting.DIRECTION,
            EntitySetting.TOOL_PATH_DIRECTION,
            EntitySetting.INCLUDE_IN_EXPORT);

    // Entity-property events that should refresh field values without rebuilding the UI.
    private static final EnumSet<EventType> VALUE_REFRESH_EVENTS = EnumSet.of(
            EventType.MOVED, EventType.RESIZED, EventType.ROTATED, EventType.SETTINGS_CHANGED);

    private final SelectionManager selectionManager;
    private final SelectionListener selectionListener;
    private final EntityListener entityListener;
    private boolean applyingEdit = false;

    public EntitySettingsPanel() {
        getStyleClass().add("entity-settings-panel");
        setSpacing(12);
        setPadding(new javafx.geometry.Insets(12));

        this.selectionManager = ControllerFactory.getController().getSelectionManager();
        this.selectionListener = event -> Platform.runLater(this::rebuild);
        selectionManager.addSelectionListener(selectionListener);

        // Refresh values (not structure) when the entities themselves change.
        this.entityListener = event -> {
            if (applyingEdit) return;
            if (VALUE_REFRESH_EVENTS.contains(event.getType())) {
                Platform.runLater(this::rebuild);
            }
        };
        ControllerFactory.getController().getDrawing().getRootEntity().addListener(entityListener);

        rebuild();
    }

    private void rebuild() {
        List<Cuttable> cuttables = selectionManager.getSelection().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .toList();

        getChildren().clear();
        if (cuttables.isEmpty()) {
            Label placeholder = new Label("Select a shape to edit its properties.");
            placeholder.setWrapText(true);
            getChildren().add(placeholder);
            return;
        }

        Set<EntitySetting> common = intersection(cuttables);

        VBox transformRows = buildRows(TRANSFORM_KEYS, common, cuttables);
        if (!transformRows.getChildren().isEmpty()) {
            getChildren().add(new CollapsibleTitledPane("Transform", transformRows));
        }

        VBox cutRows = buildRows(CUT_KEYS, common, cuttables);
        if (!cutRows.getChildren().isEmpty()) {
            getChildren().add(new CollapsibleTitledPane("Cut", cutRows));
        }
    }

    private static Set<EntitySetting> intersection(List<Cuttable> cuttables) {
        Set<EntitySetting> common = new LinkedHashSet<>(cuttables.get(0).getSettings());
        for (int i = 1; i < cuttables.size(); i++) {
            common.retainAll(cuttables.get(i).getSettings());
        }
        return common;
    }

    private VBox buildRows(List<EntitySetting> keys, Set<EntitySetting> common, List<Cuttable> cuttables) {
        VBox rows = new VBox(6);
        for (EntitySetting key : keys) {
            if (!common.contains(key)) continue;
            Region control = buildControl(key, cuttables);
            if (control != null) {
                rows.getChildren().add(new SettingsRow(key.getLabel(), control));
            }
        }
        return rows;
    }

    private Region buildControl(EntitySetting key, List<Cuttable> entities) {
        return switch (key) {
            case POSITION_X -> doubleField(
                    e -> e.getPosition().getX(),
                    (entity, v) -> entity.setPosition(new java.awt.geom.Point2D.Double(v, entity.getPosition().getY())),
                    entities, Unit.MM);
            case POSITION_Y -> doubleField(
                    e -> e.getPosition().getY(),
                    (entity, v) -> entity.setPosition(new java.awt.geom.Point2D.Double(entity.getPosition().getX(), v)),
                    entities, Unit.MM);
            case WIDTH -> doubleField(
                    e -> e.getSize().getWidth(),
                    (entity, v) -> entity.setSize(new Size(v, entity.getSize().getHeight())),
                    entities, Unit.MM);
            case HEIGHT -> doubleField(
                    e -> e.getSize().getHeight(),
                    (entity, v) -> entity.setSize(new Size(entity.getSize().getWidth(), v)),
                    entities, Unit.MM);
            case ROTATION -> doubleField(Cuttable::getRotation, Cuttable::setRotation, entities, Unit.DEGREE);
            case CUT_TYPE -> cutTypeCombo(entities);
            case START_DEPTH -> doubleField(Cuttable::getStartDepth, Cuttable::setStartDepth, entities, Unit.MM);
            case TARGET_DEPTH -> doubleField(Cuttable::getTargetDepth, Cuttable::setTargetDepth, entities, Unit.MM);
            case SPINDLE_SPEED ->
                    intField(Cuttable::getSpindleSpeed, Cuttable::setSpindleSpeed, entities, Unit.PERCENT);
            case FEED_RATE -> intField(Cuttable::getFeedRate, Cuttable::setFeedRate, entities, Unit.METERS_PER_SECOND);
            case PASSES -> intField(Cuttable::getPasses, Cuttable::setPasses, entities, Unit.TIMES);
            case LEAD_IN_PERCENT ->
                    intField(Cuttable::getLeadInPercent, Cuttable::setLeadInPercent, entities, Unit.PERCENT);
            case TOOL_PATH_ANGLE ->
                    doubleField(Cuttable::getToolPathAngle, Cuttable::setToolPathAngle, entities, Unit.DEGREE);
            case DIRECTION -> enumCombo(Direction.values(), Direction::getLabel,
                    Cuttable::getDirection, Cuttable::setDirection, entities);
            case TOOL_PATH_DIRECTION -> enumCombo(ToolPathDirection.values(),
                    ToolPathDirection::getLabel, Cuttable::getToolPathDirection, Cuttable::setToolPathDirection, entities);
            case INCLUDE_IN_EXPORT ->
                    switchControl(Cuttable::getIncludeInExport, Cuttable::setIncludeInExport, entities);
            default -> null;
        };
    }

    private TextField doubleField(ToDoubleFunction<Cuttable> getter,
                                  CuttableDoubleSetter setter,
                                  List<Cuttable> entities, Unit units) {
        UnitTextField field = numericField(getter.applyAsDouble(entities.get(0)), entities, getter::applyAsDouble, units);
        bindLivePreviewWithUndo(field, getter, setter, entities);
        return field;
    }

    private TextField intField(ToIntFunction<Cuttable> getter,
                               CuttableIntSetter setter,
                               List<Cuttable> entities, Unit units) {
        boolean mixed = entities.stream().mapToInt(getter).distinct().count() > 1;
        UnitTextField field = new UnitTextField(new UnitValue(units, mixed ? 0 : getter.applyAsInt(entities.get(0))), units);
        field.setPromptText(mixed ? "—" : "");
        field.setPrefWidth(120);
        commitOnEditInt(field, parsed -> applyToAll(entities, e -> setter.set(e, parsed)));
        return field;
    }

    private UnitTextField numericField(double initial, List<Cuttable> entities, ToDoubleFunction<Cuttable> getter, Unit units) {
        boolean mixed = entities.stream().mapToDouble(getter).distinct().count() > 1;
        UnitTextField field = new UnitTextField(new UnitValue(units, mixed ? 0 : initial), units);
        field.setPromptText(mixed ? "—" : "");
        field.setPrefWidth(120);
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
            if (applyingEdit || newVal == null) return;
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

    private ComboBox<CutType> cutTypeCombo(List<Cuttable> entities) {
        // Show the intersection of available cut types so we never set an invalid value.
        Set<CutType> available = new LinkedHashSet<>(entities.get(0).getAvailableCutTypes());
        for (int i = 1; i < entities.size(); i++) {
            available.retainAll(entities.get(i).getAvailableCutTypes());
        }

        ComboBox<CutType> combo = new ComboBox<>(FXCollections.observableArrayList(available));
        boolean mixed = entities.stream().map(Cuttable::getCutType).distinct().count() > 1;
        combo.setValue(mixed ? null : entities.get(0).getCutType());
        combo.setPromptText(mixed ? "—" : "");
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CutType value) {
                return value == null ? "" : value.getName();
            }

            @Override
            public CutType fromString(String s) {
                return null;
            }
        });
        combo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal != oldVal) {
                applyToAll(entities, e -> e.setCutType(newVal));
            }
        });
        return combo;
    }

    private <E extends Enum<E>> ComboBox<E> enumCombo(E[] values,
                                                      java.util.function.Function<E, String> labelFn,
                                                      java.util.function.Function<Cuttable, E> getter,
                                                      java.util.function.BiConsumer<Cuttable, E> setter,
                                                      List<Cuttable> entities) {
        ComboBox<E> combo = new ComboBox<>(FXCollections.observableArrayList(values));
        boolean mixed = entities.stream().map(getter).distinct().count() > 1;
        combo.setValue(mixed ? null : getter.apply(entities.get(0)));
        combo.setPromptText(mixed ? "—" : "");
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

    private SwitchButton switchControl(java.util.function.Function<Cuttable, Boolean> getter,
                                       java.util.function.BiConsumer<Cuttable, Boolean> setter,
                                       List<Cuttable> entities) {
        SwitchButton sw = new SwitchButton();
        boolean mixed = entities.stream().map(getter).distinct().count() > 1;
        // SwitchButton has no tri-state; show as off when mixed.
        sw.selectedProperty().set(!mixed && getter.apply(entities.get(0)));
        sw.selectedProperty().addListener((obs, oldVal, newVal) ->
                applyToAll(entities, e -> setter.accept(e, newVal)));
        return sw;
    }

    private void applyToAll(List<Cuttable> entities, Consumer<Cuttable> action) {
        applyingEdit = true;
        try {
            entities.forEach(action);
        } finally {
            applyingEdit = false;
        }
    }

    @FunctionalInterface
    private interface CuttableDoubleSetter {
        void set(Cuttable entity, double value);
    }

    @FunctionalInterface
    private interface CuttableIntSetter {
        void set(Cuttable entity, int value);
    }
}
