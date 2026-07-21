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

import com.willwinder.ugs.designer.entities.Anchor;
import com.willwinder.ugs.designer.entities.EntityListener;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.EntitySetting;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.DesignFlipHorizontalAction;
import com.willwinder.universalgcodesender.fx.actions.DesignFlipVerticalAction;
import com.willwinder.universalgcodesender.fx.component.CollapsibleTitledPane;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.model.Unit;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

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

    // Pure transforms never change the set of controls, so they only need a cheap value refresh
    // rather than a full (and, during a drag, sluggish) structural rebuild.
    private static final EnumSet<EventType> TRANSFORM_EVENTS = EnumSet.of(
            EventType.MOVED, EventType.RESIZED, EventType.ROTATED);
    public static final int SPACING = 12;

    private final SelectionManager selectionManager;
    private final BooleanProperty aspectRatioLocked = new SimpleBooleanProperty(false);
    private final Map<Cuttable, Double> aspectRatios = new HashMap<>();
    /**
     * Shared across all rows so every label grows to the width of the longest label. Kept for the
     * lifetime of the panel (not per rebuild) so labels don't collapse and re-grow on every event.
     */
    private final DoubleProperty labelWidth = new SimpleDoubleProperty(0);

    /**
     * Per-field updaters that re-read the current entity values into the existing controls, rebuilt
     * alongside the rows. Used to refresh values during transforms without recreating the UI.
     */
    private final List<Runnable> valueRefreshers = new ArrayList<>();
    private final EditGuard editGuard = new EditGuard();
    private final EntitySettingsControlFactory controls = new EntitySettingsControlFactory(editGuard, valueRefreshers::add);
    private Anchor positionAnchor = Anchor.BOTTOM_LEFT;
    private boolean valueRefreshScheduled = false;

    public EntitySettingsPanel() {
        getStyleClass().add("entity-settings-panel");
        setSpacing(12);
        setPadding(new javafx.geometry.Insets(12));

        this.selectionManager = ControllerFactory.getController().getSelectionManager();
        SelectionListener selectionListener = event -> Platform.runLater(this::rebuild);
        selectionManager.addSelectionListener(selectionListener);

        // Refresh values (not structure) when the entities themselves change.
        EntityListener entityListener = event -> {
            if (editGuard.isActive()) return;
            if (TRANSFORM_EVENTS.contains(event.getType())) {
                scheduleValueRefresh();
            } else if (event.getType() == EventType.SETTINGS_CHANGED) {
                Platform.runLater(this::rebuild);
            }
        };
        ControllerFactory.getController().getDrawing().getRootEntity().addListener(entityListener);
        selectionManager.addListener(entityListener);

        rebuild();
    }

    private void rebuild() {
        List<Cuttable> cuttables = selectionManager.getSelection().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .toList();

        getChildren().clear();
        valueRefreshers.clear();
        if (cuttables.isEmpty()) {
            Label placeholder = new Label("Select a shape to edit its properties.");
            placeholder.setWrapText(true);
            getChildren().add(placeholder);
            return;
        }

        Set<EntitySetting> common = intersection(cuttables);

        VBox transformRows = buildRows(TRANSFORM_KEYS, common, cuttables, labelWidth);
        if (!transformRows.getChildren().isEmpty()) {
            getChildren().add(new CollapsibleTitledPane("Transform", transformRows));
        }

        Set<EntitySetting> cutSettings = new LinkedHashSet<>(common);
        cutSettings.retainAll(commonCutTypeSettings(cuttables));
        VBox cutRows = buildRows(CUT_KEYS, cutSettings, cuttables, labelWidth);
        if (!cutRows.getChildren().isEmpty()) {
            getChildren().add(new CollapsibleTitledPane("Cut", cutRows));
        }
    }

    private static Set<EntitySetting> intersection(List<Cuttable> cuttables) {
        return intersect(cuttables.stream().map(Cuttable::getSettings));
    }

    private static Set<EntitySetting> commonCutTypeSettings(List<Cuttable> cuttables) {
        return intersect(cuttables.stream().map(cuttable -> cuttable.getCutType().getSettings()));
    }

    private static Set<EntitySetting> intersect(Stream<? extends Collection<EntitySetting>> settings) {
        return settings
                .<Set<EntitySetting>>map(LinkedHashSet::new)
                .reduce((accumulated, next) -> {
                    accumulated.retainAll(next);
                    return accumulated;
                })
                .orElseGet(LinkedHashSet::new);
    }

    private VBox buildRows(List<EntitySetting> keys, Set<EntitySetting> common, List<Cuttable> cuttables, DoubleProperty labelWidth) {
        boolean groupPosition = common.contains(EntitySetting.POSITION_X) && common.contains(EntitySetting.POSITION_Y);
        boolean positionGroupAdded = false;
        boolean groupSize = common.contains(EntitySetting.WIDTH) && common.contains(EntitySetting.HEIGHT);
        boolean sizeGroupAdded = false;

        VBox rows = new VBox(SPACING);
        for (EntitySetting key : keys) {
            if (!common.contains(key)) continue;

            // Group X and Y into a single block so the anchor selector can sit to the right of both
            // rows. The block takes the position of whichever key comes first.
            if (groupPosition && (key == EntitySetting.POSITION_X || key == EntitySetting.POSITION_Y)) {
                if (!positionGroupAdded) {
                    rows.getChildren().add(buildPositionGroup(cuttables, labelWidth));
                    positionGroupAdded = true;
                }
                continue;
            }

            // Group width and height into a single block so the aspect-ratio lock can sit to the
            // right of both rows. The block takes the position of whichever key comes first.
            if (groupSize && (key == EntitySetting.WIDTH || key == EntitySetting.HEIGHT)) {
                if (!sizeGroupAdded) {
                    rows.getChildren().add(buildSizeGroup(cuttables, labelWidth));
                    sizeGroupAdded = true;
                }
                continue;
            }

            // Put the flip buttons to the right of the rotation field.
            if (key == EntitySetting.ROTATION) {
                rows.getChildren().add(buildRotationGroup(cuttables, labelWidth));
                continue;
            }

            Region control = buildControl(key, cuttables);
            if (control != null) {
                rows.getChildren().add(new EntitySettingsRow(labelWidth, key.getLabel(), control));
            }
        }
        return rows;
    }

    private EntitySettingsRowGroup buildPositionGroup(List<Cuttable> entities, DoubleProperty labelWidth) {
        UnitTextField xField = controls.doubleField(
                e -> e.getPosition(positionAnchor).getX(),
                (entity, v) -> entity.setPosition(positionAnchor,
                        new java.awt.geom.Point2D.Double(v, entity.getPosition(positionAnchor).getY())),
                entities, Unit.MM);

        UnitTextField yField = controls.doubleField(
                e -> e.getPosition(positionAnchor).getY(),
                (entity, v) -> entity.setPosition(positionAnchor,
                        new java.awt.geom.Point2D.Double(entity.getPosition(positionAnchor).getX(), v)),
                entities, Unit.MM);

        // Changing the anchor only changes which point X/Y refer to; the entity does not move, so
        // just refresh the displayed values to the new reference point.
        AnchorSelector anchorSelector = new AnchorSelector();
        anchorSelector.setAnchor(positionAnchor);
        anchorSelector.anchorProperty().addListener((obs, oldAnchor, newAnchor) -> {
            positionAnchor = newAnchor;
            refreshFieldValue(xField, entities, e -> e.getPosition(newAnchor).getX());
            refreshFieldValue(yField, entities, e -> e.getPosition(newAnchor).getY());
        });

        EntitySettingsRow xRow = new EntitySettingsRow(labelWidth, EntitySetting.POSITION_X.getLabel(), xField);
        EntitySettingsRow yRow = new EntitySettingsRow(labelWidth, EntitySetting.POSITION_Y.getLabel(), yField);
        return new EntitySettingsRowGroup(anchorSelector, xRow, yRow);
    }

    private EntitySettingsRowGroup buildRotationGroup(List<Cuttable> entities, DoubleProperty labelWidth) {
        UnitTextField rotationField = controls.doubleField(Cuttable::getRotation, Cuttable::setRotation, entities, Unit.DEGREE);
        EntitySettingsRow rotationRow = new EntitySettingsRow(labelWidth, EntitySetting.ROTATION.getLabel(), rotationField);

        HBox flipButtons = new HBox(4,
                createFlipButton(new DesignFlipHorizontalAction()),
                createFlipButton(new DesignFlipVerticalAction()));
        flipButtons.setAlignment(Pos.CENTER_LEFT);
        return new EntitySettingsRowGroup(flipButtons, rotationRow);
    }

    private ActionButton createFlipButton(Action action) {
        ActionButton button = new ActionButton(action, 16, false);
        button.getStyleClass().add("flip-button");
        return button;
    }

    private void refreshFieldValue(UnitTextField field, List<Cuttable> entities, ToDoubleFunction<Cuttable> getter) {
        editGuard.run(() -> field.setValue(getter.applyAsDouble(entities.get(0))));
    }

    private EntitySettingsRowGroup buildSizeGroup(List<Cuttable> entities, DoubleProperty labelWidth) {
        captureAspectRatios(entities);

        UnitTextField widthField = controls.doubleField(
                e -> e.getSize().getWidth(),
                (entity, v) -> applySize(entity, v, true),
                entities, Unit.MM);

        UnitTextField heightField = controls.doubleField(
                e -> e.getSize().getHeight(),
                (entity, v) -> applySize(entity, v, false),
                entities, Unit.MM);

        // When locked, editing one dimension resizes the other in the model. Mirror that change in
        // the sibling field so its displayed value stays correct while typing.
        widthField.unitValueProperty().addListener((obs, oldVal, newVal) ->
                syncSibling(heightField, e -> e.getSize().getHeight(), entities));
        heightField.unitValueProperty().addListener((obs, oldVal, newVal) ->
                syncSibling(widthField, e -> e.getSize().getWidth(), entities));

        EntitySettingsRow widthRow = new EntitySettingsRow(labelWidth, EntitySetting.WIDTH.getLabel(), widthField);
        EntitySettingsRow heightRow = new EntitySettingsRow(labelWidth, EntitySetting.HEIGHT.getLabel(), heightField);
        return new EntitySettingsRowGroup(createAspectRatioLock(entities), widthRow, heightRow);
    }

    private ToggleButton createAspectRatioLock(List<Cuttable> entities) {
        ToggleButton toggle = new ToggleButton();
        toggle.getStyleClass().add("aspect-ratio-lock");
        toggle.setMaxHeight(Double.MAX_VALUE);
        SvgLoader.loadImageIcon("icons/lock.svg", 16, Colors.BLACKISH).ifPresent(toggle::setGraphic);

        Tooltip tooltip = new Tooltip("Lock aspect ratio");
        tooltip.setShowDelay(Duration.millis(100));
        toggle.setTooltip(tooltip);

        // The toggle is recreated on every rebuild, so keep the on/off state on the panel and just
        // reflect it here. A one-way listener (living on the toggle, not the panel property) avoids
        // leaking listeners across rebuilds. Re-capture the ratios whenever it is switched on so it
        // uses the sizes as they currently are.
        toggle.setSelected(aspectRatioLocked.get());
        toggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            aspectRatioLocked.set(isSelected);
            if (isSelected) {
                captureAspectRatios(entities);
            }
        });
        return toggle;
    }

    private void captureAspectRatios(List<Cuttable> entities) {
        aspectRatios.clear();
        for (Cuttable entity : entities) {
            double width = entity.getSize().getWidth();
            if (width > 0) {
                aspectRatios.put(entity, entity.getSize().getHeight() / width);
            }
        }
    }

    private void applySize(Cuttable entity, double value, boolean isWidth) {
        Size size = entity.getSize();
        Double ratio = aspectRatios.get(entity);
        if (aspectRatioLocked.get() && ratio != null && ratio > 0) {
            entity.setSize(isWidth ? new Size(value, value * ratio) : new Size(value / ratio, value));
        } else if (isWidth) {
            entity.setSize(new Size(value, size.getHeight()));
        } else {
            entity.setSize(new Size(size.getWidth(), value));
        }
    }

    private void syncSibling(UnitTextField sibling, ToDoubleFunction<Cuttable> getter, List<Cuttable> entities) {
        if (editGuard.isActive() || !aspectRatioLocked.get()) {
            return;
        }
        editGuard.run(() -> sibling.setValue(getter.applyAsDouble(entities.get(0))));
    }

    private Region buildControl(EntitySetting key, List<Cuttable> entities) {
        return switch (key) {
            case POSITION_X -> controls.doubleField(
                    e -> e.getPosition().getX(),
                    (entity, v) -> entity.setPosition(new java.awt.geom.Point2D.Double(v, entity.getPosition().getY())),
                    entities, Unit.MM);
            case POSITION_Y -> controls.doubleField(
                    e -> e.getPosition().getY(),
                    (entity, v) -> entity.setPosition(new java.awt.geom.Point2D.Double(entity.getPosition().getX(), v)),
                    entities, Unit.MM);
            case WIDTH -> controls.doubleField(
                    e -> e.getSize().getWidth(),
                    (entity, v) -> entity.setSize(new Size(v, entity.getSize().getHeight())),
                    entities, Unit.MM);
            case HEIGHT -> controls.doubleField(
                    e -> e.getSize().getHeight(),
                    (entity, v) -> entity.setSize(new Size(entity.getSize().getWidth(), v)),
                    entities, Unit.MM);
            case ROTATION -> controls.doubleField(Cuttable::getRotation, Cuttable::setRotation, entities, Unit.DEGREE);
            case CUT_TYPE -> cutTypeControl(entities);
            case START_DEPTH -> controls.doubleField(Cuttable::getStartDepth, Cuttable::setStartDepth, entities, Unit.MM);
            case TARGET_DEPTH -> controls.doubleField(Cuttable::getTargetDepth, Cuttable::setTargetDepth, entities, Unit.MM);
            case SPINDLE_SPEED ->
                    controls.intField(Cuttable::getSpindleSpeed, Cuttable::setSpindleSpeed, entities, Unit.PERCENT);
            case FEED_RATE ->
                    controls.intField(Cuttable::getFeedRate, Cuttable::setFeedRate, entities, Unit.METERS_PER_SECOND);
            case PASSES -> controls.intField(Cuttable::getPasses, Cuttable::setPasses, entities, Unit.TIMES);
            case LEAD_IN_PERCENT ->
                    controls.intField(Cuttable::getLeadInPercent, Cuttable::setLeadInPercent, entities, Unit.PERCENT);
            case TOOL_PATH_ANGLE ->
                    controls.doubleField(Cuttable::getToolPathAngle, Cuttable::setToolPathAngle, entities, Unit.DEGREE);
            case DIRECTION -> controls.enumCombo(Direction.values(), Direction::getLabel,
                    Cuttable::getDirection, Cuttable::setDirection, entities);
            case TOOL_PATH_DIRECTION -> controls.enumCombo(ToolPathDirection.values(),
                    ToolPathDirection::getLabel, Cuttable::getToolPathDirection, Cuttable::setToolPathDirection, entities);
            case INCLUDE_IN_EXPORT ->
                    leftAligned(controls.switchControl(Cuttable::getIncludeInExport, Cuttable::setIncludeInExport, entities));
            default -> null;
        };
    }

    private static Region leftAligned(Region control) {
        HBox box = new HBox(control);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private ComboBox<CutType> cutTypeControl(List<Cuttable> entities) {
        ComboBox<CutType> combo = controls.cutTypeCombo(entities);
        // The cut type determines which cut settings apply, so rebuild the rows when it changes.
        // The value is applied by the factory's own listener (under the edit guard, which suppresses
        // the entity-change rebuild), so we trigger the structural rebuild explicitly here.
        combo.valueProperty().addListener((obs, oldType, newType) -> {
            if (newType != null && newType != oldType) {
                Platform.runLater(this::rebuild);
            }
        });
        return combo;
    }

    // Coalesce a burst of transform events (e.g. during a drag) into a single value refresh on the
    // next pulse, so we update the fields at most once per frame instead of once per event.
    private void scheduleValueRefresh() {
        if (valueRefreshScheduled) {
            return;
        }
        valueRefreshScheduled = true;
        Platform.runLater(() -> {
            valueRefreshScheduled = false;
            valueRefreshers.forEach(Runnable::run);
        });
    }
}
