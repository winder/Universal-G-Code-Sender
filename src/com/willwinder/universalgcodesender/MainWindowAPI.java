package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.pendantui.SystemStateBean;

public interface MainWindowAPI {

	public void updateSystemState(SystemStateBean systemStateBean);

	public void sendGcodeCommand(String commandText);

	public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize);
	
	public Settings getSettings();
	
	public AbstractController getController();
	
	public void sendButtonActionPerformed();
	
	public void pauseButtonActionPerformed();
	
	public void cancelButtonActionPerformed();
	
	public void returnToZeroButtonActionPerformed();
        
	public  void resetCoordinatesButtonActionPerformed();
        
        public  void resetXCoordinateButtonActionPerformed();
        
        public  void resetYCoordinateButtonActionPerformed();
        
        public  void resetZCoordinateButtonActionPerformed();
}