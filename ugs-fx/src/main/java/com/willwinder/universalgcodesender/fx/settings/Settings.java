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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.prefs.Preferences;

public class Settings {
    private static final String WINDOW_WIDTH = "window.width";
    private static final String WINDOW_HEIGHT = "window.height";
    private static final String WINDOW_POSITION_X = "window.positionX";
    private static final String WINDOW_POSITION_Y = "window.positionY";
    private static final String WINDOW_DIVIDER_LEFT = "window.dividerLeft";
    private static final String WINDOW_DIVIDER_CONTENT = "window.dividerContent";
    private static final String WINDOW_DIVIDER_INSPECTOR = "window.dividerInspector";
    private static final String WINDOW_DIVIDER_INSPECTOR_SECTIONS = "window.dividerInspectorSections";
    private static final String PENDANT_AUTOSTART = "pendant.autostart";
    private static final String SHOW_TOOLBAR_TEXT = "window.showToolBarText";
    private static final String SHOW_MACHINE_POSITION = "window.showMachinePosition";
    private static final String DRAWER_SELECTED_INDEX = "drawer.selectedIndex";
    private static final String DRAWER_EXPANDED = "drawer.expanded";

    private static final Preferences preferences = Preferences.userNodeForPackage(Settings.class);
    private static Settings instance;

    private final DoubleProperty windowWidth = new SimpleDoubleProperty(loadDouble(WINDOW_WIDTH, 1024));
    private final DoubleProperty windowHeight = new SimpleDoubleProperty(loadDouble(WINDOW_HEIGHT, 768));
    private final DoubleProperty windowPositionX = new SimpleDoubleProperty(loadDouble(WINDOW_POSITION_X, 0));
    private final DoubleProperty windowPositionY = new SimpleDoubleProperty(loadDouble(WINDOW_POSITION_Y, 0));
    private final DoubleProperty windowDividerLeft = new SimpleDoubleProperty(loadDouble(WINDOW_DIVIDER_LEFT, 0.5));
    private final DoubleProperty windowDividerContent = new SimpleDoubleProperty(loadDouble(WINDOW_DIVIDER_CONTENT, 0.3));
    private final DoubleProperty windowDividerInspector = new SimpleDoubleProperty(loadDouble(WINDOW_DIVIDER_INSPECTOR, 0.78));
    private final DoubleProperty windowDividerInspectorSections = new SimpleDoubleProperty(loadDouble(WINDOW_DIVIDER_INSPECTOR_SECTIONS, 0.6));
    private final BooleanProperty pendantAutostart = new SimpleBooleanProperty(loadBoolean(PENDANT_AUTOSTART, false));
    private final BooleanProperty showToolbarText = new SimpleBooleanProperty(loadBoolean(SHOW_TOOLBAR_TEXT, false));
    private final BooleanProperty showMachinePosition = new SimpleBooleanProperty(loadBoolean(SHOW_MACHINE_POSITION, false));
    private final IntegerProperty drawerSelectedIndex = new SimpleIntegerProperty(loadInt(DRAWER_SELECTED_INDEX, 0));
    private final BooleanProperty drawerExpanded = new SimpleBooleanProperty(loadBoolean(DRAWER_EXPANDED, true));

    public Settings() {
        windowWidth.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_WIDTH, newVal.doubleValue()));
        windowHeight.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_HEIGHT, newVal.doubleValue()));
        windowPositionX.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_POSITION_X, newVal.doubleValue()));
        windowPositionY.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_POSITION_Y, newVal.doubleValue()));
        windowDividerLeft.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_DIVIDER_LEFT, newVal.doubleValue()));
        windowDividerContent.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_DIVIDER_CONTENT, newVal.doubleValue()));
        windowDividerInspector.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_DIVIDER_INSPECTOR, newVal.doubleValue()));
        windowDividerInspectorSections.addListener((obs, oldVal, newVal) -> saveDouble(WINDOW_DIVIDER_INSPECTOR_SECTIONS, newVal.doubleValue()));
        pendantAutostart.addListener((obs, oldVal, newVal) -> saveBoolean(PENDANT_AUTOSTART, newVal));
        showToolbarText.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_TOOLBAR_TEXT, newVal));
        showMachinePosition.addListener((obs, oldVal, newVal) -> saveBoolean(SHOW_MACHINE_POSITION, newVal));
        drawerSelectedIndex.addListener((obs, oldVal, newVal) -> saveInt(DRAWER_SELECTED_INDEX, newVal.intValue()));
        drawerExpanded.addListener((obs, oldVal, newVal) -> saveBoolean(DRAWER_EXPANDED, newVal));
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

    public DoubleProperty windowDividerInspectorProperty() {
        return windowDividerInspector;
    }

    public DoubleProperty windowDividerInspectorSectionsProperty() {
        return windowDividerInspectorSections;
    }

    public BooleanProperty pendantAutostartProperty() {
        return pendantAutostart;
    }

    public BooleanProperty showToolbarTextProperty() {
        return showToolbarText;
    }

    public BooleanProperty showMachinePositionProperty() {
        return showMachinePosition;
    }

    public IntegerProperty drawerSelectedIndexProperty() {
        return drawerSelectedIndex;
    }

    public BooleanProperty drawerExpandedProperty() {
        return drawerExpanded;
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

    private int loadInt(String key, int defaultVal) {
        return preferences.getInt(key, defaultVal);
    }

    private void saveInt(String key, int value) {
        preferences.putInt(key, value);
    }
}
