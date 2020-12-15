package com.willwinder.universalgcodesender.uielements.helpers;

import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public abstract class MouseClickListener implements MouseListener {
    private Component pressedComponent;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressedComponent = e.getComponent();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(pressedComponent != null && pressedComponent.contains(e.getPoint())) {
            onClick(e);
            pressedComponent = null;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public abstract void onClick(MouseEvent e);
}
