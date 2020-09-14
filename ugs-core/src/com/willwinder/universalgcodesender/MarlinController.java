package com.willwinder.universalgcodesender;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.marlin.MarlinFirmwareSettings;
import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;

public class MarlinController extends AbstractController {
	private static final Logger logger = Logger.getLogger(MarlinController.class.getName());

	private final MarlinFirmwareSettings firmwareSettings;
	private Capabilities capabilities = new Capabilities();

	// Polling state
	private int outstandingPolls = 0;
	private Timer positionPollTimer = null;
	private ControllerStatus controllerStatus = new ControllerStatus(ControllerState.DISCONNECTED, new Position(0, 0, 0, Units.MM),
			new Position(0, 0, 0, Units.MM));

	private MarlinCommunicator marlinComm;
	private boolean isResuming = false;

	public MarlinController() {
		this(new MarlinCommunicator());
	}

	protected MarlinController(MarlinCommunicator comm) {
		super(comm);
		this.marlinComm = comm;

		this.commandCreator = new GcodeCommandCreator();
		this.firmwareSettings = new MarlinFirmwareSettings(this);

		this.positionPollTimer = createPositionPollTimer();
		this.setSingleStepMode(true);
	}

	@Override
	public void sendOverrideCommand(Overrides command) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean handlesAllStateChangeEvents() {
		// TODO what should it do?
		return false;
	}

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IFirmwareSettings getFirmwareSettings() {
		return firmwareSettings;
	}

	@Override
	public String getFirmwareVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ControllerStatus getControllerStatus() {
		return controllerStatus;
	}

	@Override
	protected Boolean isIdleEvent() {
		// TODO: what should it do - GRBL does some real time check here
		return true;
	}

	@Override
	protected void closeCommBeforeEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void closeCommAfterEvent() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cancelSendBeforeEvent() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void cancelSendAfterEvent() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void pauseStreamingEvent() throws Exception {
		logger.info("trying to send M0");
		GcodeCommand command = createCommand("M0");
		sendCommandImmediately(command);
	}

	@Override
	protected void resumeStreamingEvent() throws Exception {
		logger.info("trying to send M108");
		GcodeCommand command = createCommand("M108");
		// sendCommandImmediately(command);
		this.marlinComm.sendRealtimeCommand(command);

		synchronized (this) {
			// need to resume otherwise the cmd will never go
			isResuming = true;
			comm.resumeSend();
		}
	}

	@Override
	protected void isReadyToSendCommandsEvent() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void isReadyToStreamCommandsEvent() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void rawResponseHandler(String response) {
		String processed = response;
		try {
			boolean verbose = false;

			if (MarlinUtils.isOkResponse(response)) {
				this.commandComplete(processed);
				logger.info("active count after rx: " + marlinComm.activeCommandListSize());
				updateControllerState("Idle", ControllerState.IDLE);
				marlinComm.setMarlinBusy(false);
				isResuming = false;
			} else if (MarlinUtils.isPausedResponse(response)) {
				updateControllerState("Paused", ControllerState.HOLD);
				marlinComm.setMarlinBusy(false);
				synchronized (this) {
					if (!isResuming) {
						comm.pauseSend();
					}
				}
				// dont stop sending, need to be able to resume with M108
			} else if (MarlinUtils.isBusyResponse(response)) {
				// do what?
				// marlinComm.setMarlinBusy(true);
				updateControllerState("Run", ControllerState.RUN);
			} else if (MarlinUtils.isMarlinStatusString(response)) {
				// Only 1 poll is sent at a time so don't decrement, reset to zero.
				this.outstandingPolls = 0;

				// Status string goes to verbose console
				verbose = true;

				// this.handleStatusString(response);

				controllerStatus = MarlinUtils.getStatusFromStatusString(
						controllerStatus, response, capabilities, getFirmwareSettings().getReportingUnits());

				dispatchStatusString(controllerStatus);

				this.checkStreamFinished();
			} else if (response.contains("Free Memory:")) {
				updateControllerState("Idle", ControllerState.IDLE);
				// this.isReady = true;
				/*
				 * resetBuffers();
				 *
				 *
				 */
				this.stopPollingPosition();
				positionPollTimer = createPositionPollTimer();
				this.beginPollingPosition();
			} else if (MarlinUtils.isMarlinEchoMessage(response)) {
				// processed = response;
			}

			if (StringUtils.isNotBlank(processed)) {
				if (verbose) {
					this.dispatchConsoleMessage(MessageType.VERBOSE, processed + "\n");
				} else {
					this.dispatchConsoleMessage(MessageType.INFO, processed + "\n");
				}
			}

		} catch (Exception e) {
			String message = "";
			if (e.getMessage() != null) {
				message = ": " + e.getMessage();
			}
			message = Localization.getString("controller.error.response")
					+ " <" + processed + ">" + message;

			logger.log(Level.SEVERE, message, e);
			this.dispatchConsoleMessage(MessageType.ERROR, message + "\n");
		}

	}

	private void updateControllerState(String str, ControllerState state) {
		controllerStatus = new ControllerStatus(state, controllerStatus.getMachineCoord(), controllerStatus.getWorkCoord());
		dispatchStatusString(controllerStatus);
	}

	@Override
	public void jogMachine(double distanceX, double distanceY, double distanceZ, double feedRate, Units units) throws Exception {
		logger.log(Level.INFO, "Adjusting manual location.");

		// G91 must be a separate command...
		GcodeCommand relCommand = createCommand("G91");
		relCommand.setTemporaryParserModalChange(true);
		sendCommandImmediately(relCommand);

		// ...as must the unit command
		String unitCommandStr = GcodeUtils.unitCommand(units);
		GcodeCommand unitCommand = createCommand(unitCommandStr);
		unitCommand.setTemporaryParserModalChange(true);
		sendCommandImmediately(unitCommand);

		// don't send units with G1 line - Marlin doesnt like it
		String commandString = GcodeUtils.generateMoveCommand("G1",
				feedRate, distanceX, distanceY, distanceZ, Units.UNKNOWN);

		GcodeCommand command = createCommand(commandString);
		command.setTemporaryParserModalChange(true);
		sendCommandImmediately(command);
		// restoreParserModalState();
	}

	/**
	 * Begin issuing GRBL status request commands.
	 */
	private void beginPollingPosition() {
		// Start sending '?' commands if supported and enabled.
		// if (this.isReady && this.capabilities != null && this.getStatusUpdatesEnabled()) {
		if (!this.positionPollTimer.isRunning()) {
			this.outstandingPolls = 0;
			this.positionPollTimer.start();
		}
		// }
	}

	/**
	 * Stop issuing GRBL status request commands.
	 */
	private void stopPollingPosition() {
		if (this.positionPollTimer.isRunning()) {
			this.positionPollTimer.stop();
		}
	}

	/**
	 * Create a timer which will execute Marlin's position polling mechanism.
	 */
	private Timer createPositionPollTimer() {
		ActionListener actionListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				java.awt.EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							if (outstandingPolls == 0) {
								outstandingPolls++;
								sendCommandImmediately(createCommand("M114"));
							} else {
								// If a poll is somehow lost after 20 intervals,
								// reset for sending another.
								outstandingPolls++;
								if (outstandingPolls >= 20) {
									outstandingPolls = 0;
								}
							}
						} catch (Exception ex) {
							dispatchConsoleMessage(MessageType.INFO, Localization.getString("controller.exception.sendingstatus")
									+ " (" + ex.getMessage() + ")\n");
							ex.printStackTrace();
						}
					}
				});

			}
		};

		// int statusUpdateRate = this.getStatusUpdateRate();
		int statusUpdateRate = 5000;
		logger.info("creating status timer with " + statusUpdateRate + " ms interval");
		return new Timer(statusUpdateRate, actionListener);
	}

	@Override
	public ControlState getControlState() {
		ControllerState state = this.controllerStatus == null ? ControllerState.UNKNOWN : this.controllerStatus.getState();
		switch (state) {
		// case JOG:
		case RUN:
			return ControlState.COMM_SENDING;
		/* TODO: check this...
		case "paused":
			// case "hold":
			// case "door":
			// case "queue":
			return ControlState.COMM_SENDING_PAUSED;
			*/
        case HOLD:
        case DOOR:
            return ControlState.COMM_SENDING_PAUSED;
		case IDLE:
			if (isStreaming()) {
				return ControlState.COMM_SENDING_PAUSED;
			} else {
				return ControlState.COMM_IDLE;
			}
			/*
			 * case "alarm": return ControlState.COMM_IDLE; case "check": if (isStreaming() && comm.isPaused()) { return ControlState.COMM_SENDING_PAUSED; }
			 * else if (isStreaming() && !comm.isPaused()) { return ControlState.COMM_SENDING; } else { return COMM_CHECK; }
			 */
		default:
			return ControlState.COMM_IDLE;
		}
	}

	@Override
	public void requestStatusReport() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	protected void statusUpdatesEnabledValueChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void statusUpdatesRateValueChanged() {
		// TODO Auto-generated method stub

	}

}
