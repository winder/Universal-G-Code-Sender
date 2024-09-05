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

import com.jogamp.newt.event.MouseListener;

import java.awt.Component;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;


public class NewtMouseListenerAdapter implements MouseListener {

    private final java.awt.event.MouseListener mouseListener;
    private final MouseWheelListener mouseWheelListener;
    private final MouseMotionListener mouseMotionListener;
    private final Component component;

    public NewtMouseListenerAdapter(Component component, java.awt.event.MouseListener mouseListener, MouseWheelListener mouseWheelListener, MouseMotionListener mouseMotionListener) {
        this.component = component;
        this.mouseListener = mouseListener;
        this.mouseWheelListener = mouseWheelListener;
        this.mouseMotionListener = mouseMotionListener;
    }

    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent e) {
        mouseListener.mouseClicked(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));
    }

    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent e) {
        mouseListener.mouseEntered(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));
    }

    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent e) {
        mouseListener.mouseExited(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));
    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent e) {
        mouseListener.mousePressed(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));
    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent e) {
        mouseListener.mouseReleased(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));
    }

    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
        mouseMotionListener.mouseMoved(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));

    }

    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent e) {
        mouseMotionListener.mouseDragged(new java.awt.event.MouseEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, e.getButton()));

    }

    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent e) {
        mouseWheelListener.mouseWheelMoved(new MouseWheelEvent(component, 0, e.getWhen(), e.getModifiers(), e.getX(), e.getY(), e.getClickCount(), false, MouseWheelEvent.WHEEL_UNIT_SCROLL, Math.round(e.getRotationScale()), Math.round(e.getRotation()[1])));
    }
}
