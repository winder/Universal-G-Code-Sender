package com.willwinder.ugs.nbp.jog;

import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;

import java.util.List;

public class RoundedPanelButtonClickListener implements RoundedPanel.RoundedPanelClickListener {

    private final JogPanelButtonEnum button;
    private final List<JogPanelListener> listeners;

    RoundedPanelButtonClickListener(java.util.List<JogPanelListener> listeners, JogPanelButtonEnum button) {
        this.listeners = listeners;
        this.button = button;
    }

    @Override
    public void onClick() {
        listeners.forEach(l -> l.onClick(button));
    }

    @Override
    public void onPressed() {
        listeners.forEach(l -> l.onPressed(button));
    }

    @Override
    public void onReleased() {
        listeners.forEach(l -> l.onReleased(button));
    }
}