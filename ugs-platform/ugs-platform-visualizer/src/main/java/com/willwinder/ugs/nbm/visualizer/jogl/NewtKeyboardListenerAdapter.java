/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.jogl;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;

import java.awt.Component;

/**
 * A keyboard listener adapter for translating between events from the NEWT JOGL canvas to AWT listeners.
 *
 * @author Joacim Breiler
 */
public class NewtKeyboardListenerAdapter implements KeyListener {
    private final java.awt.event.KeyListener keyListener;
    private final Component component;

    public NewtKeyboardListenerAdapter(Component component, java.awt.event.KeyListener keyListener) {
        this.keyListener = keyListener;
        this.component = component;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        this.keyListener.keyPressed(new java.awt.event.KeyEvent(component, 0, e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
    }

    @Override
    public void keyReleased(KeyEvent e) {
        this.keyListener.keyPressed(new java.awt.event.KeyEvent(component, 0, e.getWhen(), e.getModifiers(), e.getKeyCode(), e.getKeyChar()));
    }
}
