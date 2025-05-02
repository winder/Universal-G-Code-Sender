/*
    Copyright 2025 Will Winder

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
package com.willwinder.universalgcodesender.types;

/**
 * Settings for FX window
 *
 * @author Joacim Breiler
 */
public class FxSettings {

    private int windowWidth;
    private int windowHeight;
    private int windowPositionX;
    private int windowPositionY;
    private double dividerContentPercent;
    private double dividerLeftPercent;
    private String visualizerRapidColor = "#EDFF00FF";
    private String visualizerCompletedColor = "#BEBEBEC8";
    private String visualizerPlungeColor = "#006400FF";
    private String visualizerArcColor = "#B22222FF";
    private String visualizerFeedMinColor = "#CCFFFFFF";
    private String visualizerFeedMaxColor = "#00009EFF";
    private String visualizerSpindleMinColor = "#CCFFFFFF";
    private String visualizerSpindleMaxColor = "#00009EFF";

    public FxSettings() {
        windowWidth = 1024;
        windowHeight = 768;
        windowPositionX = 0;
        windowPositionY = 0;
        dividerContentPercent = 0.3;
        dividerLeftPercent = 0.5;
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

    public double getDividerLeftPercent() {
        return dividerLeftPercent;
    }

    public void setDividerLeftPercent(double dividerLeftPercent) {
        this.dividerLeftPercent = dividerLeftPercent;
    }

    public String getVisualizerRapidColor() {
        return this.visualizerRapidColor;
    }

    public void setVisualizerRapidColor(String color) {
        this.visualizerRapidColor = color;
    }

    public void setVisualizerCompletedColor(String color) {
        this.visualizerCompletedColor = color;
    }

    public String getVisualizerCompletedColor() {
        return visualizerCompletedColor;
    }

    public String getVisualizerPlungeColor() {
        return visualizerPlungeColor;
    }

    public void setVisualizerPlungeColor(String visualizerPlungeColor) {
        this.visualizerPlungeColor = visualizerPlungeColor;
    }

    public void setVisualizerArcColor(String visualizerArcColor) {
        this.visualizerArcColor = visualizerArcColor;
    }

    public String getVisualizerArcColor() {
        return visualizerArcColor;
    }

    public String getVisualizerFeedMaxColor() {
        return visualizerFeedMaxColor;
    }

    public void setVisualizerFeedMaxColor(String visualizerFeedMaxColor) {
        this.visualizerFeedMaxColor = visualizerFeedMaxColor;
    }

    public String getVisualizerFeedMinColor() {
        return visualizerFeedMinColor;
    }

    public void setVisualizerFeedMinColor(String visualizerFeedMinColor) {
        this.visualizerFeedMinColor = visualizerFeedMinColor;
    }

    public void setVisualizerSpindleMinColor(String visualizerSpindleMinColor) {
        this.visualizerSpindleMinColor = visualizerSpindleMinColor;
    }

    public String getVisualizerSpindleMinColor() {
        return visualizerSpindleMinColor;
    }

    public void setVisualizerSpindleMaxColor(String visualizerSpindleMaxColor) {
        this.visualizerSpindleMaxColor = visualizerSpindleMaxColor;
    }

    public String getVisualizerSpindleMaxColor() {
        return visualizerSpindleMaxColor;
    }
}
