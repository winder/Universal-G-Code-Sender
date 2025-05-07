package com.willwinder.universalgcodesender.fx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.prefs.Preferences;

public class Settings {
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";
    private static final String WINDOW_POSITION_X = "window.positionX";
    private static final String WINDOW_POSITION_Y = "window.positionY";
    private static final String WINDOW_DIVIDER_LEFT = "window.dividerLeft";
    private static final String WINDOW_DIVIDER_CONTENT = "window.dividerContent";
    private static final String PENDANT_AUTOSTART = "pendant.autostart";

    private static final Preferences preferences = Preferences.userNodeForPackage(Settings.class);
    private static Settings instance;

    private final DoubleProperty windowWidth = new SimpleDoubleProperty(loadDouble(WINDOW_WIDTH, 1024));
    private final DoubleProperty windowHeight = new SimpleDoubleProperty(loadDouble(WINDOW_HEIGHT, 768));
    private final DoubleProperty windowPositionX = new SimpleDoubleProperty(loadDouble(WINDOW_POSITION_X, 0));
    private final DoubleProperty windowPositionY = new SimpleDoubleProperty(loadDouble(WINDOW_POSITION_Y, 0));
    private final DoubleProperty windowDividerLeft = new SimpleDoubleProperty(loadDouble(WINDOW_DIVIDER_LEFT, 0.5));
    private final DoubleProperty windowDividerContent = new SimpleDoubleProperty(loadDouble(WINDOW_DIVIDER_CONTENT, 0.3));
    private final BooleanProperty pendantAutostart = new SimpleBooleanProperty(loadBoolean(PENDANT_AUTOSTART, false));

    public Settings() {
        windowWidth.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_WIDTH, newVal.doubleValue()));
        windowHeight.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_HEIGHT, newVal.doubleValue()));
        windowPositionX.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_POSITION_X, newVal.doubleValue()));
        windowPositionY.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_POSITION_Y, newVal.doubleValue()));
        windowDividerLeft.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_DIVIDER_LEFT, newVal.doubleValue()));
        windowDividerContent.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_DIVIDER_CONTENT, newVal.doubleValue()));
        pendantAutostart.addListener((obs, oldVal, newVal) -> saveBoolean(PENDANT_AUTOSTART, newVal));
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }

        return instance;
    }

    public DoubleProperty windowWidthProperty() {
        return windowWidth;
    }

    public DoubleProperty windowHeightProperty() {
        return windowHeight;
    }

    public DoubleProperty windowPositionXProperty() {
        return windowPositionX;
    }

    public DoubleProperty windowPositionYProperty() {
        return windowPositionY;
    }

    public DoubleProperty windowDividerLeftProperty() {
        return windowDividerLeft;
    }

    public DoubleProperty windowDividerContentProperty() {
        return windowDividerContent;
    }

    public BooleanProperty pendantAutostartProperty() {
        return pendantAutostart;
    }

    private double loadDouble(String key, double defaultVal) {
        return preferences.getDouble(key, defaultVal);
    }

    private void saveDouble(String key, double value) {
        preferences.putDouble(key, value);
    }


    private boolean loadBoolean(String key, boolean defaultVal) {
        return preferences.getBoolean(key, defaultVal);
    }

    private void saveBoolean(String key, boolean value) {
        preferences.putBoolean(key, value);
    }
}
