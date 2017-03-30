/*
    Copywrite 2016-2017 Will Winder

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

/**
 *
 * @author wwinder
 */
public class JogService {
    private double stepSizeXY = 1;
    private double stepSizeZ = 1;
    private double feedRate = 1;
    private Units units;

    private final BackendAPI backend;

    public JogService(BackendAPI backend) {
        this.backend = backend;

        // Init from settings.
        stepSizeXY = backend.getSettings().getManualModeStepSize();
        stepSizeZ = backend.getSettings().getzJogStepSize();
        feedRate = backend.getSettings().getJogFeedRate();
        units = Units.getUnit(backend.getSettings().getDefaultUnits());
    }

    public void increaseStepSize() {
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

    public void decreaseStepSize() {
        if (stepSizeXY > 1) {
            stepSizeXY--;
        } else if (stepSizeXY > 0.1) {
            stepSizeXY = stepSizeXY - 0.1;
        } else if (stepSizeXY > 0.01) {
            stepSizeXY = stepSizeXY - 0.01;
        }
        setStepSize(stepSizeXY);
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
        this.feedRate = rate;
        backend.getSettings().setJogFeedRate(feedRate);
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
            if (!this.backend.getSettings().useZStepSize()) {
                stepSize = stepSizeXY;
            }
            this.backend.adjustManualLocation(0, 0, z, stepSize, feedRate, units);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    /**
     * Adjusts the XY axis location.
     * @param z direction.
     */
    public void adjustManualLocationXY(int x, int y) {
        try {
            this.backend.adjustManualLocation(x, y, 0, stepSizeXY, feedRate, units);
        } catch (Exception e) {
            //NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            //DialogDisplayer.getDefault().notify(nd);
        }
    }

    public boolean canJog() {
        return backend.isConnected() && !backend.isSendingFile();
    }
}

