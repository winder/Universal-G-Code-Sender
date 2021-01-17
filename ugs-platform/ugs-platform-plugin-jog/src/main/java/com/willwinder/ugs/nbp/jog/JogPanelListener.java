/*
    Copyright 2018-2021 Will Winder

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
package com.willwinder.ugs.nbp.jog;

/**
 * A listener for button events in the {@link JogPanel}.
 *
 * @author Joacim Breiler
 */
public interface JogPanelListener {

    /**
     * Is called when the button was single clicked
     *
     * @param button the enum for the button
     */
    void onJogButtonClicked(JogPanelButtonEnum button);

    /**
     * Is called when the button has been long pressed
     *
     * @param button the enum for the button
     */
    void onJogButtonLongPressed(JogPanelButtonEnum button);

    /**
     * Is called when a long pressed button has been released
     *
     * @param button the enum for the button
     */
    void onJogButtonLongReleased(JogPanelButtonEnum button);

    /**
     * Is called when the step size of the Z-axis is changed
     *
     * @param value the step size
     */
    void onStepSizeZChanged(double value);

    /**
     * Is called when the step size of the XY-axis is changed
     *
     * @param value the step size
     */
    void onStepSizeXYChanged(double value);

    /**
     * Is called when the step size of the ABC-axis is changed
     *
     * @param value the step size
     */
    void onStepSizeABCChanged(double value);

    /**
     * Is called when the feed rate is changed
     *
     * @param value the feed rate
     */
    void onFeedRateChanged(int value);

    /**
     * Toggles the units (mm/inch)
     */
    void onToggleUnit();

    /**
     * Increases the step distance
     */
    void onIncreaseStepSize();

    /**
     * Decreases the step distance
     */
    void onDecreaseStepSize();
}
