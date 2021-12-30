/*
    Copyright 2016-2021 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerState;
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

    public void increaseABCStepSize() {
        setStepSizeABC(increaseSize(getStepSizeABC()));
    }

    public void decreaseABCStepSize() {
        setStepSizeABC(decreaseSize(getStepSizeABC()));
    }


    public void divideXYStepSize() {
        setStepSizeXY(divideSize(getStepSizeXY()));
    }

    public void divideZStepSize() {
        setStepSizeZ(divideSize(getStepSizeZ()));
    }

    public void divideABCStepSize() {
        setStepSizeABC(divideSize(getStepSizeZ()));
    }

    public void multiplyXYStepSize() {
        setStepSizeXY(multiplySize(getStepSizeXY()));
    }

    public void multiplyZStepSize() {
        setStepSizeZ(multiplySize(getStepSizeZ()));
    }

    public void multiplyABCStepSize() {
        setStepSizeABC(multiplySize(getStepSizeZ()));
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


    private Settings getSettings() {
        return backend.getSettings();
    }

    public void setStepSizeXY(double size) {
        getSettings().setManualModeStepSize(size);
    }

    public void setStepSizeZ(double size) {
        getSettings().setZJogStepSize(size);
    }

    public void setStepSizeABC(double size) {
        getSettings().setABCJogStepSize(size);
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
    public void adjustManualLocation(PartialPosition distance) {
        try {
            double feedRate = getSettings().getJogFeedRate();
            Units units = getSettings().getPreferredUnits();
            backend.adjustManualLocation(distance.getPositionIn(units), feedRate);
        } catch (Exception e) {
            // Not much we can do
            logger.log(Level.SEVERE, "Could not jog the machine", e);
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
            backend.adjustManualLocation(new PartialPosition(null, null, z * stepSize, preferredUnits), feedRate);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean useStepSizeZ() {
        return getSettings().useZStepSize();
    }

    public boolean showABCStepSize() {
        return getSettings().showABCStepSize();
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
            Double dx = x == 0 ? null : x * stepSize;
            Double dy = y == 0 ? null : y * stepSize;
            backend.adjustManualLocation(new PartialPosition(dx, dy, null, preferredUnits), feedRate);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    /**
     * Adjusts the rotation axis location.
     * @param a direction.
     * @param b direction.
     * @param c direction.
     */
    public void adjustManualLocationABC(int a, int b, int c) {
        try {
            double feedRate = getFeedRate();
            double stepSize = getStepSizeABC();
            Units preferredUnits = getUnits();
            Double da = a == 0 ? null : a * stepSize;
            Double db = b == 0 ? null : b * stepSize;
            Double dc = c == 0 ? null : c * stepSize;
            backend.adjustManualLocation(new PartialPosition(null, null, null, da, db, dc, preferredUnits), feedRate);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean canJog() {
        return backend.isConnected() &&
                (backend.getControllerState() == ControllerState.IDLE || backend.getControllerState() == ControllerState.JOG) &&
                backend.getController().getCapabilities().hasJogging();
    }

    public double getStepSizeXY() {
        return getSettings().getManualModeStepSize();
    }

    public double getStepSizeZ() {
        return getSettings().getZJogStepSize();
    }

    public double getStepSizeABC() {
        return getSettings().getABCJogStepSize();
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
