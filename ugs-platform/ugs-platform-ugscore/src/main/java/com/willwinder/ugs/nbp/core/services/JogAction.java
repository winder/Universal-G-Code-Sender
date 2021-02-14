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

import javax.swing.*;
import java.awt.event.ActionEvent;

public class JogAction extends AbstractAction {

    private transient JogService js;
    private int x, y, z, a, b, c;

    /**
     * Empty constructor to be used for serialization
     */
    public JogAction() {
    }

    public JogAction(Integer x, Integer y, Integer z, Integer a, Integer b, Integer c) {
        js = CentralLookup.getDefault().lookup(JogService.class);
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (z != 0) {
            getJogService().adjustManualLocationZ(z);
        } else if (x != 0 || y != 0) {
            getJogService().adjustManualLocationXY(x, y);
        } else if (a != 0 || b != 0 || c != 0) {
            getJogService().adjustManualLocationABC(a, b, c);
        }
    }

    @Override
    public boolean isEnabled() {
        return getJogService().canJog();
    }

    private JogService getJogService() {
        if (js == null) {
            js = CentralLookup.getDefault().lookup(JogService.class);
        }

        return js;
    }
}