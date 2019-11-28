package com.willwinder.universalgcodesender;

import static com.willwinder.universalgcodesender.AbstractCommunicator.SerialCommunicatorEvent.COMMAND_SENT;

import java.util.logging.Logger;

import com.willwinder.universalgcodesender.types.GcodeCommand;

public class MarlinCommunicator extends BufferedCommunicator {

	// TODO: this should be configurable via the UI rather than being a constant
	private static final int MAX_MARLIN_ACTIVE_COMMANDS = 10;

	private static final Logger logger = Logger.getLogger(MarlinCommunicator.class.getName());

	private boolean marlinBusy = false;

	public boolean isMarlinBusy() {
		return marlinBusy;
	}

	public void setMarlinBusy(boolean marlinBusy) {
		this.marlinBusy = marlinBusy;
	}

	/**
	 * The override logic here is for flow control and not semantically implementing a 'pause'.
	 *
	 * It would be better placed in an overridable equivalent of allowMoreCommands()
	 */
	@Override
	public boolean isPaused() {
		// protect Marlin's buffers against overflow
		if (activeCommandListSize() >= MAX_MARLIN_ACTIVE_COMMANDS) {
			return true;
		}

		if (marlinBusy) {
			return true;
		}
		return super.isPaused();
	}

	protected MarlinCommunicator() {
	}

	@Override
	public int getBufferSize() {
		return 200; // TODO: what should it be?
	}

	@Override
	protected void sendingCommand(String command) {
		logger.info("send: " + command);
	}

	@Override
	protected boolean processedCommand(String response) {
		return MarlinUtils.isOkErrorAlarmResponse(response);
	}

	@Override
	protected boolean processedCommandIsError(String response) {
		// TODO Auto-generated method stub
		return false;
	}

	public void sendRealtimeCommand(GcodeCommand command) throws Exception {
		// TODO: ideally it should go at position 2, not at the end of the queue
		this.queueCommand(command);
		String commandString = command.getCommandString();
		this.sendingCommand(commandString);
		connection.sendStringToComm(commandString + "\n");
		dispatchListenerEvents(COMMAND_SENT, command);
	}

}
