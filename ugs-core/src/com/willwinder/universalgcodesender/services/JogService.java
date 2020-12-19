/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.Settings;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class JogService {
    private static final Logger logger = Logger.getLogger(JogService.class.getSimpleName());

    private final BackendAPI backend;

    public JogService(BackendAPI backend) {
        this.backend = backend;
    }

    public static double increaseSize(double size) {
        if (size >= 1) {
            return size + 1;
        } else if (size >= 0.1) {
            return size + 0.1;
        } else if (size >= 0.01) {
            return size + 0.01;
        } else {
            return 0.01;
        }
    }

    public static double decreaseSize(double size) {
        if (size > 1) {
            return size - 1;
        } else if (size > 0.1) {
            return size - 0.1;
        } else if (size > 0.01) {
            return size - 0.01;
        }
        return size;
    }

    private static double divideSize(double size) {
        if (size > 10000) {
            return 10000;
        } else if (size <= 10000 && size > 1000) {
            return 1000;
        } else if (size <= 1000 && size > 100) {
            return 100;
        } else if (size <= 100 && size > 10) {
            return 10;
        } else if (size <= 10 && size > 1) {
            return 1;
        } else if (size <= 1 && size > 0.1) {
            return 0.1;
        } else if (size <= 0.1 ) {
            return 0.01;
        }
        return size;
    }

    private static double multiplySize(double size) {
        if (size < 0.01) {
            return 0.01;
        } else if (size >= 0.01 && size < 0.1) {
            return 0.1;
        } else if (size >= 0.1 && size < 1) {
            return 1;
        } else if (size >= 1 && size < 10) {
            return 10;
        } else if (size >= 10 && size < 100) {
            return 100;
        } else if (size >= 100 && size < 1000) {
            return 1000;
        } else if (size >= 1000 && size < 10000) {
            return 10000;
        }
        return size;
    }

    public void increaseXYStepSize() {
        setStepSizeXY(increaseSize(getStepSizeXY()));
    }

    public void decreaseXYStepSize() {
        setStepSizeXY(decreaseSize(getStepSizeXY()));
    }

    public void increaseZStepSize() {
        setStepSizeZ(increaseSize(getStepSizeZ()));
    }

    public void decreaseZStepSize() {
        setStepSizeZ(decreaseSize(getStepSizeZ()));
    }

    public void divideXYStepSize() {
        setStepSizeXY(divideSize(getStepSizeXY()));
    }

    public void divideZStepSize() {
        setStepSizeZ(divideSize(getStepSizeZ()));
    }

    public void multiplyXYStepSize() {
        setStepSizeXY(multiplySize(getStepSizeXY()));
    }

    public void multiplyZStepSize() {
        setStepSizeZ(multiplySize(getStepSizeZ()));
    }

    public void multiplyFeedRate() {
        setFeedRate(multiplySize(getFeedRate()));
    }

    public void divideFeedRate() {
        setFeedRate(divideSize(getFeedRate()));
    }

    public void increaseFeedRate() {
        setFeedRate(increaseSize(getFeedRate()));
    }

    public void decreaseFeedRate() {
        setFeedRate(decreaseSize(getFeedRate()));
    }

    public void setStepSizeXY(double size) {
        getSettings().setManualModeStepSize(size);
    }

    private Settings getSettings() {
        return backend.getSettings();
    }

    public void setStepSizeZ(double size) {
        getSettings().setzJogStepSize(size);
    }

    public void setFeedRate(double rate) {
        if( rate < 1 ) {
            getSettings().setJogFeedRate(1);
        } else {
            getSettings().setJogFeedRate(rate);
        }
    }

    public int getFeedRate() {
        return Double.valueOf(getSettings().getJogFeedRate()).intValue();
    }

    public void setUnits(Units units) {
        if (units != null) {
            getSettings().setPreferredUnits(units);
        }
    }
    
    public Units getUnits() {
        return getSettings().getPreferredUnits();
    }

    /**
     * Adjusts the location for each axises.
     */
    public void adjustManualLocation(double distanceX, double distanceY, double distanceZ) {
        try {
            double feedRate = getSettings().getJogFeedRate();
            Units units = getSettings().getPreferredUnits();
            backend.adjustManualLocation(distanceX, distanceY, distanceZ, feedRate, units);
        } catch (Exception e) {
            // Not much we can do
        }
    }

    /**
     * Adjusts the Z axis location.
     * @param z direction.
     */
    public void adjustManualLocationZ(int z) {
        try {
            double stepSize = getStepSizeZ();
            if (!useStepSizeZ()) {
                stepSize = getStepSizeXY();
            }
            double feedRate = getSettings().getJogFeedRate();
            Units preferredUnits = getSettings().getPreferredUnits();
            backend.adjustManualLocation(0, 0, z * stepSize, feedRate, preferredUnits);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean useStepSizeZ() {
        return getSettings().useZStepSize();
    }

    /**
     * Adjusts the XY axis location.
     * @param x direction.
     * @param y direction.
     */
    public void adjustManualLocationXY(int x, int y) {
        try {
            double feedRate = getFeedRate();
            double stepSize = getStepSizeXY();
            Units preferredUnits = getUnits();
            backend.adjustManualLocation(x * stepSize, y * stepSize, 0, feedRate, preferredUnits);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean canJog() {
        return backend.isConnected() &&
                !backend.isSendingFile() &&
                backend.getController().getCapabilities().hasJogging();
    }

    public double getStepSizeXY() {
        return getSettings().getManualModeStepSize();
    }

    public double getStepSizeZ() {
        return getSettings().getzJogStepSize();
    }

    public void cancelJog() {
        try {
            backend.getController().cancelSend();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn't cancel the jog", e);
        }
    }

    public void jogTo(PartialPosition position) {
        try {
            backend.getController().jogMachineTo(position, getFeedRate());
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn't jog to position " + position, e);
        }
    }
}