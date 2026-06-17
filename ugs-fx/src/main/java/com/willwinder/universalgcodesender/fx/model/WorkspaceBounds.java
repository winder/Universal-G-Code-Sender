package com.willwinder.universalgcodesender.fx.model;

/**
 * The extents of a workspace in work coordinates (millimetres). Used to size the visualizer grid
 * regardless of how the workspace determines its size (gcode bounds, design drawing, ...).
 */
public record WorkspaceBounds(double minX, double minY, double maxX, double maxY) {
}
