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

    public void increaseXYStepSize() {
        if (stepSizeXY >= 1) {
            stepSizeXY++;
        } else if (stepSizeXY >= 0.1) {
            stepSizeXY = stepSizeXY + 0.1;
        } else if (stepSizeXY >= 0.01) {
            stepSizeXY = stepSizeXY + 0.01;
        } else {
            stepSizeXY = 0.01;
        }
        setStepSize(stepSizeXY);
    }

    public void decreaseXYStepSize() {
        if (stepSizeXY > 1) {
            stepSizeXY--;
        } else if (stepSizeXY > 0.1) {
            stepSizeXY = stepSizeXY - 0.1;
        } else if (stepSizeXY > 0.01) {
            stepSizeXY = stepSizeXY - 0.01;
        }
        setStepSize(stepSizeXY);
    }


    public void increaseZStepSize() {
        double stepSize = this.stepSizeZ;
        if (stepSize >= 1) {
            stepSize++;
        } else if (stepSize >= 0.1) {
            stepSize = stepSize + 0.1;
        } else if (stepSize >= 0.01) {
            stepSize = stepSize + 0.01;
        } else {
            stepSize = 0.01;
        }
        setStepSizeZ(stepSize);
    }

    public void decreaseZStepSize() {
        double stepSize = this.stepSizeZ;
        if (stepSize > 1) {
            stepSize--;
        } else if (stepSize > 0.1) {
            stepSize = stepSize - 0.1;
        } else if (stepSize > 0.01) {
            stepSize = stepSize - 0.01;
        }
        setStepSizeZ(stepSize);
    }

    public void divideStepSize() {
        if (stepSizeXY > 100) {
            stepSizeXY = 100;
        } else if (stepSizeXY <= 100 && stepSizeXY > 10) {
            stepSizeXY = 10;
        } else if (stepSizeXY <= 10 && stepSizeXY > 1) {
            stepSizeXY = 1;
        } else if (stepSizeXY <= 1 && stepSizeXY > 0.1) {
            stepSizeXY = 0.1;
        } else if (stepSizeXY <= 0.1 ) {
            stepSizeXY = 0.01;
        }
        setStepSize(stepSizeXY);
    }

    public void multiplyStepSize() {
        if (stepSizeXY < 0.01) {
            stepSizeXY = 0.01;
        } else if (stepSizeXY >= 0.01 && stepSizeXY < 0.1) {
            stepSizeXY = 0.1;
        }  else if (stepSizeXY >= 0.1 && stepSizeXY < 1) {
            stepSizeXY = 1;
        }  else if (stepSizeXY >= 1 && stepSizeXY < 10) {
            stepSizeXY = 10;
        }  else if (stepSizeXY >= 10) {
            stepSizeXY = 100;
        }
        setStepSize(stepSizeXY);
    }

    public void setStepSize(double size) {
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
            backend.getSettings().setDefaultUnits(units.abbreviation);
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
        return backend.isConnected() && !backend.isSendingFile();
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

