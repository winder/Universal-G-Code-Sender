package com.willwinder.universalgcodesender.pendantui.v1.model;

/**
 * The supported probe/touch-off operations. Single-face probes move in one direction and zero
 * that one axis at the contact point (radius-compensated for X/Y). Center-finding probes both
 * sides of a feature the tool starts inside and zero at the midpoint - the same routine serves
 * both a round bore and a rectangular pocket/boss, only the initial safe-approach distance
 * (driven by maxTravel) differs.
 */
public enum ProbeOperation {
    Z,
    X_NEG,
    X_POS,
    Y_NEG,
    Y_POS,
    X_CENTER,
    Y_CENTER,
    CENTER
}
