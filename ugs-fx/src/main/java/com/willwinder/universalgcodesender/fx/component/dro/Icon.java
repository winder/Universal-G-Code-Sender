package com.willwinder.universalgcodesender.fx.component.dro;

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

public class Icon extends StackPane {
    public Icon(String iconPath, int targetSize) {
        SVGPath icon = SvgLoader.loadSvgPath(iconPath).orElseThrow();
        icon.getStyleClass().add("icon");

        double scale = targetSize / Math.max(icon.prefWidth(-1), icon.prefHeight(-1));
        icon.setScaleX(scale);
        icon.setScaleY(scale);

        setMinSize(targetSize, targetSize);
        setPrefSize(targetSize, targetSize);
        setMaxSize(targetSize, targetSize);

        getChildren().add(icon);
    }
}
