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
package com.willwinder.universalgcodesender.fx.settings;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineType;
import javafx.beans.property.BooleanProperty;
import java.util.List;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.prefs.Preferences;

public class VisualizerSettings {
    private static final Preferences preferences = Preferences.userNodeForPackage(VisualizerSettings.class);
    private static final String SHOW_MACHINE_MODEL = "showMachineModel";
    private static final String MACHINE_MODEL = "machineModel";
    private static final String COLOR_BACKGROUND = "color.background";
    private static final String COLOR_RAPID = "color.rapid";
    private static final String COLOR_COMPLETED = "color.completed";
    private static final String COLOR_PLUNGE = "color.plunge";
    private static final String COLOR_ARC = "color.arc";
    private static final String COLOR_FEED_MIN = "color.feedMin";
    private static final String COLOR_FEED_MAX = "color.feedMax";
    private static final String COLOR_SPINDLE_MIN = "color.spindleMin";
    private static final String COLOR_SPINDLE_MAX = "color.spindleMax";
    private static final String COLOR_RULER_LINES = "color.rulerLines";
    private static final String COLOR_RULER_TEXT = "color.rulerText";
    private static final String COLOR_DESIGN_RESIZE = "color.design.resize";
    private static final String COLOR_DESIGN_ROTATION = "color.design.rotation";
    private static final String COLOR_DESIGN_MOVE = "color.design.move";
    private static final String COLOR_DESIGN_SHAPE_OUTLINE = "color.design.shapeOutline";
    private static final String COLOR_DESIGN_SHAPE_BACKGROUND = "color.design.shapeBackground";

    private static final String MOUSE_INVERT_ZOOM = "mouse.invertZoom";
    private static final String MOUSE_INVERT_ROTATION = "mouse.invertRotation";
    private static final String MOUSE_PAN_BUTTON = "mouse.pan.button";
    private static final String MOUSE_PAN_MODIFIER = "mouse.pan.modifier";
    private static final String MOUSE_ROTATE_BUTTON = "mouse.rotate.button";
    private static final String MOUSE_ROTATE_MODIFIER = "mouse.rotate.modifier";
    private static final String MOUSE_PRIMARY_BUTTON = "mouse.primary.button";
    private static final String MOUSE_PRIMARY_MODIFIER = "mouse.primary.modifier";
    private static final String USE_PARALLEL_CAMERA = "useParallelCamera";
    private static final String SHOW_GCODE_MODEL = "showGcodeModel";
    private static final String SHOW_RULER = "showRuler";
    private static final String SHOW_GRID = "showGrid";
    private static final String SHOW_AXES = "showAxes";
    private static final String SHOW_DESIGN = "showDesign";
    private static final String SHOW_TOOL = "showTool";
    private static final String SHOW_STOCK = "showStock";
    private static final String COLOR_STOCK = "color.stock";
    private static final String STOCK_DEFAULT_TOOL_ID = "stock.defaultToolId";
    private static final String STOCK_MODE = "stock.mode";
    private static final String STOCK_DEPTH_COLORING = "stock.depthColoring";
    private static final String COLOR_STOCK_DEEP = "color.stockDeep";
    private static final String STOCK_MIN_X = "stock.minX";
    private static final String STOCK_MIN_Y = "stock.minY";
    private static final String STOCK_WIDTH = "stock.width";
    private static final String STOCK_LENGTH = "stock.length";
    private static final String STOCK_TOP = "stock.top";
    private static final String STOCK_THICKNESS = "stock.thickness";

    public enum ModifierKey {
        NONE, SHIFT, CTRL, ALT, META;

        public static ModifierKey fromString(String value, ModifierKey fallback) {
            if (value == null || value.isBlank()) return fallback;
            try {
                return ModifierKey.valueOf(value.trim().toUpperCase());
            } catch (Exception ignored) {
                return fallback;
            }
        }
    }

    private static VisualizerSettings instance;

    private final BooleanProperty showMachine = new SimpleBooleanProperty(loadBoolean(SHOW_MACHINE_MODEL, false));
    private final StringProperty machineModel = new SimpleStringProperty(loadString(MACHINE_MODEL, MachineType.GENMITSU_PRO_MAX.name()));
    private final StringProperty colorBackground = new SimpleStringProperty(loadString(COLOR_BACKGROUND, "#D3D3D3FF"));
    private final StringProperty colorRapid = new SimpleStringProperty(loadString(COLOR_RAPID, "#EDFF00FF"));
    private final StringProperty colorCompleted = new SimpleStringProperty(loadString(COLOR_COMPLETED, "#BEBEBEC8"));
    private final StringProperty colorPlunge = new SimpleStringProperty(loadString(COLOR_PLUNGE, "#006400FF"));
    private final StringProperty colorArc = new SimpleStringProperty(loadString(COLOR_ARC, "#B22222FF"));
    private final StringProperty colorFeedMin = new SimpleStringProperty(loadString(COLOR_FEED_MIN, "#CCFFFFFF"));
    private final StringProperty colorFeedMax = new SimpleStringProperty(loadString(COLOR_FEED_MAX, "#00009EFF"));
    private final StringProperty colorSpindleMin = new SimpleStringProperty(loadString(COLOR_SPINDLE_MIN, "#CCFFFFFF"));
    private final StringProperty colorSpindleMax = new SimpleStringProperty(loadString(COLOR_SPINDLE_MAX, "#00009EFF"));
    private final StringProperty colorRulerLines = new SimpleStringProperty(loadString(COLOR_RULER_LINES, "#999999FF"));
    private final StringProperty colorRulerText = new SimpleStringProperty(loadString(COLOR_RULER_TEXT, "#999999FF"));
    private final StringProperty colorDesignResize = new SimpleStringProperty(loadString(COLOR_DESIGN_RESIZE, "#4F9EB0FF"));
    private final StringProperty colorDesignRotation = new SimpleStringProperty(loadString(COLOR_DESIGN_ROTATION, "#4F9EB0FF"));
    private final StringProperty colorDesignMove = new SimpleStringProperty(loadString(COLOR_DESIGN_MOVE, "#4F9EB0FF"));
    private final StringProperty colorDesignShapeOutline = new SimpleStringProperty(loadString(COLOR_DESIGN_SHAPE_OUTLINE, "#4F9EB0FF"));
    private final StringProperty colorDesignShapeBackground = new SimpleStringProperty(loadString(COLOR_DESIGN_SHAPE_BACKGROUND, "#FFFFFFE5"));

    private final BooleanProperty invertZoom = new SimpleBooleanProperty(loadBoolean(MOUSE_INVERT_ZOOM, false));
    private final BooleanProperty invertRotation = new SimpleBooleanProperty(loadBoolean(MOUSE_INVERT_ROTATION, false));
    private final StringProperty panMouseButton = new SimpleStringProperty(loadString(MOUSE_PAN_BUTTON, "SECONDARY"));
    private final StringProperty panModifierKey = new SimpleStringProperty(loadString(MOUSE_PAN_MODIFIER, ModifierKey.NONE.name()));
    private final StringProperty rotateMouseButton = new SimpleStringProperty(loadString(MOUSE_ROTATE_BUTTON, "SECONDARY"));
    private final StringProperty rotateModifierKey = new SimpleStringProperty(loadString(MOUSE_ROTATE_MODIFIER, ModifierKey.SHIFT.name()));
    private final StringProperty primaryMouseButton = new SimpleStringProperty(loadString(MOUSE_PRIMARY_BUTTON, "PRIMARY"));
    private final StringProperty primaryModifierKey = new SimpleStringProperty(loadString(MOUSE_PRIMARY_MODIFIER, ModifierKey.NONE.name()));
    private final BooleanProperty useParallelCamera = new SimpleBooleanProperty(loadBoolean(USE_PARALLEL_CAMERA, false));
    private final BooleanProperty showGcodeModel = new SimpleBooleanProperty(loadBoolean(SHOW_GCODE_MODEL, true));
    private final BooleanProperty showRuler = new SimpleBooleanProperty(loadBoolean(SHOW_RULER, true));
    private final BooleanProperty showGrid = new SimpleBooleanProperty(loadBoolean(SHOW_GRID, true));
    private final BooleanProperty showAxes = new SimpleBooleanProperty(loadBoolean(SHOW_AXES, true));
    private final BooleanProperty showDesign = new SimpleBooleanProperty(loadBoolean(SHOW_DESIGN, true));
    private final BooleanProperty showTool = new SimpleBooleanProperty(loadBoolean(SHOW_TOOL, true));
    private final BooleanProperty showStock = new SimpleBooleanProperty(loadBoolean(SHOW_STOCK, false));
    private final StringProperty colorStock = new SimpleStringProperty(loadString(COLOR_STOCK, "#C8A96EFF"));
    private final StringProperty stockDefaultToolId = new SimpleStringProperty(loadString(STOCK_DEFAULT_TOOL_ID, ""));
    private final StringProperty stockMode = new SimpleStringProperty(loadString(STOCK_MODE, "AUTOMATIC"));
    private final StringProperty colorStockDeep = new SimpleStringProperty(loadString(COLOR_STOCK_DEEP, "#381c06"));
    private final DoubleProperty stockMinX = new SimpleDoubleProperty(loadDouble(STOCK_MIN_X, 0));
    private final DoubleProperty stockMinY = new SimpleDoubleProperty(loadDouble(STOCK_MIN_Y, 0));
    private final DoubleProperty stockWidth = new SimpleDoubleProperty(loadDouble(STOCK_WIDTH, 100));
    private final DoubleProperty stockLength = new SimpleDoubleProperty(loadDouble(STOCK_LENGTH, 100));
    private final DoubleProperty stockTop = new SimpleDoubleProperty(loadDouble(STOCK_TOP, 0));
    private final DoubleProperty stockThickness = new SimpleDoubleProperty(loadDouble(STOCK_THICKNESS, 10));

    VisualizerSettings() {
        showMachine.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_MACHINE_MODEL, newVal));
        machineModel.addListener((obs, oldVal, newVal) -> saveString(MACHINE_MODEL, newVal));
        colorBackground.addListener((obs, oldVal, newVal) -> saveString(COLOR_BACKGROUND, newVal));
        colorRapid.addListener((obs, oldVal, newVal) -> saveString(COLOR_RAPID, newVal));
        colorCompleted.addListener((obs, oldVal, newVal) -> saveString(COLOR_COMPLETED, newVal));
        colorPlunge.addListener((obs, oldVal, newVal) -> saveString(COLOR_PLUNGE, newVal));
        colorArc.addListener((obs, oldVal, newVal) -> saveString(COLOR_ARC, newVal));
        colorFeedMin.addListener((obs, oldVal, newVal) -> saveString(COLOR_FEED_MIN, newVal));
        colorFeedMax.addListener((obs, oldVal, newVal) -> saveString(COLOR_FEED_MAX, newVal));
        colorSpindleMin.addListener((obs, oldVal, newVal) -> saveString(COLOR_SPINDLE_MIN, newVal));
        colorSpindleMax.addListener((obs, oldVal, newVal) -> saveString(COLOR_SPINDLE_MAX, newVal));
        colorRulerLines.addListener((obs, oldVal, newVal) -> saveString(COLOR_RULER_LINES, newVal));
        colorRulerText.addListener((obs, oldVal, newVal) -> saveString(COLOR_RULER_TEXT, newVal));
        colorDesignResize.addListener((obs, oldVal, newVal) -> saveString(COLOR_DESIGN_RESIZE, newVal));
        colorDesignRotation.addListener((obs, oldVal, newVal) -> saveString(COLOR_DESIGN_ROTATION, newVal));
        colorDesignMove.addListener((obs, oldVal, newVal) -> saveString(COLOR_DESIGN_MOVE, newVal));
        colorDesignShapeOutline.addListener((obs, oldVal, newVal) -> saveString(COLOR_DESIGN_SHAPE_OUTLINE, newVal));
        colorDesignShapeBackground.addListener((obs, oldVal, newVal) -> saveString(COLOR_DESIGN_SHAPE_BACKGROUND, newVal));

        invertZoom.addListener((obs, oldVal, newVal) -> saveBoolean(MOUSE_INVERT_ZOOM, newVal));
        invertRotation.addListener((obs, oldVal, newVal) -> saveBoolean(MOUSE_INVERT_ROTATION, newVal));
        panMouseButton.addListener((obs, oldVal, newVal) -> saveString(MOUSE_PAN_BUTTON, newVal));
        panModifierKey.addListener((obs, oldVal, newVal) -> saveString(MOUSE_PAN_MODIFIER, newVal));
        rotateMouseButton.addListener((obs, oldVal, newVal) -> saveString(MOUSE_ROTATE_BUTTON, newVal));
        rotateModifierKey.addListener((obs, oldVal, newVal) -> saveString(MOUSE_ROTATE_MODIFIER, newVal));
        primaryMouseButton.addListener((obs, oldVal, newVal) -> saveString(MOUSE_PRIMARY_BUTTON, newVal));
        primaryModifierKey.addListener((obs, oldVal, newVal) -> saveString(MOUSE_PRIMARY_MODIFIER, newVal));
        useParallelCamera.addListener((obs, oldVal, newVal) -> saveBoolean(USE_PARALLEL_CAMERA, newVal));
        showGcodeModel.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_GCODE_MODEL, newVal));
        showRuler.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_RULER, newVal));
        showGrid.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_GRID, newVal));
        showAxes.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_AXES, newVal));
        showDesign.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_DESIGN, newVal));
        showTool.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_TOOL, newVal));
        showStock.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_STOCK, newVal));
        colorStock.addListener((obs, oldVal, newVal) -> saveString(COLOR_STOCK, newVal));
        stockDefaultToolId.addListener((obs, oldVal, newVal) -> saveString(STOCK_DEFAULT_TOOL_ID, newVal));
        stockMode.addListener((obs, oldVal, newVal) -> saveString(STOCK_MODE, newVal));
        colorStockDeep.addListener((obs, oldVal, newVal) -> saveString(COLOR_STOCK_DEEP, newVal));
        stockMinX.addListener((obs, oldVal, newVal) -> saveDouble(STOCK_MIN_X, newVal.doubleValue()));
        stockMinY.addListener((obs, oldVal, newVal) -> saveDouble(STOCK_MIN_Y, newVal.doubleValue()));
        stockWidth.addListener((obs, oldVal, newVal) -> saveDouble(STOCK_WIDTH, newVal.doubleValue()));
        stockLength.addListener((obs, oldVal, newVal) -> saveDouble(STOCK_LENGTH, newVal.doubleValue()));
        stockTop.addListener((obs, oldVal, newVal) -> saveDouble(STOCK_TOP, newVal.doubleValue()));
        stockThickness.addListener((obs, oldVal, newVal) -> saveDouble(STOCK_THICKNESS, newVal.doubleValue()));
    }

    public static VisualizerSettings getInstance() {
        if (instance == null) {
            instance = new VisualizerSettings();
        }

        return instance;
    }

    public BooleanProperty showMachineProperty() {
        return showMachine;
    }

    public StringProperty colorBackgroundProperty() {
        return colorBackground;
    }

    public StringProperty colorRapidProperty() {
        return colorRapid;
    }

    public StringProperty colorCompletedProperty() {
        return colorCompleted;
    }

    public StringProperty colorPlungeProperty() {
        return colorPlunge;
    }

    public StringProperty colorArcProperty() {
        return colorArc;
    }

    public StringProperty colorFeedMinProperty() {
        return colorFeedMin;
    }

    public StringProperty colorFeedMaxProperty() {
        return colorFeedMax;
    }

    public StringProperty colorSpindleMinProperty() {
        return colorSpindleMin;
    }

    public StringProperty colorSpindleMaxProperty() {
        return colorSpindleMax;
    }

    public StringProperty colorRulerLinesProperty() {
        return colorRulerLines;
    }

    public StringProperty colorRulerTextProperty() {
        return colorRulerText;
    }

    public StringProperty colorDesignResizeProperty() {
        return colorDesignResize;
    }

    public StringProperty colorDesignRotationProperty() {
        return colorDesignRotation;
    }

    public StringProperty colorDesignMoveProperty() {
        return colorDesignMove;
    }

    public StringProperty colorDesignShapeOutlineProperty() {
        return colorDesignShapeOutline;
    }

    public StringProperty colorDesignShapeBackgroundProperty() {
        return colorDesignShapeBackground;
    }

    public StringProperty machineModelProperty() {
        return machineModel;
    }

    public BooleanProperty invertZoomProperty() {
        return invertZoom;
    }

    /**
     * Reverses the direction the view rotates in when dragging with the rotate button.
     */
    public BooleanProperty invertRotationProperty() {
        return invertRotation;
    }

    public StringProperty panMouseButtonProperty() {
        return panMouseButton;
    }

    public StringProperty panModifierKeyProperty() {
        return panModifierKey;
    }

    public StringProperty rotateMouseButtonProperty() {
        return rotateMouseButton;
    }

    public StringProperty rotateModifierKeyProperty() {
        return rotateModifierKey;
    }

    public StringProperty primaryMouseButtonProperty() {
        return primaryMouseButton;
    }

    public StringProperty primaryModifierKeyProperty() {
        return primaryModifierKey;
    }

    public BooleanProperty useParallelCameraProperty() {
        return useParallelCamera;
    }

    public BooleanProperty showGcodeModelProperty() {
        return showGcodeModel;
    }

    public BooleanProperty showRulerProperty() {
        return showRuler;
    }

    public BooleanProperty showGridProperty() {
        return showGrid;
    }

    public BooleanProperty showAxesProperty() {
        return showAxes;
    }

    public BooleanProperty showDesignProperty() {
        return showDesign;
    }

    public BooleanProperty showToolProperty() {
        return showTool;
    }

    public BooleanProperty showStockProperty() {
        return showStock;
    }

    public StringProperty colorStockProperty() {
        return colorStock;
    }

    /**
     * Id of the tool library tool the stock simulation uses when the program selects no tool, or
     * an empty string to use the first tool in the library.
     */
    public StringProperty stockDefaultToolIdProperty() {
        return stockDefaultToolId;
    }

    /**
     * Whether the stock block is derived from the program or given by hand; the name of a
     * {@code StockSpec.Mode}.
     */
    public StringProperty stockModeProperty() {
        return stockMode;
    }

    public StringProperty colorStockDeepProperty() {
        return colorStockDeep;
    }

    public DoubleProperty stockMinXProperty() {
        return stockMinX;
    }

    public DoubleProperty stockMinYProperty() {
        return stockMinY;
    }

    public DoubleProperty stockWidthProperty() {
        return stockWidth;
    }

    public DoubleProperty stockLengthProperty() {
        return stockLength;
    }

    public DoubleProperty stockTopProperty() {
        return stockTop;
    }

    public DoubleProperty stockThicknessProperty() {
        return stockThickness;
    }

    /**
     * Every property that decides the size of the stock block, for listeners that redo the
     * simulation when any of them changes.
     */
    public List<javafx.beans.Observable> stockProperties() {
        return List.of(stockMode, stockMinX, stockMinY, stockWidth, stockLength, stockTop, stockThickness);
    }

    private String loadString(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    private void saveString(String key, String defaultValue) {
        preferences.put(key, defaultValue);
    }

    private boolean loadBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private void saveBoolean(String key, boolean value) {
        preferences.putBoolean(key, value);
    }

    private double loadDouble(String key, double defaultValue) {
        return preferences.getDouble(key, defaultValue);
    }

    private void saveDouble(String key, double value) {
        preferences.putDouble(key, value);
    }
}
