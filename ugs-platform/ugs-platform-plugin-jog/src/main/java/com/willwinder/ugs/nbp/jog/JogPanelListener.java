package com.willwinder.ugs.nbp.jog;

public interface JogPanelListener {
    void onClick(JogPanelButtonEnum button);

    void onPressed(JogPanelButtonEnum button);

    void onReleased(JogPanelButtonEnum button);
}