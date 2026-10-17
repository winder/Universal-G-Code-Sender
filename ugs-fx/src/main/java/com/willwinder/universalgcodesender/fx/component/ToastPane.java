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
package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Objects;

/**
 * A transparent layer placed over the whole window that shows short-lived messages stacked at the
 * bottom. A toast fades out by itself after a few seconds, or right away when clicked.
 */
public class ToastPane extends VBox {
    private static final Duration SHOW_DURATION = Duration.seconds(6);
    private static final Duration FADE_DURATION = Duration.millis(300);
    private static final double MAX_WIDTH = 640;
    private static final int ICON_SIZE = 48;

    public enum Type {
        INFO("info.svg", "toast-info"),
        ERROR("warning-circle.svg", "toast-error");

        private final String icon;
        private final String styleClass;

        Type(String icon, String styleClass) {
            this.icon = icon;
            this.styleClass = styleClass;
        }
    }

    public ToastPane() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/toast.css")).toExternalForm());
        getStyleClass().add("toast-pane");
        setAlignment(Pos.BOTTOM_CENTER);
        setPickOnBounds(false);
    }

    public void show(Type type, String message) {
        show(type, message, null);
    }

    /**
     * Shows a toast that runs the given action when clicked, in addition to being dismissed.
     *
     * @param onClick the action to run on click, or {@code null} for a toast that only dismisses
     */
    public void show(Type type, String message, Runnable onClick) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(type, message, onClick));
            return;
        }

        Label toast = createToast(type, message);
        getChildren().add(toast);

        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        SequentialTransition lifetime = new SequentialTransition(fadeIn, new PauseTransition(SHOW_DURATION));
        lifetime.setOnFinished(_ -> dismiss(toast));
        lifetime.play();

        toast.setOnMouseClicked(_ -> {
            lifetime.stop();
            dismiss(toast);
            if (onClick != null) {
                onClick.run();
            }
        });
    }

    private static Label createToast(Type type, String message) {
        Label toast = new Label(message);
        toast.getStyleClass().addAll("toast", type.styleClass);
        toast.setWrapText(true);
        toast.setMaxWidth(MAX_WIDTH);
        toast.setOpacity(0);
        SvgLoader.loadImageIcon("icons/" + type.icon, ICON_SIZE, Color.WHITE).ifPresent(toast::setGraphic);
        return toast;
    }

    private void dismiss(Label toast) {
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, toast);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(_ -> getChildren().remove(toast));
        fadeOut.play();
    }
}
