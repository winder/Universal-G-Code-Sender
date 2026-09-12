import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import { homeMachine, killAlarm, softReset } from "../services/machine";
import { useAppSelector } from "../hooks/useAppSelector";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faHome,
  faLockOpen,
  faRefresh,
} from "@fortawesome/free-solid-svg-icons";

// Keyed by the Alarm enum constant names from
// com.willwinder.universalgcodesender.model.Alarm - keep in sync with that
// file. UNKONWN (sic, matches the Java typo) and any alarm type this dashboard
// doesn't recognize yet fall back to the generic message below.
const ALARM_MESSAGES: Record<string, string> = {
  HARD_LIMIT:
    "A hard limit switch was triggered. The machine stopped immediately and its position may be lost - home the machine before continuing.",
  SOFT_LIMIT:
    "A commanded move would have exceeded a configured soft (software) travel limit.",
  ABORT_DURING_CYCLE:
    "A feed hold was left unresolved - the controller was reset while paused, or reset without returning to idle first.",
  PROBE_FAIL_INITIAL:
    "Probing failed: the probe was already triggered before the probe move started.",
  PROBE_FAIL_CONTACT:
    "Probing failed: the probe never made contact during the move.",
  HOMING_FAIL_RESET: "Homing was interrupted by a reset.",
  HOMING_FAIL_DOOR:
    "Homing failed: the safety door was opened during the homing cycle.",
  HOMING_FAIL_PULLOFF:
    "Homing failed: a limit switch stayed triggered after pulling off.",
  HOMING_FAIL_APPROACH:
    "Homing failed: a limit switch never triggered during the approach.",
};

const DEFAULT_MESSAGE =
  "The controller is in an alarm state. This could mean that it has lost its position and needs to be reset or homed.";

const AlarmModal = () => {
  const lastAlarm = useAppSelector((state) => state.alarm.lastAlarm);
  const message = (lastAlarm && ALARM_MESSAGES[lastAlarm]) || DEFAULT_MESSAGE;

  return (
    <Modal show={true} centered onHide={() => {}}>
      <Modal.Header>
        <Modal.Title>Alarm!</Modal.Title>
      </Modal.Header>
      <Modal.Body>{message}</Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={() => homeMachine()}>
          <FontAwesomeIcon icon={faHome} /> Home
        </Button>
        <Button variant="primary" onClick={() => killAlarm()}>
          <FontAwesomeIcon icon={faLockOpen} /> Unlock
        </Button>
        <Button variant="warning" onClick={() => softReset()}>
          <FontAwesomeIcon icon={faRefresh} /> Reset
        </Button>
      </Modal.Footer>
    </Modal>
  );
};

export default AlarmModal;
