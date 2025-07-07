package com.willwinder.universalgcodesender.fx.model;

import javafx.scene.Node;

/**
 * @param icon Could be emoji or font icon code
 */
public record SettingsListItem(String text, String icon, Node settingsPane) {

}
