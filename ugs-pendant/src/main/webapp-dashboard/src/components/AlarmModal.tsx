import Button from "react-bootstrap/Button";
import Modal from "react-bootstrap/Modal";
import { homeMachine, killAlarm, softReset } from "../services/machine";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import {
  faHome,
  faLockOpen,
  faRefresh,
} from "@fortawesome/free-solid-svg-icons";

const AlarmModal = () => {
  return (
    <Modal show={true} centered onHide={() => {}}>
      <Modal.Header>
        <Modal.Title>Alarm!</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        The controller is in an alarm state. This could mean that it has lost
        its position and needs to be reset or homed.
      </Modal.Body>
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
