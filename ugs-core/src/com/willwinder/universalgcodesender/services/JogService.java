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
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class JogService {
    private static final Logger logger = Logger.getLogger(JogService.class.getSimpleName());
    private double stepSizeXY = 1;
    private double stepSizeZ = 1;
    private Units units;

    private final BackendAPI backend;

    public JogService(BackendAPI backend) {
        this.backend = backend;

        // Init from settings.
        stepSizeXY = backend.getSettings().getManualModeStepSize();
        stepSizeZ = backend.getSettings().getzJogStepSize();
        units = backend.getSettings().getPreferredUnits();
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
        if (size > 100) {
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
        }  else if (size >= 0.1 && size < 1) {
            return 1;
        }  else if (size >= 1 && size < 10) {
            return 10;
        }  else if (size >= 10) {
            return 100;
        }
        return size;
    }

    public void increaseXYStepSize() {
        setStepSizeXY(increaseSize(stepSizeXY));
    }

    public void decreaseXYStepSize() {
        setStepSizeXY(decreaseSize(stepSizeXY));
    }

    public void increaseZStepSize() {
        setStepSizeZ(increaseSize(stepSizeZ));
    }

    public void decreaseZStepSize() {
        setStepSizeZ(decreaseSize(stepSizeZ));
    }

    public void divideXYStepSize() {
        setStepSizeXY(divideSize(stepSizeXY));
    }

    public void divideZStepSize() {
        setStepSizeZ(divideSize(stepSizeZ));
    }

    public void multiplyXYStepSize() {
        setStepSizeXY(multiplySize(stepSizeXY));
    }

    public void multiplyZStepSize() {
        setStepSizeZ(multiplySize(stepSizeZ));
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
        this.stepSizeXY = size;
        backend.getSettings().setManualModeStepSize(stepSizeXY);
    }

    public void setStepSizeZ(double size) {
        this.stepSizeZ = size;
        backend.getSettings().setzJogStepSize(stepSizeZ);
    }

    public void setFeedRate(double rate) {
        if( rate < 1 ) {
            backend.getSettings().setJogFeedRate(1);
        } else {
            backend.getSettings().setJogFeedRate(rate);
        }
    }

    public int getFeedRate() {
        return Double.valueOf(backend.getSettings().getJogFeedRate()).intValue();
    }

    public void setUnits(Units units) {
        this.units = units;
        if (units != null) {
            backend.getSettings().setPreferredUnits(units);
        }
    }
    
    public Units getUnits() {
        return this.units;
    }

    /**
     * Adjusts the Z axis location.
     */
    public void adjustManualLocation(int x, int y, int z, double stepSize) {
        try {
            double feedRate = backend.getSettings().getJogFeedRate();
            this.backend.adjustManualLocation(x, y, z, stepSize, feedRate, units);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }
    
    /**
     * Adjusts the Z axis location.
     * @param z direction.
     */
    public void adjustManualLocationZ(int z) {
        try {
            double stepSize = stepSizeZ;
            if (!useStepSizeZ()) {
                stepSize = stepSizeXY;
            }
            double feedRate = backend.getSettings().getJogFeedRate();
            this.backend.adjustManualLocation(0, 0, z, stepSize, feedRate, units);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean useStepSizeZ() {
        return this.backend.getSettings().useZStepSize();
    }

    /**
     * Adjusts the XY axis location.
     * @param x direction.
     * @param y direction.
     */
    public void adjustManualLocationXY(int x, int y) {
        try {
            double feedRate = backend.getSettings().getJogFeedRate();
            this.backend.adjustManualLocation(x, y, 0, stepSizeXY, feedRate, units);
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
        return backend.getSettings().getManualModeStepSize();
    }

    public double getStepSizeZ() {
        return backend.getSettings().getzJogStepSize();
    }

    public void cancelJog() {
        try {
            this.backend.getController().cancelSend();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Couldn't cancel the jog", e);
        }
    }
}