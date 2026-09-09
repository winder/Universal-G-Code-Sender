package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;

/**
 * What the user has said about the block of material, before the program is looked at.
 *
 * @param mode         whether the block is derived from the program or given by hand
 * @param manualBounds the block when the mode is manual, ignored otherwise
 */
public record StockSpec(Mode mode, Bounds3 manualBounds) {

    public enum Mode {
        AUTOMATIC("Automatic, from the program"),
        MANUAL("Manual");

        private final String displayName;

        Mode(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return displayName;
        }
    }

    public static final StockSpec AUTOMATIC = new StockSpec(Mode.AUTOMATIC, null);

    public static StockSpec manual(Bounds3 bounds) {
        return new StockSpec(Mode.MANUAL, bounds);
    }
}
