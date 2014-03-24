package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.listeners.ControlStateListener;

public interface MainWindowAPI {

	public abstract void registerControlStateListener(ControlStateListener controlStateListener);

	public abstract void sendGcodeCommand(String commandText);

	public abstract void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize);
}