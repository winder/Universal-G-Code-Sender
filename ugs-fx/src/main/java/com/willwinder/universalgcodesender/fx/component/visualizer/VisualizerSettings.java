package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.MachineType;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.prefs.Preferences;

public class VisualizerSettings {
    private static final Preferences preferences = Preferences.userNodeForPackage(VisualizerSettings.class);
    private static final String SHOW_MACHINE_MODEL = "showMachineModel";
    private static final String MACHINE_MODEL = "machineModel";
    private static final String COLOR_RAPID = "color.rapid";
    private static final String COLOR_COMPLETED = "color.completed";
    private static final String COLOR_PLUNGE = "color.plunge";
    private static final String COLOR_ARC = "color.arc";
    private static final String COLOR_FEED_MIN = "color.feedMin";
    private static final String COLOR_FEED_MAX = "color.feedMax";
    private static final String COLOR_SPINDLE_MIN = "color.spindleMin";
    private static final String COLOR_SPINDLE_MAX = "color.spindleMax";
    private static VisualizerSettings instance;

    private final BooleanProperty showMachine = new SimpleBooleanProperty(loadBoolean(SHOW_MACHINE_MODEL, false));
    private final StringProperty machineModel = new SimpleStringProperty(loadString(MACHINE_MODEL, MachineType.GENMITSU_PRO_MAX.name()));
    private final StringProperty colorRapid = new SimpleStringProperty(loadString(COLOR_RAPID, "#EDFF00FF"));
    private final StringProperty colorCompleted = new SimpleStringProperty(loadString(COLOR_COMPLETED, "#BEBEBEC8"));
    private final StringProperty colorPlunge = new SimpleStringProperty(loadString(COLOR_PLUNGE, "#006400FF"));
    private final StringProperty colorArc = new SimpleStringProperty(loadString(COLOR_ARC, "#B22222FF"));
    private final StringProperty colorFeedMin = new SimpleStringProperty(loadString(COLOR_FEED_MIN, "#CCFFFFFF"));
    private final StringProperty colorFeedMax = new SimpleStringProperty(loadString(COLOR_FEED_MAX, "#00009EFF"));
    private final StringProperty colorSpindleMin = new SimpleStringProperty(loadString(COLOR_SPINDLE_MIN, "#CCFFFFFF"));
    private final StringProperty colorSpindleMax = new SimpleStringProperty(loadString(COLOR_SPINDLE_MAX, "#00009EFF"));

    VisualizerSettings() {
        showMachine.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_MACHINE_MODEL, newVal));
        machineModel.addListener((obs, oldVal, newVal) -> saveString(MACHINE_MODEL, newVal));
        colorRapid.addListener((obs, oldVal, newVal) -> saveString(COLOR_RAPID, newVal));
        colorCompleted.addListener((obs, oldVal, newVal) -> saveString(COLOR_COMPLETED, newVal));
        colorPlunge.addListener((obs, oldVal, newVal) -> saveString(COLOR_PLUNGE, newVal));
        colorArc.addListener((obs, oldVal, newVal) -> saveString(COLOR_ARC, newVal));
        colorFeedMin.addListener((obs, oldVal, newVal) -> saveString(COLOR_FEED_MIN, newVal));
        colorFeedMax.addListener((obs, oldVal, newVal) -> saveString(COLOR_FEED_MAX, newVal));
        colorSpindleMin.addListener((obs, oldVal, newVal) -> saveString(COLOR_SPINDLE_MIN, newVal));
        colorSpindleMax.addListener((obs, oldVal, newVal) -> saveString(COLOR_SPINDLE_MAX, newVal));
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

    public StringProperty machineModelProperty() {
        return machineModel;
    }

    private String loadString(String key, String defaultVal) {
        return preferences.get(key, defaultVal);
    }

    private void saveString(String key, String value) {
        preferences.put(key, value);
    }

    private boolean loadBoolean(String key, boolean defaultVal) {
        return preferences.getBoolean(key, defaultVal);
    }

    private void saveBoolean(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
}
