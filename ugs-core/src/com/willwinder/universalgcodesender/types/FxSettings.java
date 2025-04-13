package com.willwinder.universalgcodesender.types;

public class FxSettings {
    public FxSettings() {
        windowWidth = 640;
        windowHeight = 520;
        windowPositionX = 0;
        windowPositionY = 0;
        dividerContentPercent = 0.3;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public int getWindowPositionX() {
        return windowPositionX;
    }

    public void setWindowPositionX(int windowPositionX) {
        this.windowPositionX = windowPositionX;
    }

    public int getWindowPositionY() {
        return windowPositionY;
    }

    public void setWindowPositionY(int windowPositionY) {
        this.windowPositionY = windowPositionY;
    }

    public double getDividerContentPercent() {
        return dividerContentPercent;
    }

    public void setDividerContentPercent(double dividerContentPercent) {
        this.dividerContentPercent = dividerContentPercent;
    }

    private int windowWidth;
    private int windowHeight;
    private int windowPositionX;
    private int windowPositionY;
    private double dividerContentPercent;
}
