package com.willwinder.universalgcodesender.fx.component.visualizer;

public interface DragHandler {
    void onDragStart(double designerX, double designerY);
    void onDrag(double startX, double startY, double currentX, double currentY);
    void onDragEnd(double startX, double startY, double currentX, double currentY);
}
