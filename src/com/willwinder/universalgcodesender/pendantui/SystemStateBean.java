package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;

public class SystemStateBean {
	private ControlState controlState = ControlState.COMM_DISCONNECTED;
	private String fileName = "";
	private String latestComment = "";
	private String activeState = "";
	private String workX = "0";
	private String workY = "0";
	private String workZ = "0";
	private String machineX = "0";
	private String machineY = "0";
	private String machineZ = "0";
	private String rowsInFile = "0";
	private String sentRows = "0";
	private String remainingRows = "0";
	private String estimatedTimeRemaining = "--:--:--";
	private String duration = "00:00:00";
	private String sendButtonText = "Send";
	private boolean sendButtonEnabled = false;
	private String pauseResumeButtonText = "Pause";
	private boolean pauseResumeButtonEnabled = false;
	private String cancelButtonText = "Cancel";
	private boolean cancelButtonEnabled = false;
	
	public SystemStateBean() {
	}

	public ControlState getControlState() {
		return controlState;
	}

	public void setControlState(ControlState controlState) {
		this.controlState = controlState;
	}

	public String getActiveState() {
		return activeState;
	}

	public void setActiveState(String activeState) {
		this.activeState = activeState;
	}

	public String getWorkX() {
		return workX;
	}

	public void setWorkX(String workX) {
		this.workX = workX;
	}

	public String getWorkY() {
		return workY;
	}

	public void setWorkY(String workY) {
		this.workY = workY;
	}

	public String getWorkZ() {
		return workZ;
	}

	public void setWorkZ(String workZ) {
		this.workZ = workZ;
	}

	public String getMachineX() {
		return machineX;
	}

	public void setMachineX(String machineX) {
		this.machineX = machineX;
	}

	public String getMachineY() {
		return machineY;
	}

	public void setMachineY(String machineY) {
		this.machineY = machineY;
	}

	public String getMachineZ() {
		return machineZ;
	}

	public void setMachineZ(String machineZ) {
		this.machineZ = machineZ;
	}

	public String getRowsInFile() {
		return rowsInFile;
	}

	public void setRowsInFile(String rowsInFile) {
		this.rowsInFile = rowsInFile;
	}

	public String getSentRows() {
		return sentRows;
	}

	public void setSentRows(String sentRows) {
		this.sentRows = sentRows;
	}

	public String getRemainingRows() {
		return remainingRows;
	}

	public void setRemainingRows(String remainingRows) {
		this.remainingRows = remainingRows;
	}

	public String getEstimatedTimeRemaining() {
		return estimatedTimeRemaining;
	}

	public void setEstimatedTimeRemaining(String estimatedTimeRemaining) {
		this.estimatedTimeRemaining = estimatedTimeRemaining;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		String fileSeparator = System.getProperty("file.separator");
		
		if(fileName.contains(fileSeparator)){
			this.fileName = fileName.substring(fileName.lastIndexOf(fileSeparator)+1);
		} else {
			this.fileName = fileName;
		}
	}

	public String getLatestComment() {
		return latestComment;
	}

	public void setLatestComment(String lastComment) {
		this.latestComment = lastComment;
	}

	public boolean isSendButtonEnabled() {
		return sendButtonEnabled;
	}

	public void setSendButtonEnabled(boolean sendButtonEnabled) {
		this.sendButtonEnabled = sendButtonEnabled;
	}

	public boolean isPauseResumeButtonEnabled() {
		return pauseResumeButtonEnabled;
	}

	public void setPauseResumeButtonEnabled(boolean pauseResumeButtonEnabled) {
		this.pauseResumeButtonEnabled = pauseResumeButtonEnabled;
	}

	public boolean isCancelButtonEnabled() {
		return cancelButtonEnabled;
	}

	public void setCancelButtonEnabled(boolean cancelButtonEnabled) {
		this.cancelButtonEnabled = cancelButtonEnabled;
	}

	public String getPauseResumeButtonText() {
		return pauseResumeButtonText;
	}

	public void setPauseResumeButtonText(String pauseResumeButtonText) {
		this.pauseResumeButtonText = pauseResumeButtonText;
	}

	public String getSendButtonText() {
		return sendButtonText;
	}

	public void setSendButtonText(String sendButtonText) {
		this.sendButtonText = sendButtonText;
	}

	public String getCancelButtonText() {
		return cancelButtonText;
	}

	public void setCancelButtonText(String cancelButtonText) {
		this.cancelButtonText = cancelButtonText;
	}

}
