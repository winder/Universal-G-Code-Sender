package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

public class InfoTooltip extends Label {
    public InfoTooltip(String tooltipText) {
        super("", SvgLoader.loadImageIcon("icons/info.svg", 20, Colors.BLUE).orElse(null));
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.ZERO);
        setTooltip(tooltip);
    }
}
