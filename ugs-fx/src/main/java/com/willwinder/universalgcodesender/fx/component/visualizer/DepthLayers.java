package com.willwinder.universalgcodesender.fx.component.visualizer;

/**
 * Z offsets that stack the flat overlays just above the bed/work plane so they layer
 * cleanly instead of z-fighting. Each value must exceed the depth-buffer resolution at the
 * model distance, yet stay small enough to look visually flat.
 */
public class DepthLayers {
    public static final double GRID_Z_OFFSET = 0.05;
    public static final double RULER_Z_OFFSET = 0.05;
    public static final double DESIGN_Z_OFFSET = 0.08;
    public static final double DESIGN_OUTLINE_Z_OFFSET = 0.09;
    public static final double GCODE_Z_OFFSET = 0.1;
}
