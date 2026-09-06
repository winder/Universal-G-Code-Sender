package com.willwinder.universalgcodesender.pendantui.v1.model;

public record ToolpathSegment(ToolpathPoint start, ToolpathPoint end, boolean rapid, boolean arc) {
}
