package com.willwinder.ugs.nbp.jog;

import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;

import java.util.List;

/**
 * Listens to RoundedPanel click events and relays them to JogPanelListeners
 *
 * @author Joacim Breiler
 */
public class JogPanelListenerProxy implements RoundedPanel.RoundedPanelClickListener {

    private final JogPanelButtonEnum button;
    private final List<JogPanelListener> listeners;

    JogPanelListenerProxy(java.util.List<JogPanelListener> listeners, JogPanelButtonEnum button) {
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