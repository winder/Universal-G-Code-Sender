package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.Utils.ControlState;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import java.io.File;

public interface MainWindowAPI {
        // Config options
        public void setFile(File file) throws Exception;
        public void applySettings(Settings settings) throws Exception;

        // Control options
        public void connect(String firmware, String port, int baudRate) throws Exception;
        public void disconnect();
        public void sendGcodeCommand(String commandText) throws Exception;
	public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize, Utils.Units units) throws Exception;
	public void send() throws Exception;
	public void pauseResume() throws Exception;
	public void cancel() throws Exception;
        public void returnToZero() throws Exception;
	public void resetCoordinatesToZero() throws Exception;
        public void resetCoordinateToZero(char coordinate) throws Exception;
        
        public void killAlarmLock() throws Exception;
        public void performHomingCycle() throws Exception;
        public void toggleCheckMode() throws Exception;
        public void issueSoftReset() throws Exception;
        public void requestParserState() throws Exception;
        
        // Controller status
        public boolean isConnected();
        public boolean isSending();
        public boolean isPaused();
        public boolean canPause();
	public boolean canCancel();
        public ControlState getControlState();
        
        // Send status
        public long getNumRows();
        public long getNumSentRows();
        public long getNumRemainingRows();

        public long getSendDuration();
        public long getSendRemainingDuration();
        public String getPauseResumeText();
        
        // Bulk status getter.
	public void updateSystemState(SystemStateBean systemStateBean);	
        
	// Shouldn't be needed often.
	public Settings getSettings();
	public AbstractController getController();
        public void applySettingsToController(Settings settings, AbstractController controller) throws Exception;
}