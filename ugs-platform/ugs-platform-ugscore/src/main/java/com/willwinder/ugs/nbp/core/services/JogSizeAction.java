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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;

public class JogSizeAction extends AbstractAction implements Serializable {
    public enum Operation {
        STEPXY_PLUS,
        STEPXY_MINUS,
        STEPXY_MULTIPLY,
        STEPXY_DIVIDE,
        STEPZ_PLUS,
        STEPZ_MINUS,
        STEPZ_MULTIPLY,
        STEPZ_DIVIDE,
        STEPABC_PLUS,
        STEPABC_MINUS,
        STEPABC_MULTIPLY,
        STEPABC_DIVIDE,
        FEED_PLUS,
        FEED_MINUS,
        FEED_MULTIPLY,
        FEED_DIVIDE,
        UNITS_TOGGLE
    }

    public enum StepType {
        XY,
        Z,
        ABC
    }

    private transient JogService js;
    private Double size = null;
    private Operation operation = null;
    private Units unit = null;
    private StepType type = null;

    /**
     * Empty constructor to be used for serialization
     */
    public JogSizeAction() {
    }

    public JogSizeAction(Units u) {
        unit = u;
    }

    public JogSizeAction(Operation op) {
        operation = op;
    }

    public JogSizeAction(double size, StepType type) {
        this.size = size;
        this.type = type;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (size != null) {
            switch (type) {
                case XY:
                    getJogService().setStepSizeXY(size);
                    break;
                case Z:
                    getJogService().setStepSizeZ(size);
                    break;
                case ABC:
                    getJogService().setStepSizeABC(size);
                    break;
            }
        } else if (operation != null) {
            switch (operation) {
                case STEPXY_MULTIPLY:
                    getJogService().multiplyXYStepSize(); //AndyCXL
                    break;
                case STEPXY_DIVIDE:
                    getJogService().divideXYStepSize();
                    break;
                case STEPXY_PLUS:
                    getJogService().increaseXYStepSize();
                    break;
                case STEPXY_MINUS:
                    getJogService().decreaseXYStepSize();
                    break;
                case STEPZ_MULTIPLY:
                    getJogService().multiplyZStepSize();
                    break;
                case STEPZ_DIVIDE:
                    getJogService().divideZStepSize();
                    break;
                case STEPZ_PLUS:
                    getJogService().increaseZStepSize();
                    break;
                case STEPZ_MINUS:
                    getJogService().decreaseZStepSize();
                    break;
                case STEPABC_MULTIPLY:
                    getJogService().multiplyABCStepSize();
                    break;
                case STEPABC_DIVIDE:
                    getJogService().divideABCStepSize();
                    break;
                case STEPABC_PLUS:
                    getJogService().increaseABCStepSize();
                    break;
                case STEPABC_MINUS:
                    getJogService().decreaseABCStepSize();
                    break;
                case FEED_PLUS:
                    getJogService().increaseFeedRate();
                    break;
                case FEED_MINUS:
                    getJogService().decreaseFeedRate();
                    break;
                case FEED_MULTIPLY:
                    getJogService().multiplyFeedRate();
                    break;
                case FEED_DIVIDE:
                    getJogService().divideFeedRate();
                    break;
                case UNITS_TOGGLE:
                    getJogService().setUnits(js.getUnits() == Units.MM ? Units.INCH : Units.MM);
                    break;
                default:
                    break;
            }
        } else if (unit != null) {
            getJogService().setUnits(unit);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private JogService getJogService() {
        if (js == null) {
            js = CentralLookup.getDefault().lookup(JogService.class);
        }

        return js;
    }
}